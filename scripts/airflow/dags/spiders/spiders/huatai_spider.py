import datetime
from selenium import webdriver
from time import sleep
import re
from terminal.dto import OptionProductType, OptionType
from dags.dbo import Terminal_Session
from dags.spiders.setting.spider_setting import SpiderSetting
from dags.spiders.utils.spider_utils import SpiderUtils
from terminal import Logging

logger = Logging.getLogger(__name__)


class HuaTaiSpider:

    @staticmethod
    def load_home1(url, cookies, cookies1, browser):
        """
        通过selenium加载首页，检验是否网页加载成功
        :param browser: 浏览器对象
        :param url: 南华网址
        :return: True or False
        """

        # 进主页
        browser.get(url)
        sleep(3)
        flag = 3
        while flag:
            # 首页操作提示，网页加载不出，重复加载，指导取到值
            try:
                result = browser.find_element_by_xpath('//h1[@class="weui-header-title"]').text
                if result == '关注华泰期权服务':
                    logger.info('二维码页面加载成功')

                    # 添加cookies
                    browser.add_cookie(cookie_dict=cookies)
                    browser.add_cookie(cookie_dict=cookies1)

                    # 再次刷新页面
                    browser.refresh()
                    sleep(10)

                    flag1 = 10
                    while flag1:
                        try:
                            result = browser.find_element_by_xpath(
                                '//table[@class="table BlackTheme"]/tbody[@id="Price_'
                                'ATMOptionList"]/tr/td[@id="atm_code0"]').text
                            if result:
                                logger.info('添加cookie成功，进入了华泰数据页')
                                return True
                            else:
                                logger.info('网页改变，需要从新构建代码')
                                return False
                        except Exception as e:
                            logger.info('正在尝试第%s次页面加载: 等待延时%s秒' % (flag1, flag1))
                            # todo 尝试几次都不成功，发邮件，不再加载网页
                            if flag1 < 20:
                                browser.refresh()
                                sleep(flag1 + 5)
                                flag1 += 5

                            else:
                                logger.error('加载四次也未成功')
                                return False
                else:
                    logger.error('网页改变，需要重新构建代码')
                    return False
            except Exception as e:
                logger.info('正在尝试第%s次页面加载: 等待延时%s秒' % (flag, flag))
                # todo 尝试三次也不成功，发邮件，不再加载网页
                if flag < 9:
                    browser.refresh()
                    sleep(flag + 3)
                    flag += 3
                else:
                    logger.error('加载三次也未成功')
                    return False

    @staticmethod
    def load_home(url, browser):
        """
        通过selenium加载首页，检验是否网页加载成功

        :param browser: 浏览器对象
        :param url: 南华网址
        :return: True or False
        """

        # 进主页
        browser.get(url)
        sleep(10)

        # 再次刷新页面
        browser.refresh()
        sleep(10)

        flag1 = 10
        while flag1:
            try:
                result = browser.find_element_by_xpath(
                    '//div[@id="Price_ATMOrderList"]/table[@class="table BlackTheme"]/'
                    'thead/tr[@class="lightBlackTheme"]/td').text
                if result == '品种':
                    logger.info('进入了华泰列表页')
                    return True
                else:
                    logger.info('网页改变，需要从新构建代码')
                    raise Exception('网页改变，需要从新构建代码')
            except Exception as e:
                if e.args[0] == '网页改变，需要从新构建代码':
                    raise Exception('网页改变，需要从新构建代码')
                logger.info('正在尝试从新加载: 等待延时%s秒' % flag1)
                if flag1 < 20:
                    browser.refresh()
                    sleep(flag1 + 5)
                    flag1 += 5

                else:
                    logger.error('首页加载3次也未成功')
                    raise Exception('首页加载3次都失败')

    @staticmethod
    def chineseMonth_to_numberMonth(chinese_month):
        """
        处理汉字月和数字月
        :param chinese_month:
        :return:
        """

        a = {'一': '1', '二': '2', '三': '3', '四': '4', '五': '5', '六': '6', '七': '7', '八': '8', '九': '9', '十': '10',
             '十一': '11', '十二': '12'}

        num_month = int(a[chinese_month])

        return num_month

    @staticmethod
    def day_to_date(i, browser):
        """
        将点击的日期弹框里的可选日里获取的day转换成date
        :param browser:
        :return:
        """
        # day
        day = \
        browser.find_elements_by_xpath('//div[contains(@style, "display: block;")]/div/table/tbody/tr/td[@class="day"'
                                       ' or @class="day active"]')[i].text

        # # month
        chinese_month = browser.find_element_by_xpath(
            '//div[@class="datetimepicker-days"]/table/thead/tr/th[@class="switch"]').text.split('月')[0]
        month = str(HuaTaiSpider.chineseMonth_to_numberMonth(chinese_month))

        # year
        str_ = browser.find_element_by_xpath(
            '//div[@class="datetimepicker-days"]/table/thead/tr/th[@class="switch"]').text
        year = re.match(r'.* (\d{4})', str_).group(1)

        date = '-'.join([year, month.zfill(2), day.zfill(2)])

        return date

    @staticmethod
    def process_data(expire_date_dict, browser, expire_date, underlier, exchange, spider_record):
        """
        获取详情页数据，并格式化入库
        :param date:  到期日
        :param underlier: 标的
        :param exchange:  交易所
        :return:
        """
        # 看跌
        put_list = []
        # 看涨
        call_list = []
        try:
            # 卖价买价数据
            trs = browser.find_elements_by_xpath('//table[@class="table BlackTheme"]/tbody[@id="Price_OptionList"]/tr')
            # 现价
            spot_price = browser.find_element_by_xpath(
                '//table[@class="table BlackTheme"]/thead/tr/td/span[@id="ftprice"]').text

            flag1 = 1
            while flag1:
                ask_or_bid_price = browser.find_element_by_xpath(
                    '//tr[@class="lightBlackTheme"]/td[@id="call_bid_type"]').text
                flag1 += 1
                if ask_or_bid_price == '买价':
                    # 确保是买卖价, 跳出循环
                    flag1 = 0
                if flag1 != 0:
                    # 再次点击,回到买卖价
                    browser.find_element_by_xpath('//tr[@class="lightBlackTheme"]/td[@id="call_bid_type"]').click()
                    sleep(1)
                if flag1 >= 7:
                    logger.error('找不到买价, 页面结构改变,需要重构代码')
                    break
            if flag1 != 0:
                return [], []

            for index, tr in enumerate(trs):
                if tr.find_element_by_xpath('./td[1]').text == 'NaN' or tr.find_element_by_xpath('./td[1]').text == '':
                    logger.info('标的物: %s 的卖价和买价为空' % underlier)
                    continue
                put_dict = {}
                call_dict = {}

                # 标的物
                put_dict['underlier'] = '.'.join([underlier, exchange]).upper()
                # 平台
                put_dict['company'] = 'HuaTai'
                # 看涨看跌
                put_dict['option_type'] = OptionType.PUT.name
                # 欧式美式
                put_dict['product_type'] = OptionProductType.VANILLA_EUROPEAN.name
                # 执行价
                put_dict['exercise_price'] = tr.find_element_by_xpath('./td[3]').text
                # 看跌买价
                put_dict['bid_price'] = tr.find_element_by_xpath('./td[4]').text
                # 看跌卖价
                put_dict['ask_price'] = tr.find_element_by_xpath('./td[5]').text
                # 现价
                put_dict['spot_price'] = spot_price
                # 观察日
                put_dict['observe_date'] = datetime.datetime.today().date()
                # term
                put_dict['term'] = expire_date_dict[expire_date]
                # 到日期
                put_dict['expire_date'] = expire_date
                # 爬虫记录
                put_dict['spider_record'] = spider_record
                put_list.append(put_dict)

                # 标的物
                call_dict['underlier'] = '.'.join([underlier, exchange]).upper()
                # 平台
                call_dict['company'] = 'HuaTai'
                # 看涨看跌
                call_dict['option_type'] = OptionType.CALL.name
                # 欧式美式
                call_dict['product_type'] = OptionProductType.VANILLA_EUROPEAN.name
                # 执行价
                call_dict['exercise_price'] = tr.find_element_by_xpath('./td[3]').text
                # 看涨买价
                call_dict['bid_price'] = tr.find_element_by_xpath('./td[1]').text
                # 看涨卖价
                call_dict['ask_price'] = tr.find_element_by_xpath('./td[2]').text
                # 现价
                call_dict['spot_price'] = spot_price
                # 观察日
                call_dict['observe_date'] = datetime.datetime.today().date()
                # term
                call_dict['term'] = expire_date_dict[expire_date]
                # 到日期
                call_dict['expire_date'] = expire_date
                # 爬虫记录
                call_dict['spider_record'] = spider_record
                call_list.append(call_dict)
        except Exception as e:
            logger.error('标的: %s 获取买卖价出现错误, 错误为:%s' % (underlier, e))
            return [], []

        if len(put_list) == 0:
            return [], []

        try:
            # 点击获取vol
            browser.find_element_by_xpath('//tr[@class="lightBlackTheme"]/td[@id="call_bid_type"]').click()
            sleep(0.5)
            browser.find_element_by_xpath('//tr[@class="lightBlackTheme"]/td[@id="call_bid_type"]').click()
            sleep(4)
            # 卖买vol数据
            trs = browser.find_elements_by_xpath('//table[@class="table BlackTheme"]/tbody[@id="Price_OptionList"]/tr')

            for index, tr in enumerate(trs):
                if tr.find_element_by_xpath('./td[1]').text == 'NaN' or tr.find_element_by_xpath('./td[1]').text == '':
                    logger.info('标的物: %s 的卖vol和买vol为空' % underlier)
                    continue
                # 看跌买价
                put_list[index]['bid_vol'] = tr.find_element_by_xpath('./td[4]').text
                # 看跌卖价
                put_list[index]['ask_vol'] = tr.find_element_by_xpath('./td[5]').text

                # 看跌买价
                call_list[index]['bid_vol'] = tr.find_element_by_xpath('./td[1]').text
                # 看跌卖价
                call_list[index]['ask_vol'] = tr.find_element_by_xpath('./td[2]').text
        except Exception as e:
            logger.error('标的: %s 获取买卖vol出现错误, 错误为:%s' % (underlier, e))
            flag = 1
            while flag:
                # 再次点击,回到买卖价
                browser.find_element_by_xpath('//tr[@class="lightBlackTheme"]/td[@id="call_bid_type"]').click()
                sleep(1)
                ask_or_bid_price = browser.find_element_by_xpath(
                    '//tr[@class="lightBlackTheme"]/td[@id="call_bid_type"]').text
                flag += 1
                if ask_or_bid_price == '买价':
                    # 确保是买卖价, 跳出循环
                    break
                if flag1 >= 7:
                    logger.error('找不到买价, 页面结构改变,需要重构代码')
            return [], []

        # 再次点击,回到买卖价
        browser.find_element_by_xpath('//tr[@class="lightBlackTheme"]/td[@id="call_bid_type"]').click()
        sleep(0.5)

        return put_list, call_list

    @staticmethod
    def get_date(db_session, browser, expire_date_dict, spider_record):
        """
        获取网页详情页买卖价数据
        :param browser:
        :param expire_date_dict:
        :return:
        """
        # 点击列表页标的，进入详情页
        browser.find_element_by_xpath(
            '//table[@class="table BlackTheme"]/tbody[@id="Price_ATMOptionList"]/tr/td[@id="atm_code0"]').click()
        sleep(10)

        # 交易所选择框
        exchange_options = browser.find_elements_by_xpath('//select[@id="exchange_name"]/option')
        # 进入详情页，点击交易所
        for exchange_option in exchange_options:
            try:
                # 点击交易所选择框
                exchange_option.click()
                sleep(10)
                # 交易所
                exchange = exchange_option.text
                # 挂钩标的
                contract_code_options = browser.find_elements_by_xpath('//select[@id="contract_code"]/optgroup/option')
                for contract_code_option in contract_code_options:
                    try:
                        # 点击标的选择框
                        contract_code_option.click()
                        sleep(5)
                        # 标的(标的物有空的)
                        underlier = contract_code_option.text

                        if underlier is None or underlier == '':
                            logger.info('挂钩标的为空')
                            continue
                        # 点击到期日选择框，弹出可选日
                        browser.find_element_by_xpath('//input[@id="expirydate"]').click()
                        sleep(0.5)

                        flag = 1
                        while flag:
                            prev_month = browser.find_element_by_xpath(
                                '//div[@class="datetimepicker-days"]/table/thead/tr/th[@class="prev"]').get_attribute(
                                'style')
                            if prev_month == 'visibility: hidden;':
                                logger.info('上月没有可选日了')
                                flag = 0
                            else:
                                # 点击上月份，直到没有上一月份图标
                                browser.find_element_by_xpath(
                                    '//div[@class="datetimepicker-days"]/table/thead/tr/th[@class="prev"]').click()
                                sleep(0.5)

                                # 如果点击上个月，显示月份等于当前月份跳出循环
                                chinese_month = browser.find_element_by_xpath(
                                    '//div[@class="datetimepicker-days"]/table/thead/tr/th[@class="switch"]').text[0]

                                num_month = HuaTaiSpider.chineseMonth_to_numberMonth(chinese_month)
                                if num_month == datetime.datetime.today().date().month:
                                    flag = 0
                        flag1 = 1
                        while flag1:
                            # 获取可选日，日期弹框显示
                            tds = browser.find_elements_by_xpath(
                                '//div[contains(@style, "display: block;")]/div/table/tbody/tr/'
                                'td[@class="day" or @class="day active"]')
                            for i in range(len(tds)):
                                try:
                                    date = HuaTaiSpider.day_to_date(i, browser)
                                    if date in expire_date_dict.keys():
                                        logger.info('标的：%s, 到期日：%s 的数据符合要求' % (underlier, date))
                                        try:
                                            # 网页可选的到期日在要求的expire_date_dict里面
                                            # 点击可选日，日期弹框消失
                                            browser.find_elements_by_xpath(
                                                '//div[contains(@style, "display: block;")]/div/table/tbody/tr/'
                                                'td[@class="day" or @class="day active"]')[
                                                i].click()
                                            sleep(4)
                                            # 获取详情页数据
                                            put_list, call_list = HuaTaiSpider.process_data(expire_date_dict, browser, date,
                                                                               underlier, exchange, spider_record)
                                            if len(put_list) == 0:
                                                logger.info('标的: %s数据为空' % underlier)
                                                browser.find_element_by_xpath('//input[@id="expirydate"]').click()
                                                sleep(0.5)
                                                continue
                                            # 保证每个call与put之中都存在ask_vol与bid_vol(获取到买卖价后,再次点击获取买卖vol时,有时会出错)
                                            again = False
                                            for put_dict in put_list:
                                                if put_dict.get('ask_vol') is None or put_dict.get('bid_vol') is None:
                                                    again = True
                                            for call_dict in call_list:
                                                if call_dict.get('ask_vol') is None or call_dict.get('bid_vol') is None:
                                                    again = True
                                            if again is True:
                                                # 重新获取数据
                                                put_list, call_list = HuaTaiSpider.process_data(expire_date_dict, browser, date,
                                                                                   underlier, exchange, spider_record)
                                            if len(put_list) == 0:
                                                logger.error(
                                                    '第一次获取到了买卖价, 再次点击获取买卖vol时出现了错误, 第二次本该有数据, 但是没有获取到,延时加载数据出现问题')
                                                browser.find_element_by_xpath('//input[@id="expirydate"]').click()
                                                sleep(0.5)
                                                continue
                                            again = False
                                            for put_dict in put_list:
                                                if put_dict.get('ask_vol') is None or put_dict.get('bid_vol') is None:
                                                    again = True
                                            for call_dict in call_list:
                                                if call_dict.get('ask_vol') is None or call_dict.get('bid_vol') is None:
                                                    again = True

                                            if again is True:
                                                logger.error('获取到了买卖价, 再次点击获取买卖vol时出现了错误')
                                                logger.error(
                                                    '获取到的数据为put_list: %s, call_list: %s' % (put_list, call_list))
                                                browser.find_element_by_xpath('//input[@id="expirydate"]').click()
                                                sleep(0.5)
                                                continue

                                            # 保存数据
                                            SpiderUtils.save_data(put_list, call_list, db_session)
                                            logger.info('标的: %s, 到期日: %s的数据保存成功' % (underlier, date))
                                        except Exception as e:
                                            logger.error('标的：%s, 到期日：%s获取数据错误, 错误为: %s' % (underlier, date, e))
                                        # 点击到期日选择框，使日期弹框弹出
                                        browser.find_element_by_xpath('//input[@id="expirydate"]').click()
                                        sleep(0.5)
                                except Exception as e:
                                    logger.error('运行错误, 错误为: %s' % e)

                            # 先判断日期弹框有没有下个月按钮
                            next_month = browser.find_element_by_xpath(
                                '//div[@class="datetimepicker-days"]/table/thead/tr/th[@class="next"]').get_attribute(
                                'style')
                            if next_month == 'visibility: hidden;':
                                # 说明下个月没有可选的日期,将日期弹框隐藏，跳出循环
                                browser.find_element_by_xpath(
                                    '//div[contains(@style, "display: block;")]/div/table/tbody/tr/td'
                                    '[@class="day" or @class="day active"]').click()
                                sleep(0.5)
                                flag1 = 0
                            else:
                                # 如果还有下个月，点击
                                browser.find_element_by_xpath(
                                    '//div[@class="datetimepicker-days"]/table/thead/tr/th[@class="next"]').click()
                                sleep(0.5)
                    except Exception as e:
                        logger.error('获取标的的时候出现错误,错误为: %s' % e)
            except Exception as e:
                logger.error('获取交易所的时候出现错误,错误为: %s' % e)

    @staticmethod
    def huatai_spider():
        # 实例化数据库连接对象
        db_session = Terminal_Session()

        try:
            # 记录爬虫启动
            spider_record = datetime.datetime.now().hour
            # 获取到期日
            expire_date_dict = SpiderUtils.expire_date_fun(SpiderSetting.term_list, db_session)

            # 运行平台设置
            # driverOptions, chromedriver_path = SpiderUtils.platform_setting(
            #     SpiderSetting.windows_chromedriver_path, webdriver, has_head=True, platform='windows')
            driverOptions, chromedriver_path = SpiderUtils.platform_setting(
                SpiderSetting.linux_chromedriver_path, webdriver, has_head=False, platform='linux')

            # 实力化 webdriver 对象
            browser = webdriver.Chrome(chrome_options=driverOptions, executable_path=chromedriver_path)

            # 华泰 url
            url = SpiderSetting.huatai_url

            # 网页改版，不需要cookies
            # cookies1 = huatai_cookies1
            # cookies = huatai_cookies
            # result = load_home1(url, cookies, cookies1, browser)

            # 检验是否首页加载成功
            HuaTaiSpider.load_home(url, browser)

            # 获取数据
            HuaTaiSpider.get_date(db_session, browser, expire_date_dict, spider_record)

            # 关闭浏览器
            browser.quit()
        except Exception as e:
            db_session.close()
            raise Exception(e)


if __name__ == '__main__':
    HuaTaiSpider.huatai_spider()
