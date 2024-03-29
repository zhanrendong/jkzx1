import sqlalchemy as sqla
import sqlalchemy.orm as sqlorm
from dags.conf import RedisConfig
from terminal.dbo import BaseModel
from terminal.utils import RedisUtils
from dags.conf import DBConfig


class RealizedVol(BaseModel):
    __tablename__ = 'realized_vol'
    uuid = sqla.Column('uuid', sqla.VARCHAR, primary_key=True)
    instrumentId = sqla.Column('instrument_id', sqla.VARCHAR, primary_key=True)
    valuationDate = sqla.Column('valuation_date', sqla.Date, primary_key=True)
    vol = sqla.Column('vol', sqla.Float)
    exfsid = sqla.Column('exfsid', sqla.VARCHAR)
    windows = sqla.Column('windows', sqla.Integer)
    updatedAt = sqla.Column('update_at', sqla.TIMESTAMP)


class OtcOptionQuote(BaseModel):
    __tablename__ = 'otc_option_quote'
    uuid = sqla.Column('uuid', sqla.Unicode, primary_key=True)
    underlier = sqla.Column('underlier', sqla.String)
    company = sqla.Column('company', sqla.String)
    optionType = sqla.Column('option_type', sqla.String)
    productType = sqla.Column('product_type', sqla.String)
    exercisePrice = sqla.Column('exercise_price', sqla.Float)
    askVol = sqla.Column('ask_vol', sqla.String)
    bidVol = sqla.Column('bid_vol', sqla.String)
    askPrice = sqla.Column('ask_price', sqla.Float)
    bidPrice = sqla.Column('bid_price', sqla.Float)
    spotPrice = sqla.Column('spot_price', sqla.Float)
    observeDate = sqla.Column('observe_date', sqla.Date)
    term = sqla.Column('term', sqla.Integer)
    expireDate = sqla.Column('expire_date', sqla.Date)
    spiderRecord = sqla.Column('spider_record', sqla.String)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)

    def __str__(self):
        return '%s' % self.underlier


class QuoteCloseBreak(BaseModel):
    __tablename__ = 'quote_close_break'
    uuid = sqla.Column('uuid', sqla.Unicode, primary_key=True)
    instrumentType = sqla.Column('instrument_type', sqla.String)
    instrumentId = sqla.Column('instrument_id', sqla.String)
    tradeDate = sqla.Column('trade_date', sqla.Date)
    breakType = sqla.Column('break_type', sqla.String)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)
    reconFrom = sqla.Column('recon_from', sqla.String)

    def __str__(self):
        return '[%s, %s]' % (self.instrumentId, self.tradeDate)


class QuoteCloseMilestone(BaseModel):
    __tablename__ = 'quote_close_milestone'
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    startDate = sqla.Column('start_date', sqla.Date)
    endDate = sqla.Column('end_date', sqla.Date)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)

    def __str__(self):
        return '[%s, %s, %s, %s]' % (self.instrumentId, self.startDate,
                                     self.endDate, self.updatedAt)


class TickSnapshot(BaseModel):
    __tablename__ = 'tick_snapshot'
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    instrumentType = sqla.Column('instrument_type', sqla.String)
    tradeDate = sqla.Column('trade_date', sqla.Date, primary_key=True)
    responseData = sqla.Column('response_data', sqla.JSON)
    responseType = sqla.Column('response_type', sqla.String)
    url = sqla.Column('url', sqla.String)
    version = sqla.Column('version', sqla.String)
    data_source = sqla.Column('data_source', sqla.String)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)


class TickSnapshotBreak(BaseModel):
    __tablename__ = 'tick_snapshot_break'
    uuid = sqla.Column('uuid', sqla.Unicode, primary_key=True)
    instrumentType = sqla.Column('instrument_type', sqla.String)
    instrumentId = sqla.Column('instrument_id', sqla.String)
    tradeDate = sqla.Column('trade_date', sqla.Date)
    breakType = sqla.Column('break_type', sqla.String)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)
    reconFrom = sqla.Column('recon_from', sqla.String)

    def __str__(self):
        return '[%s, %s]' % (self.instrumentId, self.tradeDate)


