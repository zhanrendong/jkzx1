from functools import wraps
from terminal.utils import Logging, ServerUtils
import traceback
import threading

logging = Logging.getLogger(__name__)


def exception_watch(func):
    @wraps(func)
    def with_catching(*args, **kwargs):
        try:
            return func(*args, **kwargs)
        except Exception as e:
            detail = traceback.format_exc()
            logging.error(detail)
            threading.Thread(target=exception_record_save, args=(func.__name__, kwargs, str(e), detail,)).start()
            raise e
    return with_catching


def exception_record_save(method_name, method_params, error_msg, error_detail):
    try:
        params = {
            'username': method_params.get('current_user'),
            'errorMessage': error_msg[:4000],
            'requestMethod': method_name,
            'requestParams': str(method_params)[:4000],
            'errorStackTrace': error_detail[:4000]
        }
        headers = ServerUtils.login_bct()
        ServerUtils.call_request_bct('auth-service', 'authCreateErrorLog', params, headers)
        logging.info('API:{} 调用错误日志保存成功'.format(method_name))
    except Exception as e:
        logging.error(traceback.format_exc())

