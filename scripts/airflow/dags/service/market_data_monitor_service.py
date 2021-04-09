import requests
import json
from terminal.utils import Logging

logging = Logging.getLogger(__name__)


class MarketDataMonitorService(object):
    @staticmethod
    def market_data_monitor(data):
        """
        请求生产环境行情数据，
        调用外部提供的api接口，
        监控接口是否正常工作，
        邮件提醒
        :return:
        """

        # 生产环境行情数请求 url
        url = 'http://101.132.147.68/terminal-data'

        # 生产环境行情数据请求 json data
        json_data = {
            "method": data[0],
            "params": [
                {
                    "assetClass": data[1],
                    "instrumentIds": [
                        data[2]
                    ]
                }
            ],
            "id": 0
        }

        try:
            # 发送post请求
            response = requests.post(url=url, data=json.dumps(json_data))

            result = json.loads(response.text)

            if result.get('result'):
                logging.error('行情接口工作正常%s' % json_data)
                # return {'12': data}
            else:
                logging.error('行情接口工作异常，%s, json_data: %s' % (result, json_data))
                return json_data
        except Exception as e:
            logging.error('行情接口工作异常，异常信息: %s, 接口信息: %s' % (e, json_data))
            return json_data

    @staticmethod
    def json_data_fun():
        """
        多个接口服务监控
        :return:
        """
        json_data_list = []

        json_data_parameter = ['getTickMarketData', 'fund', '510050.SH']
        json_data_parameter1 = ['getTickMarketData', 'future', 'P2006']

        json_data_list.append(json_data_parameter)
        json_data_list.append(json_data_parameter1)

        failed = []       # 请求接口失败列表
        for json_data in json_data_list:
            result = MarketDataMonitorService.market_data_monitor(json_data)
            if result is None:
                continue
            failed.append(result)

        if len(failed) != 0:
            raise Exception('行情接口异常')


if __name__ == '__main__':
    MarketDataMonitorService.json_data_fun()
























