from pyhive import hive
from sqlalchemy import create_engine, Column, Unicode, String, Float, DECIMAL
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.declarative import declarative_base
from config.data_source_config import hive_host, hive_port, hive_username, hive_database, hive_password, hive_auth
from config.bct_config import pg_terminal_connection, pg_terminal_schema

BaseModel = declarative_base()

TABLE_NAME_TRADE_DATE = 'S_TRADEDATE'
TABLE_NAME_MARKET_DIST_CUS = 'R_OTC_REPORT_CUS_DIST'
TABLE_NAME_MARKET_DIST_SUBCOMPANY = 'R_OTC_REPORT_SUBCOMPANY_DIST'
TABLE_NAME_MARKET_DIST_VARIETY = 'R_OTC_REPORT_VARIETY_DIST'

TABLE_NAME_SUMMARY = 'R_OTC_REPORT_SUMMARY'
TABLE_NAME_TRADE_SUMMARY = 'R_OTC_REPORT_TRADE_SUMMARY'
TABLE_NAME_POSITION_SUMMARY = 'R_OTC_REPORT_POSITION_SUMMARY'
TABLE_NAME_ASSET_TOOL = 'R_OTC_REPORT_ASSET_TOOL'
TABLE_NAME_CUS_TYPE = 'R_OTC_REPORT_CUS_TYPE'
TABLE_NAME_MARKET_DIST = 'R_OTC_REPORT_MARKET_DIST'
TABLE_NAME_OTC_ET_COMMODITY = 'R_OTC_REPORT_OTC_ET_COMMODITY'
TABLE_NAME_OTC_ET_CUS = 'R_OTC_REPORT_OTC_ET_CUS'
TABLE_NAME_OTC_ET_SUBCOMPANY = 'R_OTC_REPORT_OTC_ET_SUBCOMPANY'
TABLE_NAME_MARKET_MANIPULATE = 'R_OTC_REPORT_MARKET_MANIPULATE'
TABLE_NAME_COMP_PROPAGATE = 'R_OTC_REPORT_COMP_PROPAGATE'
TABLE_NAME_CUS_POS_PERCENTAGE = 'R_OTC_REPORT_CUS_POS_PERCENTAGE'


def get_hive_connection():
    return hive.Connection(host=hive_host, port=hive_port, username=hive_username,
                           password=hive_password, database=hive_database, auth=hive_auth)


def get_pg_session():
    engine = create_engine(pg_terminal_connection)
    session = sessionmaker(bind=engine)
    return session()


# 市场规模
class OTCSummaryReport(BaseModel):
    __tablename__ = TABLE_NAME_SUMMARY
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    TRDNOTIONAMOUNT = Column(Float)
    TRDTRANSNUM = Column(Float)
    OPTFEEAMOUNT = Column(DECIMAL)
    TRDCUSNUM = Column(Float)
    POSNOTIONAMOUNT = Column(Float)
    POSTRANSNUM = Column(Float)
    POSCUSNUM = Column(Float)
    INMARKETCUSNUM = Column(Float)
    FULLMARKETCUSNUM = Column(Float)
    POSVALUE = Column(DECIMAL)


# 成交结构
class OTCTradeSummaryReport(BaseModel):
    __tablename__ = TABLE_NAME_TRADE_SUMMARY
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    OPENTRDNOTIONAMOUNT = Column(Float)
    CLOSETRDNOTIONAMOUNT = Column(Float)
    ENDTRDNOTIONAMOUNT = Column(Float)
    TRDNOTIONAMOUNT = Column(Float)
    TRDOPENPREMIUM = Column(DECIMAL)
    TRDCLOSEPREMIUM = Column(DECIMAL)
    TRDENDPREMIUM = Column(DECIMAL)
    PREMIUMAMOUNT = Column(DECIMAL)
    TRDOPENCUSNUM = Column(Float)
    TRDCLOSECUSNUM = Column(Float)
    TRDENDCUSNUM = Column(Float)
    TRDCUSNUM = Column(Float)


# 持仓结构
class OTCPositionSummaryReport(BaseModel):
    __tablename__ = TABLE_NAME_POSITION_SUMMARY
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    POSCALLBUYAMOUNT = Column(Float)
    POSPUTBUYAMOUNT = Column(Float)
    POSOTHERBUYAMOUNT = Column(Float)
    POSBUYAMOUNTTOTAL = Column(Float)
    POSCALLSELLAMOUNT = Column(Float)
    POSPUTSELLAMOUNT = Column(Float)
    POSSELLAMOUNTTOTAL = Column(Float)
    POSCALLBUYCVALUE = Column(DECIMAL)
    POSPUTBUYCVALUE = Column(DECIMAL)
    POSOTHERBUYCVALUE = Column(DECIMAL)
    POSBUYVALUETOTAL = Column(DECIMAL)
    POSCALLSELLCVALUE = Column(DECIMAL)
    POSPUTSELLCVALUE = Column(DECIMAL)
    POSSELLVALUETOTAL = Column(DECIMAL)


