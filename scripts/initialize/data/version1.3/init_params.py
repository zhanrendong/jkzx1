# -*- encoding: utf-8 -*-

host = 'localhost'
admin_user = 'admin'
admin_password = '12345'

# auth
script_user_name = 'script'
script_user_password = '123456a.'
trading_dept_name = '交易部'
trader_names = ['trader1', 'trader2']
trader_password = '123!@#qwe'
books = ['options1', 'options2', 'options3']
all_page_role_name = 'all_page'
financial_department_employees_role_name = 'financial_department_employees'

# calendar
calendar_name = 'DEFAULT_CALENDAR'
vol_calendar_name = 'DEFAULT_VOL_CALENDAR'

# market data
equity_stocks = {
    '000598.SZ': ['000598.SZ', '兴蓉环境', 'SZSE', 4.8, 1],
    '600030.SH': ['600030.SH', '中信证券', 'SSE', 24, 1],
}
equity_indices = {
    '000300.SH': ['000300.SH', '沪深300', 'SSE', 3800],
}
equity_index_futures = {
    'IF1906.CFE': ['IF1906.CFE', '沪深300 1906', 'CFFEX', 3800, 300, '2019-06-21']
}
commodity_futures = {
    'RB1905.SHF': ['RB1905.SHF', '螺纹钢 0905', 'SHFE', 4000, 10, '2019-05-15']
}

# model
tenors = ['1D', '1W', '2W', '3W', '1M', '3M', '6M', '9M', '1Y']
num_days = [1, 7, 30, 92, 180, 270, 365]
strike_percent = [0.8, 0.9, 0.95, 1, 1.05, 1.1, 1.2]
labels = ['80% SPOT', '90% SPOT', '95% SPOT', '100% SPOT', '105% SPOT', '110% SPOT', '120% SPOT']
vols = [0.2, 0.22, 0.24, 0.26, 0.28, 0.3, 0.32, 0.34, 0.36]
rs = [0.04, 0.042, 0.044, 0.046, 0.048, 0.05, 0.053, 0.056, 0.06]
qs = [0, 0, 0, 0, 0, 0, 0, 0, 0]
risk_free_curve_name = 'TRADER_RISK_FREE_CURVE'
vol_surface_name = 'TRADER_VOL'
dividend_curve_name = 'TRADER_DIVIDEND_CURVE'

# pricing environment
pe_close_name = 'DEFAULT_CLOSE'
pe_risk_close_name = 'RISK_CLOSE'
pe_close_calendar = 'DEFAULT_CLOSE_CALENDARS'
pe_intraday_name = 'DEFAULT_INTRADAY'
pe_intraday_calendar_name = 'DEFAULT_INTRADAY_CALENDARS'

# client
subsidiary = '分公司000'
branch = '营业部000'
sales = '销售000'
legal_name = '交易对手000'
representative = '法人代表000'
address = '注册地址000'
contact = '联系人000'
warrantor = '担保人000'
warrantor_address = '担保人地址000'
trade_phone = '66666666'
trade_email = 'client000@email.com'
master_agreement_id = 'MA0000'
account = '000000000000'
account_name = '交易对手账号000'
bank = '火星银行'
payment_system_code = 'PS0000'

