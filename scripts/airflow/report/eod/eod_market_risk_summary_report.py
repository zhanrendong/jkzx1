# -*- coding: utf-8 -*-

_datetime_fmt = '%Y-%m-%dT%H:%M:%S'
MARKET_RISK_PRODUCT_TYPES = ['VANILLA_EUROPEAN', 'VANILLA_AMERICAN']


def eod_market_risk_summary_report(positions, pricing_environment):
    risk = {
        'deltaCash': 0,
        'gammaCash': 0,
        'vega': 0,
        'theta': 0,
        'rho': 0,
        'pricingEnvironment': pricing_environment
    }
    for p in positions:
        if has_pricing_error(p) or p.get('productType') not in MARKET_RISK_PRODUCT_TYPES:
            continue
        risk['deltaCash'] += p['deltaCash']
        risk['gammaCash'] += p['gammaCash']
        risk['vega'] += p['vega']
        risk['theta'] += p['theta']
        risk['rho'] += p['rho']
    return [risk]


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