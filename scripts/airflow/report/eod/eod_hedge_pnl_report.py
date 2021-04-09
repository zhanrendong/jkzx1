from report.basic_listed_positions import get_underlyer_positions


def get_eod_hedge_pnl(ip, headers, prcing_env):
    positions = get_underlyer_positions(ip, headers, prcing_env)
    result_positions = []
    stock_hedge_pnl = 0.0
    commodity_hedge_pnl = 0.0
    floor_options_hedge_pnl = 0.0
    for id, position in positions.items():
        for id2, p in position.items():
            result_positions.append(p)
    for p in result_positions:
        if p['totalPnl']:
            if 'SH' in p['instrumentId'] or 'CFE' in p['instrumentId'] or 'SZ' in p['instrumentId']:
                stock_hedge_pnl += p['totalPnl']
            else:
                commodity_hedge_pnl += p['totalPnl']
    hedge_all_pnl = stock_hedge_pnl + commodity_hedge_pnl + floor_options_hedge_pnl
    result = {'2.2.1 商品期货对冲盈亏': commodity_hedge_pnl, '2.2.2 个股对冲盈亏': stock_hedge_pnl,
              '2.2.3 场内期权对冲盈亏': floor_options_hedge_pnl, '2.2.4 对冲总盈亏': hedge_all_pnl}
    return result

