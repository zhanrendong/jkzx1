# -*- coding: utf-8 -*-
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy import Column, String, Date, DateTime
from sqlalchemy import create_engine
from config.bct_config import pg_terminal_connection, pg_terminal_schema
import os

os.environ['NLS_LANG'] = 'SIMPLIFIED CHINESE_CHINA.UTF8'

BaseModel = declarative_base()


def get_terminal_session():
    engine = create_engine(pg_terminal_connection)
    session = sessionmaker(bind=engine)
    return session()


class Instrument(BaseModel):
    __tablename__ = 'instrument'
    __table_args__ = {'schema': pg_terminal_schema}
    instrument_id = Column(String, primary_key=True)
    instrument_type = Column(String)
    updated_at = Column(DateTime)
    listed_date = Column(Date)
    delisted_date = Column(Date)
    asset_class = Column(String)
    data_source = Column(String)
    status = Column(String)
    short_name = Column(String)
    # 对应的期权是场内还是场外期权
    option_traded_type = Column(String)
    # 大宗商品对应的主力合约类型
    contract_type = Column(String)
