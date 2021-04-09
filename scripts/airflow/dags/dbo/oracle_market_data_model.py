import sqlalchemy as sqla
import sqlalchemy.orm as sqlorm
from sqlalchemy.ext.declarative import declarative_base as sqla_declarative_base
from dags.conf import OracleDBConfig
import os
os.environ['NLS_LANG'] = 'SIMPLIFIED CHINESE_CHINA.UTF8'

OracleBaseModel = sqla_declarative_base()


class AIndexDescription(OracleBaseModel):
    __tablename__ = 'AINDEXDESCRIPTION'
    __table_args__ = {'schema': OracleDBConfig.market_data_schema}
    # 对象ID
    OBJECT_ID = sqla.Column('OBJECT_ID', sqla.String)
    # Wind代码
    S_INFO_WINDCODE = sqla.Column('S_INFO_WINDCODE', sqla.String, primary_key=True)
    # 交易代码
    S_INFO_CODE = sqla.Column('S_INFO_CODE', sqla.String)
    # 证券简称
    S_INFO_NAME = sqla.Column('S_INFO_NAME', sqla.String)
    # 指数名称
    S_INFO_COMPNAME = sqla.Column('S_INFO_COMPNAME', sqla.String)
    # 交易所
    S_INFO_EXCHMARKET = sqla.Column('S_INFO_EXCHMARKET', sqla.String)
    # 基期
    S_INFO_INDEX_BASEPER = sqla.Column('S_INFO_INDEX_BASEPER', sqla.String)
    # 基点
    S_INFO_INDEX_BASEPT = sqla.Column('S_INFO_INDEX_BASEPT', sqla.Float)
    # 发布日期
    S_INFO_LISTDATE = sqla.Column('S_INFO_LISTDATE', sqla.String)
    # 加权方式
    S_INFO_INDEX_WEIGHTSRULE = sqla.Column('S_INFO_INDEX_WEIGHTSRULE', sqla.String)
    # 发布方
    S_INFO_PUBLISHER = sqla.Column('S_INFO_PUBLISHER', sqla.String)
    # 指数类别代码
    S_INFO_INDEXCODE = sqla.Column('S_INFO_INDEXCODE', sqla.String)
    # 指数风格
    S_INFO_INDEXSTYLE = sqla.Column('S_INFO_INDEXSTYLE', sqla.String)
    # 指数简介
    INDEX_INTRO = sqla.Column('INDEX_INTRO', sqla.String)
    # 权重类型
    WEIGHT_TYPE = sqla.Column('WEIGHT_TYPE', sqla.Float)
    # 终止发布日期
    EXPIRE_DATE = sqla.Column('EXPIRE_DATE', sqla.String)
    # 收益处理方式
    INCOME_PROCESSING_METHOD = sqla.Column('INCOME_PROCESSING_METHOD', sqla.String)
    # 变更历史
    CHANGE_HISTORY = sqla.Column('CHANGE_HISTORY', sqla.String)
    #
    OPDATE = sqla.Column('OPDATE', sqla.DateTime)
    #
    OPMODE = sqla.Column('OPMODE', sqla.String)


class AIndexEODPrices(OracleBaseModel):
    __tablename__ = 'AINDEXEODPRICES'
    __table_args__ = {'schema': OracleDBConfig.market_data_schema}
    # 对象ID
    OBJECT_ID = sqla.Column('OBJECT_ID', sqla.String)
    # Wind代码
    S_INFO_WINDCODE = sqla.Column('S_INFO_WINDCODE', sqla.String, primary_key=True)
    # 交易日期
    TRADE_DT = sqla.Column('TRADE_DT', sqla.String)
    # 货币代码
    CRNCY_CODE = sqla.Column('CRNCY_CODE', sqla.String)
    # 昨收盘价(点)
    S_DQ_PRECLOSE = sqla.Column('S_DQ_PRECLOSE', sqla.Float)
    # 开盘价(点)
    S_DQ_OPEN = sqla.Column('S_DQ_OPEN', sqla.Float)
    # 最高价(点)
    S_DQ_HIGH = sqla.Column('S_DQ_HIGH', sqla.Float)
    # 最低价(点)
    S_DQ_LOW = sqla.Column('S_DQ_LOW', sqla.Float)
    # 收盘价(点)
    S_DQ_CLOSE = sqla.Column('S_DQ_CLOSE', sqla.Float)
    # 涨跌(点)
    S_DQ_CHANGE = sqla.Column('S_DQ_CHANGE', sqla.Float)
    # 涨跌幅(%)
    S_DQ_PCTCHANGE = sqla.Column('S_DQ_PCTCHANGE', sqla.Float)
    # 成交量(手)
    S_DQ_VOLUME = sqla.Column('S_DQ_VOLUME', sqla.Float)
    # 成交金额(千元)
    S_DQ_AMOUNT = sqla.Column('S_DQ_AMOUNT', sqla.Float)
    # 证券ID
    SEC_ID = sqla.Column('SEC_ID', sqla.String)
    #
    OPDATE = sqla.Column('OPDATE', sqla.DateTime)
    #
    OPMODE = sqla.Column('OPMODE', sqla.String)


