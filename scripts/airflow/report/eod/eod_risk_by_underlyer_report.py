# -*- coding: utf-8 -*-
from datetime import datetime, timedelta
from utils import utils
from report.basic_quotes import get_underlyer_quotes
import numpy as np

_datetime_fmt = '%Y-%m-%dT%H:%M:%S'


def eod_risk_by_underlyer_report(positions, underlyer_positions, domain, headers, pricing_environment):
    """Return Eod risk collected by book and underlyer.

    positions: eod position report
    underlyer_positions: basic underlyer position report"""
    # find books and underlyers
    books = set([p['bookName'] for p in positions])
    books.update(underlyer_positions.keys())
    book_underlyer = {b: set() for b in books}
    for p in positions:
        book_underlyer[p['bookName']].add(p['underlyerInstrumentId'])
    for b, u in underlyer_positions.items():
        book_underlyer[b].update([l['underlyerInstrumentId'] for l in u.values()])
    # get quotes
    underlyers = set()
    for b in book_underlyer.values():
        for u in b:
            underlyers.add(u)
    underlyers = list(underlyers)
    quotes = get_underlyer_quotes(underlyers, datetime.now(), domain, headers)
    yst_quotes = get_underlyer_quotes(underlyers, datetime.now() - timedelta(days=1), domain, headers)
    # empty report
    risks = {b: {} for b in books}
    for b in book_underlyer:
        for u in book_underlyer[b]:
            spot = quotes[u]['close']
            if spot is None or spot == 0:
                continue
            spot_change_ratio = (spot - np.float64(yst_quotes.get(u).get('close'))) / spot
            risks[b][u] = {
                'bookName': b,
                'underlyerInstrumentId': u,
                'underlyerPrice': spot,
                'underlyerNetPosition': 0,
                'delta': 0,
                'deltaCash': 0,
                'netDelta': 0,
                'deltaDecay': 0,
                'deltaWithDecay': 0,
                'gamma': 0,
                'gammaCash': 0,
                'vega': 0,
                'theta': 0,
                'rho': 0
            }
            if not utils.is_nan(spot_change_ratio):
                risks[b][u]['underlyerPriceChangePercent'] = spot_change_ratio
    # underlyer positions
    for b in underlyer_positions.values():
        for u in b.values():
            if risks[u['bookId']].get(u['instrumentId'], None) is None:
                continue
            r = risks[u['bookId']][u['instrumentId']]
            spot = u['underlyerPrice']
            multiplier = u['underlyerInstrumentMultiplier']
            r['underlyerMultiplier'] = multiplier
            if u['instrumentId'] == u['underlyerInstrumentId']:
                r['underlyerNetPosition'] += u['netPosition']
            else:
                r['delta'] += u['delta'] / multiplier
            r['gamma'] += u['gamma'] * spot / 100 / multiplier
            r['gammaCash'] += u['gamma'] * spot * spot / 100 / multiplier
            r['vega'] += u['vega'] / 100
            r['theta'] += u['theta'] / 365
            r['rho'] += u['rhoR'] / 100
    # option risks
    for p in positions:
        if has_pricing_error(p):
            continue
        r = risks[p['bookName']][p['underlyerInstrumentId']]
        r['delta'] += p['delta']
        r['deltaDecay'] += p['deltaDecay']
        r['deltaWithDecay'] += p['deltaWithDecay']
        r['gamma'] += p['gamma']
        r['gammaCash'] += p['gammaCash']
        r['vega'] += p['vega']
        r['theta'] += p['theta']
        r['rho'] += p['rho']
        if r.get('underlyerMultiplier', None) is None:
            r['underlyerMultiplier'] = p['underlyerMultiplier']

    for b in risks:
        for u in risks[b]:
            r = risks[b][u]
            if r.get('underlyerMultiplier', None) is None:
                continue
            r['netDelta'] = r['delta'] + r['underlyerNetPosition']
            r['deltaCash'] = r['netDelta'] * r['underlyerPrice'] * r['underlyerMultiplier']
            r.pop('underlyerMultiplier')
    # flatten
    report = []
    for b in risks:
        report.extend(list(risks[b].values()))
    for re in report:
        re['pricingEnvironment'] = pricing_environment
    return report


def has_pricing_error(pos):
    return (pos['marketValue'] is None) or \
           (pos['delta'] is None) or \
           (pos['deltaDecay'] is None) or \
           (pos['deltaWithDecay'] is None) or \
           (pos['gamma'] is None) or \
           (pos['gammaCash'] is None) or \
           (pos['vega'] is None) or \
           (pos['theta'] is None) or \
           (pos['rho'] is None)
