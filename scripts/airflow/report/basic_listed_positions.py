# -*- coding: utf-8 -*-
from utils import utils


def get_underlyer_positions(domain, headers, prcing_env):
    """Return exchange listed spot/future trade statistics."""
    books = utils.call_request(domain, 'trade-service', 'trdBookListBySimilarBookName',
                               {"similarBookName": ""}, headers)['result']
    listed_data = utils.call_request(domain, 'pricing-service', 'prcPriceOnExchange',
                                     {"pricingEnvironmentId": prcing_env, 'books': books}, headers)
    if 'result' in listed_data:
        underlyers = {}
        for l in listed_data['result']:
            if None in [l[v] for v in ['marketValue', 'price', 'delta', 'gamma', 'vega', 'theta', 'rhoR']]:
                continue
            temp_dict = underlyers.get(l['bookId'], {})
            temp_dict[l['instrumentId']] = l
            underlyers[l['bookId']] = temp_dict
        return underlyers
    else:
        raise RuntimeError('Failed to fetch spot/future trade statistics.')
