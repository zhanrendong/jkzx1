# -*- coding: utf-8 -*-
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy import Column, String, CLOB, Numeric, TIMESTAMP, VARCHAR, FLOAT
from sqlalchemy import create_engine
import psycopg2
from config.data_source_config import center_oracle_database_url, REPORT_TO_ORACLE_SCHEMA
from config.bct_config import *
import os

os.environ['NLS_LANG'] = 'SIMPLIFIED CHINESE_CHINA.UTF8'
SCHEMA_REPORT_SERVICE = 'report_service'
SCHEMA_MARKET_DATA = pg_terminal_schema
TABLE_MARKET_RISK = 'market_risk_report'
TABLE_MARKET_RISK_DETAIL = 'market_risk_detail_report'
TABLE_SUBSIDIARY_MARKET_RISK = 'subsidiary_market_risk_report'
TABLE_SUBSIDIARY_MARKET_RISK_BY_UNDERLYER = 'market_risk_by_sub_underlyer_report'
TABLE_COUNTER_PARTY_MARKET_RISK = 'counter_party_market_risk_report'
TABLE_COUNTER_PARTY_MARKET_RISK_BY_UNDERLYER = 'counter_party_market_risk_by_underlyer_report'
TABLE_SPOT_SCENARIOS = 'spot_scenarios_report'
TABLE_VOL_SURFACE = 'vol_surface'
TABLE_REALIZED_VOL = 'realized_vol'

CENTER_TABLE_MARKET_RISK = 'rov_market_risk_report'
CENTER_TABLE_MARKET_RISK_DETAIL = 'rov_market_risk_detail_report'
CENTER_TABLE_SUBSIDIARY_MARKET_RISK = 'rov_subsidiary_mkt_risk_report'
CENTER_TABLE_SUBSIDIARY_MARKET_RISK_BY_UNDERLYER = 'rov_subsidiary_mkt_risk_detail'
CENTER_TABLE_COUNTER_PARTY_MARKET_RISK = 'rov_counter_party_mkt_risk'
CENTER_TABLE_COUNTER_PARTY_MARKET_RISK_BY_UNDERLYER = 'rov_counter_party_mkt_risk_dtl'
CENTER_TABLE_SPOT_SCENARIOS = 'rov_spot_scenarios_report'
CENTER_TABLE_VOL_SURFACE = 'rov_vol_surface'
CENTER_TABLE_REALIZED_VOL = 'rov_realized_vol'

BaseModel = declarative_base()


def get_pg_connection(is_terminal=False):
    if is_terminal:
        return psycopg2.connect(database=pg_terminal_database, user=pg_user, password=pg_password,
                                host=pg_host, port=pg_port)
    else:
        return psycopg2.connect(database=pg_database, user=pg_user, password=pg_password, host=pg_host, port=pg_port)


def get_oracle_session():
    engine = create_engine(center_oracle_database_url)
    session = sessionmaker(bind=engine)
    return session()


class OTCMarketRisk(BaseModel):
    __tablename__ = CENTER_TABLE_MARKET_RISK
    __table_args__ = {
        'schema': REPORT_TO_ORACLE_SCHEMA
    }
    uuid = Column(String, primary_key=True)
    created_at = Column(TIMESTAMP)
    delta_cash = Column(Numeric)
    gamma_cash = Column(Numeric)
    pricing_environment = Column(VARCHAR)
    report_name = Column(VARCHAR)
    rho = Column(Numeric)
    theta = Column(Numeric)
    valuation_date = Column(VARCHAR)
    vega = Column(Numeric)


class OTCMarketRiskDetail(BaseModel):
    __tablename__ = CENTER_TABLE_MARKET_RISK_DETAIL
    __table_args__ = {
        'schema': REPORT_TO_ORACLE_SCHEMA
    }
    uuid = Column(String, primary_key=True)
    created_at = Column(TIMESTAMP)
    delta = Column(Numeric)
    delta_cash = Column(Numeric)
    gamma = Column(Numeric)
    gamma_cash = Column(Numeric)
    party_name = Column(VARCHAR)
    pnl_change = Column(Numeric)
    report_name = Column(VARCHAR)
    report_type = Column(VARCHAR)
    rho = Column(Numeric)
    scenario_type = Column(VARCHAR)
    subsidiary = Column(VARCHAR)
    theta = Column(Numeric)
    underlyer_instrument_id = Column(VARCHAR)
    valuation_date = Column(VARCHAR)
    vega = Column(Numeric)
    exfsid = Column(VARCHAR)


