# -*- coding: utf-8 -*-
import json
import logging
import requests

from datetime import datetime, timedelta
from dags.conf import TerminalServerConfig
from dags.conf.settings import BCTServerConfig


def login(login_ip, login_port, login_body):
    login_url = 'http://' + login_ip + ':' + str(login_port) + '/oauth/token'
    login_res = requests.post(login_url, data=login_body)
    print(login_res.text)
    token = login_res.json()['access_token']
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
    }
    return headers


def call_request(ip, port, service, method, params, headers):
    if service is not None:
        url = 'http://' + ip + ':' + str(port) + '/' + service + '/api/rpc'
    else:
        url = 'http://' + ip + ':' + str(port) + '/api/rpc'
    body = {
        'jsonrpc': '2.0',
        'method': method,
        'id': 1,
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


def trans_utc_datetime(time_str):
    date_str = datetime.now().strftime('%Y-%m-%d')
    datetime_str = date_str + 'T' + time_str
    datetime_time = datetime.strptime(datetime_str, '%Y-%m-%dT%H:%M:%S')
    return datetime_time - timedelta(hours=8)


def login_terminal():
    terminal_login_body = {
        'username': TerminalServerConfig.username,
        'password': TerminalServerConfig.password,
        'client_id': TerminalServerConfig.client_id,
        'grant_type': TerminalServerConfig.grant_type
    }

    return login(TerminalServerConfig.host, TerminalServerConfig.port, terminal_login_body)


def login_bct():
    login_ip = BCTServerConfig.host
    login_port = BCTServerConfig.port
    login_body = {
        'username': BCTServerConfig.username,
        'password': BCTServerConfig.password,
        'client_id': BCTServerConfig.client_id,
        'grant_type': BCTServerConfig.grant_type,
        'captcha': BCTServerConfig.special_captcha
    }
    login_headers = {"Content-Type": "application/json"}

    login_url = 'http://' + login_ip + ':' + str(login_port) + '/auth-service/users/login'
    login_res = requests.post(login_url, data=json.dumps(login_body), headers=login_headers)
    print(login_res.text)
    token = login_res.json()['result']['token']
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
    }

    return headers


def call_request_from_bct(ip, port, service, method, params):
    # 登陆获取headers
    headers = login_bct()
    if service is not None:
        url = 'http://' + ip + ':' + str(port) + '/' + service + '/api/rpc'
    else:
        url = 'http://' + ip + ':' + str(port) + '/api/rpc'
    body = {
        'jsonrpc': '2.0',
        'method': method,
        'id': 1,
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


def call_terminal_request(service, method, params, headers):
    return call_request(TerminalServerConfig.host, TerminalServerConfig.port, service, method, params, headers)


def get_instrument_list(header):
    try:
        instrument_list = call_terminal_request('terminal-service', 'mktInstrumentsListPaged', {}, header)['result']['page']
    except Exception as e:
        print("failed update market data for: " + TerminalServerConfig.host + ",Exception:" + str(e))
        instrument_list = None
    return instrument_list

