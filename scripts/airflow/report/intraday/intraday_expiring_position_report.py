# -*- coding: utf-8 -*-

_datetime_fmt = '%Y-%m-%dT%H:%M:%S'
from datetime import datetime


def intraday_expiring_position_report(expirings, risks, cash_flows):
    """Return report on positions expire today.

    expirings: expiring positions from basic position report
    risks: basic risk report
    cash_flows: basic cash flow report
    """
    report = {}
    timestamp = str(datetime.now())
    for p_id, p in expirings.items():
        multiplier = p['underlyerMultiplier']
        # position info
        re = {
            'bookName': p['bookName'],
            'partyName': p['counterPartyName'],
            'positionId': p['positionId'],
            'tradeId': p['tradeId'],
            'underlyerInstrumentId': p['underlyerInstrumentId'],
            'productType': p['productType'],
            'initialNumber': p['initialNumber'],
            'unwindNumber': p['unwindNumber'],
            'number': p['quantity'] / multiplier,
            'tradeDate': p['tradeDate'],
            'expirationDate': p['expirationDate'],
            'underlyerMultiplier': multiplier,
            # 'daysInYear': p['daysInYear']
        }
        # cash flow
        if p_id in cash_flows:
            c = cash_flows[p_id]
            re['premium'] = c['open']
            re['unwindAmount'] = c['unwind'] + c['settle']
        else:
            re['premium'] = p['actualPremium']
            re['unwindAmount'] = 0
        # market value and risks
        r = risks[p_id]
        if r['isSuccess']:
            spot = r['underlyerPrice']
            re['marketValue'] = r['price']
            re['pnl'] = re['marketValue'] + re['premium'] + re['unwindAmount']
            re['delta'] = r['delta'] / multiplier
            re['deltaCash'] = r['delta'] * spot
            re['gamma'] = r['gamma'] * spot / 100 / multiplier
            re['gammaCash'] = re['gamma'] * spot * multiplier
            re['vega'] = r['vega'] / 100
            re['theta'] = r['theta'] / 365
            re['rho'] = r['rhoR'] / 100
            # re['underlyerPrice'] = r['underlyerPrice']
            # re['r'] = r['r']
            # re['vol'] = r['vol']
            # re['q'] = r['q']
        else:
            re['marketValue'] = None
            re['pnl'] = None
            re['delta'] = None
            re['deltaCash'] = None
            re['gamma'] = None
            re['gammaCash'] = None
            re['vega'] = None
            re['theta'] = None
            re['rho'] = None
            # re['underlyerPrice'] = None
            # re['r'] = None
            # re['vol'] = None
            # re['q'] = None
        re['message'] = r['message']
        re['deltaDecay'] = None if re['delta'] is None else -re['delta']
        re['deltaWithDecay'] = 0
        re['pricingEnvironment'] = r.get('pricing_environment', '')
        re['createdAt'] = timestamp
        report[p_id] = re
    return list(report.values())
