from datetime import datetime, timedelta
import uuid
import random

from market_data.market_db_utils import *
from config.data_source_config import *


def rate_change_calc(rate, item):
    return None if item is None else (1 + rate) * item


def get_oracle_session():
    return get_session(wind_oracle_database_url)


def generate_today_gold_spot_eod_price(today=datetime.now()):
    today_str = today.strftime('%Y%m%d')
    session = get_oracle_session()
    latest_list = get_latest_eod_price(session, CGoldSpotEodPrices, today_str)
    if latest_list is None or len(latest_list) == 0:
        return
    try:
        delete_eod_price_by_date(session, CGoldSpotEodPrices, today_str)
        for data in latest_list:
            eod_price = CGoldSpotEodPrices()
            rate = random.randint(-500, 500) / 10000
            eod_price.OBJECT_ID = str(uuid.uuid1())
            eod_price.S_INFO_WINDCODE = data.S_INFO_WINDCODE
            eod_price.TRADE_DT = today_str
            eod_price.DELAY_PAY_TYPECODE = data.DELAY_PAY_TYPECODE
            eod_price.OPDATE = today
            eod_price.OPMODE = data.OPMODE
            eod_price.S_DQ_OPEN = rate_change_calc(rate, data.S_DQ_OPEN)
            eod_price.S_DQ_HIGH = rate_change_calc(rate, data.S_DQ_HIGH)
            eod_price.S_DQ_LOW = rate_change_calc(rate, data.S_DQ_LOW)
            eod_price.S_DQ_CLOSE = rate_change_calc(rate, data.S_DQ_CLOSE)
            eod_price.S_DQ_AVGPRICE = rate_change_calc(rate, data.S_DQ_AVGPRICE)
            eod_price.S_DQ_VOLUME = rate_change_calc(rate, data.S_DQ_VOLUME)
            eod_price.S_DQ_AMOUNT = rate_change_calc(rate, data.S_DQ_AMOUNT)
            eod_price.S_DQ_OI = rate_change_calc(rate, data.S_DQ_OI)
            eod_price.DEL_AMT = rate_change_calc(rate, data.DEL_AMT)
            eod_price.S_DQ_SETTLE = rate_change_calc(rate, data.S_DQ_SETTLE)
            eod_price.S_PCT_CHG = rate * 100
            session.add(eod_price)
        session.commit()
    except Exception as e:
        print(e)
    finally:
        session.close()


def generate_today_index_eod_price(today=datetime.now()):
    today_str = today.strftime('%Y%m%d')
    session = get_oracle_session()
    latest_list = get_latest_eod_price(session, AIndexEodPrices, today_str)
    if latest_list is None or len(latest_list) == 0:
        return
    try:
        delete_eod_price_by_date(session, AIndexEodPrices, today_str)
        for data in latest_list:
            eod_price = AIndexEodPrices()
            rate = random.randint(-500, 500) / 10000
            eod_price.OBJECT_ID = str(uuid.uuid1())
            eod_price.S_INFO_WINDCODE = data.S_INFO_WINDCODE
            eod_price.TRADE_DT = today_str
            eod_price.CRNCY_CODE = data.CRNCY_CODE
            eod_price.SEC_ID = data.SEC_ID
            eod_price.OPDATE = today
            eod_price.OPMODE = data.OPMODE
            eod_price.S_DQ_PRECLOSE = data.S_DQ_CLOSE
            eod_price.S_DQ_OPEN = rate_change_calc(rate, data.S_DQ_OPEN)
            eod_price.S_DQ_HIGH = rate_change_calc(rate, data.S_DQ_HIGH)
            eod_price.S_DQ_LOW = rate_change_calc(rate, data.S_DQ_LOW)
            eod_price.S_DQ_CLOSE = rate_change_calc(rate, data.S_DQ_CLOSE)
            eod_price.S_DQ_CHANGE = rate * data.S_DQ_CLOSE if data.S_DQ_CLOSE is not None else None
            eod_price.S_DQ_PCTCHANGE = rate * 100
            eod_price.S_DQ_VOLUME = rate_change_calc(rate, data.S_DQ_VOLUME)
            eod_price.S_DQ_AMOUNT = rate_change_calc(rate, data.S_DQ_AMOUNT)
            session.add(eod_price)
        session.commit()
    except Exception as e:
        print(e)
    finally:
        session.close()


