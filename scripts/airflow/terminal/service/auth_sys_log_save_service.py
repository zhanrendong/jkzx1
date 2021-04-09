import json

from terminal.utils import ServerUtils
from terminal.dto import Constant, CustomException
from terminal.utils.auth_sys_operation_converter import auth_sys_operation_type
from terminal.utils import logging


class AuthSysLogSaveService:

    @staticmethod
    def auth_sys_log_save(auth_ret, body, token, ip):
        try:
            body = json.loads(body)
            method = body['method']
            operation = auth_sys_operation_type.get(method)
            if operation:
                params = {
                    "username": auth_ret['username'],
                    "operation": operation,
                    "service": Constant.SERVICE,
                    "method": method,
                    "params": body.get('params')
                }
                headers = {
                    'Authorization': 'Bearer ' + token,
                    'X-Forwarded-For': ip
                }
                ServerUtils.call_request_bct('auth-service', 'authSysLogSave', params, headers)
        except CustomException as e:
            logging.error('调用接口: authSysLogSave保存用户操作失败')