class TickSnapshotMilestone(BaseModel):
    __tablename__ = 'tick_snapshot_milestone'
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    startDate = sqla.Column('start_date', sqla.Date)
    endDate = sqla.Column('end_date', sqla.Date)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)

    def __str__(self):
        return '[%s, %s, %s, %s]' % (self.instrumentId, self.startDate,
                                     self.endDate, self.updatedAt)


class OptionClose(BaseModel):
    __tablename__ = 'option_close'
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    tradeDate = sqla.Column('trade_date', sqla.Date, primary_key=True)
    closePrice = sqla.Column('close_price', sqla.Float)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)


class OptionCloseBreak(BaseModel):
    __tablename__ = 'option_close_break'
    uuid = sqla.Column('uuid', sqla.Unicode, primary_key=True)
    instrumentId = sqla.Column('instrument_id', sqla.String)
    breakType = sqla.Column('break_type', sqla.String)
    tradeDate = sqla.Column('trade_date', sqla.Date)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)


class OptionCloseMilestone(BaseModel):
    __tablename__ = 'option_close_milestone'
    instrumentId = sqla.Column('instrument_id', sqla.String, primary_key=True)
    startDate = sqla.Column('start_date', sqla.Date)
    endDate = sqla.Column('end_date', sqla.Date)
    updatedAt = sqla.Column('updated_at', sqla.DateTime)


