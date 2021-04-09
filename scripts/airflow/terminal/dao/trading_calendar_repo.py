from terminal.dbo import TradingCalendar, InstrumentCalendar
from terminal.dto import TradingCalendarType
import logging


class TradingCalendarRepo:
    @staticmethod
    def get_holiday_list(db_session, start_date=None, end_date=None, calendar_name=TradingCalendarType.CHINA.name):
        if start_date is None or end_date is None:
            calendars = db_session.query(TradingCalendar).filter(TradingCalendar.name == calendar_name).all()
        else:
            calendars = db_session. \
                query(TradingCalendar). \
                filter(TradingCalendar.holiday >= start_date,
                       TradingCalendar.holiday <= end_date,
                       TradingCalendar.name == calendar_name). \
                all()
        holidays = [calendar.holiday for calendar in calendars]
        if holidays is None:
            holidays = []
        return holidays

    @staticmethod
    def load_special_dates(db_session, instrument):
        # TODO: 如果是大宗商品，需要load所有的special dates
        calendars = db_session.query(InstrumentCalendar).filter(
            InstrumentCalendar.instrumentId == instrument.instrumentId).all()
        special_days = [calendar.specialDate for calendar in calendars]
        logging.info("当前的获取到标的物%s的特殊天信息为：%s" % (instrument.instrumentId,
                                                ','.join([date.strftime('%Y%m%d') for date in special_days])))
        return special_days

    @staticmethod
    def delete_trading_calendars_by_holiday(db_session, holidays):
        """
        根据holidays删除交易日历表节假日
        :param db_session:
        :param holidays:
        :return:
        """
        logging.info('开始删除交易日历节假日: %s' % holidays)
        db_session. \
            query(TradingCalendar). \
            filter(TradingCalendar.holiday.in_(holidays)). \
            delete(synchronize_session=False)
        db_session.commit()
        logging.info('删除成功')

    @staticmethod
    def save_trading_calendars(db_session, trading_calendars):
        """
        保存交易日历节假日
        :param db_session:
        :param trading_calendars:
        :return:
        """
        logging.info('开始保存节假日: %s' % [_.holiday for _ in trading_calendars])
        db_session.add_all(trading_calendars)
        db_session.commit()
        logging.info('成功保存了节假日')
