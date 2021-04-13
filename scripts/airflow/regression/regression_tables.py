from eod_pd import EOD_BASIC_SUB_COMPANIES_FROM_ORACLE, EOD_BASIC_INSTRUMENT_CONTRACT_TYPE, EOD_BASIC_POSITIONS, \
    EOD_BASIC_POSITION_MAP
from regression.RegressionResult import RegressionDBResult, RegressionRedisResult, RegressionRedisListResult, \
    RegressionRedisDictResult, RegressionRedisDictListResult

bct_trading_holiday = RegressionDBResult(
    db_name='bct',
    name='reference_data_service.trading_holiday',
    keys=['calendar_id', 'holiday'],
    values=['note'])

bct_vol_special_date = RegressionDBResult(
    db_name='bct',
    name='reference_data_service.vol_special_date',
    keys=['calendar_id', 'special_date'],
    values=['note', 'weight'])

terminal_trading_calendar = RegressionDBResult(
    db_name='terminal_data',
    name='market_data.trading_calendar',
    keys=['name', 'holiday'],
    values=[])

terminal_instrument = RegressionDBResult(
    db_name='terminal_data',
    name='market_data.instrument',
    keys=['instrument_id'],
    values=['instrument_type', 'listed_date', 'delisted_date', 'asset_class', 'data_source', 'status',
            'short_name', 'option_traded_type', 'contract_type'])

terminal_quote = RegressionDBResult(
    db_name='terminal_data',
    name='market_data.quote_close',
    keys=['instrument_id', 'trade_date'],
    values=['open_price', 'close_price', 'high_price', 'low_price', 'settle_price', 'volume',
            'amount', 'pre_close_price', 'return_rate', 'data_source'],
    roundings={
        'open_price': 2, 'close_price': 2, 'high_price': 2, 'low_price': 2, 'settle_price': 2, 'volume': 2,
        'amount': 2, 'pre_close_price': 2, 'return_rate': 2})

bct_instrument = RegressionDBResult(
    db_name='bct',
    name='market_data_service.instrument',
    keys=['instrument_id'],
    values=['asset_class', 'asset_sub_class', 'instrument_info', 'instrument_type'])

bct_quote = RegressionDBResult(
    db_name='bct',
    name='market_data_service.quote_close',
    keys=['instrument_id', 'valuation_date'],
    values=['close', 'high', 'low', 'open', 'settle'],
    roundings={'close': 2, 'high': 2, 'low': 2, 'open': 2, 'settle': 2})

bct_model = RegressionDBResult(
    db_name='bct',
    name='model_service.model_data',
    keys=['model_id'],
    values=['instance', 'model_data', 'model_info', 'model_name', 'model_type', 'underlyer', 'valuation_date'])

bct_auth_resource = RegressionDBResult(
    db_name='bct',
    name='auth_service.auth_resource',
    keys=['resource_name', 'resource_type'],
    values=['sort'])

bct_party = RegressionDBResult(
    db_name='bct',
    name='reference_data_service.party',
    keys=['legal_name'],
    values=['warrantor_address', 'warrantor', 'subsidiary_name', 'sales_name', 'party_status', 'master_agreement_id',
            'legal_representative', 'contact', 'client_type', 'branch_name'])

bct_trade_index = RegressionDBResult(
    db_name='bct',
    name='trade_service.trade_position_index',
    keys=['trade_id', 'position_id'],
    values=['book_name', 'counter_party_name', 'effective_date', 'expiration_date', 'instrument_id', 'lcm_event_type',
            'product_type', 'sales_name', 'trade_date'])

bct_trade = RegressionDBResult(
    db_name='bct',
    name='trade_snapshot_model.trade',
    keys=['trade_id'],
    values=['book_name', 'comment', 'trader', 'trade_status', 'trade_date'])

bct_position = RegressionDBResult(
    db_name='bct',
    name='trade_snapshot_model.position',
    keys=['position_id'],
    values=['book_name', 'asset', 'counterparty', 'counterparty_account', 'lcm_event_type', 'position_account',
            'quantity'])

