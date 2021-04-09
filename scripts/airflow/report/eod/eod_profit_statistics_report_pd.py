from datetime import datetime


def eod_profit_statistics_report(price_data, position_index, cash_flow):
    position_index = position_index[['asset.underlyerInstrumentId', 'productType']].reset_index()
    price_data = price_data[['price', 'positionId']]
    all_data = cash_flow.merge(position_index, on='positionId', how='left').merge(
        price_data, on='positionId', how='left').fillna(0)
    all_data.rename(columns={'asset.underlyerInstrumentId': 'underlyerInstrumentId', 'price': 'marketValue'},
                    inplace=True)
    all_data['total'] = all_data['open'] + all_data['unwind'] + all_data['settle']
    report_data = all_data[['underlyerInstrumentId', 'positionId', 'total', 'marketValue', 'timestamp', 'productType']]
    report_data['pnl'] = report_data['total'] + report_data['marketValue']

    today_filter = report_data['timestamp'].str.contains(datetime.now().strftime('%Y-%m-%d'))
    stock_filter = report_data['underlyerInstrumentId'].str.endswith('.CFE') | report_data[
        'underlyerInstrumentId'].str.endswith('.SZ') | report_data['underlyerInstrumentId'].str.endswith('.SH')
    product_fileter = report_data['productType'].str.contains('VANILLA')

    # 个股持仓盈亏
    stock_hold_pnl = report_data[stock_filter]['total'].sum()
    # 商品持仓盈亏
    commodity_hold_pnl = report_data[~stock_filter]['total'].sum()
    # 奇异持仓盈亏
    exotic_hold_pnl = report_data[~product_fileter]['total'].sum()
    # 个股今日了结盈亏
    stock_today_to_end_pnl = report_data[today_filter & stock_filter]['pnl'].sum()
    # 商品今日了结盈亏
    commodity_today_to_end_pnl = report_data[today_filter][~stock_filter]['pnl'].sum()
    # 个股历史了结盈亏
    stock_histroy_to_end_pnl = report_data[~today_filter][stock_filter]['pnl'].sum()
    # 商品历史了结盈亏
    commodity_histroy_to_end_pnl = report_data[~today_filter][~stock_filter]['pnl'].sum()
    # 奇异今日了结盈亏
    exotic_today_to_end_pnl = report_data[today_filter][~product_fileter]['pnl'].sum()
    # 奇异历史了结盈亏
    exotic_histroy_to_end_pnl = report_data[~today_filter][~product_fileter]['pnl'].sum()

    stock_to_end_all_pnl = stock_histroy_to_end_pnl + stock_today_to_end_pnl
    commodity_to_end_all_pnl = commodity_today_to_end_pnl + commodity_histroy_to_end_pnl
    exotic_to_end_all_pnl = exotic_histroy_to_end_pnl + exotic_today_to_end_pnl
    stock_pnl = stock_to_end_all_pnl + stock_hold_pnl
    commodity_pnl = commodity_to_end_all_pnl + commodity_hold_pnl
    exotic_pnl = exotic_to_end_all_pnl + exotic_hold_pnl
    option_today_to_end_pnl = commodity_today_to_end_pnl + stock_today_to_end_pnl + exotic_today_to_end_pnl
    option_histroy_to_end_pnl = commodity_histroy_to_end_pnl + stock_histroy_to_end_pnl + exotic_histroy_to_end_pnl
    option_to_end_all_pnl = option_today_to_end_pnl + option_histroy_to_end_pnl
    option_hold_pnl = stock_hold_pnl + commodity_hold_pnl + exotic_hold_pnl
    option_pnl = option_to_end_all_pnl
    result = {}
    result['2.1.1.1 商品期权今日了结盈亏'] = commodity_today_to_end_pnl
    result['2.1.1.2 商品期权历史了结盈亏'] = commodity_histroy_to_end_pnl
    result['2.1.1.3 商品期权了结盈亏'] = commodity_to_end_all_pnl
    result['2.1.1.4 商品期权持仓盈亏'] = commodity_hold_pnl
    result['2.1.1.5 商品期权总盈亏'] = commodity_pnl
    result['2.1.2.1 个股期权今日了结盈亏'] = stock_today_to_end_pnl
    result['2.1.2.2 个股期权历史了结盈亏'] = stock_histroy_to_end_pnl
    result['2.1.2.3 个股期权了结盈亏'] = stock_to_end_all_pnl
    result['2.1.2.4 个股期权持仓盈亏'] = stock_hold_pnl
    result['2.1.2.5 个股期权总盈亏'] = stock_pnl
    result['2.1.3.1 奇异期权今日了结盈亏'] = exotic_today_to_end_pnl
    result['2.1.3.2 奇异期权历史了结盈亏'] = exotic_histroy_to_end_pnl
    result['2.1.3.3 奇异期权了结盈亏'] = exotic_to_end_all_pnl
    result['2.1.3.4 奇异期权持仓盈亏'] = exotic_hold_pnl
    result['2.1.3.5 奇异期权总盈亏'] = exotic_pnl
    result['2.1.4.1 期权今日了结盈亏'] = option_today_to_end_pnl
    result['2.1.4.2 期权历史了结盈亏'] = option_histroy_to_end_pnl
    result['2.1.4.3 期权了结盈亏'] = option_to_end_all_pnl
    result['2.1.4.4 期权持仓盈亏'] = option_hold_pnl
    result['2.1.4.5 期权总盈亏'] = option_pnl
    return result
