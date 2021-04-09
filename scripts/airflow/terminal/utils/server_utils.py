# -*- coding: utf-8 -*-
import logging
import requests
from terminal.utils import Logging

logging = Logging.getLogger(__name__)


class ServerUtils:
    host = None
    username = None
    password = None
    special_captcha = None

    @staticmethod
    def set_server_params(params):
        host, username, password, special_captcha = params
        if host is not None:
            ServerUtils.host = host
        if username is not None:
            ServerUtils.username = username
        if password is not None:
            ServerUtils.password = password
        if special_captcha is not None:
            ServerUtils.special_captcha = special_captcha

    @staticmethod
    def login_bct(ip=None, user_name=None, password=None):
        login_body = {
            'userName': ServerUtils.username if user_name is None else user_name,
            'password': ServerUtils.password if password is None else password,
            'captcha': ServerUtils.special_captcha
        }
        ip = ServerUtils.host if ip is None else ip
        login_url = 'http://' + ip + '/auth-service/users/login'
        login_res = requests.post(login_url, json=login_body)
        token = login_res.json()['token']
        headers = {
            'Authorization': 'Bearer ' + token
        }
        return headers

    @staticmethod
    def call_request_bct(service, method, params, headers, ip=None):
        ip = ServerUtils.host if ip is None else ip
        url = 'http://' + ip + '/' + service + '/api/rpc'
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

    @staticmethod
    def verify_ip_token_bound(ip_addr=None, user_name=None, token=None):
        login_body = {
            'ip': ip_addr,
            'username': user_name,
            'token': token
        }
        ip = ServerUtils.host
        login_url = 'http://' + ip + '/auth-service/users/verify-ip-token'
        return requests.post(login_url, json=login_body).json()