terminal_otc_position = RegressionDBResult(
    db_name='terminal_data',
    name='market_data.r_otc_position',
    keys=['"RECORDID"'],
    values=['"REPORTID"', '"REPORTDATE"', '"COMPANYID"', '"MAINBODYCODE"', '"SUBID"', '"MAINBODYNAME"',
            '"CUSTOMERTYPE"', '"NOCID"', '"FUTURESID"', '"ANALOGUECODE"', '"ANALOGUENAME"', '"ANALOGUECUSTYPE"',
            '"ANALOGUENOCID"', '"TRANSCONFIRNUMBER"', '"TRANSCONFIRTIME"', '"POSITIONDATE"', '"ANALOGUEFUID"',
            '"TRANSCODE"', '"UTIID"', '"ASSETTYPE"', '"TOOLTYPE"',
            '"OPTEXERCTMTYPE"', '"OPTRIGHTTYPE"', '"RTVALMEPAY"', '"UNDERASSTYPE"', '"UNDERASSVARIT"', '"STANDASSCONT"',
            '"CONTRACTVALUE"', '"VALMETHOD"', '"BUYPOSMONYE"', '"SALEPOSMONYE"', '"MONEYACCOUNT"', '"BUYPOSAMOUNT"',
            '"SALEPOSAMOUNT"', '"QUANTITYUNIT"', '"TOTPOSMONYE"', '"NETPOSMONYE"', '"TOPOSAMOUNT"', '"NETPOSAMOUNT"',
            '"STATUS"', '"LEGID"', '"SIDE"', '"PARTICIPATERATE"', '"ANNUALIZED"', '"STRIKE"', '"VALUATIONSPOT"',
            '"TRADENOTIONAL"', '"CLOSEDNOTIONAL"', '"PRICESYMBOL"', '"EXCHANGERATE"', '"TRADEQAUNTITY"',
            '"CLOSEDQUANTITY"', '"IMPLIEDVOL"', '"EFFECTIVEDAY"', '"DELTA"', '"GAMMA"', '"VEGA"', '"THETA"', '"RHO"',
            '"DELTACASH"', '"INTERESTRATE"', '"DIVIDEND"'],
    roundings={'STRIKE': 2, 'CONTRACTVALUE': 2, 'BUYPOSMONYE': 2, 'SALEPOSMONYE': 2, 'BUYPOSAMOUNT': 2,
               'SALEPOSAMOUNT': 2, 'TOTPOSMONYE': 2, 'NETPOSMONYE': 2, 'TOPOSAMOUNT': 2, 'NETPOSAMOUNT': 2,
               'PARTICIPATERATE': 2, 'EXCHANGERATE': 2, 'EFFECTIVEDAY': 2, 'INTERESTRATE': 2, 'DIVIDEND': 2}
)

terminal_otc_trade = RegressionDBResult(
    db_name='terminal_data',
    name='market_data.r_otc_tradedata',
    keys=['"RECORDID"'],
    values=['"REPORTID"', '"REPORTDATE"', '"COMPANYID"', '"MAINBODYCODE"', '"SUBID"', '"MAINBODYNAME"', '"NOCID"',
            '"CUSTOMERTYPE"', '"FUTURESID"', '"ANALOGUECODE"', '"ANALOGUENAME"', '"ANALOGUENOCID"', '"ANALOGUECUSTYPE"',
            '"ANALOGUEFUID"', '"MAINPROTTYPE"', '"MAINPROTDATE"', '"ISCREDIT"', '"CREDITLINE"', '"INITMARGINREQ"',
            '"MAINTAINMARGIN"', '"OPERTYPE"', '"TRANSCODE"', '"UTIID"', '"TRANSCONFIRNUMBER"', '"TRANSCONFIRTIME"',
            '"EFFECTDATE"', '"EXPIREDATE"', '"EXERCISEDATE"', '"EARLYTERMDATE"', '"SUBJMATTERINFO"', '"DIRECTREPPARTY"',
            '"ASSETTYPE"', '"TOOLTYPE"', '"OPTEXERCTMTYPE"', '"OPTRIGHTTYPE"', '"RTVALMEPAY"', '"UNDERASSTYPE"',
            '"UNDERASSVARIT"', '"STANDASSCONT"', '"STANDASSTRADPLC"', '"GENERALNAMNUM"', '"VALUUNIT"', '"EXECUTPRICE"',
            '"ASSENTRYPRICE"', '"PRICESYMBOL"', '"ACCOUNTMONEY"', '"NOMINALAMOUNT"', '"PREMIUMAMOUNT"',
            '"CONTRACTVALUE"', '"VALMETHOD"', '"SETTMETHOD"', '"FINALSETTDAY"', '"SETTPRIMETHED"', '"ONEPRICE"',
            '"COMMREFPRICE"', '"STATUS"', '"LEGID"', '"TRADEQAUNTITY"', '"PARTICIPATIONRATE"', '"ANNUALIZED"',
            '"EXCHANGERATE"', '"TRADENOTIONAL"'],
    roundings={'CREDITLINE': 2, 'INITMARGINREQ': 2, 'MAINTAINMARGIN': 2, 'EXECUTPRICE': 2, 'PREMIUMAMOUNT': 2,
               'CONTRACTVALUE': 2, 'ONEPRICE': 2, 'COMMREFPRICE': 2, 'PARTICIPATIONRATE': 2, 'EXCHANGERATE': 2}
)

