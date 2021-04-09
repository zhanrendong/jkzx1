from tornado import web
from tornado_sqlalchemy import SessionMixin
from terminal.jsonrpc import async_dispatch as dispatch
from terminal.jsonrpc.methods import Methods
from terminal.jsonrpc.response import Response
from terminal.service import AuthSysLogSaveService
from .custom_json_encoder import CustomJSONEncoder
from terminal.conf import JWTConfig
from terminal.dto import InvalidTokenException
import jwt
from terminal.jsonrpc import create_exception_response
from terminal.utils import ServerUtils


class JsonRpcHandler(SessionMixin, web.RequestHandler):
    # 使用自定义的json_encoder
    Response.json_encoder = CustomJSONEncoder
    # 定义当前Handler需要提供的rpc api, 默认使用global_methods
    methods = None
    # 设置dispatch方法参数
    basic_logging: bool = False
    convert_camel_case: bool = False
    debug: bool = False
    trim_log_values: bool = False

    @staticmethod
    def set_dispatcher(dispatch_params):
        basic_logging, convert_camel_case, debug, trim_log_values = dispatch_params
        JsonRpcHandler.basic_logging = basic_logging
        JsonRpcHandler.convert_camel_case = convert_camel_case
        JsonRpcHandler.debug = debug
        JsonRpcHandler.trim_log_values = trim_log_values

    def prepare(self):
        pass

    async def post(self):
        auth_ret = self.verify_token()

        # 只有通过权限验证才会执行业务逻辑
        ip = None
        if auth_ret:
            http_headers = self.request.headers
            ip = http_headers.get('X-Forwarded-For')
            if ip is None or len(ip) == 0 or 'unknown' in ip:
                ip = http_headers.get("Proxy-Client-IP")
            if ip is None or len(ip) == 0 or 'unknown' in ip:
                ip = http_headers.get("WL-Proxy-Client-IP")
            if ip is None or len(ip) == 0 or 'unknown' in ip:
                ip = http_headers.get("HTTP_CLIENT_IP")
            if ip is None or len(ip) == 0 or 'unknown' in ip:
                ip = http_headers.get("X-Real-IP");
            if ip is not None and len(ip) != 0:
                ip = ip.split(',')[0]
            if ip is None or len(ip) == 0 or 'unknown' in ip:
                ip = self.request.remote_ip

            # verify_res = ServerUtils.verify_ip_token_bound(ip,
            #                                                auth_ret['username'],
            #                                                self.request.headers
            #                                                .get(JWTConfig.TOKEN_KEY)[len(JWTConfig.TOKEN_PREFIX):])
            # if verify_res is False or verify_res == 'false':
            #     auth_ret = False
        if not auth_ret:
            response = create_exception_response(InvalidTokenException('无效的登陆信息'), JsonRpcHandler.debug, code=107)
            self.write(str(response))
        else:
            request = self.request.body.decode()
            # 用户操作日期记录
            AuthSysLogSaveService.auth_sys_log_save(auth_ret, request, self.request.headers.get(
                JWTConfig.TOKEN_KEY)[len(JWTConfig.TOKEN_PREFIX):], ip)
            response = await dispatch(request, methods=self.methods, context=self,
                                      basic_logging=JsonRpcHandler.basic_logging,
                                      convert_camel_case=JsonRpcHandler.convert_camel_case,
                                      debug=JsonRpcHandler.debug,
                                      trim_log_values=JsonRpcHandler.trim_log_values)
            if response.wanted:
                self.write(str(response))
        self.set_header('Content-Type', 'application/json;charset=UTF-8')

    def on_finish(self):
        pass

    def get_current_user(self):
        pass

    def verify_token(self):
        try:
            token = self.request.headers.get(JWTConfig.TOKEN_KEY)
            return jwt.decode(token[len(JWTConfig.TOKEN_PREFIX):], JWTConfig.SIGN_SECRET, algorithms=[JWTConfig.SIGN_ALGO])
        except Exception as e:
            return False

    @staticmethod
    def create_methods():
        # 创建自定义的方法
        return Methods()

