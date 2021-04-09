from datetime import datetime


def intraday_daily_pnl_by_underlyer_report(risks, cash_flows_today, underlyer_positions, position_index,
                                           yst_positions, yst_historical_pnl, pricing_environment):
    """Return intraday daily pnl collected by book and underlyer.

    risks: basic risk report
    cash_flows_today: basic today's cash flow report
    underlyer_positions: basic underlyer position report
    position_index: position index from basic position report
    yst_positions: yesterday's EoD position report
    yst_historical_pnl: yesterday's EoD historical pnl report
    """
    timestamp = str(datetime.now())
    book_underlyer = {}
    for book, underlyers in underlyer_positions.items():
        book_underlyer[book] = set([u['underlyerInstrumentId'] for u in underlyers.values()])
    for r in risks:
        index = position_index[r]
        book = index['bookName']
        if book not in book_underlyer:
            book_underlyer[book] = set()
        book_underlyer[book].add(index['underlyerInstrumentId'])
    # set up report
    pnls = {b: {} for b in book_underlyer}
    for b, b_pnls in pnls.items():
        for u in book_underlyer[b]:
            b_pnls[u] = {
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
    # option pnl and greek contributions
    yst_positions_index = {p['positionId']: p for p in yst_positions}
    for p_id, r in risks.items():
        if not r['isSuccess']:
            continue
        index = position_index[p_id]
        pnl = pnls[index['bookName']][index['underlyerInstrumentId']]
        pnl['dailyOptionPnl'] += r['price']
        if p_id in yst_positions_index:
            yst = yst_positions_index[p_id]
            multiplier = index['underlyerMultiplier']
            number = yst['number']
            if yst['marketValue'] is None or multiplier == 0 or number == 0:
                continue

            fraction = abs(r['quantity'] / number / multiplier)
            yst_spot = yst['underlyerPrice']
            d_spot = r['underlyerPrice'] - yst_spot
            pnl['pnlContributionDelta'] += yst['delta'] * multiplier * d_spot * fraction
            pnl['pnlContributionGamma'] += yst['gamma'] * multiplier / yst_spot * d_spot * d_spot * 50 * fraction
            pnl['pnlContributionVega'] += yst['vega'] * (r['vol'] - yst['vol']) * 100 * fraction
            pnl['pnlContributionTheta'] += yst['theta'] * fraction
            pnl['pnlContributionRho'] += yst['rho'] * (r['r'] - yst['r']) * 100 * fraction
    # add today's cash flows
    for p_id, c in cash_flows_today.items():
        index = position_index[p_id]
        book = index['bookName']
        if book not in book_underlyer or index['underlyerInstrumentId'] not in book_underlyer[book]:
            continue
        pnl = pnls[book][index['underlyerInstrumentId']]
        total_cf = c['open'] + c['unwind'] + c['settle']
        pnl['dailyOptionPnl'] += total_cf
        # new/settle pnl contribution
        if p_id not in risks:
            if p_id in yst_positions_index:  # totally settled
                if yst_positions_index[p_id]['marketValue'] is None:
                    continue
                pnl['pnlContributionSettled'] += total_cf - yst_positions_index[p_id]['marketValue']
            else:  # opened and closed in one day
                pnl['pnlContributionSettled'] += total_cf
        else:
            if p_id in yst_positions_index:  # partially settled?
                pnl['pnlContributionSettled'] += total_cf
                yst_p = yst_positions_index[p_id]
                r = risks[p_id]
                if not r['isSuccess'] or yst_p['marketValue'] is None:
                    continue
                pnl['pnlContributionSettled'] -= yst_p['marketValue'] \
                                                 * (yst_p['number'] - r['quantity'] / index['underlyerMultiplier']) / \
                                                 yst_p['number']
            else:  # new
                r = risks[p_id]
                if not r['isSuccess']:
                    continue
                pnl['pnlContributionNew'] += total_cf + risks[p_id]['price']
    # add current underlyer positions
    for b, book_up in underlyer_positions.items():
        for up in book_up.values():
            pnls[b][up['underlyerInstrumentId']]['dailyUnderlyerPnl'] += \
                up['totalPnl'] if up['totalPnl'] is not None else 0
    # subtract yesterday's position
    for p in yst_historical_pnl:
        book = p['bookName']
        if book not in book_underlyer or p['underlyerInstrumentId'] not in book_underlyer[book]:
            continue
        pnl = pnls[book][p['underlyerInstrumentId']]
        pnl['dailyOptionPnl'] -= p['optionMarketValue']
        pnl['dailyUnderlyerPnl'] -= p['underlyerPnl']
    # flatten
    report = []
    for book_pnl in pnls.values():
        report.extend(list(book_pnl.values()))
    # total pnl and unexplained pnl contribution
    for r in report:
        r['dailyPnl'] = r['dailyOptionPnl'] + r['dailyUnderlyerPnl']
        r['pnlContributionUnexplained'] = \
            r['dailyOptionPnl'] - r['pnlContributionDelta'] - r['pnlContributionGamma'] - r['pnlContributionVega'] \
            - r['pnlContributionTheta'] - r['pnlContributionRho'] - r['pnlContributionNew'] \
            - r['pnlContributionSettled']
        r['pricingEnvironment'] = pricing_environment
        r['createdAt'] = timestamp
    return report
