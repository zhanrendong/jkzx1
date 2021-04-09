from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy import Column, String, Float, Date, BIGINT
from sqlalchemy import create_engine
import os
from config.data_source_config import SCHEMA_WIND_MARKET

os.environ['NLS_LANG'] = 'SIMPLIFIED CHINESE_CHINA.UTF8'

BaseOracle = declarative_base()


def get_session(oracle_url):
    engine = create_engine(oracle_url)
    session = sessionmaker(bind=engine)
    return session()


class AIndexEodPrices(BaseOracle):
    __tablename__ = 'AINDEXEODPRICES'
    __table_args__ = {'schema': SCHEMA_WIND_MARKET}
    OBJECT_ID = Column(String, primary_key=True)
    S_INFO_WINDCODE = Column(String)
    TRADE_DT = Column(String)
    CRNCY_CODE = Column(String)
    S_DQ_PRECLOSE = Column(Float)
    S_DQ_OPEN = Column(Float)
    S_DQ_HIGH = Column(Float)
    S_DQ_LOW = Column(Float)
    S_DQ_CLOSE = Column(Float)
    S_DQ_CHANGE = Column(Float)
    S_DQ_PCTCHANGE = Column(Float)
    S_DQ_VOLUME = Column(Float)
    S_DQ_AMOUNT = Column(Float)
    SEC_ID = Column(String)
    OPDATE = Column(Date)
    OPMODE = Column(String)


class AShareEodPrices(BaseOracle):
    __tablename__ = 'ASHAREEODPRICES'
    __table_args__ = {'schema': SCHEMA_WIND_MARKET}
    OBJECT_ID = Column(String, primary_key=True)
    S_INFO_WINDCODE = Column(String)
    TRADE_DT = Column(String)
    CRNCY_CODE = Column(String)
    S_DQ_PRECLOSE = Column(Float)
    S_DQ_OPEN = Column(Float)
    S_DQ_HIGH = Column(Float)
    S_DQ_LOW = Column(Float)
    S_DQ_CLOSE = Column(Float)
    S_DQ_CHANGE = Column(Float)
    S_DQ_PCTCHANGE = Column(Float)
    S_DQ_VOLUME = Column(Float)
    S_DQ_AMOUNT = Column(Float)
    S_DQ_ADJPRECLOSE = Column(Float)
    S_DQ_ADJOPEN = Column(Float)
    S_DQ_ADJHIGH = Column(Float)
    S_DQ_ADJLOW = Column(Float)
    S_DQ_ADJCLOSE = Column(Float)
    S_DQ_ADJFACTOR = Column(Float)
    S_DQ_AVGPRICE = Column(Float)
    S_DQ_TRADESTATUS = Column(String)
    OPDATE = Column(Date)
    OPMODE = Column(String)


class CBondFuturesEodPrices(BaseOracle):
    __tablename__ = 'CBONDFUTURESEODPRICES'
    __table_args__ = {'schema': SCHEMA_WIND_MARKET}
    OBJECT_ID = Column(String, primary_key=True)
    S_INFO_WINDCODE = Column(String)
    TRADE_DT = Column(String)
    S_DQ_PRESETTLE = Column(Float)
    S_DQ_OPEN = Column(Float)
    S_DQ_HIGH = Column(Float)
    S_DQ_LOW = Column(Float)
    S_DQ_CLOSE = Column(Float)
    S_DQ_SETTLE = Column(Float)
    S_DQ_VOLUME = Column(Float)
    S_DQ_AMOUNT = Column(Float)
    S_DQ_OI = Column(Float)
    S_DQ_CHANGE = Column(Float)
    S_DQ_CCCODE = Column(String)
    OPDATE = Column(Date)
    OPMODE = Column(String)


class CCommodityFuturesEodPrices(BaseOracle):
    __tablename__ = 'CCOMMODITYFUTURESEODPRICES'
    __table_args__ = {'schema': SCHEMA_WIND_MARKET}
    OBJECT_ID = Column(String, primary_key=True)
    S_INFO_WINDCODE = Column(String)
    TRADE_DT = Column(String)
    S_DQ_PRESETTLE = Column(Float)
    S_DQ_OPEN = Column(Float)
    S_DQ_HIGH = Column(Float)
    S_DQ_LOW = Column(Float)
    S_DQ_CLOSE = Column(Float)
    S_DQ_SETTLE = Column(Float)
    S_DQ_VOLUME = Column(Float)
    S_DQ_AMOUNT = Column(Float)
    S_DQ_OI = Column(Float)
    S_DQ_CHANGE = Column(Float)
    S_DQ_OICHANGE = Column(Float)
    FS_INFO_TYPE = Column(String)
    OPDATE = Column(Date)
    OPMODE = Column(String)


class CGoldSpotEodPrices(BaseOracle):
    __tablename__ = 'CGOLDSPOTEODPRICES'
    __table_args__ = {'schema': SCHEMA_WIND_MARKET}
    OBJECT_ID = Column(String, primary_key=True)
    S_INFO_WINDCODE = Column(String)
    TRADE_DT = Column(String)
    S_DQ_OPEN = Column(Float)
    S_DQ_HIGH = Column(Float)
    S_DQ_LOW = Column(Float)
    S_DQ_CLOSE = Column(Float)
    S_DQ_AVGPRICE = Column(Float)
    S_DQ_VOLUME = Column(Float)
    S_DQ_AMOUNT = Column(Float)
    S_DQ_OI = Column(Float)
    DEL_AMT = Column(Float)
    S_DQ_SETTLE = Column(Float)
    DELAY_PAY_TYPECODE = Column(BIGINT)
    S_PCT_CHG = Column(Float)
    OPDATE = Column(Date)
    OPMODE = Column(String)


