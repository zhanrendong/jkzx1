from airflow.models import Variable
from datetime import datetime, timedelta
from utils.utils import get_trading_date_by_offset, login
from config.bct_config import bct_host, bct_login_body
from utils.utils import get_tn_trade_date

date_fmt = '%Y-%m-%d'
year_fmt = '%Y'
AIRFLOW_TASK_EOD_VALUATION_DATE = 'AIRFLOW_TASK_EOD_VALUATION_DATE'
HIVE_MARKET_REPORT_IMPORT_DAYS = 'HIVE_MARKET_REPORT_IMPORT_DAYS'
MANUAL_TRIGGER_START_DATE = 'MANUAL_TRIGGER_START_DATE'
MANUAL_TRIGGER_END_DATE = 'MANUAL_TRIGGER_END_DATE'
FAIR_VOL_MARKING_PROCESS_NUM = 'FAIR_VOL_MARKING_PROCESS_NUM'
CALENDAR_IMPORT_YEAR = 'CALENDAR_IMPORT_YEAR'


def set_airflow_variable(key, value):
    try:
        status = Variable.set(key, value)
        return status
    except Exception:
        return None


def get_airflow_variable(key, default_val=None):
    try:
        status = Variable.get(key, default_val)
        return status
    except Exception:
        return default_val


def get_valuation_date():
    valuation_date = None
    try:
        valuation_date = get_airflow_variable(AIRFLOW_TASK_EOD_VALUATION_DATE)
        datetime.strptime(valuation_date, date_fmt)
    except Exception as e:
        print(e)
        valuation_date = get_t2_trade_date()
    print("当前估值日为: {}".format(valuation_date))
    return valuation_date


def set_valuation_date():
    valuation_date = get_t2_trade_date()
    set_airflow_variable(AIRFLOW_TASK_EOD_VALUATION_DATE, valuation_date)


def get_t2_trade_date():
    headers = login(bct_host, bct_login_body)
    trading_date_result = get_trading_date_by_offset(-2, bct_host, headers)
    if 'error' in trading_date_result:
        raise RuntimeError('T-2交易日获取失败：{}'.format(str(trading_date_result)))
    trading_date = trading_date_result.get('result')
    datetime.strptime(trading_date, date_fmt)
    print(trading_date)
    return trading_date


def init_hive_market_report_import_days():
    set_airflow_variable(HIVE_MARKET_REPORT_IMPORT_DAYS, 7)


def init_manual_trigger_date():
    eod_date = datetime.now()
    start_date = eod_date - timedelta(days=7)
    set_airflow_variable(MANUAL_TRIGGER_START_DATE, start_date.strftime(date_fmt))
    set_airflow_variable(MANUAL_TRIGGER_END_DATE, eod_date.strftime(date_fmt))


def get_hive_market_report_start_date():
    days = int(get_airflow_variable(HIVE_MARKET_REPORT_IMPORT_DAYS))
    end_date = get_valuation_date()
    start_date = get_tn_trade_date(end_date, days * -1, bct_host, bct_login_body)
    return start_date


def get_manual_trigger_start_date():
    return get_airflow_variable(MANUAL_TRIGGER_START_DATE)


def get_manual_trigger_end_date():
    return get_airflow_variable(MANUAL_TRIGGER_END_DATE)


def init_fair_vol_marking_process_num():
    set_airflow_variable(FAIR_VOL_MARKING_PROCESS_NUM, 4)


def set_calendar_import_year():
    current_year = datetime.now().strftime(year_fmt)
    set_airflow_variable(CALENDAR_IMPORT_YEAR, current_year)


def get_calendar_import_year():
    default_current_year = datetime.now().strftime(year_fmt)
    return get_airflow_variable(CALENDAR_IMPORT_YEAR, default_current_year)
