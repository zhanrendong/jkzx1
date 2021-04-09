# -*- coding: utf-8 -*-
from datetime import date, timedelta

import init_params
import utils
from init_instruments import create_commodity_futures, create_equity_stock, create_equity_index, create_commodity_spot, save_whitelist
from init_market_data import save_quote
from init_models import create_risk_free_curve, create_vol, create_dividend_curve

if __name__ == '__main__':
    print('Initializing startup test environment.')
    trader = init_params.trader_names[0]
    host = init_params.host
    token = utils.login(trader, init_params.trader_password, init_params.host)

    today = date.today()
    yesterday = today - timedelta(days=1)

    # set up underlyer instruments
    create_commodity_futures('RB1912.SHF', '螺纹钢 1912', 'SHFE', 10, '2019-12-16', host, token)
    create_commodity_spot('AU9999.SGE', '黄金9999', 'SGE', 10, host, token)
    create_equity_stock('600030.SH', '中信证券', 'SSE', 1, host, token)
    create_equity_index('000300.SH', '沪深300', 'SSE', host, token)
    save_whitelist('RB1912.SHF', 'SHF', 1000000000, host, token)
    save_whitelist('AU9999.SGE', 'SGE', 1000000000, host, token)
    save_whitelist('600030.SH', 'SSE', 1000000000, host, token)
    save_whitelist('000300.SH', 'SSE', 1000000000, host, token)

    # fix underlyer quotes and models
    save_quote('RB1912.SHF', 'close', {'close': 5200}, yesterday, host, token)
    save_quote('RB1912.SHF', 'intraday', {'last': 5220, 'bid': 5210, 'ask': 5230}, today, host, token)
    save_quote('RB1912.SHF', 'close', {'close': 5300, 'settle': 5300}, today, host, token)
    save_quote('AU9999.SGE', 'close', {'close': 287.19}, yesterday, host, token)
    save_quote('AU9999.SGE', 'intraday', {'last': 286.60, 'bid': 286.65, 'ask': 286.70}, today, host, token)
    save_quote('AU9999.SGE', 'close', {'close': 286.60}, today, host, token)
    save_quote('600030.SH', 'close', {'close': 25}, yesterday, host, token)
    save_quote('600030.SH', 'intraday', {'last': 24, 'bid': 23.9, 'ask': 24.1}, today, host, token)
    save_quote('600030.SH', 'close', {'close': 24}, today, host, token)
    save_quote('000300.SH', 'close', {'close': 4000}, yesterday, host, token)
    save_quote('000300.SH', 'intraday', {'last': 4080}, today, host, token)
    save_quote('000300.SH', 'close', {'close': 4050}, today, host, token)

    # fix models
    tenors = init_params.tenors
    create_risk_free_curve(init_params.risk_free_curve_name, 'intraday', tenors,
                           [0.05 for i in tenors], today, host, token)
    create_risk_free_curve(init_params.risk_free_curve_name, 'close', tenors,
                           [0.05 for i in tenors], today, host, token)
    create_vol(init_params.vol_surface_name, 'RB1912.SHF', 'intraday', 5200,
               tenors, [0.16 for i in tenors], today, host, token)
    create_vol(init_params.vol_surface_name, 'RB1912.SHF', 'close', 5200,
               tenors, [0.16 for i in tenors], today, host, token)
    create_vol(init_params.vol_surface_name, 'AU9999.SGE', 'intraday', 286.60,
               tenors, [0.16 for i in tenors], today, host, token)
    create_vol(init_params.vol_surface_name, 'AU9999.SGE', 'close', 286.60,
           tenors, [0.16 for i in tenors], today, host, token)
    create_vol(init_params.vol_surface_name, '600030.SH', 'intraday', 25,
               tenors, [0.18 for i in tenors], today, host, token)
    create_vol(init_params.vol_surface_name, '600030.SH', 'close', 25,
               tenors, [0.18 for i in tenors], today, host, token)
    create_vol(init_params.vol_surface_name, '000300.SH', 'intraday', 4000,
               tenors, [0.14 for i in tenors], today, host, token)
    create_vol(init_params.vol_surface_name, '000300.SH', 'close', 4000,
               tenors, [0.14 for i in tenors], today, host, token)
    create_dividend_curve(init_params.dividend_curve_name, 'RB1912.SHF', 'intraday',
                          tenors, [0 for i in tenors], today, host, token)
    create_dividend_curve(init_params.dividend_curve_name, 'RB1912.SHF', 'close',
                          tenors, [0 for i in tenors], today, host, token)
    create_dividend_curve(init_params.dividend_curve_name, 'AU9999.SGE', 'intraday',
                          tenors, [0 for i in tenors], today, host, token)
    create_dividend_curve(init_params.dividend_curve_name, 'AU9999.SGE', 'close',
                          tenors, [0 for i in tenors], today, host, token)
    create_dividend_curve(init_params.dividend_curve_name, '600030.SH', 'intraday',
                          tenors, [0 for i in tenors], today, host, token)
    create_dividend_curve(init_params.dividend_curve_name, '600030.SH', 'close',
                          tenors, [0 for i in tenors], today, host, token)
    create_dividend_curve(init_params.dividend_curve_name, '000300.SH', 'intraday',
                          tenors, [0 for i in tenors], today, host, token)
    create_dividend_curve(init_params.dividend_curve_name, '000300.SH', 'close',
                          tenors, [0 for i in tenors], today, host, token)
