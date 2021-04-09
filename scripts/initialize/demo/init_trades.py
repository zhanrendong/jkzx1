# -*- encoding: utf-8 -*-

from datetime import datetime, timedelta, date

import trade_templates
import init_auth
import init_client
import utils

_DAYS_IN_YEAR = 365
_UNIT_LOT = 'LOT'
_UNIT_CNY = 'CNY'
_UNIT_PERCENT = 'PERCENT'
_DIRECTION_BUYER = 'BUYER'
_DIRECTION_SELLER = 'SELLER'
_OPTION_CALL = 'CALL'
_OPTION_PUT = 'PUT'
_KNOCK_UP = 'UP'
_KNOCK_DOWN = 'DOWN'
_REBATE_PAY_AT_EXPIRY = 'PAY_AT_EXPIRY'
_REBATE_PAY_WHEN_HIT = 'PAY_WHEN_HIT'
_OBSERVATION_DAILY = 'DAILY'
_OBSERVATION_CONTINUOUS = 'CONTINUOUS'
_OBSERVATION_TERMINAL = 'TERMINAL'
_EXCHANGE_OPEN = 'OPEN'
_EXCHANGE_CLOSE = 'CLOSE'
_datetime_fmt = '%Y-%m-%dT%H:%M:%S'
_date_fmt = '%y%m%d'
_date_fmt2 = '%Y-%m-%d'


def instrument_info(underlyer, host, token):
    return utils.call('mktInstrumentInfo', {
        'instrumentId': underlyer
    }, 'market-data-service', host, token)


def list_quote(underlyer, valuation_date, host, token):
    return utils.call('mktQuotesListPaged', {
        'instrumentIds': [underlyer],
        'valuationDate': valuation_date.strftime(_datetime_fmt),
        'timezone': 'Asia/Shanghai',
        'page': None,
        'pageSize': None
    }, 'market-data-service', host, token)['page'][0]


def create_trade(trade, valid_time, host, token):
    return utils.call('trdTradeCreate', {
        'trade': trade,
        'validTime': valid_time.strftime(_datetime_fmt)
    }, 'trade-service', host, token)


def calc_premium(trade):
    """Return premium amount, round to 0.01."""
    asset = trade['positions'][0]['asset']
    direction = -1 if asset['direction'] == _DIRECTION_BUYER else 1
    if asset['premiumType'].upper() != _UNIT_PERCENT:
        return round(asset['premium'] * direction, 2)
    notional = asset['notionalAmount']
    if asset['notionalAmountType'] == _UNIT_LOT:
        notional *= asset['initialSpot'] * asset['underlyerMultiplier']
    if asset['annualized']:
        notional *= asset['term'] / asset['daysInYear']
    return round(notional * asset['premium'] * direction, 2)


def search_account(legal_name, host, token):
    return utils.call('clientAccountSearch', {
        'legalName': legal_name
    }, 'reference-data-service', host, token)


def create_client_cash_flow(account_id, trade_id, cash_flow, margin_flow, host, token):
    trade = utils.call('trdTradeSearch', {
        'tradeId': trade_id
    }, 'trade-service', host, token)[0]
    position = trade['positions'][0]
    direction = position['asset']['direction']
    client = position['counterPartyCode']
    task = utils.call('cliTasksGenerateByTradeId', {
        'legalName': client,
        'tradeId': trade_id
    }, 'reference-data-service', host, token)[0]
    utils.call('clientChangePremium', {
        'tradeId': trade_id,
        'accountId': task['accountId'],
        'premium': task['premium'],
        'information': None
    }, 'reference-data-service', host, token)
    res = utils.call('clientSaveAccountOpRecord', {
        'accountOpRecord': {
            'accountId': task['accountId'],
            'cashChange': task['premium'] * -1,
            'counterPartyCreditBalanceChange': 0,
            'counterPartyFundChange': 0,
            'creditBalanceChange': 0,
            'debtChange': 0,
            'event': 'CHANGE_PREMIUM',
            'legalName': client,
            'premiumChange': task['premium'],
            'tradeId': trade_id
        }
    }, 'reference-data-service', host, token)
    return utils.call('cliMmarkTradeTaskProcessed', {
        'uuidList': [task['uuid']]
    }, 'reference-data-service', host, token)