def generate_today_share_eod_price(today=datetime.now()):
    today_str = today.strftime('%Y%m%d')
    session = get_oracle_session()
    latest_list = get_latest_eod_price(session, AShareEodPrices, today_str)
    if latest_list is None or len(latest_list) == 0:
        return
    try:
        delete_eod_price_by_date(session, AShareEodPrices, today_str)
        for data in latest_list:
            eod_price = AShareEodPrices()
            rate = random.randint(-500, 500) / 10000
            eod_price.OBJECT_ID = str(uuid.uuid1())
            eod_price.S_INFO_WINDCODE = data.S_INFO_WINDCODE
            eod_price.TRADE_DT = today_str
            eod_price.CRNCY_CODE = data.CRNCY_CODE
            eod_price.S_DQ_TRADESTATUS = data.S_DQ_TRADESTATUS
            eod_price.OPDATE = today
            eod_price.OPMODE = data.OPMODE
            eod_price.S_DQ_PRECLOSE = data.S_DQ_CLOSE
            eod_price.S_DQ_OPEN = rate_change_calc(rate, data.S_DQ_OPEN)
            eod_price.S_DQ_HIGH = rate_change_calc(rate, data.S_DQ_HIGH)
            eod_price.S_DQ_LOW = rate_change_calc(rate, data.S_DQ_LOW)
            eod_price.S_DQ_CLOSE = rate_change_calc(rate, data.S_DQ_CLOSE)
            eod_price.S_DQ_CHANGE = rate * data.S_DQ_CLOSE if data.S_DQ_CLOSE is not None else None
            eod_price.S_DQ_PCTCHANGE = rate * 100
            eod_price.S_DQ_VOLUME = rate_change_calc(rate, data.S_DQ_VOLUME)
            eod_price.S_DQ_AMOUNT = rate_change_calc(rate, data.S_DQ_AMOUNT)
            eod_price.S_DQ_ADJFACTOR = (data.S_DQ_ADJFACTOR or 1) * data.S_DQ_CLOSE / eod_price.S_DQ_PRECLOSE if data.S_DQ_CLOSE is not None else 1
            eod_price.S_DQ_ADJPRECLOSE = eod_price.S_DQ_ADJFACTOR * eod_price.S_DQ_PRECLOSE if eod_price.S_DQ_PRECLOSE is not None else None
            eod_price.S_DQ_ADJOPEN = eod_price.S_DQ_ADJFACTOR * eod_price.S_DQ_OPEN if eod_price.S_DQ_OPEN is not None else None
            eod_price.S_DQ_ADJHIGH = eod_price.S_DQ_ADJFACTOR * eod_price.S_DQ_HIGH if eod_price.S_DQ_HIGH is not None else None
            eod_price.S_DQ_ADJLOW = eod_price.S_DQ_ADJFACTOR * eod_price.S_DQ_LOW if eod_price.S_DQ_LOW is not None else None
            eod_price.S_DQ_ADJCLOSE = eod_price.S_DQ_ADJFACTOR * eod_price.S_DQ_CLOSE if eod_price.S_DQ_CLOSE is not None else None
            eod_price.S_DQ_AVGPRICE = rate_change_calc(rate, data.S_DQ_AVGPRICE)
            session.add(eod_price)
        session.commit()
    except Exception as e:
        print(e)
    finally:
        session.close()


