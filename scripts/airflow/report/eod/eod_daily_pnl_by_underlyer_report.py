def eod_daily_pnl_by_underlyer_report(risks, yst_positions, historical_pnl, yst_historical_pnl, cash_flows_today,
                                      position_index, pricing_environment):
    """Return daily pnl by underlyer and book.

    risks: eod basic risk report
    yst_positions: eod position report yesterday
    historical_pnl: historical pnl by underlyer report today
    yst_historical_pnl: historical pnl by underlyer report yesterday
    cash_flows_today: basic cash flows today
    position_index: second result of basic position report
    """
    # books and underlyers
    book_underlyer = {b: set() for b in set([p['bookName'] for p in historical_pnl])}
    for p in historical_pnl:
        book_underlyer[p['bookName']].add(p['underlyerInstrumentId'])
    # for p in risks:
    #     index = position_index[p]
    #     if index['bookName'] not in book_underlyer:
    #         book_underlyer[index['bookName']] = set()
    #     book_underlyer[index['bookName']].add(index['underlyerInstrumentId'])
    # set up report
    pnls = {b: {} for b in book_underlyer}
    for b in book_underlyer:
        for u in book_underlyer[b]:
            pnls[b][u] = {
                'bookName': b,
                'underlyerInstrumentId': u,
                'dailyPnl': 0,
                'dailyOptionPnl': 0,
                'dailyUnderlyerPnl': 0,
                'pnlContributionNew': 0,
                'pnlContributionSettled': 0,
                'pnlContributionDelta': 0,
                'pnlContributionGamma': 0,
                'pnlContributionVega': 0,
                'pnlContributionTheta': 0,
                'pnlContributionRho': 0,
                'pnlContributionUnexplained': 0
            }
    # pnls
    for p in historical_pnl:
        b = p['bookName']
        underlyer = p['underlyerInstrumentId']
        pnl = pnls[b][underlyer]
        pnl['dailyOptionPnl'] = p['optionPnl']
        pnl['dailyUnderlyerPnl'] = p['underlyerPnl']
        pnl['dailyPnl'] = p['pnl']
    for p in yst_historical_pnl:
        b = p['bookName']
        if b not in pnls:
            continue
        underlyer = p['underlyerInstrumentId']
        if underlyer not in pnls[b]:
            continue
        pnl = pnls[b][underlyer]
        pnl['dailyOptionPnl'] -= p['optionPnl']
        pnl['dailyUnderlyerPnl'] -= p['underlyerPnl']
        pnl['dailyPnl'] -= p['pnl']
    # greeks contribution
    yst_position_by_id = {r['positionId']: r for r in yst_positions}
    for r in risks.values():
        p_id = r['positionId']
        if not r['isSuccess']:
            continue
        index = position_index[p_id]
        pnl = pnls[index['bookName']][index['underlyerInstrumentId']]
        if p_id not in yst_position_by_id:
            continue
        yst = yst_position_by_id[p_id]
        if yst['marketValue'] is None or yst['number'] == 0:
            continue
        multiplier = index['underlyerMultiplier']
        fraction = abs(r['quantity'] / yst['number'] / multiplier)
        yst_spot = yst['underlyerPrice']
        d_spot = r['underlyerPrice'] - yst_spot
        pnl['pnlContributionDelta'] += yst['delta'] * multiplier * d_spot * fraction
        pnl['pnlContributionGamma'] += yst['gamma'] * multiplier / yst_spot * d_spot * d_spot * 50 * fraction
        if yst['vol'] is not None:
            pnl['pnlContributionVega'] += yst['vega'] * (r['vol'] - yst['vol']) * 100 * fraction
        pnl['pnlContributionTheta'] += yst['theta'] * fraction
        if yst['r'] is not None:
            pnl['pnlContributionRho'] += yst['rho'] * (r['r'] - yst['r']) * 100 * fraction
    # new and settled positions contribution
    for p_id, c in cash_flows_today.items():
        index = position_index[p_id]
        pnl = pnls[index['bookName']][index['underlyerInstrumentId']]
        if p_id not in risks:
            if p_id in yst_position_by_id:  # totally settled
                market_value = yst_position_by_id[p_id]['marketValue']
                if market_value is None:
                    continue
                pnl['pnlContributionSettled'] += c['open'] + c['unwind'] + c['settle'] - market_value
            else:  # opened and closed in one day
                pnl['pnlContributionSettled'] += c['open'] + c['unwind'] + c['settle']
        else:
            if p_id in yst_position_by_id:  # partially settled?
                pnl['pnlContributionSettled'] += c['open'] + c['unwind'] + c['settle']
                r = risks[p_id]
                yst_p = yst_position_by_id[p_id]
                market_value = yst_p['marketValue']
                if (market_value is None) or (not r['isSuccess']):
                    continue
                pnl['pnlContributionSettled'] -= market_value \
                                                 * (yst_p['number'] - r['quantity'] / index['underlyerMultiplier']) / \
                                                 yst_p['number']
            else:  # new
                r = risks[p_id]
                if not r['isSuccess']:
                    continue
                pnl['pnlContributionNew'] += c['open'] + c['unwind'] + c['settle'] + risks[p_id]['price']

    # flatten
    report = []
    for book_pnl in pnls.values():
        report.extend(list(book_pnl.values()))
    # unexplained pnl contribution
    for r in report:
        r['pnlContributionUnexplained'] = \
            r['dailyOptionPnl'] - r['pnlContributionDelta'] - r['pnlContributionGamma'] - r['pnlContributionVega'] \
            - r['pnlContributionTheta'] - r['pnlContributionRho'] - r['pnlContributionNew'] \
            - r['pnlContributionSettled']
        r['pricingEnvironment'] = pricing_environment
    return report
