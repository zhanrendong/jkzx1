# -*- coding: utf-8 -*-
import dags.utils.server_utils as server_utils
from terminal.utils import Logging

logging = Logging.getLogger(__name__)


class DividendCurveService:
    @staticmethod
    def import_to_terminal():
        header = server_utils.login_terminal()
        instrument_ids = [instrument['instrumentId'] for instrument in server_utils.get_instrument_list(header)]
        failed_instrument_ids = []
        for instrument_id in instrument_ids:
            # TODO 需要抓取分红信息进行填充
            params = {"save": True, "instance": "intraday", "instruments": [
                {
                    "quote": 0,
                    "tenor": "1D",
                    "use": True
                }
            ], "modelName": "TRADER_DIVIDEND_CURVE", "underlyer": "510050.SH", 'underlyer': instrument_id}
            ret = server_utils.call_terminal_request('terminal-service', 'mdlCurveDividendCreate',
                                                     params, header)
            if 'error' in ret:
                logging.error('处理标的物失败：%s，异常：%s' % (instrument_id, ret))
                failed_instrument_ids.append(instrument_id)
            else:
                logging.info('处理标的物成功：%s' % instrument_id)
        if len(failed_instrument_ids) > 0:
            err_msg = '有标的物处理失败：%d of %d, 失败的标的物为：%s'(len(failed_instrument_ids),
                                                      len(instrument_ids), ','.join(failed_instrument_ids))
            logging.error(err_msg)
            raise Exception(err_msg)
        logging.info('全部标的物处理结束')


if __name__ == '__main__':
    DividendCurveService.import_to_terminal()