terminal_otc_pos_snapshot = RegressionDBResult(
    db_name='terminal_data',
    name='market_data.otc_position_snapshot',
    keys=['trans_code', 'report_date'],
    values=['instrument_id', 'main_body_name', 'trade_notional', 'implied_vol', 'interest_rate', 'dividend',
            'position_date'],
    roundings={'trade_notional': 2, 'implied_vol': 2, 'interest_rate': 2, 'dividend': 2}
)

bct_subcompany_names = RegressionRedisListResult(
    name=EOD_BASIC_SUB_COMPANIES_FROM_ORACLE,
    keys=[RegressionRedisListResult.LIST_KEY],
    values=[RegressionRedisListResult.LIST_VALUE]
)

bct_instrument_type = RegressionRedisDictResult(
    name=EOD_BASIC_INSTRUMENT_CONTRACT_TYPE,
    keys=[RegressionRedisDictResult.DICT_KEY],
    values=[RegressionRedisDictResult.DICT_VALUE]
)

bct_otc_positions = RegressionRedisDictListResult(
    name=EOD_BASIC_POSITIONS,
    keys=['positionId'],
    values=['asset.annValRatio', 'asset.annualized', 'asset.annualizedActualNotionalAmount',
            'asset.annualizedActualNotionalAmountByLot', 'asset.annualizedActualPremium', 'asset.counterpartyCode',
            'asset.daysInYear', 'asset.direction', 'asset.effectiveDate', 'asset.exerciseType', 'asset.expirationDate',
            'asset.frontPremium', 'asset.initialSpot', 'asset.minimumPremium', 'asset.notionalAmount',
            'asset.notionalAmountType', 'asset.optionType', 'asset.participationRate', 'asset.premium',
            'asset.premiumType', 'asset.settlementDate', 'asset.specifiedPrice', 'asset.strike', 'asset.strikeType',
            'asset.term', 'asset.underlyerInstrumentId', 'asset.underlyerMultiplier', 'assetClass', 'bookName',
            'comment', 'counterPartyAccountCode', 'counterPartyAccountName', 'counterPartyCode', 'counterPartyName',
            'lcmEventType', 'partyCode', 'partyName', 'portfolioNames', 'positionAccountCode', 'positionAccountName',
            'productType', 'quantity', 'salesCode', 'salesCommission', 'salesName', 'tradeDate', 'tradeId',
            'tradeStatus', 'trader', 'asset.actualNotional', 'actualPremium'],
    roundings={
        'asset.annualizedActualNotionalAmount': 4, 'asset.annualizedActualNotionalAmountByLot': 4,
        'asset.annualizedActualPremium': 4, 'asset.daysInYear': 0, 'asset.frontPremium': 4, 'asset.initialSpot': 4,
        'asset.minimumPremium': 4, 'asset.notionalAmount': 4, 'asset.participationRate': 4, 'asset.premium': 4,
        'asset.strike': 4, 'asset.term': 0, 'asset.underlyerMultiplier': 0, 'quantity': 4, 'asset.actualNotional': 4,
        'actualPremium': 4
    }
)

bct_otc_position_map = RegressionRedisDictListResult(
    name=EOD_BASIC_POSITION_MAP,
    keys=['tradeId'],
    values=['bookName', 'asset.underlyerInstrumentId', 'asset.underlyerMultiplier', 'productType'],
    roundings={'asset.underlyerMultiplier': 0}
)

future_contract_info = RegressionResultTable(
    db_name='terminal_data',
    name='market_data.future_contract_info',
    keys=['contract_type', 'trade_date'],
    values=['primary_contract_id', 'secondary_contract_id'],
)

terminal_realized_vol = RegressionResultTable(
    db_name='terminal_data',
    name='market_data.realized_vol',
    keys=['instrument_id', 'valuation_date'],
    values=['vol', 'exfsid', 'windows'],
    roundings={'vol': 2}
)

vol_surface = RegressionResultTable(
    db_name='terminal_data',
    name='market_data.vol_surface',
    keys=['instrument_id', 'valuation_date', 'instance'],
    values=['model_info', 'fitting_model', 'strike_type', 'tag', 'tag'],
)