class AShareDescription(OracleBaseModel):
    __tablename__ = 'ASHAREDESCRIPTION'
    __table_args__ = {'schema': OracleDBConfig.market_data_schema}
    # 对象ID
    OBJECT_ID = sqla.Column('OBJECT_ID', sqla.String)
    # Wind代码
    S_INFO_WINDCODE = sqla.Column('S_INFO_WINDCODE', sqla.String, primary_key=True)
    # 交易代码
    S_INFO_CODE = sqla.Column('S_INFO_CODE', sqla.String)
    # 证券简称
    S_INFO_NAME = sqla.Column('S_INFO_NAME', sqla.String)
    # 公司中文名称
    S_INFO_COMPNAME = sqla.Column('S_INFO_COMPNAME', sqla.String)
    # 公司英文名称
    S_INFO_COMPNAMEENG = sqla.Column('S_INFO_COMPNAMEENG', sqla.String)
    # ISIN代码
    S_INFO_ISINCODE = sqla.Column('S_INFO_ISINCODE', sqla.String)
    # 交易所
    S_INFO_EXCHMARKET = sqla.Column('S_INFO_EXCHMARKET', sqla.String)
    # 上市板类型
    S_INFO_LISTBOARD = sqla.Column('S_INFO_LISTBOARD', sqla.String)
    # 上市日期
    S_INFO_LISTDATE = sqla.Column('S_INFO_LISTDATE', sqla.String)
    # 退市日期
    S_INFO_DELISTDATE = sqla.Column('S_INFO_DELISTDATE', sqla.String)
    #
    S_INFO_SEDOLCODE = sqla.Column('S_INFO_SEDOLCODE', sqla.String)
    # 货币代码
    CRNCY_CODE = sqla.Column('CRNCY_CODE', sqla.String)
    # 简称拼音
    S_INFO_PINYIN = sqla.Column('S_INFO_PINYIN', sqla.String)
    # 上市板
    S_INFO_LISTBOARDNAME = sqla.Column('S_INFO_LISTBOARDNAME', sqla.String)
    # 是否在沪股通或深港通范围内
    IS_SHSC = sqla.Column('IS_SHSC', sqla.Float)
    # 公司ID
    S_INFO_COMPCODE = sqla.Column('S_INFO_COMPCODE', sqla.String)
    #
    OPDATE = sqla.Column('OPDATE', sqla.DateTime)
    #
    OPMODE = sqla.Column('OPMODE', sqla.String)


