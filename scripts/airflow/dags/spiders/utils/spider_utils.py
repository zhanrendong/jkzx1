import datetime
from dags.dbo import OtcOptionQuote
from terminal.dbo import Instrument
from terminal.dto import InstrumentType
import re
import uuid
from terminal import Logging
from terminal.dao import TradingCalendarRepo
from terminal.utils import DateTimeUtils

logger = Logging.getLogger(__name__)


class SpiderUtils:

    @staticmethod
    def platform_setting(chromedriver_path, webdriver, has_head=False, platform='linux'):
        """
        运行平台配置
        :param chromedriver_path: 浏览器驱动可执行文件路径
        :param webdriver: selnium 实例
        :param has_head: 浏览器是否有头
        :param platform: 运行平台
        :return:
        """
        driverOptions = webdriver.ChromeOptions()

        if platform == 'linux':
            # linux chromedriver 路径
            linux_chromedriver_path = chromedriver_path

            if has_head == False:
                # 无头设置
                driverOptions.add_argument('--headless')
            # 使chromen能在root权限运行
            driverOptions.add_argument('--no-sandbox')
            # 防反扒
            driverOptions.add_argument("user-agent='Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko)"
                                       " Chrome/62.0.3202.94 Safari/537.36'")
            # driverOptions.add_argument('--disable-dev-shm-usage')
            # 不显示图片
            # driverOptions.add_argument('blink-settings=imagesEnabled=false')
            # gpu设置
            # driverOptions.add_argument('--disable-gpu')

            return driverOptions, linux_chromedriver_path
        elif platform == 'windows':
            # windows chromedriver 路径
            windows_chromedriver_path = chromedriver_path

            if has_head == False:
                # 无头设置
                driverOptions.add_argument('--headless')
            # 防反扒
            driverOptions.add_argument("user-agent='Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko)"
                                       " Chrome/62.0.3202.94 Safari/537.36'")

            return driverOptions, windows_chromedriver_path

    @staticmethod
    def expire_date_fun(term_list, db_session):
        """
        根据当前日期和特定步长天数列表，除去节假日和双休，计算到期交易日
        [1，3，5，10，22，44，66，123]: 特定步长天数列表
        使用business_calendar库的Calendar方法

        :param date: term_list(特定步长天数列表)
        :return: expire_date_dict
        """

        # 获取当前时间
        current_day = datetime.date.today()

        # 获取节假日
        holidays = TradingCalendarRepo.get_holiday_list(db_session)

        # # todo 获取特殊日
        # special_dates = load_special_dates(db_session, underlier)
        special_dates = []
        # 根据step获取交易日是哪天，并与步长天数组成字典
        # 将到期日与天数组成一个字典
        expire_date_dict = {}
        for step_date in term_list:
            expire_date = DateTimeUtils.get_trading_day(current_day, holidays, special_dates, step_date)
            expire_date = expire_date.strftime('%Y-%m-%d')
            expire_date_dict[expire_date] = step_date

        return expire_date_dict

    @staticmethod
    def get_contract_type(db_session):
        """
        去instrument表里取instrument_type=FUTURE的数据
        :param db_session:
        :return:
        """
        # 查询instrumentType == 'FUTURE'
        instrument_objs = db_session.query(Instrument).filter(
            Instrument.instrumentType == InstrumentType.FUTURE.name).distinct().all()

        contract_type_dict = {}
        for instrument in instrument_objs:
            pre_str = re.match(r'^([A-Za-z]+)', instrument.instrumentId).group(1)
            contract_type_dict[pre_str] = instrument.instrumentId.split('.')[1]

        logger.info('get contract_type: %s ' % contract_type_dict)

        return contract_type_dict

    @staticmethod
    def exchanged_underlier(contract_type_dict, underlier):
        """
        如
        underlier = A1902
        constract_type = A.DEC

        结果
        A1902.DEC

        :param underlier:
        :return:
        """

        # 获取underlier的前缀字母
        pre_str = re.match(r'^([A-Za-z]+)', underlier).group(1)

        # 获取constract_type后缀
        suf_str = contract_type_dict.get(pre_str)

        if suf_str:
            # 使用 . 拼接 underlier 与 suf_str
            exc_underlier = '.'.join([underlier, suf_str])
            logger.info('exchanged underlier: %s' % exc_underlier)

            return exc_underlier
        else:
            logger.error('underlier no contract type, underlier is %s' % underlier)
            return None

    @staticmethod
    def save_data(put_list, call_list, db_session):
        """
        保存数据
        :param put_list:
        :param call_list:
        :return:
        """
        # 批量写入数据库
        objects = []
        for put in put_list:
            item = OtcOptionQuote(underlier=put['underlier'],
                                  uuid=uuid.uuid1(),
                                  company=put['company'],
                                  optionType=put['option_type'],
                                  productType=put['product_type'],
                                  exercisePrice=put['exercise_price'],
                                  askPrice=put['ask_price'],
                                  bidPrice=put['bid_price'],
                                  askVol=put['ask_vol'],
                                  bidVol=put['bid_vol'],
                                  spotPrice=put['spot_price'],
                                  observeDate=put['observe_date'],
                                  term=put['term'],
                                  expireDate=put['expire_date'],
                                  spiderRecord=put['spider_record'],
                                  updatedAt=datetime.datetime.now())
            objects.append(item)
        for call in call_list:
            item = OtcOptionQuote(underlier=call['underlier'],
                                  uuid=uuid.uuid1(),
                                  company=call['company'],
                                  optionType=call['option_type'],
                                  productType=call['product_type'],
                                  exercisePrice=call['exercise_price'],
                                  askPrice=call['ask_price'],
                                  bidPrice=call['bid_price'],
                                  askVol=call['ask_vol'],
                                  bidVol=call['bid_vol'],
                                  spotPrice=call['spot_price'],
                                  observeDate=call['observe_date'],
                                  term=call['term'],
                                  expireDate=call['expire_date'],
                                  spiderRecord=call['spider_record'],
                                  updatedAt=datetime.datetime.now())
            objects.append(item)

        # 添加并提交
        db_session.add_all(objects)
        db_session.commit()
