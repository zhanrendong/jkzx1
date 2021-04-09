def get_eod_hedge_pnl(positions):
    position_data = positions[{'instrumentId', 'totalPnl'}]
    stock_hedge_pnl_message = position_data.where(
        position_data['instrumentId'].str.contains('SZ')
        | position_data['instrumentId'].str.contains('CFE')
        | position_data['instrumentId'].str.contains('SH'))
    commodity_hedge_pnl_message = position_data.mask(
        position_data['instrumentId'].str.contains('SZ')
        | position_data['instrumentId'].str.contains('CFE')
        | position_data['instrumentId'].str.contains('SH'))
    result = {}
    result['2.2.1 商品期货对冲盈亏'] = commodity_hedge_pnl_message.sum()['totalPnl']
    result['2.2.2 个股对冲盈亏'] = stock_hedge_pnl_message.sum()['totalPnl']
    result['2.2.3 场内期权对冲盈亏'] = 0.0
    result['2.2.4 对冲总盈亏'] = result['2.2.1 商品期货对冲盈亏'] + result['2.2.2 个股对冲盈亏'] + result['2.2.3 场内期权对冲盈亏']
    return result
