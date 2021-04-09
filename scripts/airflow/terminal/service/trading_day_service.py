from terminal.dao import TradingCalendarRepo
from terminal.utils import DateTimeUtils


class TradingDayService:
    @staticmethod
    def go_trading_day(db_session, current_date, step, direction=1):
        special_dates = []
        holidays = TradingCalendarRepo.get_holiday_list(db_session)
        return DateTimeUtils.get_trading_day(current_date, holidays, special_dates, step, direction=direction)

    @staticmethod
    def get_holiday_list(db_session, start_date=None, end_date=None):
        holidays = TradingCalendarRepo.get_holiday_list(db_session)
        # TODO: 根据开始和结束日期进行过滤
        return holidays

    @staticmethod
    def get_effective_days_num(db_session, start_date, end_date):
        holidays = TradingDayService.get_holiday_list(db_session)
        return DateTimeUtils.get_effective_days_num(start_date, end_date, holidays)

    @staticmethod
    def is_trading_day(db_session, current_date):
        special_dates = []
        holidays = TradingCalendarRepo.get_holiday_list(db_session)
        return DateTimeUtils.is_trading_day(current_date, holidays, special_dates)




