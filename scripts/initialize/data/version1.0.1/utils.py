# -*- coding: utf-8 -*-
import redis
import logging
import requests

from datetime import datetime, timedelta


def get_redis_conn(ip):
    return redis.Redis(host=ip, port=6379, db=0)


def login(login_ip, login_body):
    login_url = 'http://' + login_ip + ':16016/auth-service/users/login'
    login_res = requests.post(login_url, json=login_body)
    token = login_res.json()['token']
    headers = {
        'Authorization': 'Bearer ' + token
    }
    return headers


def call_request(ip, service, method, params, headers):
    url = 'http://' + ip + ':16016/' + service + '/api/rpc'
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
