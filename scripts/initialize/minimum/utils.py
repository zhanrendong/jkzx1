# -*- coding: utf-8 -*-
import requests
from gmssl.sm4 import CryptSM4, SM4_ENCRYPT
import base64
import os

ENCODING_UTF8 = 'utf-8'
KEY = 'KaYup#asD1%79iYu'
special_captcha = 'CAPTCHA@tongyu%bct78'


def bct_port():
    return os.getenv('BCT_PORT', bct_port)


def login(username, password, host):
    """ Log in to get a token for subsequent remote calls
    """
    url = 'http://' + host + ':' + bct_port() + '/auth-service/users/login'
    body = {
        'username': encrypt_data_cbc(username),
        'password': encrypt_data_cbc(password),
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
    """Call an API provided by a service
    Usage: Provide the API name and arguments and the function will call
        the remote service through HTTP POST.

    Args:
        service (string): the service providing the method, e.g. market-data-service
        method (string): the API to be called
        params (dict): the API parameters as a dictionary
        host (string): BCT server ip/domain
        token (string): the token held by the logged-in user for permission check

    Returns:
        The result of the API call

    Raise:
        RuntimeError: The call will catch all exceptions and re-raise as a RuntimeError.
            The caller can examine the message returned. There are two possibilities:
            1. The error is from the requests library
            2. The error is from the remote service. This is raised when
                the returned result contains an error field.
    """
    url = 'http://' + host + ':' + bct_port() + '/' + ('' if service is None else (service + '/')) + 'api/rpc'
    body = {
        "method": method,
        "params": params
    }
    headers = {}
    if token is not None:
        headers = {
            'Authorization': 'Bearer ' + token
        }
    res = requests.post(url, json=body, headers=headers)
    json = res.json()
    if 'error' in json:
        raise RuntimeError('error calling method {method}: {msg}'.format(method=method, msg=json['error']))
        # print('error calling method {method}: {msg}'.format(method=method, msg=json['error']['message']))
    else:
        return json['result']


def call_request(method, params, service, host, token=None):
    url = 'http://' + host + ':' + bct_port() + '/' + ('' if service is None else (service + '/')) + 'api/rpc'
    body = {
        "method": method,
        "params": params
    }
    headers = {}
    if token is not None:
        headers = {
            'Authorization': 'Bearer ' + token
        }
    res = requests.post(url, json=body, headers=headers)
    json = res.json()
    if 'error' in json:
        raise RuntimeError('error calling method {method}: {msg}'.format(method=method, msg=json['error']))
        # print('error calling method {method}: {msg}'.format(method=method, msg=json['error']['message']))
    else:
        return json


def encrypt_data_cbc(original_text):
    crypt_sm4 = CryptSM4()
    crypt_sm4.set_key(KEY.encode(encoding=ENCODING_UTF8), SM4_ENCRYPT)
    encrypt_data = crypt_sm4.crypt_ecb(original_text.encode(encoding=ENCODING_UTF8))
    return base64.b64encode(encrypt_data).decode(encoding=ENCODING_UTF8)
