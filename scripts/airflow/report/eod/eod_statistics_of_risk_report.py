def get_eod_statics_of_risk(price_data):
    stock_delta = 0.0
    stock_gamma = 0.0
    stock_vega = 0.0
    commodity_delta = 0.0
    commodity_gamma = 0.0
    commodity_vega = 0.0
    for position_id, p in price_data.items():
        underlyerInstrumentId = p.get('underlyerInstrumentId', '')
        if underlyerInstrumentId == '':
            continue
        code_end = underlyerInstrumentId.split('.')[1]
        if code_end in ['SH', 'CFE', 'SZ']:
            stock_delta += float(p.get('delta', 0.0)) if p.get('delta', 0.0) is not None else 0.0
            stock_gamma += float(p.get('gamma', 0.0)) if p.get('gamma', 0.0) is not None else 0.0
            stock_vega += float(p.get('vega', 0.0)) if p.get('vega', 0.0) is not None else 0.0
        else:
            commodity_delta += float(p.get('delta', 0.0)) if p.get('delta', 0.0) is not None else 0.0
            commodity_gamma += float(p.get('gamma', 0.0)) if p.get('gamma', 0.0) is not None else 0.0
            commodity_vega += float(p.get('vega', 0.0)) if p.get('vega', 0.0) is not None else 0.0
    result = {"4.1.1 个股DELTA": round(stock_delta, 4), "4.1.2 商品DELTA": round(commodity_delta, 4),
              "4.2.1 个股GAMMA": round(stock_gamma, 4), "4.2.2 商品GAMMA": round(commodity_gamma, 4),
              "4.3.1 个股VEGA": round(stock_vega, 4), "4.3.2 商品VEGA": round(commodity_vega, 4)}
    return result
