from regression.RegressionTestCase import RegressionResultTable

bct_trading_holiday = RegressionResultTable(
    db_name='bct',
    name='reference_data_service.trading_holiday',
    keys=['calendar_id', 'holiday'],
    values=['note'])

bct_vol_special_date = RegressionResultTable(
    db_name='bct',
    name='reference_data_service.vol_special_date',
    keys=['calendar_id', 'special_date'],
    values=['note', 'weight'])

terminal_trading_calendar = RegressionResultTable(
    db_name='terminal_data',
    name='market_data.trading_calendar',
    keys=['name', 'holiday'],
    values=[])

terminal_instrument = RegressionResultTable(
    db_name='terminal_data',
    name='market_data.instrument',
    keys=['instrument_id'],
    values=['instrument_type', 'listed_date', 'delisted_date', 'asset_class', 'data_source', 'status',
            'short_name', 'option_traded_type', 'contract_type'])

terminal_quote = RegressionResultTable(
    db_name='terminal_data',
    name='market_data.quote_close',
    keys=['instrument_id', 'trade_date'],
    values=['open_price', 'close_price', 'high_price', 'low_price', 'settle_price', 'volume',
            'amount', 'pre_close_price', 'return_rate', 'data_source'],
    roundings={
        'open_price': 2, 'close_price': 2, 'high_price': 2, 'low_price': 2, 'settle_price': 2, 'volume': 2,
        'amount': 2, 'pre_close_price': 2, 'return_rate': 2})

bct_instrument = RegressionResultTable(
    db_name='bct',
    name='market_data_service.instrument',
    keys=['instrument_id'],
    values=['asset_class', 'asset_sub_class', 'instrument_info', 'instrument_type'])

bct_quote = RegressionResultTable(
    db_name='bct',
    name='market_data_service.quote_close',
    keys=['instrument_id', 'valuation_date'],
    values=['close', 'high', 'low', 'open', 'settle'],
    roundings={'close': 2, 'high': 2, 'low': 2, 'open': 2, 'settle': 2})

bct_model = RegressionResultTable(
    db_name='bct',
    name='model_service.model_data',
    keys=['model_id'],
    values=['instance', 'model_data', 'model_info', 'model_name', 'model_type', 'underlyer', 'valuation_date'])

bct_auth_resource = RegressionResultTable(
    db_name='bct',
    name='auth_service.auth_resource',
    keys=['resource_name', 'resource_type'],
    values=['sort'])

bct_party = RegressionResultTable(
    db_name='bct',
    name='reference_data_service.party',
    keys=['legal_name'],
    values=['warrantor_address', 'warrantor', 'subsidiary_name', 'sales_name', 'party_status', 'master_agreement_id',
            'legal_representative', 'contact', 'client_type', 'branch_name'])

bct_trade_index = RegressionResultTable(
    db_name='bct',
    name='trade_service.trade_position_index',
    keys=['trade_id', 'position_id'],
    values=['book_name', 'counter_party_name', 'effective_date', 'expiration_date', 'instrument_id', 'lcm_event_type',
            'product_type', 'sales_name', 'trade_date'])

bct_trade = RegressionResultTable(
    db_name='bct',
    name='trade_snapshot_model.trade',
    keys=['trade_id'],
    values=['book_name', 'comment', 'trader', 'trade_status', 'trade_date'])

bct_position = RegressionResultTable(
    db_name='bct',
    name='trade_snapshot_model.position',
    keys=['position_id'],
    values=['book_name', 'asset', 'counterparty', 'counterparty_account', 'lcm_event_type', 'position_account',
            'quantity'])

terminal_otc_position = RegressionResultTable(
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

terminal_otc_trade = RegressionResultTable(
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

terminal_otc_pos_snapshot = RegressionResultTable(
    db_name='terminal_data',
    name='market_data.otc_position_snapshot',
    keys=['trans_code', 'report_date'],
    values=['instrument_id', 'main_body_name', 'trade_notional', 'implied_vol', 'interest_rate', 'dividend',
            'position_date'],
    roundings={'trade_notional': 2, 'implied_vol': 2, 'interest_rate': 2, 'dividend': 2}
)
