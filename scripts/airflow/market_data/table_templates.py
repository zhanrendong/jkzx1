# -*- coding: utf-8 -*-
from config.data_source_config import *
from market_data.market_db_utils import *
import os
os.environ['NLS_LANG'] = 'SIMPLIFIED CHINESE_CHINA.UTF8'

_datetime_fmt = '%Y%m%d'
WIND_SCHEMA = 'WBSJ'


def get_oracle_session():
    return get_session(wind_oracle_database_url)


def get_eod_price_by_date(session, table_dto, trade_date):
    data_list = session.query(table_dto).filter(table_dto.TRADE_DT == trade_date.strftime(_datetime_fmt)).all()
    if data_list is None:
        return []
    return data_list


def fetch_gold_spot_eod_price(session, cur_date):
    return get_eod_price_by_date(session, CGoldSpotEodPrices, cur_date)


def fetch_commodity_futures_eod_price(session, cur_date):
    return get_eod_price_by_date(session, CCommodityFuturesEodPrices, cur_date)


def fetch_index_futures_eod_price(session, cur_date):
    return get_eod_price_by_date(session, CIndexFuturesEodPrices, cur_date)


def fetch_bond_futures_eod_price(session, cur_date):
    return get_eod_price_by_date(session, CBondFuturesEodPrices, cur_date)


def fetch_share_eod_price(session, cur_date):
    return get_eod_price_by_date(session, AShareEodPrices, cur_date)


def fetch_index_eod_price(session, cur_date):
    return get_eod_price_by_date(session, AIndexEodPrices, cur_date)


def get_description_list(session, table_dto):
    data_list = session.query(table_dto).all()
    if data_list is None:
        return []
    return data_list


def fetch_gold_spot_description(session):
    return get_description_list(session, CGoldSpotDescription)


def fetch_futures_description(session):
    data_list = session.query(CFuturesDescription.S_INFO_WINDCODE, CFuturesDescription.S_INFO_NAME,
                              CFuturesDescription.S_INFO_EXCHMARKET, CFuturesDescription.S_INFO_DELISTDATE,
                              CFuturesContPro.S_INFO_PUNIT, CFuturesContPro.S_INFO_TUNIT, CFuturesContPro.FS_INFO_PUNIT,
                              CFuturesContPro.S_INFO_CEMULTIPLIER, CFuturesContPro.S_SUB_TYPCODE,
                              CFuturesContPro.S_INFO_RTD)\
        .join(CFuturesContPro, CFuturesContPro.S_INFO_WINDCODE == CFuturesDescription.S_INFO_WINDCODE)\
        .all()
    if data_list is None:
        return []
    return data_list


def fetch_index_description(session):
    return get_description_list(session, AIndexDescription)


def fetch_stock_description(session):
    return get_description_list(session, AShareDescription)