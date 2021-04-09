from report.basic_positions import get_positions
from report.basic_listed_positions import get_underlyer_positions


def get_eod_subject_computing(ip, headers, prcing_env):
    positions = get_positions(ip, headers)
    position = {}
    positionsnew = []
    # 个股
    stock = 0.0
    stock_swap = 0.0
    # 商品
    commodity = 0.0
    commodity_forward = 0.0
    # 奇异
    exotic = 0.0
    # 净持仓的
    net_position = 0.0
    # 绝对持仓
    absolute_position = 0.0
    eod_underlyer_positions = get_underlyer_positions(ip, headers, prcing_env)
    for id, p in positions[0].items():
        position = p
        positionsnew.append(position)
    for p in positionsnew:
        instrument_id = p.get('underlyerInstrumentId', '')
        if instrument_id.endswith('.SH') or instrument_id.endswith('.SZ') or instrument_id.endswith('.CFE'):
            stock += p['notionalAmount']
        else:
            commodity += p['notionalAmount']
    for p in positionsnew:
        if 'VANILLA' not in p['productType']:
            exotic += p['notionalAmount']
    commodity_all = commodity + commodity_forward
    stock_all = stock + stock_swap
    for id, position in eod_underlyer_positions.items():
        for id2, p in position.items():
            if p['marketValue']:
                net_position += p['marketValue']
    # 个股对冲
    hedge = net_position
    result = {}
    result['1.1.1 一般商品期权对应的名义本金'] = commodity
    result['1.1.2 商品远期对应的名义本金'] = commodity_forward
    result['1.1.3 商品期权标的总资产(名义本金)'] = commodity_all
    result['1.2.1 一般个股期权标对应的名义本金'] = stock
    result['1.2.2 互换对应的名义本金'] = stock_swap
    result['1.2.4 个股期权标的的总资产'] = stock_all
    result['1.3.1 奇异期权标的总资产(名义本金)'] = exotic
    result['1.3.1.1 按净持仓的名义本金'] = net_position
    result['1.3.1.2 按绝对持仓的名本金'] = absolute_position
    result['1.3.2 个股对冲的名义本金'] = hedge
    return result
