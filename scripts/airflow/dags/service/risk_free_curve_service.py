# -*- coding: utf-8 -*-
import dags.utils.server_utils as server_utils
from terminal.utils import Logging

logging = Logging.getLogger(__name__)


class RiskFreeCurveService:
    @staticmethod
    def import_to_terminal():
        header = server_utils.login_terminal()
        # TODO 需要抓取SHIBOR利率信息进行填充
        params = {
            "save": True,
            "instance": "intraday",
            "instruments": [
                {
                    "tenor": "2W",
                    "quote": 0.0238,
                    "use": True
                },
                {
                    "tenor": "1M",
                    "quote": 0.0257,
                    "use": True
                },
                {
                    "tenor": "3M",
                    "quote": 0.0292,
                    "use": True
                },
                {
                    "tenor": "6M",
                    "quote": 0.0295,
                    "use": True
                },
                {
                    "tenor": "9M",
                    "quote": 0.0311,
                    "use": True
                },
                {
                    "tenor": "1Y",
                    "quote": 0.0321,
                    "use": True
                }
            ],
            "modelName": "TRADER_RISK_FREE_CURVE"
        }
        logging.info('开始写入Risk Free Curve: %s' % params)
        ret = server_utils.call_terminal_request('terminal-service', 'mdlCurveRiskFreeCreate',
                                                 params, header)
        if 'error' in ret:
            err_msg = '处理失败, 异常：%s' % ret
            logging.error(err_msg)
            raise Exception(err_msg)
        else:
            logging.info('处理成功')


if __name__ == '__main__':
    RiskFreeCurveService.import_to_terminal()
