import utils
import init_params
from datetime import date

_LCM_EVENT_UNWIND = 'UNWIND'
_LCM_EVENT_EXERCISE = 'EXERCISE'
_LCM_EVENT_KNOCK_OUT = 'KNOCK_OUT'
_ACCOUNT_EVENT_UNWIND = 'UNWIND_TRADE'
_ACCOUNT_EVENT_SETTLE = 'SETTLE_TRADE'
_DIRECTION_BUYER = 'BUYER'
_DIRECTION_SELLER = 'SELLER'
_date_fmt = '%Y-%m-%d'


def unwind(trade_id, position_id, amount, value, lot_number, lot_value, user,
           host, token):
    """Unwind trade.

    value is always positive."""
    trade = utils.call('trdTradeSearch', {
        'tradeId': trade_id
    }, 'trade-service', host, token)[0]
    position = list(filter(lambda p: p['positionId'] == position_id, trade['positions']))[0]
    direction = position['asset']['direction']
    client = position['counterPartyCode']
    utils.call('trdTradeLCMEventProcess', {
        'tradeId': trade_id,
        'positionId': position_id,
        'userLoginId': user,
        'eventType': _LCM_EVENT_UNWIND,
        'eventDetail': {
            'unWindAmount': str(amount),
            'unWindAmountValue': str(value),
            'unWindLot': str(lot_number),
            'unWindLotValue': str(lot_value)
        }
    }, 'trade-service', host, token)
    task = utils.call('cliTasksGenerateByTradeId', {
        'legalName': client,
        'tradeId': trade_id
    }, 'reference-data-service', host, token)[0]
    utils.call('clientSettleTrade', {
        'tradeId': trade_id,
        'accountEvent': _ACCOUNT_EVENT_UNWIND,
        'accountId': task['accountId'],
        'amount': amount * (-1 if direction == _DIRECTION_BUYER else 1),
        'premium': task['premium'] * (-1 if direction == _DIRECTION_SELLER else 1)
    }, 'reference-data-service', host, token)
    return utils.call('cliMmarkTradeTaskProcessed', {
        'uuidList': [task['uuid']]
    }, 'reference-data-service', host, token)


def exercise(trade_id, position_id, notional, settle_amount, lot_number, underlyer_price, user,
             host, token):
    """Exercise option.

    settle_amount is positive if receive, negative if pay."""
    trade = utils.call('trdTradeSearch', {
        'tradeId': trade_id
    }, 'trade-service', host, token)[0]
    position = list(filter(lambda p: p['positionId'] == position_id, trade['positions']))[0]
    direction = position['asset']['direction']
    client = position['counterPartyCode']
    utils.call('trdTradeLCMEventProcess', {
        'tradeId': trade_id,
        'positionId': position_id,
        'userLoginId': user,
        'eventType': _LCM_EVENT_EXERCISE,
        'eventDetail': {
            'notionalAmount': str(notional),
            'settleAmount': str(settle_amount),
            'numOfOptions': str(lot_number),
            'underlyerPrice': str(underlyer_price)
        }
    }, 'trade-service', host, token)
    task = utils.call('cliTasksGenerateByTradeId', {
        'legalName': client,
        'tradeId': trade_id
    }, 'reference-data-service', host, token)[0]
    utils.call('clientSettleTrade', {
        'tradeId': trade_id,
        'accountEvent': _ACCOUNT_EVENT_SETTLE,
        'accountId': task['accountId'],
        'amount': -settle_amount,
        'premium': task['premium'] * (-1 if direction == _DIRECTION_SELLER else 1)
    }, 'reference-data-service', host, token)
    return utils.call('cliMmarkTradeTaskProcessed', {
        'uuidList': [task['uuid']]
    }, 'reference-data-service', host, token)


def knock_out(trade_id, position_id, notional, settle_amount, underlyer_price, knock_out_date, user,
              host, token):
    """knock out option.

    settle_amount is positive if receive, negative if pay."""
    trade = utils.call('trdTradeSearch', {
        'tradeId': trade_id
    }, 'trade-service', host, token)[0]
    position = list(filter(lambda p: p['positionId'] == position_id, trade['positions']))[0]
    direction = position['asset']['direction']
    client = position['counterPartyCode']
    utils.call('trdTradeLCMEventProcess', {
        'tradeId': trade_id,
        'positionId': position_id,
        'userLoginId': user,
        'eventType': _LCM_EVENT_KNOCK_OUT,
        'eventDetail': {
            'notionalAmount': str(notional),
            'settleAmount': str(settle_amount),
            'knockOutDate': knock_out_date.strftime(_date_fmt),
            'underlyerPrice': str(underlyer_price)
        }
    }, 'trade-service', host, token)
    task = utils.call('cliTasksGenerateByTradeId', {
        'legalName': client,
        'tradeId': trade_id
    }, 'reference-data-service', host, token)[0]
    utils.call('clientSettleTrade', {
        'tradeId': trade_id,
        'accountEvent': _ACCOUNT_EVENT_SETTLE,
        'accountId': task['accountId'],
        'amount': -settle_amount,
        'premium': task['premium'] * (-1 if direction == _DIRECTION_SELLER else 1)
    }, 'reference-data-service', host, token)
    return utils.call('cliMmarkTradeTaskProcessed', {
        'uuidList': [task['uuid']]
    }, 'reference-data-service', host, token)


if __name__ == '__main__':
    host = init_params.host
    trader = init_params.trader_names[0]
    token = utils.login(trader, init_params.trader_password, host)

    today = date.today()

    # unwind
    print('Unwind INIT_concave_0.')
    unwind('INIT_concave_0', 'INIT_concave_0_0', 200000, 1000, 8000, 1000 / 8000, trader, host, token)
    print('Unwind INIT_concave_1.')
    unwind('INIT_concave_1', 'INIT_concave_1_0', 2600000, 100000, 50, 100000 / 50, trader, host, token)

    # exercise
    print('Exercise INIT_digital_0.')
    exercise('INIT_digital_0', 'INIT_digital_0_0', 1000000, 4109.59, 40000, 24, trader, host, token)
    print('Exercise INIT_digital_1.')
    exercise('INIT_digital_1', 'INIT_digital_1_0', 5200000, 200000, 100, 5300, trader, host, token)
    print('Exercise INIT_american_0.')
    exercise('INIT_american_0', 'INIT_american_0_0', 1000000, -7397.26, 40000, 24, trader, host, token)
    print('Exercise INIT_american_1.')
    exercise('INIT_american_1', 'INIT_american_1_0', 5200000, -150000, 100, 5300, trader, host, token)

    # knock out
    print('Knock out INIT_sharkFin_0.')
    knock_out('INIT_sharkFin_0', 'INIT_sharkFin_0_0', 1000000, -821.92, 24, today, trader, host, token)
    print('Knock out INIT_sharkFin_1.')
    knock_out('INIT_sharkFin_1', 'INIT_sharkFin_1_0', 100, -10000, 5300, today, trader, host, token)
