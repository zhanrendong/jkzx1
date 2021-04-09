import time
import random
import requests

from dags.dbo import Terminal_Session
from terminal.dto import OptionType
import datetime
import json

from dags.spiders.setting.spider_setting import SpiderSetting
from dags.spiders.utils.spider_utils import SpiderUtils
from terminal import Logging

logger = Logging.getLogger(__name__)


class YinHeSpider:

    @staticmethod
    def process_detail_data(expire_date_dict, contract_type_dict, response_detail, underlier_dict):
        """
        格式化响应数据，
        :param response_detail: 响应数据
        :param underlier_dict:  标的信息字典
        :return: 看涨看跌字典列表
        """

        data = json.loads(response_detail.text)['data']

        # 看跌
        put_list = []
        # 看涨
        call_list = []

        # 组合特定格式标的
        underlierType = underlier_dict['underlierType']
        expireDate = underlier_dict['expireDate']
        underlier = ''.join([underlierType, expireDate])

        # 例underlier -> underlier.DEC
        exc_underlier = SpiderUtils.exchanged_underlier(contract_type_dict, underlier)

        # 现价
        spot_price = data['result']['spot']

        # 标的列表数据
        underlier_list = data['result']['quotingUnits']
        for underlier in underlier_list:
            put_dict = {}
            call_dict = {}

            # 标的物
            put_dict['underlier'] = exc_underlier.upper()
            # 平台
            put_dict['company'] = 'YinHe'
            # 看涨看跌
            put_dict['option_type'] = OptionType.PUT.name
            # 欧式美式
            put_dict['product_type'] = data['result']['optionType']
            # 执行价
            put_dict['exercise_price'] = underlier['strike']
            # 看跌买价
            put_dict['ask_price'] = round(underlier['buyPut'], 2)
            # 看跌卖价
            put_dict['bid_price'] = round(underlier['sellPut'], 2)
            # 现价
            put_dict['spot_price'] = spot_price
            # 观察日
            put_dict['observe_date'] = datetime.datetime.today().date()
            # term
            put_dict['term'] = expire_date_dict[underlier_dict['expire_date']]
            # 到日期
            put_dict['expire_date'] = underlier_dict['expire_date']
            put_list.append(put_dict)

            # 标的物
            call_dict['underlier'] = exc_underlier.upper()
            # 平台
            call_dict['company'] = 'YinHe'
            # 看涨看跌
            call_dict['option_type'] = OptionType.CALL.name
            # 欧式美式
            call_dict['product_type'] = data['result']['optionType']
            # 执行价
            call_dict['exercise_price'] = underlier['strike']
            # 看涨买价
            call_dict['ask_price'] = round(underlier['buyCall'], 2)
            # 看涨卖价
            call_dict['bid_price'] = round(underlier['sellCall'], 2)
            # 现价
            call_dict['spot_price'] = spot_price
            # 观察日
            call_dict['observe_date'] = datetime.datetime.today().date()
            # term
            call_dict['term'] = expire_date_dict[underlier_dict['expire_date']]
            # 到日期
            call_dict['expire_date'] = underlier_dict['expire_date']
            call_list.append(call_dict)

        logger.info('put list: %s; call list: %s' % (put_list, call_list))
        return put_list, call_list

    @staticmethod
    def process_list_data(response_list):
        """
        格式化列表页响应数据
        :param response_list: 响应数据
        :return: 标的信息字典列表
        """
        result = json.loads(response_list.text)['data']['result']
        underlier_list = []
        for i in result:
            underlier_dict = {}
            # 到期日
            underlier_dict['expire_date'] = i['option']['expireDate']
            # 看涨看跌
            underlier_dict['option_type'] = i['option']['optionType']
            # 欧式美式
            underlier_dict['product_type'] = i['option']['productType']
            # 现价
            underlier_dict['spot_price'] = i['option']['spot'].split('.')[0]
            # 行权价
            underlier_dict['exercise_price'] = i['option']['strike'].split('.')[0]
            # 标的到期日
            underlier_dict['expireDate'] = i['option']['underlier']['expireDate']
            # 标的类型
            underlier_dict['underlierType'] = i['option']['underlier']['underlierType']

            underlier_list.append(underlier_dict)

        return underlier_list

    @staticmethod
    def yinhe_spider():
        # 实例化数据库连接对象
        db_session = Terminal_Session()

        expire_date_dict = SpiderUtils.expire_date_fun(SpiderSetting.term_list, db_session)
        # 到instrument里取instrument_type=FUTURN的数据
        contract_type_dict = SpiderUtils.get_contract_type(db_session)

        # 列表页
        for i in expire_date_dict.keys():
            # 银河列表页post请求url
            post_list_url = SpiderSetting.yinhe_list_url

            # 银河列表页post请求表单数据
            post_list_data = {'json': '{"method":"quotingATM","params":{"underlierList":null,"expireDate":"%s"}}' % i}
            time.sleep(random.randint(1, 9))

            # 发送列表页post请求
            response_list = []
            flag = 0
            while flag < 3:
                try:
                   response_list = requests.post(url=post_list_url, data=post_list_data, headers={'Connection': 'close'})
                   break
                except Exception as e:
                    flag += 1
                    if i == 3:
                        logger.info('列表页发送3次post请求失败，错误: %s' % e)
                        raise Exception('列表页发送3次post请求失败，错误: %s' % e)

            # 处理列表页数据
            underlier_list = YinHeSpider.process_list_data(response_list)

            # 详情页
            for underlier_dict in underlier_list:
                # 银河详情页post请求url
                post_detail_url = SpiderSetting.yinhe_detail_url

                # 银河详情页post请求表单数据
                post_datail_data = {
                    'json': '{"method":"tQuoting","params":{"underlier":{"underlierType":"%s","expireDate":"%s"},'
                            '"expireDate":"%s","productType":"%s"}}' % (underlier_dict['underlierType'],
                                                                        underlier_dict['expireDate'],
                                                                        underlier_dict['expire_date'],
                                                                        underlier_dict['product_type'])}
                time.sleep(random.randint(1, 9))

                # 发送详情页 post 请求
                response_detail = None
                flag1 = 0
                while flag1 < 3:
                    try:
                        response_detail = requests.post(url=post_detail_url, data=post_datail_data,
                                                        headers={'Connection': 'close'})
                        break
                    except Exception as e:
                        flag += 1
                        if i == 3:
                            logger.info('详情页发送3次post请求失败，错误: %s' % e)
                            raise Exception('详情页发送3次post请求失败，错误: %s' % e)

                # 处理详情页数据
                put_list, call_list = YinHeSpider.process_detail_data(expire_date_dict, contract_type_dict,
                                                                      response_detail, underlier_dict)

                # 保存数据
                SpiderUtils.save_data(put_list, call_list, db_session)


if __name__ == '__main__':
    YinHeSpider.yinhe_spider()