class ROTCPosition(BaseModel):
    __tablename__ = 'r_otc_position'
    # 记录id
    RECORDID = sqla.Column('RECORDID', sqla.Unicode, primary_key=True)
    # 报表id
    REPORTID = sqla.Column('REPORTID', sqla.String)
    # 报送日期
    REPORTDATE = sqla.Column('REPORTDATE', sqla.String, primary_key=True)
    # 公司id
    COMPANYID = sqla.Column('COMPANYID', sqla.String)
    # 报告主体编码
    MAINBODYCODE = sqla.Column('MAINBODYCODE', sqla.String)
    # 报告主体公司id
    SUBID = sqla.Column('SUBID', sqla.String)
    # 报告主体名称
    MAINBODYNAME = sqla.Column('MAINBODYNAME', sqla.String)
    # 报告主体的客户类型
    CUSTOMERTYPE = sqla.Column('CUSTOMERTYPE', sqla.String)
    # 报告主体的统一社会信用代码
    NOCID = sqla.Column('NOCID', sqla.String)
    # 报告主体的统一开户编码
    FUTURESID = sqla.Column('FUTURESID', sqla.String)
    # 交易对手方编码
    ANALOGUECODE = sqla.Column('ANALOGUECODE', sqla.String)
    # 交易对手方名称
    ANALOGUENAME = sqla.Column('ANALOGUENAME', sqla.String)
    # 交易对手方的客户类型
    ANALOGUECUSTYPE = sqla.Column('ANALOGUECUSTYPE', sqla.String)
    # 交易对手方的统一社会信用代码
    ANALOGUENOCID = sqla.Column('ANALOGUENOCID', sqla.String)
    # 交易确认书编号
    TRANSCONFIRNUMBER = sqla.Column('TRANSCONFIRNUMBER', sqla.String)
    # 交易确认时间
    TRANSCONFIRTIME = sqla.Column('TRANSCONFIRTIME', sqla.String)
    # 持仓日期
    POSITIONDATE = sqla.Column('POSITIONDATE', sqla.String)
    # 交易对手方的统一开户编码
    ANALOGUEFUID = sqla.Column('ANALOGUEFUID', sqla.String)
    # 交易编码UTI
    TRANSCODE = sqla.Column('TRANSCODE', sqla.String)
    # 交易编码UTIUUID
    UTIID = sqla.Column('UTIID', sqla.String)
    # 资产类型
    ASSETTYPE = sqla.Column('ASSETTYPE', sqla.String)
    # 工具类型
    TOOLTYPE = sqla.Column('TOOLTYPE', sqla.String)
    # 期权行权时间类型
    OPTEXERCTMTYPE = sqla.Column('OPTEXERCTMTYPE', sqla.String)
    # 期权权利类型
    OPTRIGHTTYPE = sqla.Column('OPTRIGHTTYPE', sqla.String)
    # 产品结构
    RTVALMEPAY = sqla.Column('RTVALMEPAY', sqla.String)
    # 标的资产类型
    UNDERASSTYPE = sqla.Column('UNDERASSTYPE', sqla.String)
    # 标的资产品种
    UNDERASSVARIT = sqla.Column('UNDERASSVARIT', sqla.String)
    # 标的资产对应合约
    STANDASSCONT = sqla.Column('STANDASSCONT', sqla.String)
    # 合约价值
    CONTRACTVALUE = sqla.Column('CONTRACTVALUE', sqla.Float)
    # 估值方法
    VALMETHOD = sqla.Column('VALMETHOD', sqla.String)
    # 买持仓金额
    BUYPOSMONYE = sqla.Column('BUYPOSMONYE', sqla.Float)
    # 卖持仓金额
    SALEPOSMONYE = sqla.Column('SALEPOSMONYE', sqla.Float)
    # 计价货币
    MONEYACCOUNT = sqla.Column('MONEYACCOUNT', sqla.String)
    # 买持仓数量
    BUYPOSAMOUNT = sqla.Column('BUYPOSAMOUNT', sqla.Float)
    # 卖持仓数量
    SALEPOSAMOUNT = sqla.Column('SALEPOSAMOUNT', sqla.Float)
    # 数量单位
    QUANTITYUNIT = sqla.Column('QUANTITYUNIT', sqla.String)
    # 总持仓金额
    TOTPOSMONYE = sqla.Column('TOTPOSMONYE', sqla.Float)
    # 净持仓金额
    NETPOSMONYE = sqla.Column('NETPOSMONYE', sqla.Float)
    # 总持仓数量
    TOPOSAMOUNT = sqla.Column('TOPOSAMOUNT', sqla.Float)
    # 净持仓数量
    NETPOSAMOUNT = sqla.Column('NETPOSAMOUNT', sqla.Float)
    # 状态
    STATUS = sqla.Column('STATUS', sqla.String)
    # 多腿编号
    LEGID = sqla.Column('LEGID', sqla.String)
    # 填报方方向
    SIDE = sqla.Column('SIDE', sqla.String)
    # 参与率
    PARTICIPATERATE = sqla.Column('PARTICIPATERATE', sqla.Float)
    # 是否为年化期权
    ANNUALIZED = sqla.Column('ANNUALIZED', sqla.String)
    # 执行价格
    STRIKE = sqla.Column('STRIKE', sqla.Float)
    # 合约估值时标的价格
    VALUATIONSPOT = sqla.Column('VALUATIONSPOT', sqla.String)
    # 总名义金额
    TRADENOTIONAL = sqla.Column('TRADENOTIONAL', sqla.String)
    # 已平仓总名义金额
    CLOSEDNOTIONAL = sqla.Column('CLOSEDNOTIONAL', sqla.String)
    # 价格符号
    PRICESYMBOL = sqla.Column('PRICESYMBOL', sqla.String)
    # 外汇对人民币汇率
    EXCHANGERATE = sqla.Column('EXCHANGERATE', sqla.Float)
    # 总名义数量
    TRADEQAUNTITY = sqla.Column('TRADEQAUNTITY', sqla.String)
    # 已平仓总名义数量
    CLOSEDQUANTITY = sqla.Column('CLOSEDQUANTITY', sqla.String)
    # 估值波动率
    IMPLIEDVOL = sqla.Column('IMPLIEDVOL', sqla.String)
    # 一年有效天数
    EFFECTIVEDAY = sqla.Column('EFFECTIVEDAY', sqla.Float)
    # DELTA
    DELTA = sqla.Column('DELTA', sqla.String)
    # GAMMA
    GAMMA = sqla.Column('GAMMA', sqla.String)
    # VEGA
    VEGA = sqla.Column('VEGA', sqla.String)
    # THETA
    THETA = sqla.Column('THETA', sqla.String)
    # RHO
    RHO = sqla.Column('RHO', sqla.String)
    # DELTACASH
    DELTACASH = sqla.Column('DELTACASH', sqla.String)
    # 无风险利率
    INTERESTRATE = sqla.Column('INTERESTRATE', sqla.Float)
    # 贴现率
    DIVIDEND = sqla.Column('DIVIDEND', sqla.Float)
    UPDATEAT = sqla.Column('UPDATEAT', sqla.DateTime)