def generate_trade_id(underlyer, product_type, count):
    return 'INIT_' + product_type + '_' + str(count)


def create_trade_and_client_cash_flow(trade, host, token):
    trade_id = trade['tradeId']
    position = trade['positions'][0]
    counter_party = position['counterPartyCode']
    premium_cash = calc_premium(trade)
    account_id = search_account(counter_party, host, token)[0]['accountId']
    margin = 0 if position['asset']['direction'] == _DIRECTION_BUYER else 0
    create_trade(trade, datetime.now(), host, token)
    create_client_cash_flow(account_id, trade_id, -premium_cash, -margin, host, token)
    print('Created: ' + trade_id)


def create_exe_trade(instrument, trade_id, book, direction, number, multiplier,
                     price, open_close, deal_time, account, host, token):
    utils.call('exeTradeRecordSave', {
        'bookId': book,
        'tradeId': trade_id,
        'instrumentId': instrument,
        'direction': direction,
        'dealAmount': number,
        'multiplier': multiplier,
        'dealPrice': price,
        'openClose': open_close,
        'dealTime': deal_time.strftime(_datetime_fmt),
        'tradeAccount': account
    }, 'exchange-service', host, token)


def add_vol_instrument_preference(user, instrument, host, token):
    utils.call('prefPreferenceVolInstrumentAdd', {
        'userName': user,
        'volInstrument': instrument
    }, 'user-preference-service', host, token)


def add_dividende_instrument_preference(user, instrument, host, token):
    utils.call('prefPreferenceDividendInstrumentAdd', {
        'userName': user,
        'dividendInstrument': instrument
    }, 'user-preference-service', host, token)


