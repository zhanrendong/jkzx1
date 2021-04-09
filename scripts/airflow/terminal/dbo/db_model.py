import sqlalchemy as sqla
from sqlalchemy import Column, Unicode, String, Float, DECIMAL
from sqlalchemy.ext.declarative import declarative_base as sqla_declarative_base

BaseModel = sqla_declarative_base()


class OtcAtmQuote(BaseModel):
    __tablename__ = 'otc_atm_quote'
    uuid = sqla.Column('uuid', sqla.Unicode, primary_key=True)
    varietyType = sqla.Column('variety_type', sqla.String)
    underlyer = sqla.Column('underlyer', sqla.String)
    valuationDate = sqla.Column('valuation_date', sqla.Date)
    optionType = sqla.Column('option_type', sqla.String)
    productType = sqla.Column('product_type', sqla.String)
    expireDate = sqla.Column('expire_date', sqla.Date)
    source = sqla.Column('source', sqla.String)
    legalEntityName = sqla.Column('legal_entity_name', sqla.String)
    askVol = sqla.Column('ask_vol', sqla.Float)
    bidVol = sqla.Column('bid_vol', sqla.Float)
    volEdge = sqla.Column('vol_edge', sqla.Float)
    ptm = sqla.Column('ptm', sqla.Float)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)


class Instrument(BaseModel):
    __tablename__ = 'instrument'
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    instrumentType = sqla.Column('instrument_type', sqla.String)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)
    listedDate = sqla.Column('listed_date', sqla.Date)
    delistedDate = sqla.Column('delisted_date', sqla.Date)
    # TODO: 默认数据源为TONGLIAN
    assetClass = sqla.Column('asset_class', sqla.String)
    dataSource = sqla.Column('data_source', sqla.String)
    status = sqla.Column('status', sqla.String)
    shortName = sqla.Column('short_name', sqla.String)
    # 对应的期权是场内还是场外期权
    optionTradedType = sqla.Column('option_traded_type', sqla.String)
    # 大宗商品对应的主力合约类型
    contractType = sqla.Column('contract_type', sqla.String)

    def __str__(self):
        return '[%s, %s, %s]' % (self.instrumentId, self.instrumentType, self.status)


class InstrumentCalendar(BaseModel):
    __tablename__ = 'instrument_calendar'
    uuid = sqla.Column('uuid', sqla.String, primary_key=True)
    instrumentId = sqla.Column('instrument_id', sqla.String)
    specialDate = sqla.Column('special_date', sqla.Date)
    comment = sqla.Column('comment', sqla.String)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)


class QuoteClose(BaseModel):
    __tablename__ = 'quote_close'
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    tradeDate = sqla.Column('trade_date', sqla.Date, primary_key=True)
    openPrice = sqla.Column('open_price', sqla.Float)
    closePrice = sqla.Column('close_price', sqla.Float)
    highPrice = sqla.Column('high_price', sqla.Float)
    lowPrice = sqla.Column('low_price', sqla.Float)
    settlePrice = sqla.Column('settle_price', sqla.Float)
    dataSource = sqla.Column('data_source', sqla.String)
    # 交易量
    volume = sqla.Column('volume', sqla.Float)
    # 交易金额
    amount = sqla.Column('amount', sqla.Float)
    # 前一天的收盘价
    preClosePrice = sqla.Column('pre_close_price', sqla.Float)
    # 涨跌幅
    returnRate = sqla.Column('return_rate', sqla.Float)
    # 报价时间戳
    quoteTime = sqla.Column('quote_time', sqla.TIMESTAMP)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)


class FutureContractInfo(BaseModel):
    __tablename__ = 'future_contract_info'
    contractType = sqla.Column('contract_type', sqla.String, primary_key=True)
    tradeDate = sqla.Column('trade_date', sqla.Date, primary_key=True)
    primaryContractId = sqla.Column('primary_contract_id', sqla.String)
    secondaryContractId = sqla.Column('secondary_contract_id', sqla.String)


class TradingCalendar(BaseModel):
    __tablename__ = 'trading_calendar'
    uuid = sqla.Column('uuid', sqla.Unicode, primary_key=True)
    name = sqla.Column('name', sqla.String)
    holiday = sqla.Column('holiday', sqla.Date)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)


class OptionStructure(BaseModel):
    __tablename__ = 'option_structure'
    # 基础信息
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    shortName = sqla.Column('short_name', sqla.String)
    listedDate = sqla.Column('listed_date', sqla.Date)
    delistedDate = sqla.Column('delisted_date', sqla.Date)
    assetClass = sqla.Column('asset_class', sqla.String)
    instrumentType = sqla.Column('instrument_type', sqla.String)

    tradeCode = sqla.Column('trade_code', sqla.String)
    fullName = sqla.Column('full_name', sqla.String)
    underlier = sqla.Column('underlier', sqla.String)
    contractType = sqla.Column('contract_type', sqla.String)
    optionType = sqla.Column('option_type', sqla.String)
    exerciseType = sqla.Column('exercise_type', sqla.String)
    exercisePrice = sqla.Column('exercise_price', sqla.String)
    contractUnit = sqla.Column('contract_unit', sqla.String)
    limitMonth = sqla.Column('limit_month', sqla.String)
    expireDate = sqla.Column('expire_date', sqla.Date)
    exerciseDate = sqla.Column('exercise_date', sqla.Date)
    settlementDate = sqla.Column('settlement_date', sqla.Date)
    referencePrice = sqla.Column('reference_price', sqla.String)
    settleMode = sqla.Column('settle_mode', sqla.String)
    contractState = sqla.Column('contract_state', sqla.String)