class AShareEODPrices(OracleBaseModel):
    __tablename__ = 'ASHAREEODPRICES'
    __table_args__ = {'schema': OracleDBConfig.market_data_schema}
    # 对象ID
    OBJECT_ID = sqla.Column('OBJECT_ID', sqla.String)
    # Wind代码
    S_INFO_WINDCODE = sqla.Column('S_INFO_WINDCODE', sqla.String, primary_key=True)
    # 交易日期
    TRADE_DT = sqla.Column('TRADE_DT', sqla.String)
    # 货币代码
    CRNCY_CODE = sqla.Column('CRNCY_CODE', sqla.String)
    # 昨收盘价(元)
    S_DQ_PRECLOSE = sqla.Column('S_DQ_PRECLOSE', sqla.Float)
    # 开盘价(元)
    S_DQ_OPEN = sqla.Column('S_DQ_OPEN', sqla.Float)
    # 最高价(元)
    S_DQ_HIGH = sqla.Column('S_DQ_HIGH', sqla.Float)
    # 最低价(元)
    S_DQ_LOW = sqla.Column('S_DQ_LOW', sqla.Float)
    # 收盘价(元)
    S_DQ_CLOSE = sqla.Column('S_DQ_CLOSE', sqla.Float)
    # 涨跌(元)
    S_DQ_CHANGE = sqla.Column('S_DQ_CHANGE', sqla.Float)
    # 涨跌幅(%)
    S_DQ_PCTCHANGE = sqla.Column('S_DQ_PCTCHANGE', sqla.Float)
    # 成交量(手)
    S_DQ_VOLUME = sqla.Column('S_DQ_VOLUME', sqla.Float)
    # 成交金额(千元)
    S_DQ_AMOUNT = sqla.Column('S_DQ_AMOUNT', sqla.Float)
    # 复权昨收盘价(元)
    S_DQ_ADJPRECLOSE = sqla.Column('S_DQ_ADJPRECLOSE', sqla.Float)
    # 复权开盘价(元)
    S_DQ_ADJOPEN = sqla.Column('S_DQ_ADJOPEN', sqla.Float)
    # 复权最高价(元)
    S_DQ_ADJHIGH = sqla.Column('S_DQ_ADJHIGH', sqla.Float)
    # 复权最低价(元)
    S_DQ_ADJLOW = sqla.Column('S_DQ_ADJLOW', sqla.Float)
    # 复权收盘价(元)
    S_DQ_ADJCLOSE = sqla.Column('S_DQ_ADJCLOSE', sqla.Float)
    # 复权因子
    S_DQ_ADJFACTOR = sqla.Column('S_DQ_ADJFACTOR', sqla.Float)
    # 均价(VWAP)
    S_DQ_AVGPRICE = sqla.Column('S_DQ_AVGPRICE', sqla.Float)
    # 交易状态
    S_DQ_TRADESTATUS = sqla.Column('S_DQ_TRADESTATUS', sqla.String)
    #
    OPDATE = sqla.Column('OPDATE', sqla.DateTime)
    #
    OPMODE = sqla.Column('OPMODE', sqla.String)


class GoldSpotDescription(OracleBaseModel):
    __tablename__ = 'CGOLDSPOTDESCRIPTION'
    __table_args__ = {'schema': OracleDBConfig.market_data_schema}
    # 对象ID
    OBJECT_ID = sqla.Column('OBJECT_ID', sqla.String)
    # Wind代码
    S_INFO_WINDCODE = sqla.Column('S_INFO_WINDCODE', sqla.String, primary_key=True)
    # 交易代码
    S_INFO_CODE = sqla.Column('S_INFO_CODE', sqla.String)
    # 证券中文简称
    S_INFO_NAME = sqla.Column('S_INFO_NAME', sqla.String)
    # 交易所
    S_INFO_EXCHMARKET = sqla.Column('S_INFO_EXCHMARKET', sqla.String)
    # 交易单位
    S_INFO_PUNIT = sqla.Column('S_INFO_PUNIT', sqla.String)
    #
    OPDATE = sqla.Column('OPDATE', sqla.DateTime)
    #
    OPMODE = sqla.Column('OPMODE', sqla.String)


