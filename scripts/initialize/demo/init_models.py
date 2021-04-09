# -*- coding: utf-8 -*-
from datetime import date

import init_auth
import utils
from init_calendars import calendar_name, vol_calendar_name

model_name = '默认'
model_instance = 'intraday'

_date_fmt = '%Y-%m-%d'


def create_instruments(tenors, values):
    return [{'tenor': tenor, 'quote': value, 'use': True} for tenor, value in zip(tenors, values)]


def create_vol_instruments(tenors, strike_percent, labels, vols):
    return [{'tenor': tenor,
             'vols': [{'percent': p, 'label': l, 'quote': vol} for (p, l) in zip(strike_percent, labels)]}
            for tenor, vol in zip(tenors, vols)]


def create_vol(name, underlyer, instance, spot, tenors, strike_percent, labels, vols, val, host, token):
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
        'instruments': create_vol_instruments(tenors, strike_percent, labels, vols),
        'daysInYear': 245,
        'useCalendarForTenor': True,
        'calendars': [calendar_name],
        'useVolCalendar': True,
        'volCalendar': vol_calendar_name
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
    host = init_auth.host
    token = utils.login(init_auth.script_user_name, init_auth.script_user_password, host)

    val = date.today()
    field = 'last'
    tenors = ['1D', '1W', '2W', '3W', '1M', '3M', '6M', '9M', '1Y']
    strike_percent = [0.8, 0.9, 0.95, 1, 1.05, 1.1, 1.2]
    labels = ['80% SPOT', '90% SPOT', '95% SPOT', '100% SPOT', '105% SPOT', '110% SPOT', '120% SPOT']
    vols = [0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2]
    rs = [0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04]
    qs = [0, 0, 0, 0, 0, 0, 0, 0, 0]

    print('========== Creating models ==========')
    res = create_risk_free_curve(model_name, model_instance, tenors, rs, val, host, token)
    print(res)
    whitelist = utils.call('mktInstrumentWhitelistList', {}, 'market-data-service', host, token)
    for underlyer in whitelist:
        instrument_id = underlyer['instrumentId']
        res = utils.call('mktInstrumentInfo', {'instrumentId': instrument_id}, 'market-data-service', host, token)
        if res is None:
            print(instrument_id + ' 不存在')
        else:
            quote = get_quote(instrument_id, val, host, token)
            res = create_vol(model_name + '波动率曲面', instrument_id, model_instance, quote[field],
                             tenors, strike_percent, labels, vols, val, host, token)
            print(res)
            res = create_dividend_curve(model_name + '分红曲线', instrument_id, model_instance, tenors, qs, val,
                                        host, token)
            print(res)
