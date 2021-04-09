# -*- coding: utf-8 -*-


def eod_historical_pnl_by_underlyer_report(positions, cash_flows, underlyer_positions, position_map,
                                           pricing_environment):
    """Return historical pnl of both option and underlyer trades.

    positions: eod position report
    cash_flows: eod basic cash flow report
    underlyer_positions: eod basic underlyer position report
    position_map: position id to book and underlyer map
    """
    # books and underlyers
    books = set([p['bookName'] for p in position_map.values()])
    books.update(underlyer_positions.keys())
    book_underlyer = {b: set() for b in books}
    for p in position_map.values():
        book_underlyer[p['bookName']].add(p['underlyerInstrumentId'])
    for b, u in underlyer_positions.items():
        book_underlyer[b].update(u.keys())

    # set up report
    pnls = {b: {} for b in books}
    for b in book_underlyer:
        for u in book_underlyer[b]:
            pnls[b][u] = {
                'bookName': b,
                'underlyerInstrumentId': u,
                'pnl': 0,
                'optionPremium': 0,
                'optionUnwindAmount': 0,
                'optionSettleAmount': 0,
                'optionMarketValue': 0,
                'optionPnl': 0,
                'underlyerBuyAmount': 0,
                'underlyerSellAmount': 0,
                'underlyerNetPosition': 0,
                'underlyerPrice': 0,
                'underlyerMarketValue': 0,
                'underlyerPnl': 0
            }
    # add underlyer pnls
    for b in underlyer_positions.values():
        for up in b.values():
            pnl = pnls[up['bookId']][up['instrumentId']]
            pnl['underlyerPrice'] = up['underlyerPrice']
            pnl['underlyerBuyAmount'] = up['historyBuyAmount']
            pnl['underlyerSellAmount'] = up['historySellAmount']
            pnl['underlyerNetPosition'] = up['netPosition']
            pnl['underlyerMarketValue'] = up['marketValue'] / up['underlyerInstrumentMultiplier'] \
                if up['marketValue'] is not None else 0
            pnl['underlyerPnl'] = up['totalPnl'] if up['totalPnl'] is not None else 0
    # add live options market value
    for p in positions:
        if p['marketValue'] is None:
            continue
        pnl = pnls[p['bookName']][p['underlyerInstrumentId']]
        pnl['underlyerPrice'] = p['underlyerPrice']
        pnl['optionMarketValue'] += p['marketValue']
    # add cash flows
    for c in cash_flows.values():
        if c['positionId'] not in position_map:
            continue
        p_map = position_map[c['positionId']]
        pnl = pnls[p_map['bookName']][p_map['underlyerInstrumentId']]
        pnl['optionPremium'] += c['open']
        pnl['optionUnwindAmount'] += c['unwind']
        pnl['optionSettleAmount'] += c['settle']
    # flatten
    report = []
    for book_pnl in pnls.values():
        report.extend(list(book_pnl.values()))
    # calculate pnl
    for r in report:
        r['optionPnl'] = r['optionPremium'] + r['optionUnwindAmount'] + r['optionSettleAmount'] + r['optionMarketValue']
        r['pnl'] = r['optionPnl'] + r['underlyerPnl']
        r['pricingEnvironment'] = pricing_environment
    return report