class OTCSubsidiaryMarketRisk(BaseModel):
    __tablename__ = CENTER_TABLE_SUBSIDIARY_MARKET_RISK
    __table_args__ = {
        'schema': REPORT_TO_ORACLE_SCHEMA
    }
    uuid = Column(String, primary_key=True)
    created_at = Column(TIMESTAMP)
    delta_cash = Column(Numeric)
    gamma_cash = Column(Numeric)
    report_name = Column(VARCHAR)
    rho = Column(Numeric)
    subsidiary = Column(VARCHAR)
    theta = Column(Numeric)
    valuation_date = Column(VARCHAR)
    vega = Column(Numeric)


class OTCSubsidiaryMarketRiskDetail(BaseModel):
    __tablename__ = CENTER_TABLE_SUBSIDIARY_MARKET_RISK_BY_UNDERLYER
    __table_args__ = {
        'schema': REPORT_TO_ORACLE_SCHEMA
    }
    uuid = Column(String, primary_key=True)
    book_name = Column(VARCHAR)
    created_at = Column(TIMESTAMP)
    delta = Column(Numeric)
    delta_cash = Column(Numeric)
    gamma = Column(Numeric)
    gamma_cash = Column(Numeric)
    pricing_environment = Column(VARCHAR)
    report_name = Column(VARCHAR)
    rho = Column(Numeric)
    theta = Column(Numeric)
    underlyer_instrument_id = Column(VARCHAR)
    valuation_date = Column(VARCHAR)
    vega = Column(Numeric)
    exfsid = Column(VARCHAR)


class OTCCounterPartyMarketRisk(BaseModel):
    __tablename__ = CENTER_TABLE_COUNTER_PARTY_MARKET_RISK
    __table_args__ = {
        'schema': REPORT_TO_ORACLE_SCHEMA
    }
    uuid = Column(String, primary_key=True)
    created_at = Column(TIMESTAMP)
    delta_cash = Column(Numeric)
    gamma_cash = Column(Numeric)
    party_name = Column(VARCHAR)
    report_name = Column(VARCHAR)
    rho = Column(Numeric)
    theta = Column(Numeric)
    valuation_date = Column(VARCHAR)
    vega = Column(Numeric)


class OTCCounterPartyMarketRiskDetail(BaseModel):
    __tablename__ = CENTER_TABLE_COUNTER_PARTY_MARKET_RISK_BY_UNDERLYER
    __table_args__ = {
        'schema': REPORT_TO_ORACLE_SCHEMA
    }
    uuid = Column(String, primary_key=True)
    created_at = Column(TIMESTAMP)
    delta = Column(Numeric)
    delta_cash = Column(Numeric)
    gamma = Column(Numeric)
    gamma_cash = Column(Numeric)
    party_name = Column(VARCHAR)
    report_name = Column(VARCHAR)
    rho = Column(Numeric)
    theta = Column(Numeric)
    underlyer_instrument_id = Column(VARCHAR)
    valuation_date = Column(VARCHAR)
    vega = Column(Numeric)
    exfsid = Column(VARCHAR)


class OTCSpotScenarios(BaseModel):
    __tablename__ = CENTER_TABLE_SPOT_SCENARIOS
    __table_args__ = {
        'schema': REPORT_TO_ORACLE_SCHEMA
    }
    uuid = Column(String, primary_key=True)
    asset_class = Column(VARCHAR)
    content_name = Column(VARCHAR)
    created_at = Column(TIMESTAMP)
    instrument_id = Column(VARCHAR)
    instrument_type = Column(VARCHAR)
    report_name = Column(VARCHAR)
    report_type = Column(VARCHAR)
    valuation_date = Column(VARCHAR)
    scenarios = Column(CLOB)
    exfsid = Column(VARCHAR)


class OTCVolSurface(BaseModel):
    __tablename__ = CENTER_TABLE_VOL_SURFACE
    __table_args__ = {
        'schema': REPORT_TO_ORACLE_SCHEMA
    }
    uuid = Column(String, primary_key=True)
    instrument_id = Column(VARCHAR)
    valuation_date = Column(VARCHAR)
    instance = Column(VARCHAR)
    model_info = Column(CLOB)
    fitting_model = Column(CLOB)
    strike_type = Column(VARCHAR)
    updated_at = Column(TIMESTAMP)
    tag = Column(VARCHAR)
    source = Column(VARCHAR)


class OTCRealizedVol(BaseModel):
    __tablename__ = CENTER_TABLE_REALIZED_VOL
    __table_args__ = {
        'schema': REPORT_TO_ORACLE_SCHEMA
    }
    uuid = Column(String, primary_key=True)
    instrument_id = Column(VARCHAR)
    valuation_date = Column(VARCHAR)
    vol = Column(FLOAT)
    windows = Column(Numeric)
    update_at = Column(TIMESTAMP)
    exfsid = Column(VARCHAR)