class FutureStructure(BaseModel):
    __tablename__ = 'future_structure'
    # 基础信息
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    shortName = sqla.Column('short_name', sqla.String)
    listedDate = sqla.Column('listed_date', sqla.Date)
    delistedDate = sqla.Column('delisted_date', sqla.Date)
    assetClass = sqla.Column('asset_class', sqla.String)
    instrumentType = sqla.Column('instrument_type', sqla.String)

    firstDeliDate = sqla.Column('first_deli_date', sqla.Date)
    lastDeliDate = sqla.Column('last_deli_date', sqla.Date)
    contMultNum = sqla.Column('cont_mult_num', sqla.Integer)
    contMultUnit = sqla.Column('cont_mult_unit', sqla.String)
    contractType = sqla.Column('contract_type', sqla.String)


class StockStructure(BaseModel):
    __tablename__ = 'stock_structure'
    # 基础信息
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    shortName = sqla.Column('short_name', sqla.String)
    listedDate = sqla.Column('listed_date', sqla.Date)
    delistedDate = sqla.Column('delisted_date', sqla.Date)
    assetClass = sqla.Column('asset_class', sqla.String)
    instrumentType = sqla.Column('instrument_type', sqla.String)


class FundStructure(BaseModel):
    __tablename__ = 'fund_structure'
    # 基础信息
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    shortName = sqla.Column('short_name', sqla.String)
    listedDate = sqla.Column('listed_date', sqla.Date)
    delistedDate = sqla.Column('delisted_date', sqla.Date)
    assetClass = sqla.Column('asset_class', sqla.String)
    instrumentType = sqla.Column('instrument_type', sqla.String)


class IndexStructure(BaseModel):
    __tablename__ = 'index_structure'
    # 基础信息
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    shortName = sqla.Column('short_name', sqla.String)
    listedDate = sqla.Column('listed_date', sqla.Date)
    delistedDate = sqla.Column('delisted_date', sqla.Date)
    assetClass = sqla.Column('asset_class', sqla.String)
    instrumentType = sqla.Column('instrument_type', sqla.String)


class VolSurface(BaseModel):
    __tablename__ = 'vol_surface'
    uuid = sqla.Column('uuid', sqla.Unicode, primary_key=True)
    instrumentId = sqla.Column('instrument_id', sqla.String)
    valuationDate = sqla.Column('valuation_date', sqla.Date)
    instance = sqla.Column('instance', sqla.String)
    modelInfo = sqla.Column('model_info', sqla.JSON)
    fittingModels = sqla.Column('fitting_model', sqla.JSON)
    strikeType = sqla.Column('strike_type', sqla.String)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)
    tag = sqla.Column('tag', sqla.String)
    source = sqla.Column('source', sqla.String)


class OTCPositionSnapshot(BaseModel):
    __tablename__ = 'otc_position_snapshot'
    transCode = sqla.Column('trans_code', sqla.Unicode, primary_key=True)
    recordId = sqla.Column('record_id', sqla.String)
    # 报送日期
    reportDate = sqla.Column('report_date', sqla.Date, primary_key=True)
    # 持仓日期
    positionDate = sqla.Column('position_date', sqla.Date)
    # 标的资产对应合约
    instrumentId = sqla.Column('instrument_id', sqla.String)
    # 报告主体名称
    mainBodyName = sqla.Column('main_body_name', sqla.String)
    # 总名义本金
    tradeNotional = sqla.Column('trade_notional', sqla.Float)
    # 估值波动率
    impliedVol = sqla.Column('implied_vol', sqla.Float)
    # 无风险利率
    interestRate = sqla.Column('interest_rate', sqla.Float)
    # 贴现率
    dividend = sqla.Column('dividend', sqla.Float)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)


# 市场规模
class OTCSummaryReport(BaseModel):
    __tablename__ = 'R_OTC_REPORT_SUMMARY'
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
    __tablename__ = 'R_OTC_REPORT_TRADE_SUMMARY'
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
    __tablename__ = 'R_OTC_REPORT_POSITION_SUMMARY'
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
    __tablename__ = 'R_OTC_REPORT_ASSET_TOOL'
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
    __tablename__ = 'R_OTC_REPORT_CUS_TYPE'
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
    __tablename__ = 'R_OTC_REPORT_MARKET_DIST'
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
    __tablename__ = 'R_OTC_REPORT_OTC_ET_COMMODITY'
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
    __tablename__ = 'R_OTC_REPORT_OTC_ET_CUS'
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    ETACCOUNTCUSNUM = Column(Float)
    OTCCUSPOSAMOUNT = Column(Float)
    ETCUSPOSAMOUNT = Column(DECIMAL)
    ETCUSRIGHT = Column(DECIMAL)


# 子公司联动
class OTCEtSubCompanyReport(BaseModel):
    __tablename__ = 'R_OTC_REPORT_OTC_ET_SUBCOMPANY'
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    COMMODITYID = Column(String)
    MAINBODYNAME = Column(String)
    OTCSUBPOSAMOUNT = Column(Float)
    ETSUBPOSAMOUNT = Column(DECIMAL)


# 操纵风险
class OTCMarketManipulateReport(BaseModel):
    __tablename__ = 'R_OTC_REPORT_MARKET_MANIPULATE'
    UUID = Column(Unicode, primary_key=True)
    STATDATE = Column(String)
    INTERCOMPCUSNUM = Column(Float)
    INTERCOMPTRD = Column(Float)
    INTERCOMPPOS = Column(Float)


# 子公司传染风险
class OTCCompPropagateReport(BaseModel):
    __tablename__ = 'R_OTC_REPORT_COMP_PROPAGATE'
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
    __tablename__ = 'R_OTC_REPORT_CUS_POS_PERCENTAGE'
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