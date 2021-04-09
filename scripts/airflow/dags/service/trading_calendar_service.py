import uuid
from datetime import datetime
from dags.conf.settings import BCTServerConfig
from dags.dbo import create_db_session
from dags.utils.server_utils import call_request_from_bct
from terminal.dbo import TradingCalendar
from terminal.dao import TradingCalendarRepo
from terminal.dto import TradingCalendarType
from terminal.utils import Logging, DateTimeUtils

logging = Logging.getLogger(__name__)


class TradingCalendarService:

    @staticmethod
    def filter_trading_calendar(holidays, existing_holidays):
        """
        过滤交易日历, 以请求的数据为准
        :param holidays:
        :param existing_holidays:
        :return:
        """
        extra_holidays = list(set(existing_holidays).difference(set(holidays)))
        missing_holidays = list(set(holidays).difference(set(existing_holidays)))
        extra_trading_calendars = TradingCalendarService.convert_to_trading_calendar(extra_holidays)
        missing_trading_calendars = TradingCalendarService.convert_to_trading_calendar(missing_holidays)
        return missing_trading_calendars, extra_trading_calendars

    @staticmethod
    def save_data(db_session, trading_calendars, existing_holidays):
        """
        保存数据
        :param db_session:
        :param trading_calendars:
        :param existing_holidays:
        :param force_update:
        :return:
        """
        holidays = [_.holiday for _ in trading_calendars]
        missing_trading_calendars, extra_trading_calendars = TradingCalendarService.\
            filter_trading_calendar(holidays, existing_holidays)

        if len(missing_trading_calendars) != 0:
            TradingCalendarRepo.save_trading_calendars(db_session, missing_trading_calendars)
        else:
            logging.info('获取到的交易日历与表中相同, 没有缺的交易日历假节日')
        if len(extra_trading_calendars) != 0:
            holidays = [_.holiday for _ in extra_trading_calendars]
            TradingCalendarRepo.delete_trading_calendars_by_holiday(db_session, holidays)
        else:
            logging.info('获取到的交易日历与表中相同, 没有多的的交易日历假节日')

    @staticmethod
    def convert_to_trading_calendar(holidays, calendar_name=TradingCalendarType.CHINA.name):
        """
        格式化model
        :param holidays:
        :param calendar_name:
        :return:
        """
        trading_calendars = []
        for holiday in holidays:
            item = TradingCalendar(uuid=uuid.uuid1(),
                                   name=calendar_name,
                                   holiday=holiday,
                                   updatedAt=datetime.now())
            trading_calendars.append(item)
        return trading_calendars

    @staticmethod
    def get_holidays():

        ip = BCTServerConfig.host
        port = BCTServerConfig.port
        service = 'reference-data-service'
        method = 'refTradingCalendarGet'
        params = {'calendarId': 'DEFAULT_CALENDAR'}
        holidays = []
        try:
            result = call_request_from_bct(ip, port, service, method, params)
            if result == 'error':
                logging.error('发送请求获取trading calendar失败')
            else:
                holidays = result.get('result').get('holidays')
                holidays = [DateTimeUtils.str2date(_.get('holiday')) for _ in holidays]
        except Exception as e:
            logging.error('发送请求获取trading calendar失败')
        return holidays

    @staticmethod
    def update_trading_calendar():
        """
        更新交易日历
        :return:
        """
        db_session = create_db_session
        try:
            holidays = TradingCalendarService.get_holidays()
            trading_calendars = TradingCalendarService.convert_to_trading_calendar(holidays)
            existing_holidays = TradingCalendarRepo.get_holiday_list(db_session, min(holidays), max(holidays))
            TradingCalendarService.save_data(db_session, trading_calendars, existing_holidays)
        except Exception as e:
            logging.error('运行错误: %s' % e)


if __name__ == '__main__':
    TradingCalendarService.update_trading_calendar()
