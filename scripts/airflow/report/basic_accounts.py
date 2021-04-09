# -*- coding: utf-8 -*-
from utils import utils


def get_accounts(domain, headers):
    """Return client accounts."""
    accounts_data = utils.call_request(domain, 'reference-data-service', 'clientAccountSearch', {}, headers)
    if 'result' in accounts_data:
        return accounts_data['result']
    else:
        raise RuntimeError('Failed to fetch client accounts.')


def get_funds(domain, headers):
    """Return account cash flows."""
    funds_data = utils.call_request(domain, 'reference-data-service', 'cliFundEvnetListAll', {}, headers)
    if 'result' in funds_data:
        return funds_data['result']
    else:
        raise RuntimeError('Failed to fetch client account cash flows.')