if __name__ == '__main__':
    host = 'localhost'
    trader = init_auth.trade_user_name
    token = utils.login(trader, init_auth.risk_user_password, host)

    now = datetime.now()
    today = date.today()

    # commodity futures
    cf = 'RB1912.SHF'
    cf_multiplier = 10
    cf_price = 'close'
    cf_spot = 5200

    # equity stock
    es = '600030.SH'
    es_multiplier = 1
    es_price = 'close'
    es_spot = 25

    # equity index
    ei = '000300.SH'
    ei_multiplier = 1
    ei_price = 'close'
    ei_spot = 4000

    book1 = '交易簿1'  # analytic pricer
    book2 = '交易簿2'  # MC pricer
    legal_name = init_client.legal_name[0]
    sales = init_client.sales

    print('========== Setting user instrument preference ==========')
    for i in [cf, es, ei]:
        add_vol_instrument_preference(trader, i, host, token)
        add_dividende_instrument_preference(trader, i, host, token)
        add_vol_instrument_preference(init_auth.risk_user_name, i, host, token)
        add_dividende_instrument_preference(init_auth.risk_user_name, i, host, token)


    print('========== Creating trades ==========')

    # 远期
    term = 30
    expiry = today + timedelta(days=25)
    trade = trade_templates.forward(
        generate_trade_id(es, 'forward', 0), book1, expiry - timedelta(days=term),
        _UNIT_PERCENT, 1, expiry,
        _DIRECTION_BUYER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, _UNIT_PERCENT, 0.003,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 美式 年化
    term = 30
    expiry = today + timedelta(days=20)
    trade = trade_templates.vanilla_american(
        generate_trade_id(es, 'american', 0), book1, expiry - timedelta(days=term),
        _OPTION_PUT, _UNIT_PERCENT, 1.05,
        _DIRECTION_SELLER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.05,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 美式 非年化
    term = 30
    expiry = today + timedelta(days=20)
    trade = trade_templates.vanilla_american(
        generate_trade_id(cf, 'american', 1), book1, expiry - timedelta(days=term),
        _OPTION_CALL, _UNIT_CNY, 5150,
        _DIRECTION_SELLER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 100000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 欧式 年化
    term = 30
    expiry = today + timedelta(days=29)
    trade = trade_templates.vanilla_european(
        generate_trade_id(es, 'european', 0), book1, expiry - timedelta(days=term),
        _OPTION_CALL, _UNIT_PERCENT, 1,
        _DIRECTION_BUYER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.025,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 欧式 非年化
    term = 30
    expiry = today + timedelta(days=29)
    trade = trade_templates.vanilla_european(
        generate_trade_id(ei, 'european', 1), book1, expiry - timedelta(days=term),
        _OPTION_PUT, _UNIT_CNY, 5250,
        _DIRECTION_BUYER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 100000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 一触即付 年化
    term = 30
    expiry = today + timedelta(days=9)
    trade = trade_templates.one_touch(
        generate_trade_id(es, 'oneTouch', 0), book1, expiry - timedelta(days=term),
        _OPTION_CALL, _UNIT_PERCENT, 1.05, _UNIT_PERCENT, 0.05, _REBATE_PAY_AT_EXPIRY,
        _DIRECTION_BUYER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.015,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 一触即付 非年化
    term = 30
    expiry = today + timedelta(days=9)
    trade = trade_templates.one_touch(
        generate_trade_id(cf, 'oneTouch', 1), book1, expiry - timedelta(days=term),
        _OPTION_PUT, _UNIT_CNY, 5000, _UNIT_CNY, 200000, _REBATE_PAY_WHEN_HIT,
        _DIRECTION_BUYER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 75000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 欧式二元 年化
    term = 30
    expiry = today + timedelta(days=0)
    trade = trade_templates.digital(
        generate_trade_id(es, 'digital', 0), book1, expiry - timedelta(days=term),
        _OPTION_CALL, _UNIT_PERCENT, 0.95, _UNIT_PERCENT, 0.05,
        _DIRECTION_BUYER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.04,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 欧式二元 非年化
    term = 30
    expiry = today + timedelta(days=0)
    trade = trade_templates.digital(
        generate_trade_id(cf, 'digital', 1), book1, expiry - timedelta(days=term),
        _OPTION_PUT, _UNIT_CNY, 5400, _UNIT_CNY, 200000,
        _DIRECTION_BUYER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 150000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 欧式价差 年化
    term = 30
    expiry = today + timedelta(days=1)
    trade = trade_templates.vertical_spread(
        generate_trade_id(es, 'spread', 0), book1, expiry - timedelta(days=term),
        _OPTION_PUT, _UNIT_PERCENT, 0.95, 1.05,
        _DIRECTION_SELLER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.045,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 欧式价差 非年化
    term = 30
    expiry = today + timedelta(days=1)
    trade = trade_templates.vertical_spread(
        generate_trade_id(cf, 'spread', 1), book1, expiry - timedelta(days=term),
        _OPTION_CALL, _UNIT_CNY, 5100, 5200,
        _DIRECTION_SELLER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 50000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 单鲨 年化
    term = 30
    expiry = today + timedelta(days=15)
    trade = trade_templates.single_shark_fin(
        generate_trade_id(es, 'sharkFin', 0), book1, expiry - timedelta(days=term),
        _UNIT_PERCENT, 1.05, _UNIT_PERCENT, 0.97, _UNIT_PERCENT, 0.01, _REBATE_PAY_AT_EXPIRY, _OBSERVATION_CONTINUOUS,
        _DIRECTION_SELLER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.015,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 单鲨 非年化
    term = 30
    expiry = today + timedelta(days=15)
    trade = trade_templates.single_shark_fin(
        generate_trade_id(cf, 'sharkFin', 1), book1, expiry - timedelta(days=term),
        _UNIT_CNY, 5000, _UNIT_CNY, 5250, _UNIT_CNY, 10000, _REBATE_PAY_WHEN_HIT, _OBSERVATION_CONTINUOUS,
        _DIRECTION_SELLER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 15000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 双鲨 年化
    term = 30
    expiry = today + timedelta(days=15)
    trade = trade_templates.double_shark_fin(
        generate_trade_id(es, 'doubleSharkFin', 0), book1, expiry - timedelta(days=term),
        _UNIT_PERCENT, 0.99, 1.01, _UNIT_PERCENT, 0.9, 1.1, _UNIT_PERCENT, 0.01, 0.01, _REBATE_PAY_AT_EXPIRY,
        _OBSERVATION_CONTINUOUS,
        _DIRECTION_BUYER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.025,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 双鲨 非年化
    term = 30
    expiry = today + timedelta(days=15)
    trade = trade_templates.double_shark_fin(
        generate_trade_id(cf, 'doubleSharkFin', 1), book1, expiry - timedelta(days=term),
        _UNIT_CNY, 5150, 5250, _UNIT_CNY, 4800, 5600, _UNIT_CNY, 50000, 50000,
        _REBATE_PAY_WHEN_HIT, _OBSERVATION_TERMINAL,
        _DIRECTION_BUYER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 90000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 鹰式 年化
    term = 30
    expiry = today + timedelta(days=3)
    trade = trade_templates.eagle(
        generate_trade_id(es, 'eagle', 0), book1, expiry - timedelta(days=term),
        _UNIT_PERCENT, 0.85, 0.9, 0.95, 1,
        _DIRECTION_SELLER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.015,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 鹰式 非年化
    term = 30
    expiry = today + timedelta(days=3)
    trade = trade_templates.eagle(
        generate_trade_id(cf, 'eagle', 1), book1, expiry - timedelta(days=term),
        _UNIT_CNY, 5000, 5050, 5150, 5200,
        _DIRECTION_SELLER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_CNY, 5200000, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 10000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 美式双触碰 年化
    term = 30
    expiry = today + timedelta(days=14)
    trade = trade_templates.double_touch(
        generate_trade_id(es, 'doubleTouch', 0), book1, expiry - timedelta(days=term),
        _UNIT_PERCENT, 0.90, 1.1, _UNIT_PERCENT, 0.1, _REBATE_PAY_AT_EXPIRY,
        _DIRECTION_SELLER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.01,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 美式双触碰 非年化
    term = 30
    expiry = today + timedelta(days=14)
    trade = trade_templates.double_touch(
        generate_trade_id(cf, 'doubleTouch', 1), book1, expiry - timedelta(days=term),
        _UNIT_CNY, 5000, 5200, _UNIT_CNY, 200000, _REBATE_PAY_WHEN_HIT,
        _DIRECTION_SELLER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 100000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 美式双不触碰 年化
    term = 30
    expiry = today + timedelta(days=5)
    trade = trade_templates.double_no_touch(
        generate_trade_id(es, 'doubleNoTouch', 0), book1, expiry - timedelta(days=term),
        _UNIT_PERCENT, 0.9, 1.1, _UNIT_PERCENT, 0.05,
        _DIRECTION_BUYER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.045,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 美式双不触碰 非年化
    term = 30
    expiry = today + timedelta(days=5)
    trade = trade_templates.double_no_touch(
        generate_trade_id(cf, 'doubleNoTouch', 1), book1, expiry - timedelta(days=term),
        _UNIT_CNY, 5000, 5400, _UNIT_CNY, 500000,
        _DIRECTION_BUYER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 200000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 二元凹式 年化
    term = 30
    expiry = today + timedelta(days=10)
    trade = trade_templates.concava(
        generate_trade_id(es, 'concave', 0), book1, expiry - timedelta(days=term),
        _UNIT_PERCENT, 0.95, 1.05, _UNIT_PERCENT, 0.05,
        _DIRECTION_SELLER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.03,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 二元凹式 非年化
    term = 30
    expiry = today + timedelta(days=10)
    trade = trade_templates.concava(
        generate_trade_id(cf, 'concave', 1), book1, expiry - timedelta(days=term),
        _UNIT_CNY, 5100, 5300, _UNIT_CNY, 200000,
        _DIRECTION_SELLER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 100000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 二元凸式 年化
    term = 30
    expiry = today + timedelta(days=11)
    trade = trade_templates.convex(
        generate_trade_id(es, 'convex', 0), book1, expiry - timedelta(days=term),
        _UNIT_PERCENT, 0.95, 1.05, _UNIT_PERCENT, 0.05,
        _DIRECTION_BUYER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.03,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 二元凸式 非年化
    term = 30
    expiry = today + timedelta(days=11)
    trade = trade_templates.convex(
        generate_trade_id(cf, 'convex', 1), book1, expiry - timedelta(days=term),
        _UNIT_CNY, 5100, 5300, _UNIT_CNY, 200000,
        _DIRECTION_BUYER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 100000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 三层阶梯 年化
    term = 30
    expiry = today + timedelta(days=7)
    trade = trade_templates.double_digital(
        generate_trade_id(es, 'doubleDigital', 0), book1, expiry - timedelta(days=term),
        _OPTION_CALL, _UNIT_PERCENT, 1, 1.05, _UNIT_PERCENT, 0.03, 0.05,
        _DIRECTION_BUYER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.015,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 三层阶梯 非年化
    term = 30
    expiry = today + timedelta(days=7)
    trade = trade_templates.double_digital(
        generate_trade_id(cf, 'doubleDigital', 1), book1, expiry - timedelta(days=term),
        _OPTION_PUT, _UNIT_CNY, 5100, 5200, _UNIT_CNY, 100000, 50000,
        _DIRECTION_BUYER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 10000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 四层阶梯 年化
    term = 30
    expiry = today + timedelta(days=8)
    trade = trade_templates.triple_digital(
        generate_trade_id(es, 'tripleDigital', 0), book1, expiry - timedelta(days=term),
        _OPTION_PUT, _UNIT_PERCENT, 0.95, 1, 1.05, _UNIT_PERCENT, 0.1, 0.07, 0.04,
        _DIRECTION_SELLER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.025,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 四层阶梯 非年化
    term = 30
    expiry = today + timedelta(days=8)
    trade = trade_templates.triple_digital(
        generate_trade_id(cf, 'tripleDigital', 1), book1, expiry - timedelta(days=term),
        _OPTION_CALL, _UNIT_CNY, 5100, 5200, 5300, _UNIT_CNY, 100000, 200000, 300000,
        _DIRECTION_SELLER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 150000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 区间累积 年化
    term = 30
    expiry = today + timedelta(days=term)
    fixings = {(expiry - timedelta(days=i)).strftime(_date_fmt2): None for i in range(term - 1, -1, -1)}
    trade = trade_templates.range_accruals(
        generate_trade_id(es, 'rangeAccrul', 0), book1, expiry - timedelta(days=term),
        _UNIT_PERCENT, 0.95, 1.05, _UNIT_PERCENT, 0.05, fixings,
        _DIRECTION_BUYER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.04,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 区间累积 非年化
    term = 30
    expiry = today + timedelta(days=term)
    fixings = {(expiry - timedelta(days=i)).strftime(_date_fmt2): None for i in range(term - 1, -1, -1)}
    trade = trade_templates.range_accruals(
        generate_trade_id(cf, 'rangeAccrul', 1), book1, expiry - timedelta(days=term),
        _UNIT_CNY, 5000, 5400, _UNIT_CNY, 250000, fixings,
        _DIRECTION_BUYER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 200000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 跨式 年化
    term = 30
    expiry = today + timedelta(days=5)
    trade = trade_templates.straddle(
        generate_trade_id(es, 'straddle', 0), book1, expiry - timedelta(days=term),
        _UNIT_PERCENT, 0.95, 1.05,
        _DIRECTION_BUYER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.01,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 跨式 非年化
    term = 30
    expiry = today + timedelta(days=5)
    trade = trade_templates.straddle(
        generate_trade_id(cf, 'straddle', 1), book1, expiry - timedelta(days=term),
        _UNIT_CNY, 5200, 5300,
        _DIRECTION_BUYER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 150000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 亚式 年化
    term = 30
    expiry = today + timedelta(days=term)
    weights = {(expiry - timedelta(days=i)).strftime(_date_fmt2): 1 for i in range(term - 1, -1, -1)}
    fixings = {d: None for d in weights}
    trade = trade_templates.asian(
        generate_trade_id(es, 'asian', 0), book1, expiry - timedelta(days=term),
        _OPTION_CALL, _UNIT_PERCENT, 1, '1D', weights, fixings,
        _DIRECTION_SELLER, es, es_multiplier, es_price, es_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.01,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 亚式 非年化
    term = 30
    expiry = today + timedelta(days=term)
    weights = {(expiry - timedelta(days=i)).strftime(_date_fmt2): 1 for i in range(term - 1, -1, -1)}
    fixings = {d: None for d in weights}
    trade = trade_templates.asian(
        generate_trade_id(cf, 'asian', 1), book1, expiry - timedelta(days=term),
        _OPTION_PUT, _UNIT_CNY, 5200, '1D', weights, fixings,
        _DIRECTION_SELLER, cf, cf_multiplier, cf_price, cf_spot, expiry,
        _UNIT_LOT, 100, 1, False, term, _DAYS_IN_YEAR, _UNIT_CNY, 60000,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 雪球式AutoCall 年化
    term = 360
    expiry = today + timedelta(days=345)
    observation_dates = [(expiry - timedelta(days=i * 30)).strftime(_date_fmt2) for i in range(11, -1, -1)]
    trade = trade_templates.autocall(
        generate_trade_id(ei, 'autocall', 0), book2, expiry - timedelta(days=term),
        observation_dates, '1', _KNOCK_UP, _UNIT_PERCENT, 1, 0.2, 'FIXED', 0.03, _UNIT_PERCENT, None,
        _DIRECTION_SELLER, ei, ei_multiplier, ei_price, ei_spot, expiry,
        _UNIT_CNY, 1000000, 1, True, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.035,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 凤凰式AutoCall 年化
    term = 360
    expiry = today + timedelta(days=345)
    observation_dates = [(expiry - timedelta(days=i * 30)).strftime(_date_fmt2) for i in range(11, -1, -1)]
    fixings = {d: None for d in observation_dates}
    knock_in_observation_dates = [(expiry - timedelta(days=i)).strftime(_date_fmt2) for i in range(term - 1, -1, -1)]
    trade = trade_templates.autocall_phoenix(
        generate_trade_id(ei, 'phoenix', 0), book2, expiry - timedelta(days=term),
        _KNOCK_UP, _UNIT_PERCENT, 1, 0.7, 0.2, fixings, knock_in_observation_dates, '1', None, _UNIT_PERCENT, 0.7,
        _OPTION_PUT, _UNIT_PERCENT, 1,
        _DIRECTION_SELLER, ei, ei_multiplier, ei_price, ei_spot, expiry,
        _UNIT_CNY, 1000000, 1, False, term, _DAYS_IN_YEAR, _UNIT_PERCENT, 0.05,
        expiry - timedelta(days=term), trader, legal_name, sales)
    create_trade_and_client_cash_flow(trade, host, token)

    # 场内对冲
    exchange_trade_account = '00000000'
    trade_id = 'INIT_' + cf.replace('.', '_')
    create_exe_trade(cf, trade_id, book1, _DIRECTION_BUYER, 600, cf_multiplier, 5220,
                     _EXCHANGE_OPEN, now, exchange_trade_account, host, token)
    print('Created: ' + trade_id)
    trade_id = 'INIT_' + es.replace('.', '_')
    create_exe_trade(es, trade_id, book1, _DIRECTION_SELLER, 10000, es_multiplier, 23.5,
                     _EXCHANGE_OPEN, now, exchange_trade_account, host, token)
    print('Created: ' + trade_id)
