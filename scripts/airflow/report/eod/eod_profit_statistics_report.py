from report.basic_cashflows import get_cash_flow, get_cash_flows_today
from report.basic_positions import get_positions
from datetime import datetime, timedelta
from utils import utils


def check_hold(position, duration_positions):
    for p in duration_positions:
        if position['positionId'] == p['positionId']:
            position['underlyerInstrumentId'] = p['underlyerInstrumentId']
            position['productType'] = p['productType']
        else:
            position['underlyerInstrumentId'] = ''
            position['productType'] = ''


def find_underid_by_pid(positionid, trade_data):
    trade_data = trade_data['result']
    for t in trade_data:
        for p in t['positions']:
            if p['positionId'] == positionid:
                return p['asset']['underlyerInstrumentId']


def find_protype_by_pid(positionid, trade_data):
    trade_data = trade_data['result']
    for t in trade_data:
        for p in t['positions']:
            if p['positionId'] == positionid:
                return p['productType']


def get_price(price_data, position_id):
    for p in price_data:
        if p['positionId'] == position_id:
            return p


def eod_profit_statistics_report(ip, headers, price_data):
    _datetime_fmt = '%Y-%m-%dT%H:%M:%S.%fZ'
    histroy_position = []
    today_position = []
    all_position = []
    now = datetime.now().strftime('%Y-%m-%d')
    trade_data = utils.call_request(ip, 'trade-service', 'trdTradeSearch', {}, headers)
    for id, p in get_cash_flow(ip, headers).items():
        p['underlyerInstrumentId'] = find_underid_by_pid(p['positionId'], trade_data)
        p['productType'] = find_protype_by_pid(p['positionId'], trade_data)
        if now not in p['timestamp']:
            histroy_position.append(p)
        else:
            today_position.append(p)
    for id, p in get_cash_flow(ip, headers).items():
        p['underlyerInstrumentId'] = find_underid_by_pid(p['positionId'], trade_data)
        p['productType'] = find_protype_by_pid(p['positionId'], trade_data)
        all_position.append(p)
    duration_positions = []
    positions = get_positions(ip, headers)
    # 个股持仓盈亏
    stock_hold_pnl = 0.0
    # 个股今日了结盈亏
    stock_today_to_end_pnl = 0.0
    # 个股历史了结盈亏
    stock_histroy_to_end_pnl = 0.0
    # 商品持仓盈亏
    commodity_hold_pnl = 0.0
    # 商品今日了结盈亏
    commodity_today_to_end_pnl = 0.0
    # 商品历史了结盈亏
    commodity_histroy_to_end_pnl = 0.0
    # 奇异持仓盈亏
    exotic_hold_pnl = 0.0
    # 奇异今日了结盈亏
    exotic_today_to_end_pnl = 0.0
    # 奇异历史了结盈亏
    exotic_histroy_to_end_pnl = 0.0
    for id, p in positions[0].items():
        duration_positions.append(p)
    for p in all_position:
        check_hold(p, duration_positions)
    for p in all_position:
        if p['underlyerInstrumentId'] != '' and p['underlyerInstrumentId'].split('.')[1] is not None:
            code_end = p['underlyerInstrumentId'].split('.')[1]
            if code_end in ['SZ', 'SH']:
                stock_hold_pnl += p['open'] + p['unwind'] + p['settle']
            else:
                commodity_hold_pnl += p['open'] + p['unwind'] + p['settle']
    for p in all_position:
        if p['productType'] != '':
            if 'VANILLA' not in p['productType']:
                exotic_hold_pnl += p['open'] + p['unwind'] + p['settle']
    for p in today_position:
        for position_id, price in price_data.items():
            if p['underlyerInstrumentId'] == price.get('underlyerInstrumentId', ''):
                p['marketValue'] = price.get('price', 0.0)
            else:
                p['marketValue'] = 0.0
    for p in histroy_position:
        for position_id, price in price_data.items():
            if p['underlyerInstrumentId'] == price.get('underlyerInstrumentId', ''):
                p['marketValue'] = price.get('price', 0.0)
            else:
                p['marketValue'] = 0.0
    if len(today_position):
        for postion in today_position:
            if postion['underlyerInstrumentId'] is not None and postion['underlyerInstrumentId'].split('.')[
                1] is not None:
                code_end = postion['underlyerInstrumentId'].split('.')[1]
                if code_end in ['SH', 'CFE', 'SZ']:
                    stock_today_to_end_pnl += postion['open'] + postion['unwind'] + postion['settle'] + postion.get(
                    'marketValue', 0.0)
                else:
                    commodity_today_to_end_pnl += postion['open'] + postion['unwind'] + postion['settle'] + postion.get(
                    'marketValue', 0.0)
    for postion in histroy_position:
        if postion['underlyerInstrumentId'] is not None and postion['underlyerInstrumentId'].split('.')[
            1] is not None:
            code_end = postion['underlyerInstrumentId'].split('.')[1]
            if code_end in ['SH', 'CFE', 'SZ']:
                stock_histroy_to_end_pnl += postion['open'] + postion['unwind'] + postion['settle'] + postion.get(
                    'marketValue', 0.0)
            else:
                commodity_histroy_to_end_pnl += postion['open'] + postion['unwind'] + postion['settle'] + postion.get(
                    'marketValue', 0.0)
    for postion in today_position:
        if postion['productType'] != '':
            if 'VANILLA' not in postion['productType']:
                exotic_today_to_end_pnl += postion['open'] + postion['unwind'] + postion['settle'] + postion.get(
                    'marketValue', 0.0)
    for postion in histroy_position:
        if postion['productType'] != '':
            if 'VANILLA' not in postion['productType']:
                exotic_histroy_to_end_pnl += postion['open'] + postion['unwind'] + postion['settle'] + postion.get(
                    'marketValue', 0.0)
    stock_to_end_all_pnl = stock_histroy_to_end_pnl + stock_today_to_end_pnl  # 个股了结盈亏
    commodity_to_end_all_pnl = commodity_today_to_end_pnl + commodity_histroy_to_end_pnl  # 商品了结盈亏
    exotic_to_end_all_pnl = exotic_histroy_to_end_pnl + exotic_today_to_end_pnl  # 奇异了结盈亏
    stock_pnl = stock_to_end_all_pnl + stock_hold_pnl  # 个股期权总盈亏
    commodity_pnl = commodity_to_end_all_pnl + commodity_hold_pnl  # 商品期权总盈亏
    exotic_pnl = exotic_to_end_all_pnl + exotic_hold_pnl  # 奇异期权总盈亏
    option_today_to_end_pnl = commodity_today_to_end_pnl + stock_today_to_end_pnl + exotic_today_to_end_pnl  # 期权今日了结盈亏
    option_histroy_to_end_pnl = commodity_histroy_to_end_pnl + stock_histroy_to_end_pnl + exotic_histroy_to_end_pnl  # 期权历史了结盈亏
    option_to_end_all_pnl = option_today_to_end_pnl + option_histroy_to_end_pnl  # 期权了结盈亏
    option_hold_pnl = stock_hold_pnl + commodity_hold_pnl + exotic_hold_pnl  # 期权持仓盈亏
    option_pnl = option_to_end_all_pnl  # 期权总盈亏
    result = {'2.1.1.1 商品期权今日了结盈亏': commodity_today_to_end_pnl, '2.1.1.2 商品期权历史了结盈亏': commodity_histroy_to_end_pnl,
              '2.1.1.3 商品期权了结盈亏': commodity_to_end_all_pnl, '2.1.1.4 商品期权持仓盈亏': commodity_hold_pnl,
              '2.1.1.5 商品期权总盈亏': commodity_pnl, '2.1.2.1 个股期权今日了结盈亏': stock_today_to_end_pnl,
              '2.1.2.2 个股期权历史了结盈亏': stock_histroy_to_end_pnl, '2.1.2.3 个股期权了结盈亏': stock_to_end_all_pnl,
              '2.1.2.4 个股期权持仓盈亏': stock_hold_pnl, '2.1.2.5 个股期权总盈亏': stock_pnl,
              '2.1.3.1 奇异期权今日了结盈亏': exotic_today_to_end_pnl, '2.1.3.2 奇异期权历史了结盈亏': exotic_histroy_to_end_pnl,
              '2.1.3.3 奇异期权了结盈亏': exotic_to_end_all_pnl, '2.1.3.4 奇异期权持仓盈亏': exotic_hold_pnl,
              '2.1.3.5 奇异期权总盈亏': exotic_pnl, '2.1.4.1 期权今日了结盈亏': option_today_to_end_pnl,
              '2.1.4.2 期权历史了结盈亏': option_histroy_to_end_pnl, '2.1.4.3 期权了结盈亏': option_to_end_all_pnl,
              '2.1.4.4 期权持仓盈亏': option_hold_pnl, '2.1.4.5 期权总盈亏': option_pnl}
    return result

