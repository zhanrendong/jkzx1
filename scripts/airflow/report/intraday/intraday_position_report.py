# -*- coding: utf-8 -*-

from datetime import datetime, timedelta

from utils import utils

_datetime_fmt = '%Y-%m-%dT%H:%M:%S'


def intraday_position_report(positions, risks, cash_flows, pricing_environment, domain, headers):
    """Return intraday position report.

    positions: basic position report, live positions(not expiring)
    risks: basic risk report
    cash_flows: basic cash flow report
    """
    def construct_report(pos, cfs, rks):
        timestamp = str(datetime.now())
        report = {}
        for p_id, p in pos.items():
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
            if p_id in cfs:
                c = cfs[p_id]
                re['premium'] = c['open']
                re['unwindAmount'] = c['unwind'] + c['settle']
            else:
                re['premium'] = p['actualPremium']
                re['unwindAmount'] = 0
            # market value and risks
            r = rks[p_id]
            if r['isSuccess']:
                spot = r['underlyerPrice']
                re['marketValue'] = r['price']
                re['pnl'] = re['marketValue'] + re['premium'] + re['unwindAmount']
                re['delta'] = r['delta'] / multiplier
                re['deltaCash'] = r['delta'] * spot / multiplier
                re['gamma'] = r['gamma'] * spot / 100 / multiplier
                re['gammaCash'] = re['gamma'] * spot
                re['vega'] = r['vega'] / 100
                re['theta'] = r['theta'] / 365
                re['rho'] = r['rhoR'] / 100
                # re['underlyerPrice'] = r['underlyerPrice']
                re['r'] = r['r']
                re['vol'] = r['vol']
                re['q'] = r['q']
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
                re['r'] = None
                re['vol'] = None
                re['q'] = None
            re['message'] = r['message']
            re['deltaDecay'] = None
            re['deltaWithDecay'] = None
            re['pricingEnvironment'] = r.get('pricing_environment', '')
            re['createdAt'] = timestamp
            report[p_id] = re
        return report

    report = construct_report(positions, cash_flows, risks)
    trade_ids = list(set([r['tradeId'] for r in report.values()]))

    # delta decay
    decay_time = (datetime.now() + timedelta(days=1)).replace(hour=9, minute=0, second=0, microsecond=0)
    params = {
        'requests': ['delta'],
        'tradeIds': trade_ids,
        'pricingEnvironmentId': pricing_environment,
        'valuationDateTime': decay_time.strftime(_datetime_fmt),
        'timezone': None
    }
    decay_data = utils.call_request(domain, 'pricing-service', 'prcPrice', params, headers)
    if 'result' in decay_data:
        for d in decay_data['result']:
            p_id = d['positionId']
            if p_id not in report:
                continue
            re = report[p_id]
            if not risks[p_id]['isSuccess']:
                continue
            if d['delta'] is not None:
                re['deltaWithDecay'] = d['delta'] / re['underlyerMultiplier']
                re['deltaDecay'] = re['deltaWithDecay'] - re['delta']
            else:
                re['message'] = 'Delta decay 定价返回NaN'
    if 'diagnostics' in decay_data:
        for d in decay_data['diagnostics']:
            p_id = d['key']
            if p_id not in report:
                continue
            re = report[p_id]
            if re['message'] == '':
                re['message'] = 'Delta decay ' + d['message']
    return list(report.values())