class GoldSpotEODPrices(OracleBaseModel):
    __tablename__ = 'CGOLDSPOTEODPRICES'
    __table_args__ = {'schema': OracleDBConfig.market_data_schema}
    # 对象ID
    OBJECT_ID = sqla.Column('OBJECT_ID', sqla.String)
    # Wind代码
    S_INFO_WINDCODE = sqla.Column('S_INFO_WINDCODE', sqla.String, primary_key=True)
    # 日期
    TRADE_DT = sqla.Column('TRADE_DT', sqla.String)
    # 开盘价(元)
    S_DQ_OPEN = sqla.Column('S_DQ_OPEN', sqla.Float)
    # 最高价(元)
    S_DQ_HIGH = sqla.Column('S_DQ_HIGH', sqla.Float)
    # 最低价(元)
    S_DQ_LOW = sqla.Column('S_DQ_LOW', sqla.Float)
    # 收盘价(元)
    S_DQ_CLOSE = sqla.Column('S_DQ_CLOSE', sqla.Float)
    # 均价(元)
    S_DQ_AVGPRICE = sqla.Column('S_DQ_AVGPRICE', sqla.Float)
    # 成交量(千克)
    S_DQ_VOLUME = sqla.Column('S_DQ_VOLUME', sqla.Float)
    # 成交金额(元)
    S_DQ_AMOUNT = sqla.Column('S_DQ_AMOUNT', sqla.Float)
    # 持仓量(手)
    S_DQ_OI = sqla.Column('S_DQ_OI', sqla.Float)
    # 交收量(手)
    DEL_AMT = sqla.Column('DEL_AMT', sqla.Float)
    # 结算价(元)
    S_DQ_SETTLE = sqla.Column('S_DQ_SETTLE', sqla.Float)
    # 延期补偿费支付方式向类别代码
    DELAY_PAY_TYPECODE = sqla.Column('DELAY_PAY_TYPECODE', sqla.Float)
    # 涨跌幅(%)
    S_PCT_CHG = sqla.Column('S_PCT_CHG', sqla.Float)
    #
    OPDATE = sqla.Column('OPDATE', sqla.DateTime)
    #
    OPMODE = sqla.Column('OPMODE', sqla.String)


class FuturesDescription(OracleBaseModel):
    __tablename__ = 'CFUTURESDESCRIPTION'
    __table_args__ = {'schema': OracleDBConfig.market_data_schema}
    # 对象ID
    OBJECT_ID = sqla.Column('OBJECT_ID', sqla.String)
    # Wind代码
    S_INFO_WINDCODE = sqla.Column('S_INFO_WINDCODE', sqla.String, primary_key=True)
    # 交易代码
    S_INFO_CODE = sqla.Column('S_INFO_CODE', sqla.String)
    # 证券中文简称
    S_INFO_NAME = sqla.Column('S_INFO_NAME', sqla.String)
    # 证券英文简称
    S_INFO_ENAME = sqla.Column('S_INFO_ENAME', sqla.String)
    # 标准合约代码
    FS_INFO_SCCODE = sqla.Column('FS_INFO_SCCODE', sqla.String)
    # 合约类型
    FS_INFO_TYPE = sqla.Column('FS_INFO_TYPE', sqla.Float)
    # 连续合约类型
    FS_INFO_CCTYPE = sqla.Column('FS_INFO_CCTYPE', sqla.Float)
    # 交易所
    S_INFO_EXCHMARKET = sqla.Column('S_INFO_EXCHMARKET', sqla.String)
    # 上市日期
    S_INFO_LISTDATE = sqla.Column('S_INFO_LISTDATE', sqla.String)
    # 最后交易日期
    S_INFO_DELISTDATE = sqla.Column('S_INFO_DELISTDATE', sqla.String)
    # 交割月份
    FS_INFO_DLMONTH = sqla.Column('FS_INFO_DLMONTH', sqla.String)
    # 挂牌基准价
    FS_INFO_LPRICE = sqla.Column('FS_INFO_LPRICE', sqla.Float)
    # 最后交割日
    FS_INFO_LTDLDATE = sqla.Column('FS_INFO_LTDLDATE', sqla.String)
    # 证券中文名称
    S_INFO_FULLNAME = sqla.Column('S_INFO_FULLNAME', sqla.String)
    # 交券日
    S_INFO_VOUCHERDATE = sqla.Column('S_INFO_VOUCHERDATE', sqla.String)
    # 缴款日
    S_INFO_PAYMENTDATE = sqla.Column('S_INFO_PAYMENTDATE', sqla.String)
    #
    OPDATE = sqla.Column('OPDATE', sqla.DateTime)
    #
    OPMODE = sqla.Column('OPMODE', sqla.String)


