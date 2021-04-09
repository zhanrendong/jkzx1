# -*- coding: utf-8 -*-
from datetime import date

import utils
from init_params import *

_date_fmt = '%Y-%m-%d'


def create_instruments(tenors, values):
    return [{'tenor': tenor, 'quote': value, 'use': True} for tenor, value in zip(tenors, values)]


def create_vol_instruments(tenors, vols):
    return [{'tenor': tenor,
             'vols': [{'percent': p, 'label': l, 'quote': vol} for (p, l) in zip(strike_percent, labels)]}
            for tenor, vol in zip(tenors, vols)]


def create_vol(name, underlyer, instance, spot, tenors, vols, val, host, token):
    params = {
        'save': True,
        'modelName': name,
        'valuationDate': val.strftime(_date_fmt),
        'instance': instance,
        'underlyer': {
            'instrumentId': underlyer,
            'instance': instance,
            'field': 'close' if instance.upper() == 'CLOSE' else 'last',
            'quote': spot
        },
        'instruments': create_vol_instruments(tenors, vols),
        'daysInYear': 365
    }
    return utils.call('mdlVolSurfaceInterpolatedStrikeCreate', params, 'model-service', host, token)


def create_risk_free_curve(name, instance, tenors, rates, val, host, token):
    curve = {
        'save': True,
        'modelName': name,
        'valuationDate': val.strftime(_date_fmt),
        'instance': instance,
        'instruments': create_instruments(tenors, rates)
    }
    return utils.call('mdlCurveRiskFreeCreate', curve, 'model-service', host, token)


def create_dividend_curve(name, underlyer, instance, tenors, rates, val, host, token):
    dividend = {
        'save': True,
        'underlyer': underlyer,
        'modelName': name,
        'valuationDate': val.strftime(_date_fmt),
        'instance': instance,
        'instruments': create_instruments(tenors, rates)
    }
    return utils.call('mdlCurveDividendCreate', dividend, 'model-service', host, token)


def get_quote(instrument_id, val, host, token):
    return utils.call('mktQuotesListPaged', {
        'instrumentIds': [instrument_id],
        'valuationDate': val.strftime(_date_fmt),
        'timezone': 'Asia/Shanghai',
        'page': None,
        'pageSize': None
    }, 'market-data-service', host, token)['page'][0]


if __name__ == '__main__':
    import init_auth

    token = utils.login(init_auth.trader_names[0], init_auth.trader_password, host)
    val = date.today()

    print('========== Creating models ==========')
    name = risk_free_curve_name
    res = create_risk_free_curve('TRADER_RISK_FREE_CURVE', 'close', tenors, rs, val, host, token)
    print(res)
    res = create_risk_free_curve('TRADER_RISK_FREE_CURVE', 'intraday', tenors, rs, val, host, token)
    print(res)
    whitelist = utils.call('mktInstrumentWhitelistList', {}, 'market-data-service', host, token)
    for underlyer in whitelist:
        instrument_id = underlyer['instrumentId']
        print('======== Creating Vols for ' + instrument_id + ' ==========')
        res = utils.call('mktInstrumentInfo', {'instrumentId': instrument_id}, 'market-data-service',
                         host, token)
        if res is None:
            print(instrument_id + ' 不存在')
        else:
            quote = get_quote(instrument_id, val, host, token)
            print('---------- Creating vol surfaces ----------')
            name = vol_surface_name
            res = create_vol(name, instrument_id, 'close', quote['close'], tenors, vols, val, host, token)
            print(res)
            res = create_vol(name, instrument_id, 'intraday', quote['last'], tenors, vols, val, host, token)
            print(res)
            print('---------- Creating dividend curves ----------')
            name = dividend_curve_name
            res = create_dividend_curve(name, instrument_id, 'close', tenors, qs, val, host, token)
            print(res)
            res = create_dividend_curve(name, instrument_id, 'intraday', tenors, qs, val, host, token)
            print(res)