class ROTCTradeData(BaseModel):
    __tablename__ = 'r_otc_tradedata'
    # 记录id
    RECORDID = sqla.Column('RECORDID', sqla.Unicode, primary_key=True)
    # 报表id
    REPORTID = sqla.Column('REPORTID', sqla.String)
    # 报送日期
    REPORTDATE = sqla.Column('REPORTDATE', sqla.String, primary_key=True)
    # 公司id
    COMPANYID = sqla.Column('COMPANYID', sqla.String)
    # 报告主体编码
    MAINBODYCODE = sqla.Column('MAINBODYCODE', sqla.String)
    # 报告主体公司id
    SUBID = sqla.Column('SUBID', sqla.String)
    # 报告主体公司(子公司名称)
    MAINBODYNAME = sqla.Column('MAINBODYNAME', sqla.String)
    # 报告主体的统一社会信用代码
    NOCID = sqla.Column('NOCID', sqla.String)
    # 报告主体的客户类型
    CUSTOMERTYPE = sqla.Column('CUSTOMERTYPE', sqla.String)
    # 报告主体的统一开户编码
    FUTURESID = sqla.Column('FUTURESID', sqla.String)
    # 交易对手方编码
    ANALOGUECODE = sqla.Column('ANALOGUECODE', sqla.String)
    # 交易对手方名称
    ANALOGUENAME = sqla.Column('ANALOGUENAME', sqla.String)
    # 交易对手方的统一社会信用代码
    ANALOGUENOCID = sqla.Column('ANALOGUENOCID', sqla.String)
    # 交易对手方的客户类型
    ANALOGUECUSTYPE = sqla.Column('ANALOGUECUSTYPE', sqla.String)
    # 交易对手方的统一开户编码
    ANALOGUEFUID = sqla.Column('ANALOGUEFUID', sqla.String)
    # 主协议类型
    MAINPROTTYPE = sqla.Column('MAINPROTTYPE', sqla.String)
    # 主协议日期
    MAINPROTDATE = sqla.Column('MAINPROTDATE', sqla.String)
    # 是否有授信
    ISCREDIT = sqla.Column('ISCREDIT', sqla.String)
    # 授信额度
    CREDITLINE = sqla.Column('CREDITLINE', sqla.Float)
    # 初始保证金要求
    INITMARGINREQ = sqla.Column('INITMARGINREQ', sqla.Float)
    # 维持保证金
    MAINTAINMARGIN = sqla.Column('MAINTAINMARGIN', sqla.Float)
    # 操作类型
    OPERTYPE = sqla.Column('OPERTYPE', sqla.String)
    # 交易编码UTI
    TRANSCODE = sqla.Column('TRANSCODE', sqla.String)
    # 交易编码UTIUUID
    UTIID = sqla.Column('UTIID', sqla.String)
    # 交易确认书编号
    TRANSCONFIRNUMBER = sqla.Column('TRANSCONFIRNUMBER', sqla.String)
    # 交易确认时间
    TRANSCONFIRTIME = sqla.Column('TRANSCONFIRTIME', sqla.String)
    # 生效日
    EFFECTDATE = sqla.Column('EFFECTDATE', sqla.String)
    # 到期日
    EXPIREDATE = sqla.Column('EXPIREDATE', sqla.String)
    # 行权日
    EXERCISEDATE = sqla.Column('EXERCISEDATE', sqla.String)
    # 提前终止日期
    EARLYTERMDATE = sqla.Column('EARLYTERMDATE', sqla.String)
    # 标的物信息（UPI）
    SUBJMATTERINFO = sqla.Column('SUBJMATTERINFO', sqla.String)
    # 填报方方向
    DIRECTREPPARTY = sqla.Column('DIRECTREPPARTY', sqla.String)
    # 资产类型
    ASSETTYPE = sqla.Column('ASSETTYPE', sqla.String)
    # 工具类型
    TOOLTYPE = sqla.Column('TOOLTYPE', sqla.String)
    # 期权行权时间类型
    OPTEXERCTMTYPE = sqla.Column('OPTEXERCTMTYPE', sqla.String)
    # 期权权利类型
    OPTRIGHTTYPE = sqla.Column('OPTRIGHTTYPE', sqla.String)
    # 产品结构
    RTVALMEPAY = sqla.Column('RTVALMEPAY', sqla.String)
    # 标的资产类型
    UNDERASSTYPE = sqla.Column('UNDERASSTYPE', sqla.String)
    # 标的资产品种
    UNDERASSVARIT = sqla.Column('UNDERASSVARIT', sqla.String)
    # 标的资产对应合约
    STANDASSCONT = sqla.Column('STANDASSCONT', sqla.String)
    # 标的资产交易场所
    STANDASSTRADPLC = sqla.Column('STANDASSTRADPLC', sqla.String)
    # 总名义数量
    GENERALNAMNUM = sqla.Column('GENERALNAMNUM', sqla.String)
    # 数量单位
    VALUUNIT = sqla.Column('VALUUNIT', sqla.String)
    # 执行价格
    EXECUTPRICE = sqla.Column('EXECUTPRICE', sqla.Float)
    # 标的资产进场价格
    ASSENTRYPRICE = sqla.Column('ASSENTRYPRICE', sqla.String)
    # 价格符号
    PRICESYMBOL = sqla.Column('PRICESYMBOL', sqla.String)
    # 计价货币
    ACCOUNTMONEY = sqla.Column('ACCOUNTMONEY', sqla.String)
    # 总名义金额
    NOMINALAMOUNT = sqla.Column('NOMINALAMOUNT', sqla.String)
    # 期权费金额
    PREMIUMAMOUNT = sqla.Column('PREMIUMAMOUNT', sqla.Float)
    # 合约价值
    CONTRACTVALUE = sqla.Column('CONTRACTVALUE', sqla.Float)
    # 估值方法
    VALMETHOD = sqla.Column('VALMETHOD', sqla.String)
    # 结算方式
    SETTMETHOD = sqla.Column('SETTMETHOD', sqla.String)
    # 最后结算日
    FINALSETTDAY = sqla.Column('FINALSETTDAY', sqla.String)
    # 结算价确认方式
    SETTPRIMETHED = sqla.Column('SETTPRIMETHED', sqla.String)
    # 一口价价格
    ONEPRICE = sqla.Column('ONEPRICE', sqla.Float)
    # 商品参考价格
    COMMREFPRICE = sqla.Column('COMMREFPRICE', sqla.Float)
    # 状态
    STATUS = sqla.Column('STATUS', sqla.String)
    # 多腿编号
    LEGID = sqla.Column('LEGID', sqla.String)
    # 交易名义数量
    TRADEQAUNTITY = sqla.Column('TRADEQAUNTITY', sqla.String)
    # 参与率
    PARTICIPATIONRATE = sqla.Column('PARTICIPATIONRATE', sqla.Float)
    # 是否为年化期权
    ANNUALIZED = sqla.Column('ANNUALIZED', sqla.String)
    # 外汇对人民币汇率
    EXCHANGERATE = sqla.Column('EXCHANGERATE', sqla.Float)
    # 交易名义金额
    TRADENOTIONAL = sqla.Column('TRADENOTIONAL', sqla.String)
    UPDATEAT = sqla.Column('UPDATEAT', sqla.DateTime)


# 初始化数据库
engine = sqla.create_engine(DBConfig.db_connection,
                            connect_args={'options': '-csearch_path={}'.format(DBConfig.default_schema)},
                            echo=DBConfig.show_sql)
BaseModel.metadata.bind = engine
BaseModel.metadata.create_all()
create_db_session = sqlorm.scoped_session(sqlorm.sessionmaker(bind=engine))

# 初始化Redis
RedisUtils.set_redis_params((RedisConfig.redis_host, RedisConfig.redis_port))
create_redis_session = RedisUtils.get_redis_session
