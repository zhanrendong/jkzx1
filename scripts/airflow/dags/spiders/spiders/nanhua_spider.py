from selenium import webdriver
from time import sleep
import time
import datetime

from terminal.dto import OptionType, OptionProductType
from dags.dbo import Terminal_Session
from dags.spiders.setting.spider_setting import SpiderSetting
from dags.spiders.utils.spider_utils import SpiderUtils
from terminal import Logging

logger = Logging.getLogger(__name__)


class NanHuaSpider:

    @staticmethod
    def load_home(browser, url):
        """
        通过selenium加载首页，检验是否网页加载成功

        :param browser: 浏览器对象
        :param url: 南华网址
        :return: True or False
        """

        # 进主页
        browser.get(url)
        sleep(3)
        flag = 1
        while flag:
            # 首页操作提示，网页加载不出，重复加载，指导取到值
            try:
                result = browser.find_element_by_xpath('//div[@class="classification_info"]/li').text
                if result:
                    logger.info('进入首页成功')
                    break
                else:
                    logger.info('网页结构改变，需要重新构建代码')
                    raise Exception('网页结构改变，需要重新构建代码')
            except Exception as e:
                if e.args[0] == '网页改变，需要从新构建代码':
                    raise Exception('网页改变，需要从新构建代码')
                logger.info('正在尝试第%s次页面加载: 等待延时%s秒' % (flag, flag))
                if flag < 9:
                    browser.refresh()
                    sleep(flag + 3)
                    flag += 1
                else:
                    logger.error('加载三次也未成功')
                    raise Exception('加载三次也未成功')

    @staticmethod
    def format_date(date):
        """
        格式化时间，将不标准的时间格式化，
        只能处理 2019-9-6或者2019/7/9类似的时间
        :param date:
        :return:
        """
        if '-' in date:
            date_ = date.split('-')
            if len(date_[1]) == 1:
                date_[1] = '0' + date_[1]
            if len(date_[2]) == 1:
                date_[2] = '0' + date_[2]
            date = '-'.join(date_)
        elif '/' in date:
            date_ = date.split('/')
            if len(date_[1]) == 1:
                date_[1] = '0' + date_[1]
            if len(date_[2]) == 1:
                date_[2] = '0' + date_[2]
            date = '-'.join(date_)
        else:
            logger.error('网页到日期弹框获取的日期属性值格式变化，date: %s' % date)
        return date

    @staticmethod
    def process_data(browser, expire_date, underlier, exchange, expire_date_dict):
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

        # 卖价买价数据
        trs = browser.find_elements_by_xpath('//table[@id="T_table"]/tbody/tr')
        # 现价
        spot_price = trs[0].find_element_by_xpath('./th[2]').text

        for index, tr in enumerate(trs):
            if index == 0 or index == 1:
                continue
            if tr.find_element_by_xpath('./td[4]').text == '---' or tr.find_element_by_xpath('./td[5]').text == '---':
                logger.info('标的物: %s 的卖价和买价为空' % underlier)
                continue
            put_dict = {}
            call_dict = {}

            # 标的物
            put_dict['underlier'] = '.'.join([underlier, exchange])
            # 平台
            put_dict['company'] = 'NanHua'
            # 看涨看跌
            put_dict['option_type'] = OptionType.PUT.name
            # 欧式美式
            put_dict['product_type'] = OptionProductType.VANILLA_EUROPEAN.name
            # 执行价
            put_dict['exercise_price'] = tr.find_element_by_xpath('./td[3]').text
            # 看跌买价
            put_dict['ask_price'] = tr.find_element_by_xpath('./td[4]').text
            # 看跌卖价
            put_dict['bid_price'] = tr.find_element_by_xpath('./td[5]').text
            # 现价
            put_dict['spot_price'] = spot_price
            # 观察日
            put_dict['observe_date'] = datetime.datetime.today().date()
            # term
            put_dict['term'] = expire_date_dict[expire_date]
            # 到日期
            put_dict['expire_date'] = expire_date
            put_list.append(put_dict)

            # 标的物
            call_dict['underlier'] = '.'.join([underlier, exchange])
            # 平台
            call_dict['company'] = 'NanHua'
            # 看涨看跌
            call_dict['option_type'] = OptionType.CALL.name
            # 欧式美式
            call_dict['product_type'] = OptionProductType.VANILLA_EUROPEAN.name
            # 执行价
            call_dict['exercise_price'] = tr.find_element_by_xpath('./td[3]').text
            # 看涨买价
            call_dict['ask_price'] = tr.find_element_by_xpath('./td[1]').text
            # 看涨卖价
            call_dict['bid_price'] = tr.find_element_by_xpath('./td[2]').text
            # 现价
            call_dict['spot_price'] = spot_price
            # 观察日
            call_dict['observe_date'] = datetime.datetime.today().date()
            # term
            call_dict['term'] = expire_date_dict[expire_date]
            # 到日期
            call_dict['expire_date'] = expire_date
            call_list.append(call_dict)

        logger.info('put list: %s; call list: %s' % (put_list, call_list))
        return put_list, call_list

    @staticmethod
    def month_subtract(default_date):
        """
        传入日期，与当前日期做减法
        :param default_date:
        :return: 如 {'2019-09-21': 4}
        """

        default_date = datetime.datetime.strptime(default_date, '%Y-%m-%d')
        default_year = default_date.year
        default_month = default_date.month

        current_date = datetime.datetime.today().date()
        current_year = current_date.year
        current_month = current_date.month

        step_dict = {}
        month_sub = (((default_year - current_year) * 12) + default_month) - current_month
        if month_sub < 0:
            step_dict[-month_sub] = 'negative'
            logger.error('网页到期日选择框内的默认日期小于当前日期 默认期日为: %s' % default_date.date())
        else:
            step_dict[month_sub] = 'positive'

        return step_dict

    @staticmethod
    def get_data(db_session, browser, expire_date_dict):
        """
        获取标的物数据
        :return:
        """

        # 在列表页点击，进入详情页
        browser.find_element_by_xpath('//table[@id="tablevalue"]/tbody/tr/td[2]').click()
        time.sleep(2)

        # 点击交易所选择框
        province_options = browser.find_elements_by_xpath(
            '//div[@id="Tpage"]/div[@class="t_top"]/div[@class="col-xs-3"]/select[@name="province"]/option')
        for province_option in province_options:
            # 点击交易所选择框
            province_option.click()
            time.sleep(2)
            # 交易所
            exchange = province_option.text

            # 标的选择框
            city_options = browser.find_elements_by_xpath(
                '//div[@id="Tpage"]/div[@class="t_top"]/div[@class="col-xs-3"]/select[@name="city"]/option')
            for city_option in city_options:
                # 点击标的
                city_option.click()
                time.sleep(2)
                # 标的大写
                underlier = city_option.text.upper()

                # todo 点击期权类型选择框

                control_flag = 1  # 控制进入if/else语句和跳出while循环
                while control_flag:
                    if control_flag == 1:

                        # 获取到期日选择框内的默认日期
                        default_date = browser.find_element_by_xpath(
                            '//div[@id="Tpage"]/div[@class="t_top"]/div[@class="col-xs-3"]/button[@id="T_date"]').text
                        # 获取默认日期和当前日期差了几个月
                        step_dict = NanHuaSpider.month_subtract(default_date)
                        # 根据相差月份，点击到期日选择框，再点击month_sub次，将网页数据刷新到当前月份
                        # 点击到期日选择框
                        browser.find_element_by_xpath(
                            '//div[@id="Tpage"]/div[@class="t_top"]/div[@class="col-xs-3"]/button[@id="T_date"]').click()
                        time.sleep(1)
                        for i in range(list(step_dict.keys())[0]):
                            if list(step_dict.values())[0] == 'negative':
                                # 点击到期日选择框后，弹框弹出来点击下一月份按钮
                                browser.find_element_by_xpath('//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-'
                                                              'header"]/i[@class="layui-icon laydate-icon'
                                                              ' laydate-next-m"]').click()
                                time.sleep(1)
                            elif list(step_dict.values())[0] == 'positive':
                                # 点击到期日选择框后，弹框弹出来点击上一月份按钮
                                browser.find_element_by_xpath(
                                    '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-header"]/i[@class="layui-'
                                    'icon laydate-icon laydate-prev-m"]').click()
                                time.sleep(1)

                        # for循环便利后，日期弹框显示的就是当前月
                        tds = browser.find_elements_by_xpath(
                            '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/td[@class='
                            '"" or @class="layui-this"]')
                        # 判断当前月是否有可选日期
                        flag = 1
                        if len(tds) == 0:
                            # 如果没有可选日期, 跳到下一个月份
                            while flag:
                                # 点击下一月份按钮
                                browser.find_element_by_xpath(
                                    '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-header"]/i[@class="layui-'
                                    'icon laydate-icon laydate-next-m"]').click()
                                time.sleep(1)
                                # 如果点击下一月份按钮，都没有可选日期， 跳出循环
                                tds = browser.find_elements_by_xpath(
                                    '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/td'
                                    '[@class="" or @class="layui-this"]')
                                if len(tds):
                                    # 有可选的日期，跳出循环
                                    break
                                else:
                                    # 如果点击下个月按钮十次都没有可选的日期， 跳出循环
                                    flag += 1
                                    if flag > 10:
                                        flag = 0
                        if flag == 0:
                            # 点击下个月按钮十次都没有而可选的日期，再次跳出循环
                            break
                        # 不在if里的while里点击日期弹框中的可选日期，刷新网页数据，统一在这里点击，在里面点击会造成冲突
                        browser.find_element_by_xpath(
                            '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/td[@class='
                            '"" or @class="layui-this"]').click()

                        # 点击到期日选择框
                        browser.find_element_by_xpath(
                            '//div[@id="Tpage"]/div[@class="t_top"]/div[@class="col-xs-3"]/button[@id="T_date"]').click()
                        time.sleep(1)

                        # 点击到期日选择框后，弹框首次弹出来不用点击下一月份按钮，可以直接取到值
                        tds = browser.find_elements_by_xpath(
                            '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/td[@class='
                            '"" or @class="layui-this"]')

                        for i in range(len(tds)):
                            # 网页可选的日期(到期日)
                            date = browser.find_elements_by_xpath(
                                '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/td'
                                '[@class="" or @class="layui-this"]')[i].get_attribute('lay-ymd')

                            # 将网页上的不标准date格式化
                            date = NanHuaSpider.format_date(date)
                            if date in list(expire_date_dict.keys()):
                                logger.info('标的：%s, 到期日：%s 的数据符合要求' % (underlier, date))
                                # 网页可选的到期日在要求的expire_date_dict里面
                                # 点击那一天并获取数据，日期弹窗隐藏
                                browser.find_elements_by_xpath(
                                    '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/'
                                    'tr/td[@class="" or @class="layui-this"]')[i].click()
                                sleep(0.5)
                                # 在详情页取数据
                                put_list, call_list = NanHuaSpider.process_data(browser, date, underlier, exchange, expire_date_dict)

                                # 保存数据
                                SpiderUtils.save_data(put_list, call_list, db_session)

                            # for循环第一次不进入if语句日期弹框是显示的
                            # 第一次for循环进去if，日期弹框不显示，需要重新点击到期日框，因为for循环，需要再将日期弹窗点出来
                            browser.find_element_by_xpath('//div[@id="Tpage"]/div[@class="t_top"]/div[@class="col-xs-3"]'
                                                          '/button[@id="T_date"]').click()
                            time.sleep(1)
                        # 由于最后一次for循环将日期弹窗点了出来，还需要将弹窗隐藏
                        browser.find_elements_by_xpath(
                            '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/td[@class'
                            '="" or @class="layui-this"]')[0].click()
                        # 设置为2，要循环下一个月再下一个月，直到下一个月没有可选 到期日
                        control_flag = 2

                    elif control_flag == 2:

                        # 再次点击到期日选择框
                        browser.find_element_by_xpath(
                            '//div[@id="Tpage"]/div[@class="t_top"]/div[@class="col-xs-3"]/button[@id="T_date"]').click()
                        sleep(0.5)

                        # 点击到期日选择框后，弹框弹出来点击下一月份按钮
                        browser.find_element_by_xpath(
                            '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-header"]/i[@class="layui-icon'
                            ' laydate-icon laydate-next-m"]').click()
                        sleep(0.5)

                        # 前面有值的月份没问题，但是当上一步点击完以后日期弹窗没有可选的日期时，下面一句就会报错,当tds为空是，不能点击日期弹框，可以点击日期弹框，还是上个月的值
                        tds = browser.find_elements_by_xpath(
                            '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/'
                            'tr/td[@class="" or @class="layui-this"]')

                        next_flag = 1  # 控制下一月份有没有值
                        if len(tds) == 0:
                            while next_flag:
                                # 如果取到的值为空，则说明点击日期弹框后，日期弹框没有可选的到期日了,要多点击下个月按钮几次，确保没有数据
                                browser.find_element_by_xpath(
                                    '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-header"]/i[@class="layui-'
                                    'icon laydate-icon laydate-next-m"]').click()
                                sleep(0.5)
                                # 查看是否有可选日期
                                tds = browser.find_elements_by_xpath(
                                    '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/'
                                    'td[@class="" or @class="layui-this"]')
                                if len(tds):
                                    # 再次点击下一月按钮，有数据
                                    break
                                if len(tds) == 0:
                                    # 如果多次点击并且没有数据， 则跳出循环
                                    next_flag += 1
                                if next_flag > 6:
                                    next_flag = 0

                        if next_flag == 0:
                            # 多次点击没有可选的日期后，该标的物就没有数据，跳出循环
                            control_flag = 0
                            continue

                        # 再次点击是为了将 到期日 框里的值刷新,为了防止下面for循环里的if进不去，再次点击到期日框的时候，日期弹框还是上个月的值
                        browser.find_element_by_xpath(
                            '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/td[@class='
                            '"" or @class="layui-this"]').click()
                        sleep(0.5)

                        # 再次点击到期日框，显示出弹框日期里的可选的日期
                        browser.find_element_by_xpath(
                            '//div[@id="Tpage"]/div[@class="t_top"]/div[@class="col-xs-3"]/button[@id="T_date"]').click()
                        sleep(0.5)
                        tds = browser.find_elements_by_xpath(
                            '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/td[@class='
                            '"" or @class="layui-this"]')

                        # 如果tds不为空，则说明点击日期弹框后，日期弹框没有可选的到期日了
                        for i in range(len(tds)):
                            # 网页可选的日期(到期日)
                            date = browser.find_elements_by_xpath(
                                '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/td'
                                '[@class="" or @class="layui-this"]')[i].get_attribute('lay-ymd')

                            # 将网页上的不标准date格式化
                            date = NanHuaSpider.format_date(date)

                            if date in list(expire_date_dict.keys()):
                                logger.info('标的：%s, 到期日：%s 的数据符合要求' % (underlier, date))
                                # 网页可选的到期日在要求的expire_date_dict里面
                                # 点击那一天并获取数据
                                browser.find_elements_by_xpath(
                                    '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/'
                                    'td[@class="" or @class="layui-this"]')[i].click()
                                sleep(0.5)
                                # 在详情页取数据
                                put_list, call_list = NanHuaSpider.process_data(browser, date, underlier, exchange, expire_date_dict)

                                # 保存数据
                                SpiderUtils.save_data(put_list, call_list, db_session)

                            # 因为for循环，需要再将日期弹窗点出来,再上面for循环上不仅需要tds，还需要点击弹窗日期的第一个可选日期，要不然下面执行点击操作的时候，如果进不到if判断里，到期日框里的默认值还是上一次的值
                            browser.find_element_by_xpath(
                                '//div[@id="Tpage"]/div[@class="t_top"]/div[@class="col-xs-3"]/button[@id="T_date"]').click()
                            time.sleep(1)
                        # 由于最后一次for循环将日期弹窗点了出来，还需要将弹窗隐藏
                        browser.find_elements_by_xpath(
                            '//div[@id="layui-laydate3"]/div/div[@class="layui-laydate-content"]/table/tbody/tr/td'
                            '[@class="" or @class="layui-this"]')[0].click()

    @staticmethod
    def hidden_bounced(browser):
        """
        隐藏弹框
        :param result: 加载首页结果
        :return:
        """
        i = 0
        flag = True
        while flag:
            # 延时五秒
            time.sleep(5)
            # 取消弹框
            js = 'document.querySelector("#layui-m-layer1").style.display="none"'
            try:
                browser.execute_script(js)
                break
            except Exception as e:
                i += 1
                if i < 6:
                    logger.info('弹框还没加载出来，需要等待')
                else:
                    logger.error('加载等待30秒，弹框还没出现')
                    raise Exception('加载等待30秒，弹框还没出现')

    @staticmethod
    def nanhua_spider():
        # 实例化数据库连接对象
        db_session = Terminal_Session()

        # 获取到期日
        expire_date_dict = SpiderUtils.expire_date_fun(SpiderSetting.term_list, db_session)

        # 运行平台设置
        # driverOptions, chromedriver_path = SpiderUtils.platform_setting(SpiderSetting.windows_chromedriver_path,
        #                                                                 webdriver, has_head=True, platform='windows')
        driverOptions, chromedriver_path = SpiderUtils.platform_setting(SpiderSetting.linux_chromedriver_path, webdriver,
                                                                        has_head=False, platform='linux')

        # 实力化 webdriver 对象
        browser = webdriver.Chrome(chrome_options=driverOptions, executable_path=chromedriver_path)

        # 南华 url
        url = SpiderSetting.nanhua_url

        # 检验是否首页加载成功
        NanHuaSpider.load_home(browser, url)

        # 注意:::: 隐藏弹框(调试的时候，在此方法打断点，如果等弹窗自己消失，就会异常)
        NanHuaSpider.hidden_bounced(browser)

        # 获取标的underlier数据
        NanHuaSpider.get_data(db_session, browser, expire_date_dict)

        # 关闭浏览器
        browser.quit()


if __name__ == '__main__':
    NanHuaSpider.nanhua_spider()