class CIndexFuturesEodPrices(BaseOracle):
    __tablename__ = 'CINDEXFUTURESEODPRICES'
    __table_args__ = {'schema': SCHEMA_WIND_MARKET}
    OBJECT_ID = Column(String, primary_key=True)
    S_INFO_WINDCODE = Column(String)
    TRADE_DT = Column(String)
    S_DQ_PRESETTLE = Column(Float)
    S_DQ_OPEN = Column(Float)
    S_DQ_HIGH = Column(Float)
    S_DQ_LOW = Column(Float)
    S_DQ_CLOSE = Column(Float)
    S_DQ_SETTLE = Column(Float)
    S_DQ_VOLUME = Column(Float)
    S_DQ_AMOUNT = Column(Float)
    S_DQ_OI = Column(Float)
    S_DQ_CHANGE = Column(Float)
    FS_INFO_TYPE = Column(String)
    OPDATE = Column(Date)
    OPMODE = Column(String)


def get_latest_eod_price(session, table_dto, cur_date):
    latest_date = session.query(table_dto.TRADE_DT).filter(table_dto.TRADE_DT < cur_date)\
        .order_by(table_dto.TRADE_DT.desc()).first()
    if latest_date is None:
        return []
    data_list = session.query(table_dto).filter(table_dto.TRADE_DT == latest_date[0]).all()
    if data_list is None or len(data_list) == 0:
        return []
    return data_list


def delete_eod_price_by_date(session, table_dto, cur_date):
    session.query(table_dto).filter(table_dto.TRADE_DT == cur_date).delete()
    session.commit()


class AIndexDescription(BaseOracle):
    __tablename__ = 'AINDEXDESCRIPTION'
    __table_args__ = {'schema': SCHEMA_WIND_MARKET}
    OBJECT_ID = Column(String, primary_key=True)
    S_INFO_WINDCODE = Column(String)
    S_INFO_NAME = Column(String)
    S_INFO_EXCHMARKET = Column(String)


class AShareDescription(BaseOracle):
    __tablename__ = 'ASHAREDESCRIPTION'
    __table_args__ = {'schema': SCHEMA_WIND_MARKET}
    OBJECT_ID = Column(String, primary_key=True)
    S_INFO_WINDCODE = Column(String)
    S_INFO_NAME = Column(String)
    S_INFO_EXCHMARKET = Column(String)


class CGoldSpotDescription(BaseOracle):
    __tablename__ = 'CGOLDSPOTDESCRIPTION'
    __table_args__ = {'schema': SCHEMA_WIND_MARKET}
    OBJECT_ID = Column(String, primary_key=True)
    S_INFO_WINDCODE = Column(String)
    S_INFO_CODE = Column(String)
    S_INFO_NAME = Column(String)
    S_INFO_EXCHMARKET = Column(String)
    S_INFO_PUNIT = Column(String)


class CFuturesDescription(BaseOracle):
    __tablename__ = 'CFUTURESDESCRIPTION'
    __table_args__ = {'schema': SCHEMA_WIND_MARKET}
    OBJECT_ID = Column(String, primary_key=True)
    S_INFO_WINDCODE = Column(String)
    S_INFO_CODE = Column(String)
    S_INFO_NAME = Column(String)
    S_INFO_EXCHMARKET = Column(String)
    S_INFO_DELISTDATE = Column(String)


class CFuturesContPro(BaseOracle):
    __tablename__ = 'CFUTURESCONTPRO'
    __table_args__ = {'schema': SCHEMA_WIND_MARKET}
    OBJECT_ID = Column(String, primary_key=True)
    S_INFO_WINDCODE = Column(String)
    S_INFO_PUNIT = Column(Float)
    S_INFO_TUNIT = Column(String)
    FS_INFO_PUNIT = Column(String)
    S_INFO_CEMULTIPLIER = Column(Float)
    S_SUB_TYPCODE = Column(BIGINT)
    S_INFO_RTD = Column(Float)
    # S_INFO_TYPE_CODE = Column(Float) # 万得新增字段


def get_wind_code_and_exchange_relation(session):
    instrument_exchange_dict = {}
    spot_list = session.query(CGoldSpotDescription).filter(CGoldSpotDescription.S_INFO_EXCHMARKET != 'SCE').all()
    if spot_list is not None and len(spot_list) > 0:
        for item in spot_list:
            if item.S_INFO_CODE is not None:
                instrument_exchange_dict[item.S_INFO_CODE.upper()] = item.S_INFO_EXCHMARKET.upper()
    futures_list = session.query(CFuturesDescription).all()
    if futures_list is not None and len(futures_list) > 0:
        for item in futures_list:
            if item.S_INFO_CODE is not None:
                instrument_exchange_dict[item.S_INFO_CODE.upper()] = item.S_INFO_EXCHMARKET.upper()
    return instrument_exchange_dict
