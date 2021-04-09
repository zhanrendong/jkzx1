# -*- coding: utf-8 -*-
from trade_import import utils


def get_instruments(ip, headers, params):
    return utils.call_request(ip, 'market-data-service', 'mktInstrumentsListPaged', params, headers)['page']


def get_all_instrument(ip, headers):
    params = {}
    return get_instruments(ip, headers, params)


def get_all_instrument_id(ip, headers):
    params = {
        'instrumentIdPart': ''
    }
    return utils.call_request(ip, 'market-data-service', 'mktInstrumentSearch', params, headers)


def get_instrument_by_id(ip, headers, instrument_id):
    params = {
        'instrumentIds': [instrument_id]
    }
    result = get_instruments(ip, headers, params)
    if len(result) == 0:
        return None
    return result[0]


def save_instrument(ip, headers, instrument_id, asset_class, instrument_type, instrument_info):
    params = {
        'instrumentId': instrument_id,
        'assetClass': asset_class,
        'instrumentType': instrument_type,
        'instrumentInfo': instrument_info
    }
    return utils.call_request(ip, 'market-data-service', 'mktInstrumentCreate', params, headers)