class CommodityFuturesEODPrices(OracleBaseModel):
    __tablename__ = 'CCOMMODITYFUTURESEODPRICES'
    __table_args__ = {'schema': OracleDBConfig.market_data_schema}
    # 对象ID
    OBJECT_ID = sqla.Column('OBJECT_ID', sqla.String)
    # Wind代码
    S_INFO_WINDCODE = sqla.Column('S_INFO_WINDCODE', sqla.String, primary_key=True)
    # 交易日期
    TRADE_DT = sqla.Column('TRADE_DT', sqla.String)
    # 前结算价(元)
    S_DQ_PRESETTLE= sqla.Column('S_DQ_PRESETTLE', sqla.String)
    # 开盘价(元)
    S_DQ_OPEN = sqla.Column('S_DQ_OPEN', sqla.Float)
    # 最高价(元)
    S_DQ_HIGH = sqla.Column('S_DQ_HIGH', sqla.Float)
    # 最低价(元)
    S_DQ_LOW = sqla.Column('S_DQ_LOW', sqla.Float)
    # 收盘价(元)
    S_DQ_CLOSE = sqla.Column('S_DQ_CLOSE', sqla.Float)
    # 结算价(元)
    S_DQ_SETTLE = sqla.Column('S_DQ_SETTLE', sqla.Float)
    # 成交量(手)
    S_DQ_VOLUME = sqla.Column('S_DQ_VOLUME', sqla.Float)
    # 成交金额(万元)
    S_DQ_AMOUNT = sqla.Column('S_DQ_AMOUNT', sqla.Float)
    # 持仓量(手)
    S_DQ_OI = sqla.Column('S_DQ_OI', sqla.Float)
    # 涨跌(元)
    S_DQ_CHANGE = sqla.Column('S_DQ_CHANGE', sqla.Float)
    # 持仓量变化
    S_DQ_OICHANGE = sqla.Column('S_DQ_OICHANGE', sqla.Float)
    # 合约类型
    FS_INFO_TYPE = sqla.Column('FS_INFO_TYPE', sqla.String)
    #
    OPDATE = sqla.Column('OPDATE', sqla.DateTime)
    #
    OPMODE = sqla.Column('OPMODE', sqla.String)


class IndexFuturesEODPrices(OracleBaseModel):
    __tablename__ = 'CINDEXFUTURESEODPRICES'
    __table_args__ = {'schema': OracleDBConfig.market_data_schema}
    # 对象ID
    OBJECT_ID = sqla.Column('OBJECT_ID', sqla.String)
    # Wind代码
    S_INFO_WINDCODE = sqla.Column('S_INFO_WINDCODE', sqla.String, primary_key=True)
    # 交易日期
    TRADE_DT = sqla.Column('TRADE_DT', sqla.String)
    # 前结算价(元)
    S_DQ_PRESETTLE = sqla.Column('S_DQ_PRESETTLE', sqla.String)
    # 开盘价(元)
    S_DQ_OPEN = sqla.Column('S_DQ_OPEN', sqla.Float)
    # 最高价(元)
    S_DQ_HIGH = sqla.Column('S_DQ_HIGH', sqla.Float)
    # 最低价(元)
    S_DQ_LOW = sqla.Column('S_DQ_LOW', sqla.Float)
    # 收盘价(元)
    S_DQ_CLOSE = sqla.Column('S_DQ_CLOSE', sqla.Float)
    # 结算价(元)
    S_DQ_SETTLE = sqla.Column('S_DQ_SETTLE', sqla.Float)
    # 成交量(手)
    S_DQ_VOLUME = sqla.Column('S_DQ_VOLUME', sqla.Float)
    # 成交金额(万元)
    S_DQ_AMOUNT = sqla.Column('S_DQ_AMOUNT', sqla.Float)
    # 持仓量(手)
    S_DQ_OI = sqla.Column('S_DQ_OI', sqla.Float)
    # 涨跌(元)
    S_DQ_CHANGE = sqla.Column('S_DQ_CHANGE', sqla.Float)
    # 合约类型
    FS_INFO_TYPE = sqla.Column('FS_INFO_TYPE', sqla.String)
    #
    OPDATE = sqla.Column('OPDATE', sqla.DateTime)
    #
    OPMODE = sqla.Column('OPMODE', sqla.String)


engine = sqla.create_engine(OracleDBConfig.market_data_db_connection, echo=False)
# 初始化数据库
# OracleBaseModel.metadata.bind = engine
# OracleBaseModel.metadata.create_all()

Oracle_Session = sqlorm.scoped_session(sqlorm.sessionmaker(bind=engine))