# 资产和工具结构
class OTCAssetToolReport(BaseModel):
    __tablename__ = TABLE_NAME_ASSET_TOOL
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    ASSETTYPE = Column(String)
    TOOLTYPE = Column(String)
    TRDTRANSNUM = Column(Float)
    TRDNOTIONAMOUNT = Column(Float)
    POSTRANSNUM = Column(Float)
    POSNOTIONAMOUNT = Column(Float)
    INMARKETCUSNUM = Column(Float)


# 客户类型结构
class OTCCusTypeReport(BaseModel):
    __tablename__ = TABLE_NAME_CUS_TYPE
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    CUSTYPE = Column(String)
    ASSETTYPE = Column(String)
    TRDTRANSNUM = Column(Float)
    TRDNOTIONAMOUNT = Column(Float)
    POSTRANSNUM = Column(Float)
    POSNOTIONAMOUNT = Column(Float)
    INMARKETCUSNUM = Column(Float)


# 市场集中度
class OTCMarketDistReport(BaseModel):
    __tablename__ = TABLE_NAME_MARKET_DIST
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    TOTALPOS = Column(Float)
    TOP3POS = Column(Float)
    TOP3POSDIST = Column(Float)
    TOP5POS = Column(Float)
    TOP5POSDIST = Column(Float)
    TOP10POS = Column(Float)
    TOP10POSDIST = Column(Float)
    DISTTYPE = Column(String)


# 品种联动
class OTCEtCommodityReport(BaseModel):
    __tablename__ = TABLE_NAME_OTC_ET_COMMODITY
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    COMMODITYID = Column(String)
    OTCPOSAMOUNT = Column(Float)
    OTCPOSRATIO = Column(Float)
    ETPOSAMOUNT = Column(DECIMAL)
    ETPOSRATIO = Column(DECIMAL)
    OTCETRATIO = Column(Float)


# 交易对手方联动
class OTCEtCusReport(BaseModel):
    __tablename__ = TABLE_NAME_OTC_ET_CUS
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    ETACCOUNTCUSNUM = Column(Float)
    OTCCUSPOSAMOUNT = Column(Float)
    ETCUSPOSAMOUNT = Column(DECIMAL)
    ETCUSRIGHT = Column(DECIMAL)


# 子公司联动
class OTCEtSubCompanyReport(BaseModel):
    __tablename__ = TABLE_NAME_OTC_ET_SUBCOMPANY
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    COMMODITYID = Column(String)
    MAINBODYNAME = Column(String)
    OTCSUBPOSAMOUNT = Column(Float)
    ETSUBPOSAMOUNT = Column(DECIMAL)


# 操纵风险
class OTCMarketManipulateReport(BaseModel):
    __tablename__ = TABLE_NAME_MARKET_MANIPULATE
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    INTERCOMPCUSNUM = Column(Float)
    INTERCOMPTRD = Column(Float)
    INTERCOMPPOS = Column(Float)


# 子公司传染风险
class OTCCompPropagateReport(BaseModel):
    __tablename__ = TABLE_NAME_COMP_PROPAGATE
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    INTERCOMPNUM = Column(Float)
    INTERCOMPTRDAMOUNT = Column(Float)
    COMPTRDAMOUNTTOTAL = Column(Float)
    TRDRATIO = Column(Float)
    INTERCOMPPOSAMOUNT = Column(Float)
    COMPPOSAMOUNTTOTAL = Column(Float)
    POSRATIO = Column(Float)


# 对手方场内外合并持仓占比
class OTCCusPosPercentageReport(BaseModel):
    __tablename__ = TABLE_NAME_CUS_POS_PERCENTAGE
    __table_args__ = {'schema': pg_terminal_schema}
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    ANALOGUENAME = Column(String)
    UNDERASSVARIT = Column(String)
    CUSPOSITIVEDELTA = Column(Float)
    CUSSHORTPOSITION = Column(Float)
    CUSNEGATIVEDELTA = Column(Float)
    CUSLONGPOSITION = Column(Float)
    EXCHANGEMAXPOS = Column(Float)
    EXCHANGEPOS = Column(Float)
    CUSEXGOTCRATIO = Column(Float)
