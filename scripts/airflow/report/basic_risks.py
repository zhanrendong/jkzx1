# -*- coding: utf-8 -*-

from utils import utils

_datetime_fmt = '%Y-%m-%dT%H:%M:%S'


def get_risks(positions_list, pricing_environment, valuation_datetime, domain, headers):
    """Return pricing results of given positions.

    positions_list: list of basic positions(live, and possibly expiring positions)"""
    trade_ids = set()
    position_ids = set()
    for positions in positions_list:
        position_ids.update([p['positionId'] for p in positions.values()])
        trade_ids.update([p['tradeId'] for p in positions.values()])
    requests = ['price', 'delta', 'gamma', 'vega', 'theta', 'rho_r']
    params = {
        'requests': requests,
        'tradeIds': list(trade_ids),
        'pricingEnvironmentId': pricing_environment,
        'valuationDateTime': valuation_datetime.strftime(_datetime_fmt),
        'timezone': None
    }
    pricing_data = utils.call_request(domain, 'pricing-service', 'prcPrice', params, headers)
    if 'result' in pricing_data:
        risks = []
        for r in pricing_data['result']:
            if r['positionId'] not in position_ids:
                continue
            position_ids.remove(r['positionId'])
            risks.append(r)
            r['isSuccess'] = True
            r['message'] = ''
            for req in requests:
                if req == 'rho_r':
                    req = 'rhoR'
                if r[req] is None:
                    r['isSuccess'] = False
                    r['message'] = '定价返回空'
                    break
        for d in pricing_data['diagnostics']:
            p_id = d['key']
            if p_id in position_ids:
                position_ids.remove(p_id)
            risks.append({
                'positionId': p_id,
                'isSuccess': False,
                'message': d['message']
            })
        for p_id in position_ids:
            risks.append({
                'positionId': p_id,
                'isSuccess': False,
                'message': '定价返回空'
            })
        pe_description = utils.get_pricing_env_description(pricing_environment, domain, headers)
        for risk in risks:
            risk['pricing_environment'] = pe_description
        return {r['positionId']: r for r in risks}, pricing_data['result']
    else:
        raise RuntimeError('Failed to price trades.')
