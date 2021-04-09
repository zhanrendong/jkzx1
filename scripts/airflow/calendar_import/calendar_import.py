from trade_import.db_utils import get_session, STradeDate
from utils.utils import login, call_request
from config.bct_config import bct_host, bct_login_body
from datetime import datetime

TRADE_CALENDAR_ID = "DEFAULT_CALENDAR"
VOL_CALENDAR_ID = "DEFAULT_VOL_CALENDAR"
VOL_NON_TRADING_WEIGHT = '0'
year_fmt = '%Y'


def get_trading_calendar(calendar_id, headers):
    return call_request(bct_host, 'reference-data-service', 'refTradingCalendarGet', {'calendarId': calendar_id}, headers)


def get_vol_calendar(calendar_id, headers):
    return call_request(bct_host, 'reference-data-service', 'refVolCalendarGet', {'calendarId': calendar_id}, headers)


def delete_trading_calendar_holiday(calendar_id, holidays, headers):
    return call_request(bct_host, 'reference-data-service', 'refTradingHolidaysDelete',
                        {'calendarId': calendar_id,
                         'holidays': [{'holiday': i} for i in holidays]}, headers)


def delete_vol_calendar_holiday(holiday_uuid_list, headers):
    return call_request(bct_host, 'reference-data-service', 'refVolSpecialDatesDelete',
                        {'specialDateUUIDs': holiday_uuid_list}, headers)


def add_trading_holidays(calendar_id, holidays, headers):
    params = {
        'calendarId': calendar_id,
        'holidays': [{'holiday': i} for i in holidays]
    }
    return call_request(bct_host, 'reference-data-service', 'refTradingHolidaysAdd', params, headers)


def add_vol_holidays(calendar_id, holidays, weight, headers):
    params = {
        'calendarId': calendar_id,
        'specialDates': [{'specialDate': i, 'weight': weight, 'note': ""} for i in holidays]
    }
    return call_request(bct_host, 'reference-data-service', 'refVolSpecialDatesAdd', params, headers)


def calendar_import(current_year=datetime.now().strftime(year_fmt)):
    hive_session = None
    try:
        exist_trading_holidays = []
        exist_vol_holidays_uuid = []
        start_date = current_year + '-01-01'
        end_date = current_year + '-12-31'
        headers = login(bct_host, bct_login_body)
        trading_calendar = get_trading_calendar(TRADE_CALENDAR_ID, headers)
        trading_holidays = trading_calendar.get('result').get('holidays')
        if trading_holidays is not None and len(trading_holidays) > 0:
            exist_trading_holidays = list(filter(lambda i: i >= start_date and i <= end_date,
                                                 list(map(lambda x: x.get('holiday'), trading_holidays))))
        vol_calendar = get_vol_calendar(VOL_CALENDAR_ID, headers)
        vol_holidays = vol_calendar.get('result').get('specialDates')
        if vol_holidays is not None and len(vol_holidays) > 0:
            exist_vol_holidays_uuid = list(map(lambda i: i.get('uuid'),
                                               list(filter(lambda x: x.get('specialDate') >= start_date and x.get('specialDate') <= end_date, vol_holidays))))

        hive_session = get_session()
        date_list = hive_session.query(STradeDate.TRADEDATE)\
            .filter(STradeDate.FLAG == 'N',
                    STradeDate.TRADEDATE >= start_date,
                    STradeDate.TRADEDATE <= end_date)\
            .all()
        non_trading_days = list(map(lambda x: x.TRADEDATE, date_list))
        if len(non_trading_days) > 0:
            del_result = delete_trading_calendar_holiday(TRADE_CALENDAR_ID, exist_trading_holidays, headers)
            if 'error' in del_result:
                raise RuntimeError('当前{}年交易日历删除失败：{}'.format(current_year, str(del_result)))
            result = add_trading_holidays(TRADE_CALENDAR_ID, non_trading_days, headers)
            if 'error' in result:
                raise RuntimeError('交易日历添加失败：{}'.format(str(result)))
            del_result = delete_vol_calendar_holiday(exist_vol_holidays_uuid, headers)
            if 'error' in del_result:
                raise RuntimeError('当前{}年波动率日历删除失败：{}'.format(current_year, str(del_result)))
            result = add_vol_holidays(VOL_CALENDAR_ID, non_trading_days, VOL_NON_TRADING_WEIGHT, headers)
            if 'error' in result:
                raise RuntimeError('波动率日历添加失败：{}'.format(str(result)))
            print('当前{}年交易日历导入成功'.format(current_year))
        else:
            print('当前{}年数据源中未查询到非交易日'.format(current_year))
    finally:
        if hive_session is not None:
            hive_session.close()


if __name__ == '__main__':
    calendar_import()
