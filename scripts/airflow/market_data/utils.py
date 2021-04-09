# -*- coding: utf-8 -*-

import requests
import logging
from config.bct_config import special_captcha


def login_token(username, password, host):
    url = 'http://' + host + '/auth-service/users/login'
    body = {
        'username': username,
        'password': password,
        'captcha': special_captcha
    }
    res = requests.post(url, json=body)
    try:
        json = res.json()
    except Exception as e:
        print(e)
        print('\t=>' + res.text)
        print(res.headers)
        raise RuntimeError('error logging in: ')
    if 'error' in json:
        raise RuntimeError('error logging in: ' + json['error']['message'])
    return json['result']['token']


def call(method, params, service, host, token=None):
    url = 'http://' + host + '/' + ('' if service is None else (service + '/')) + 'api/rpc'
    body = {
        'method': method,
        'params': params
    }
    headers = {}
    if token is not None:
        headers = {
            'Authorization': 'Bearer ' + token
        }
    try:
        res = requests.post(url, json=body, headers=headers)
        json = res.json()
        if 'error' in json:
            logging.info("failed execute " + method + " to:" + host + ",error:" + json['error']['message'])
            raise RuntimeError('error execute in: ' + json['error']['message'])
        else:
            logging.info("success execute " + method + ",callRequest:" + str(len(params)) + " to " + host)
            return json['result']
    except Exception as e:
        print(str(res))
        print(e)
        logging.info("failed execute " + method + " to:" + host + "Exception:" + str(e))
        raise e


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