def generate_today_bond_future_eod_price(today=datetime.now()):
    today_str = today.strftime('%Y%m%d')
    session = get_oracle_session()
    latest_list = get_latest_eod_price(session, CBondFuturesEodPrices, today_str)
    if latest_list is None or len(latest_list) == 0:
        return
    try:
        delete_eod_price_by_date(session, CBondFuturesEodPrices, today_str)
        for data in latest_list:
            eod_price = CBondFuturesEodPrices()
            rate = random.randint(-500, 500) / 10000
            eod_price.OBJECT_ID = str(uuid.uuid1())
            eod_price.S_INFO_WINDCODE = data.S_INFO_WINDCODE
            eod_price.TRADE_DT = today_str
            eod_price.S_DQ_CCCODE = data.S_DQ_CCCODE
            eod_price.OPDATE = today
            eod_price.OPMODE = data.OPMODE
            eod_price.S_DQ_PRESETTLE = data.S_DQ_SETTLE or data.S_DQ_CLOSE
            eod_price.S_DQ_OPEN = rate_change_calc(rate, data.S_DQ_OPEN)
            eod_price.S_DQ_HIGH = rate_change_calc(rate, data.S_DQ_HIGH)
            eod_price.S_DQ_LOW = rate_change_calc(rate, data.S_DQ_LOW)
            eod_price.S_DQ_CLOSE = rate_change_calc(rate, data.S_DQ_CLOSE)
            eod_price.S_DQ_SETTLE = rate_change_calc(rate, data.S_DQ_SETTLE)
            eod_price.S_DQ_VOLUME = rate_change_calc(rate, data.S_DQ_VOLUME)
            eod_price.S_DQ_AMOUNT = rate_change_calc(rate, data.S_DQ_AMOUNT)
            eod_price.S_DQ_OI = rate_change_calc(rate, data.S_DQ_OI)
            eod_price.S_DQ_CHANGE = rate * data.S_DQ_CLOSE if data.S_DQ_CLOSE is not None else None
            session.add(eod_price)
        session.commit()
    except Exception as e:
        print(e)
    finally:
        session.close()


def generate_today_commodity_future_eod_price(today=datetime.now()):
    today_str = today.strftime('%Y%m%d')
    session = get_oracle_session()
    latest_list = get_latest_eod_price(session, CCommodityFuturesEodPrices, today_str)
    if latest_list is None or len(latest_list) == 0:
        return
    try:
        delete_eod_price_by_date(session, CCommodityFuturesEodPrices, today_str)
        for data in latest_list:
            eod_price = CCommodityFuturesEodPrices()
            rate = random.randint(-500, 500) / 10000
            eod_price.OBJECT_ID = str(uuid.uuid1())
            eod_price.S_INFO_WINDCODE = data.S_INFO_WINDCODE
            eod_price.TRADE_DT = today_str
            eod_price.FS_INFO_TYPE = data.FS_INFO_TYPE
            eod_price.OPDATE = today
            eod_price.OPMODE = data.OPMODE
            eod_price.S_DQ_PRESETTLE = data.S_DQ_SETTLE or data.S_DQ_CLOSE
            eod_price.S_DQ_OPEN = rate_change_calc(rate, data.S_DQ_OPEN)
            eod_price.S_DQ_HIGH = rate_change_calc(rate, data.S_DQ_HIGH)
            eod_price.S_DQ_LOW = rate_change_calc(rate, data.S_DQ_LOW)
            eod_price.S_DQ_CLOSE = rate_change_calc(rate, data.S_DQ_CLOSE)
            eod_price.S_DQ_SETTLE = rate_change_calc(rate, data.S_DQ_SETTLE)
            eod_price.S_DQ_VOLUME = rate_change_calc(rate, data.S_DQ_VOLUME)
            eod_price.S_DQ_AMOUNT = rate_change_calc(rate, data.S_DQ_AMOUNT)
            eod_price.S_DQ_OI = rate_change_calc(rate, data.S_DQ_OI)
            eod_price.S_DQ_CHANGE = rate * data.S_DQ_CLOSE if data.S_DQ_CLOSE is not None else None
            eod_price.S_DQ_OICHANGE = rate_change_calc(rate, data.S_DQ_OICHANGE)
            session.add(eod_price)
        session.commit()
    except Exception as e:
        print(e)
    finally:
        session.close()


