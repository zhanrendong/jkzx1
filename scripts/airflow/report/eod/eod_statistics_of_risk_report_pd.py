def get_eod_statics_of_risk(price_data):
    risk = price_data[{'underlyerInstrumentId', 'delta', 'gamma', 'vega'}].fillna(0)
    stock_condition = risk['underlyerInstrumentId'].str.endswith('.CFE') | risk['underlyerInstrumentId'].str.endswith(
        '.SZ') | risk['underlyerInstrumentId'].str.endswith('.SH')
    stock_data = risk[stock_condition]
    commodity_data = risk[~stock_condition]

    stock_delta = round(stock_data['delta'].sum(), 4)
    commodity_delta = round(commodity_data['delta'].sum(), 4)
    stock_gamma = round(stock_data['gamma'].sum(), 4)
    commodity_gamma = round(commodity_data['gamma'].sum(), 4)
    stock_vega = round(stock_data['vega'].sum(), 4)
    commodity_vega = round(commodity_data['vega'].sum(), 4)

    result = {}
    result["4.1.1 个股DELTA"] = '∞' if stock_delta == float("inf") else stock_delta
    result["4.1.2 商品DELTA"] = '∞' if commodity_delta == float("inf") else commodity_delta
    result["4.2.1 个股GAMMA"] = '∞' if stock_gamma == float("inf") else stock_gamma
    result["4.2.2 商品GAMMA"] = '∞' if commodity_gamma == float("inf") else commodity_gamma
    result["4.3.1 个股VEGA"] = '∞' if stock_vega == float("inf") else stock_vega
    result["4.3.2 商品VEGA"] = '∞' if commodity_vega == float("inf") else commodity_vega
    return result
