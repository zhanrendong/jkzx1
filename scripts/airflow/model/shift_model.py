from utils import utils
from datetime import date, timedelta
from config.bct_config import bct_password, bct_user

_MODEL_TYPE_VOL_SURFACE = 'VOL_SURFACE'
_MODEL_TYPE_RISK_FREE_CURVE = 'RISK_FREE_CURVE'
_MODEL_TYPE_DIVIDEND_CURVE = 'DIVIDEND_CURVE'
_date_fmt = '%Y-%m-%d'


def shift_model_valuation_date(host, headers, model_type, name, instrument_id, instance,
                               valuation_date, new_valuation_date):
    return utils.call_request(host, 'model-service', 'mdlModelShiftValuationDate', {
        'modelType': model_type,
        'modelName': name,
        'instance': instance,
        'underlyer': instrument_id,
        'valuationDate': valuation_date.strftime(_date_fmt),
        'newValuationDate': new_valuation_date.strftime(_date_fmt),
        'save': True
    }, headers)


def shift_model_valuation_date_batch(host, headers, model_type, name, instrument_ids, instance,
                                     valuation_date, new_valuation_date):
    return utils.call_request(host, 'model-service', 'mdlModelShiftValuationDateBatch', {
        'modelType': model_type,
        'modelName': name,
        'instance': instance,
        'underlyers': instrument_ids,
        'valuationDate': valuation_date.strftime(_date_fmt),
        'newValuationDate': new_valuation_date.strftime(_date_fmt),
        'save': True
    }, headers)


def get_whitelist_instrumeents(host, headers):
    whitelist = utils.call_request(host, 'market-data-service', 'mktInstrumentWhitelistList', {}, headers)['result']
    return [i['instrumentId'] for i in whitelist]


def shift_models(host, headers, vol_surfaces, risk_free_curves, dividend_curves):
    today = date.today()
    new_val_date = today + timedelta(days=1)
    instrument_ids = get_whitelist_instrumeents(host, headers)
    for i in instrument_ids:
        print(i)
        for vs in vol_surfaces:
            return shift_model_valuation_date(host, headers, _MODEL_TYPE_VOL_SURFACE,
                                              vs['name'], i, vs['instance'], today, new_val_date)
        for rc in risk_free_curves:
            return shift_model_valuation_date(host, headers, _MODEL_TYPE_RISK_FREE_CURVE,
                                              rc['name'], i, rc['instance'], today, new_val_date)
        for dc in dividend_curves:
            return shift_model_valuation_date(host, headers, _MODEL_TYPE_DIVIDEND_CURVE,
                                              dc['name'], i, dc['instance'], today, new_val_date)


def shift_models_batch(host, headers, vol_surfaces, risk_free_curves, dividend_curves):
    today = date.today()
    new_val_date = today + timedelta(days=1)
    instrument_ids = get_whitelist_instrumeents(host, headers)
    for rc in risk_free_curves:
        res = shift_model_valuation_date(host, headers, _MODEL_TYPE_RISK_FREE_CURVE,
                                         rc['name'], None, rc['instance'], today, new_val_date)
        if 'result' in res:
            print('Shifted {n}({i}).'.format(n=rc['name'], i=rc['instance']))
        else:
            print('Error occurs when shifting {n}({i}).'.format(n=rc['name'], i=rc['instance']))
    for vs in vol_surfaces:
        res = shift_model_valuation_date_batch(host, headers, _MODEL_TYPE_VOL_SURFACE,
                                               vs['name'], instrument_ids, vs['instance'], today, new_val_date)
        if 'result' in res:
            print('Shifted {n} ({i}): {num}.'.format(n=vs['name'], i=vs['instance'], num=len(res['result'])))
        else:
            print('Error occurs when shifting {n}({i}).'.format(n=vs['name'], i=vs['instance']))
    for dc in dividend_curves:
        res = shift_model_valuation_date_batch(host, headers, _MODEL_TYPE_DIVIDEND_CURVE,
                                               dc['name'], instrument_ids, dc['instance'], today, new_val_date)
        if 'result' in res:
            print('Shifted {n} ({i}): {num}.'.format(n=dc['name'], i=dc['instance'], num=len(res['result'])))
        else:
            print('Error occurs when shifting {n}({i}).'.format(n=dc['name'], i=dc['instance']))


if __name__ == '__main__':
    host = 'localhost'
    login_body = {
        'userName': bct_user,
        'password': bct_password
    }
    headers = utils.login(host, login_body)

    vol_surfaces = [{'name': 'TRADER_VOL', 'instance': 'intraday'},
                    {'name': 'TRADER_VOL', 'instance': 'close'}]
    risk_free_curves = [{'name': 'TRADER_RISK_FREE_CURVE', 'instance': 'intraday'},
                        {'name': 'TRADER_RISK_FREE_CURVE', 'instance': 'close'}]
    dividend_curves = [{'name': 'TRADER_DIVIDEND_CURVE', 'instance': 'intraday'},
                       {'name': 'TRADER_DIVIDEND_CURVE', 'instance': 'close'}]
    shift_models_batch(host, headers, vol_surfaces, risk_free_curves, dividend_curves)