def generate_today_index_future_eod_price(today=datetime.now()):
    today_str = today.strftime('%Y%m%d')
    session = get_oracle_session()
    latest_list = get_latest_eod_price(session, CIndexFuturesEodPrices, today_str)
    if latest_list is None or len(latest_list) == 0:
        return
    try:
        delete_eod_price_by_date(session, CIndexFuturesEodPrices, today_str)
        for data in latest_list:
            eod_price = CIndexFuturesEodPrices()
            rate = random.randint(-500, 500) / 10000
            eod_price.OBJECT_ID = str(uuid.uuid1())
            eod_price.S_INFO_WINDCODE = data.S_INFO_WINDCODE
            eod_price.TRADE_DT = today_str
            eod_price.FS_INFO_TYPE = data.FS_INFO_TYPE
            eod_price.OPDATE = today
            eod_price.OPMODE = data.OPMODE
            eod_price.S_DQ_PRESETTLE = data.S_DQ_SETTLE or data.S_DQ_CLOSE
            eod_price.S_DQ_OPEN = rate_change_calc(rate, data.S_DQ_OPEN)
            eod_price.S_DQ_HIGH = rate_change_calc(rate, data.S_DQ_HIGH)
            eod_price.S_DQ_LOW = rate_change_calc(rate, data.S_DQ_LOW)
            eod_price.S_DQ_CLOSE = rate_change_calc(rate, data.S_DQ_CLOSE)
            eod_price.S_DQ_SETTLE = rate_change_calc(rate, data.S_DQ_SETTLE)
            eod_price.S_DQ_VOLUME = rate_change_calc(rate, data.S_DQ_VOLUME)
            eod_price.S_DQ_AMOUNT = rate_change_calc(rate, data.S_DQ_AMOUNT)
            eod_price.S_DQ_OI = rate_change_calc(rate, data.S_DQ_OI)
            eod_price.S_DQ_CHANGE = rate * data.S_DQ_CLOSE if data.S_DQ_CLOSE is not None else None
            session.add(eod_price)
        session.commit()
    except Exception as e:
        print(e)
    finally:
        session.close()


if __name__ == '__main__':
    # 批量生成
    non_work_days = ['2019-08-03', '2019-08-03', '2019-08-10', '2019-08-11', '2019-08-17', '2019-08-18',
                     '2019-08-24', '2019-08-25', '2019-08-31', '2019-09-01', '2019-09-07', '2019-09-08',
                     '2019-09-14', '2019-09-15', '2019-09-21', '2019-09-22', '2019-09-28', '2019-09-29',
                     '2019-10-05', '2019-10-06', '2019-10-12', '2019-10-13', '2019-10-19', '2019-10-20',
                     '2019-09-13', '2019-10-01', '2019-10-02', '2019-10-03', '2019-10-04', '2019-10-07']

    cur_day = datetime.strptime('2019-08-15', '%Y-%m-%d')
    while cur_day < datetime.strptime('2019-10-23', '%Y-%m-%d'):
        if cur_day.strftime('%Y-%m-%d') in non_work_days:
            cur_day += timedelta(days=1)
            continue
        print('start gold')
        generate_today_gold_spot_eod_price(cur_day)
        print('start index')
        generate_today_index_eod_price(cur_day)
        print('start share')
        generate_today_share_eod_price(cur_day)
        print('start bond')
        generate_today_bond_future_eod_price(cur_day)
        print('start future')
        generate_today_commodity_future_eod_price(cur_day)
        print('start index future')
        generate_today_index_future_eod_price(cur_day)

        print('generate over')
        print(cur_day)
        cur_day += timedelta(days=1)
