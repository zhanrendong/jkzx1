def get_eod_subject_computing(position_result, underlyer_positions):
    positions = position_result[['asset.underlyerInstrumentId', 'asset.notionalAmount', 'productType']].fillna(0.0)
    stock_condition = positions['asset.underlyerInstrumentId'].str.endswith('.CFE') | positions[
        'asset.underlyerInstrumentId'].str.endswith('.SZ') | positions['asset.underlyerInstrumentId'].str.endswith('.SH')
    stock_message = positions[stock_condition]
    commodity_message = positions[~stock_condition]
    exotic_message = positions.mask(positions['productType'].str.contains('VANILLA'))

    result = {}
    result['1.1.1 一般商品期权对应的名义本金'] = commodity_message[{'asset.notionalAmount'}].sum()['asset.notionalAmount']
    result['1.1.2 商品远期对应的名义本金'] = 0.0
    result['1.1.3 商品期权标的总资产(名义本金)'] = result['1.1.1 一般商品期权对应的名义本金'] + result['1.1.2 商品远期对应的名义本金']
    result['1.2.1 一般个股期权标对应的名义本金'] = stock_message[{'asset.notionalAmount'}].sum()['asset.notionalAmount']
    result['1.2.2 互换对应的名义本金'] = 0.0
    result['1.2.4 个股期权标的的总资产'] = result['1.2.1 一般个股期权标对应的名义本金'] + result['1.2.2 互换对应的名义本金']
    result['1.3.1 奇异期权标的总资产(名义本金)'] = exotic_message[{'asset.notionalAmount'}].sum()['asset.notionalAmount']
    result['1.3.1.1 按净持仓的名义本金'] = underlyer_positions['marketValue'].fillna(0.0).sum()
    result['1.3.1.2 按绝对持仓的名本金'] = 0.0
    result['1.3.2 个股对冲的名义本金'] = result['1.3.1.1 按净持仓的名义本金']
    return result
