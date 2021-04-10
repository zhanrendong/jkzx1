# -*- coding: utf-8 -*-
import redis
import logging
import requests
import numpy as np
import os
from datetime import datetime, timedelta
from config.bct_config import special_captcha, bct_port


def get_bct_host():
    return os.getenv('BCT_HOST', 'localhost')


def get_bct_port():
    return os.getenv('BCT_PORT', bct_port)


def get_redis_conn(ip):
    return redis.Redis(host=ip, port=6379, db=0)


def login(login_ip, login_body):
    login_url = 'http://' + login_ip + ':' + get_bct_port() + '/auth-service/users/login'
    login_body['captcha'] = special_captcha
    login_res = requests.post(login_url, json=login_body)
    token = login_res.json()['token']
    headers = {
        'Authorization': 'Bearer ' + token
    }
    return headers


def call_request(ip, service, method, params, headers):
    url = 'http://' + ip + ':' + get_bct_port() + '/' + service + '/api/rpc'
    body = {
        'method': method,
        'params': params
    }
    try:
        res = requests.post(url, json=body, headers=headers)
        json = res.json()
        if 'error' in json:
            logging.info("failed execute " + method + " to:" + ip + ",error:" + json['error']['message'])
            return 'error'
        else:
            logging.info("success execute " + method + ",callRequest:" + str(len(params)) + " to " + ip)
            return json
    except Exception as e:
        logging.info("failed execute " + method + " to:" + ip + "Exception:" + str(e))
        raise e


def get_real_time_market_data(params):
    url = 'http://101.132.147.68/terminal-data'
    body = {
        'method': 'getTickMarketData',
        'params': [params],
        'id': 0
    }
    try:
        res = requests.post(url, json=body)
        json = res.json()
        # return json
        if 'error' in json:
            msg = "failed get market data from " + url + ", for " + params['assetClass'] + " : " + \
                  str(params['instrumentIds']) + ",error:" + json['error']['data']['message']
            logging.info(msg)
            return 'error'
        else:
            logging.info("success get market data from " + url + ",callRequest:" + str(len(params)))
            return json
    except Exception as e:
        logging.info("failed get market data from " + url + ",Exception:" + str(e))
        raise e


def trans_datetime(time_str):
    date_str = datetime.now().strftime('%Y-%m-%d')
    datetime_str = date_str + 'T' + time_str
    datetime_time = datetime.strptime(datetime_str, '%Y-%m-%dT%H:%M:%S')
    return datetime_time


def trans_utc_datetime(time_str):
    date_str = datetime.now().strftime('%Y-%m-%d')
    datetime_str = date_str + 'T' + time_str
    datetime_time = datetime.strptime(datetime_str, '%Y-%m-%dT%H:%M:%S')
    return datetime_time - timedelta(hours=8)


def get_pricing_env_description(pricing_env, ip, headers):
    return call_request(ip, 'pricing-service', 'prcPricingEnvironmentGet', {'pricingEnvironmentId': pricing_env},
                        headers)['result'].get('description', '')


def get_correlation(instruments, ip, headers):
    if instruments[0] == instruments[1]:
        return 1
    correlation = call_request(ip, 'market-data-service', 'mktCorrelationMatrixGet', {'instrumentIds': instruments},
                               headers)
    return correlation['result'][0][1] if correlation != 'error' else np.nan


def get_sca_id(legal_name, headers, ip):
    party = call_request(ip, 'reference-data-service', 'refPartyGetByLegalName', {'legalName': legal_name}, headers)
    return party['result']['masterAgreementId'] if party != 'error' else '-'


def get_all_books(ip, headers):
    params = {'resourceType': 'BOOK'}
    books_info_list = call_request(ip, 'auth-service', 'authResourceGetAll', params, headers).get('result') or []
    return list(map(lambda x: x.get('resourceName'), books_info_list))


def get_trading_date_by_offset(offset, ip, headers):
    return call_request(ip, 'reference-data-service', 'refTradeDateByOffsetGet', {'offset': offset}, headers)


def get_tn_trade_date(cur_date, offset, bct_host, bct_login_body):
    headers = login(bct_host, bct_login_body)
    trading_date_result = call_request(bct_host, 'reference-data-service', 'refTradeDateByOffsetGet',
                                       {'offset': offset, 'currentDate': cur_date}, headers)
    if 'error' in trading_date_result:
        raise RuntimeError('T-n交易日获取失败：{}'.format(str(trading_date_result)))
    trading_date = trading_date_result.get('result')
    print(trading_date)
    return trading_date


def call_terminal_request(ip, service, method, params, headers):
    url = 'http://' + ip + '/' + service + '/api/rpc'
    body = {
        'method': method,
        'params': params,
        'id': 1,
        'jsonrpc': '2.0'
    }
    try:
        res = requests.post(url, json=body, headers=headers)
        json = res.json()
        if 'error' in json:
            logging.info("failed execute " + method + " to:" + ip + ",error:" + json['message'])
            raise RuntimeError('error execute in: ' + json['message'])
        else:
            logging.info("success execute " + method + ",callRequest:" + str(len(params)) + " to " + ip)
            return json['result']
    except Exception as e:
        logging.info("failed execute " + method + " to:" + ip + "Exception:" + str(e))
        raise e


def get_terminal_quotes_list_by_instruments_and_dates(instrument_ids, trade_dates, terminal_host, headers):
    headers['Content-Type'] = 'application/json'
    params = {
        'instrumentIds': instrument_ids,
        'tradeDates': trade_dates
    }
    return call_terminal_request(terminal_host, 'data-service', 'getInstrumentQuoteCloseBatch', params, headers)


def remove_nan_and_inf(lists):
    for dicts in lists:
        invalid_keys = []
        for k, v in dicts.items():
            if is_nan(v) or v == np.inf or v == -np.inf:
                invalid_keys.append(k)
        for k in invalid_keys:
            del dicts[k]
    return lists


def get_val(nums, index1, index2=None):
    if is_nan(nums) or nums is None:
        return np.nan
    val = nums[index1] if index2 is None else nums[index1][index2]
    if isinstance(val, str):
        return np.nan if val == 'NaN' else val
    else:
        return np.float64(val)


def is_nan(num):
    return num != num


def chunks(l, n):
    """Yield successive n-sized chunks from l."""
    for i in range(0, len(l), n):
        yield l[i:i + n]
