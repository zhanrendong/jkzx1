import requests
import json
from lxml import etree
import datetime
import time

from terminal.dto import OptionType, OptionProductType
from dags.dbo import Terminal_Session
from dags.spiders.setting.spider_setting import SpiderSetting
from dags.spiders.utils.spider_utils import SpiderUtils
from terminal import Logging

logger = Logging.getLogger(__name__)


class LuZhengSpider:

    @staticmethod
    def data_processing(contract_type_dict, response_dict, expire_date, step, underlier):
        """
        处理post请求返回的格式化数据并入库
        :param response_dict:
        :param expire_date:
        :param step:
        :return:
        """

        # 标的物价格列表
        price_list = response_dict['obj']['priceList']

        # 看跌
        put_list = []
        # 看涨
        call_list = []

        # 例underlier -> underlier.DEC
        exc_underlier = SpiderUtils.exchanged_underlier(contract_type_dict, underlier)

        if exc_underlier:
            for price in price_list:
                put_dict = {}
                call_dict = {}
                # 标的物
                put_dict['underlier'] = exc_underlier
                # 平台
                put_dict['company'] = 'LuZheng'
                # 看涨看跌
                put_dict['option_type'] = OptionType.PUT.name
                # 欧式美式
                put_dict['product_type'] = OptionProductType.VANILLA_AMERICAN.name
                # 执行价
                put_dict['exercise_price'] = price['optPrice']
                # 看跌买价
                put_dict['ask_price'] = price['put1']
                # 看跌卖价
                put_dict['bid_price'] = price['put2']
                # 现价
                put_dict['spot_price'] = response_dict['obj']['futurePrice']
                # 观察日
                put_dict['observe_date'] = datetime.datetime.today().date()
                # term
                put_dict['term'] = step
                # 到日期
                put_dict['expire_date'] = expire_date
                put_list.append(put_dict)

                # 标的物
                call_dict['underlier'] = exc_underlier
                # 平台
                call_dict['company'] = 'LuZheng'
                # 看涨看跌
                call_dict['option_type'] = OptionType.CALL.name
                # 欧式美式
                call_dict['product_type'] = OptionProductType.VANILLA_AMERICAN.name
                # 执行价
                call_dict['exercise_price'] = price['optPrice']
                # 看涨买价
                call_dict['ask_price'] = price['call1']
                # 看涨卖价
                call_dict['bid_price'] = price['call2']
                # 现价
                call_dict['spot_price'] = response_dict['obj']['futurePrice']
                # 观察日
                call_dict['observe_date'] = datetime.datetime.today().date()
                # term
                call_dict['term'] = step
                # 到日期
                call_dict['expire_date'] = expire_date
                call_list.append(call_dict)

        logger.info('put list: %s; call list: %s' % (put_list, call_list))
        return put_list, call_list

    @staticmethod
    def get_fun(url):
        """
        使用requests库的get方法去请求鲁证期货网站

        :param url: 鲁证期货get请求网址
        :return: underlier_list(获取挂钩标的,是个标的列表)
        """
        i = 0
        while i < 3:
            try:
                # 发送get请求
                response = requests.get(url=url, headers={'Connection': 'close'})

                # 将相应数据转换成可进行dom操作的对象
                html_obj = etree.HTML(response.text)

                # 标的物列表
                underlier_list = html_obj.xpath('//option/text()')
                logger.info('标的物列表: %s' % underlier_list)

                return underlier_list
            except Exception as e:
                i += 1
                if i == 3:
                    logger.error('发送3次get请求失败，url: %s, 错误: %s' % (url, e))
                    raise Exception('发送3次get请求失败，url: %s, 错误: %s' % (url, e))

    @staticmethod
    def post_fun(contract_type_dict, url, underlier, expire_date, step):
        """
        使用requests库的post方法， 去请求鲁证期货网站

        :param url: 鲁证期货post请求网址
        :param underlier_list: 标的物列表
        :param expire_date_dict:
        :return:
        """

        # 请求参数 form data
        form_data = {
            'expiryDate': expire_date,
            'contractcode': underlier
        }

        response = None
        flag = 0
        while flag < 3:
            try:
                # 发送post请求
                response = requests.post(url=url, data=form_data, headers={'Connection': 'close'})
                break
            except Exception as e:
                flag += 1
                if flag == 3:
                    logger.error('发送3次post请求失败, url: %s; 错误: %s' % (url, e))
                    return [], []

        if response:
            put_list, call_list = [], []
            # 字符串转字典
            response_dict = json.loads(response.text)

            # 鲁证期货网站对于到期日expire_date查询有限制， 不在范围的到日期expire_date
            # 响应没有数据，可以做一下判断响应数据会有成功或者失败
            # 成功代表传进来的到期日expire_date没有超出查询范围
            # 失败代表传进来的到期日expire_date超出查询范围或者服务器异常
            if response_dict['success'] == False:
                failed_reasons = response_dict['msg']
                logger.info('失败原因: %s' % failed_reasons)
                return put_list, call_list
            else:
                # 处理相应数据
                put_list, call_list = LuZhengSpider.data_processing(contract_type_dict, response_dict, expire_date, step, underlier)

                return put_list, call_list

    @staticmethod
    def luzheng_spider():
        # 实例化数据库连接对象
        db_session = Terminal_Session()

        # 获取到期日
        expire_date_dict = SpiderUtils.expire_date_fun(SpiderSetting.term_list, db_session)

        # 到instrument里取instrument_type=FUTURN的数据
        contract_type_dict = SpiderUtils.get_contract_type(db_session)

        # luzheng get url
        get_url = SpiderSetting.luzheng_get_url
        # 通过向鲁证期货发送get请求获取标的 underlier 列表
        underlier_list = LuZhengSpider.get_fun(get_url)

        # 鲁证期货 post url
        post_url = SpiderSetting.luzheng_post_url

        # 通过向鲁证期货发送post请求获取标的 underlier 价格数据
        for underlier in underlier_list:
            for expire_date, step in expire_date_dict.items():
                time.sleep(5)
                # 处理数据
                put_list, call_list = LuZhengSpider.post_fun(contract_type_dict, post_url, underlier, expire_date, step)

                if len(put_list) == 0 or len(call_list) == 0:
                    continue
                # 保存数据
                SpiderUtils.save_data(put_list, call_list, db_session)

        # 关闭数据库连接
        db_session.close()


if __name__ == '__main__':
    LuZhengSpider.luzheng_spider()
