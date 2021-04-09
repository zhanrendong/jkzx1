# -*- coding: utf-8 -*-
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy import Column, String, Float, Date, CLOB, DateTime, Numeric
from sqlalchemy import create_engine
from config.data_source_config import hive_database_url, HIVE_TRADEDATE_SCHEMA, HIVE_TRADE_SCHEMA
import os

os.environ['NLS_LANG'] = 'SIMPLIFIED CHINESE_CHINA.UTF8'

dec_to_exe = {
    'CFFEX': 'CFE',
    'CZCE': 'CZC',
    'SHFE': 'SHF'
}


BaseModel = declarative_base()


def get_session():
    engine = create_engine(hive_database_url)
    session = sessionmaker(bind=engine)
    return session()


class ROTCPosition(BaseModel):
    __tablename__ = 'R_OTC_POSITION_CLEAN'
    __table_args__ = {
        'schema': HIVE_TRADE_SCHEMA
    }
    RECORDID = Column(Float, primary_key=True)
    REPORTID = Column(String)
    REPORTDATE = Column(String)
    COMPANYID = Column(String)
    MAINBODYCODE = Column(String)
    SUBID = Column(String)
    MAINBODYNAME = Column(String)
    CUSTOMERTYPE = Column(String)
    NOCID = Column(String)
    FUTURESID = Column(String)
    ANALOGUECODE = Column(String)
    ANALOGUENAME = Column(String)
    ANALOGUECUSTYPE = Column(String)
    ANALOGUENOCID = Column(String)
    TRANSCONFIRNUMBER = Column(String)
    TRANSCONFIRTIME = Column(String)
    POSITIONDATE = Column(String)
    ANALOGUEFUID = Column(String)
    TRANSCODE = Column(String)
    UTIID = Column(String)
    ASSETTYPE = Column(String)
    TOOLTYPE = Column(String)
    OPTEXERCTMTYPE = Column(String)
    OPTRIGHTTYPE = Column(String)
    RTVALMEPAY = Column(String)
    UNDERASSTYPE = Column(String)
    UNDERASSVARIT = Column(String)
    STANDASSCONT = Column(String)
    CONTRACTVALUE = Column(Float)
    VALMETHOD = Column(String)
    BUYPOSMONYE = Column(Float)
    SALEPOSMONYE = Column(Float)
    MONEYACCOUNT = Column(String)
    BUYPOSAMOUNT = Column(Float)
    SALEPOSAMOUNT = Column(Float)
    QUANTITYUNIT = Column(String)
    TOTPOSMONYE = Column(Float)
    NETPOSMONYE = Column(Float)
    TOPOSAMOUNT = Column(Float)
    NETPOSAMOUNT = Column(Float)
    STATUS = Column(String)
    LEGID = Column(String)
    SIDE = Column(String)
    PARTICIPATERATE = Column(Float)
    ANNUALIZED = Column(String)
    STRIKE = Column(Float)
    VALUATIONSPOT = Column(String)
    TRADENOTIONAL = Column(String)
    CLOSEDNOTIONAL = Column(String)
    PRICESYMBOL = Column(String)
    EXCHANGERATE = Column(Float)
    TRADEQAUNTITY = Column(String)
    CLOSEDQUANTITY = Column(String)
    IMPLIEDVOL = Column(String)
    EFFECTIVEDAY = Column(Float)
    DELTA = Column(String)
    GAMMA = Column(String)
    VEGA = Column(String)
    THETA = Column(String)
    RHO = Column(String)
    DELTACASH = Column(String)
    INTERESTRATE = Column(Float)
    DIVIDEND = Column(Float)
    ANNVALRATIO = Column(Float)
    EFFECTDATE = Column(String)
    EXPIREDATE = Column(String)


class ROTCTradeData(BaseModel):
    __tablename__ = 'R_OTC_TRADEDATA_CLEAN'
    __table_args__ = {
        'schema': HIVE_TRADE_SCHEMA
    }
    RECORDID = Column(Float, primary_key=True)
    REPORTID = Column(String)
    REPORTDATE = Column(String)
    COMPANYID = Column(String)
    MAINBODYCODE = Column(String)
    SUBID = Column(String)
    MAINBODYNAME = Column(String)
    NOCID = Column(String)
    CUSTOMERTYPE = Column(String)
    FUTURESID = Column(String)
    ANALOGUECODE = Column(String)
    ANALOGUENAME = Column(String)
    ANALOGUENOCID = Column(String)
    ANALOGUECUSTYPE = Column(String)
    ANALOGUEFUID = Column(String)
    MAINPROTTYPE = Column(String)
    MAINPROTDATE = Column(String)
    ISCREDIT = Column(String)
    CREDITLINE = Column(Float)
    INITMARGINREQ = Column(Float)
    MAINTAINMARGIN = Column(Float)
    OPERTYPE = Column(String)
    TRANSCODE = Column(String)
    UTIID = Column(String)
    TRANSCONFIRNUMBER = Column(String)
    TRANSCONFIRTIME = Column(String)
    EFFECTDATE = Column(String)
    EXPIREDATE = Column(String)
    EXERCISEDATE = Column(String)
    EARLYTERMDATE = Column(String)
    SUBJMATTERINFO = Column(String)
    DIRECTREPPARTY = Column(String)
    ASSETTYPE = Column(String)
    TOOLTYPE = Column(String)
    OPTEXERCTMTYPE = Column(String)
    OPTRIGHTTYPE = Column(String)
    RTVALMEPAY = Column(String)
    UNDERASSTYPE = Column(String)
    UNDERASSVARIT = Column(String)
    STANDASSCONT = Column(String)
    STANDASSTRADPLC = Column(String)
    GENERALNAMNUM = Column(String)
    VALUUNIT = Column(String)
    EXECUTPRICE = Column(Float)
    ASSENTRYPRICE = Column(String)
    PRICESYMBOL = Column(String)
    ACCOUNTMONEY = Column(String)
    NOMINALAMOUNT = Column(String)
    PREMIUMAMOUNT = Column(Float)
    CONTRACTVALUE = Column(Float)
    VALMETHOD = Column(String)
    SETTMETHOD = Column(String)
    FINALSETTDAY = Column(String)
    SETTPRIMETHED = Column(String)
    ONEPRICE = Column(Float)
    COMMREFPRICE = Column(Float)
    STATUS = Column(String)
    LEGID = Column(String)
    TRADEQAUNTITY = Column(String)
    PARTICIPATIONRATE = Column(Float)
    ANNUALIZED = Column(String)
    EXCHANGERATE = Column(Float)
    TRADENOTIONAL = Column(String)
    ANNVALRATIO = Column(Float)


class ROTCCompanyType(BaseModel):
    __tablename__ = 'R_OTC_COMPANY_TYPE'
    __table_args__ = {
        'schema': HIVE_TRADE_SCHEMA
    }
    FUTURESID = Column(String, primary_key=True)
    LEVELONETYPE = Column(String, primary_key=True)
    LEVELTWOTYPE = Column(String, primary_key=True)
    CLASSIFYNAME = Column(String)
    CLIENTNAME = Column(String, primary_key=True)
    NOCID = Column(String, primary_key=True)


class STradeDate(BaseModel):
    __tablename__ = 'S_TRADEDATE'
    __table_args__ = {
        'schema': HIVE_TRADEDATE_SCHEMA
    }
    TRADEDATE = Column(String, primary_key=True)
    FLAG = Column(String)
