# -*- coding: utf-8 -*-
import requests

uri = 'http://dev02.tongyu.tech:16011/api/rpc'
body = {
    'jsonrpc': '2.0',
    'method': '',
    'id': '1',
    'params': {}
}


def call(method, params):
    """Call an API provided by quant-service
    Usage: Provide the API name and arguments and the function will call 
        the locally running quant-service through HTTP POST.

    Args:
        method (string): The API to be called
        params (dict): The API parameters as a dictionary

    Returns:
        The result of the API call
    
    Raise:
        RuntimeError: The call will catch all exceptions and re-raise as a RuntimeError.
            The caller can examine the message returned. There are two possibilities:
            1. The error is from the requests library
            2. The error is from quant-service. This is raised when
                the status code is neither 200 nor 201.
    """
    body['method'] = method
    body['params'] = params
    try:
        res = requests.post(uri, json=body)
    except Exception as e:
        # print 'Unexpected error', sys.exc_info()[0]
        raise RuntimeError(e.message)
    if res.status_code != 200 and res.status_code != 201:
        raise RuntimeError(res.json()['error']['message'])
    return res.json()['result']
