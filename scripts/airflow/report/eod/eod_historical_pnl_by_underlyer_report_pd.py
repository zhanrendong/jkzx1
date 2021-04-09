# -*- coding: utf-8 -*-
import pandas as pd
from utils.utils import get_val


def eod_historical_pnl_by_underlyer_report(positions, cash_flows, underlyer_positions, live_position_index,
                                           today_terminated_position_index, pricing_environment):
    """Return historical pnl of both option and underlyer trades.

    positions: eod position report
    cash_flows: eod basic cash flow report
    underlyer_positions: eod basic underlyer position report
    live_position_index: live position id to book and underlyer
    today_terminated_position_index: today terminated position id to book and underlyer
    """
    position_map = pd.concat([live_position_index, today_terminated_position_index])
    key = ['bookName', 'underlyerInstrumentId']
    positions = positions[positions['productType'] != 'CASH_FLOW']
    exchange_positions = positions[positions.listedOption]
    positions = process_positions(positions[~positions.listedOption])

    exchange = exchange_positions[['bookName', 'underlyerInstrumentId', 'premium', 'marketValue']] \
        .groupby(key).sum().reset_index()

    exchange.rename(columns={'marketValue': 'listMarketValue', 'premium': 'listPremium'}, inplace=True)

    # books and underlyers
    positions.rename(columns={'marketValue': 'optionMarketValue'}, inplace=True)
    positions_data = positions[['bookName', 'underlyerInstrumentId', 'optionMarketValue']]
    positions_data = positions_data.groupby(['bookName', 'underlyerInstrumentId']).sum().reset_index()
    underlyer_positions.drop(columns=['underlyerInstrumentId'], inplace=True)
    underlyer_positions.rename(columns={'bookId': 'bookName', 'historySellAmount': 'underlyerSellAmount',
                                        'totalPnl': 'underlyerPnl', 'historyBuyAmount': 'underlyerBuyAmount',
                                        'netPosition': 'underlyerNetPosition', 'instrumentId': 'underlyerInstrumentId',
                                        'marketValue': 'underlyerMarketValue'}, inplace=True)
    # prep underlyerPrice
    underlyer_price = positions[['underlyerInstrumentId', 'underlyerPrice']].dropna().drop_duplicates()
    listed_underlyer_price = underlyer_positions[['underlyerInstrumentId', 'underlyerPrice']] \
        .dropna().drop_duplicates(subset='underlyerInstrumentId')
    underlyer_price_exhange = listed_underlyer_price[
        ~listed_underlyer_price.underlyerInstrumentId.isin(underlyer_price.underlyerInstrumentId)]
    underlyer_price_all = pd.concat([underlyer_price, underlyer_price_exhange], ignore_index=True, sort=False)

    underlyer_positions = underlyer_positions[
        ['bookName', 'underlyerInstrumentId', 'underlyerNetPosition', 'underlyerMarketValue', 'underlyerBuyAmount',
         'underlyerSellAmount', 'underlyerPnl']]

    position_map, cash_flows = process_position_map_and_cash_flows(position_map, cash_flows)

    cash_flows.rename(columns={'open': 'optionPremium', 'unwind': 'optionUnwindAmount', 'settle': 'optionSettleAmount'},
                      inplace=True)
    cash_flows_pd = pd.merge(cash_flows, position_map, on='positionId', how='outer')
    cash_flows_data = cash_flows_pd.groupby(['bookName', 'underlyerInstrumentId']).sum().reset_index()
    cash_flows_data.drop(columns=['asset.underlyerMultiplier'], inplace=True)
    underlyer_positions = pd.merge(cash_flows_data, underlyer_positions, on=key, how='outer')
    report_data = pd.merge(underlyer_positions, positions_data, on=key, how='outer')
    report_data = report_data.merge(exchange, on=key, how='left')
    report_data.fillna(0, inplace=True)
    report_data['optionPremium'] = report_data['optionPremium'] + report_data['listPremium']
    report_data['optionMarketValue'] = report_data['optionMarketValue'] + report_data['listMarketValue']

    report_data['optionPnl'] = report_data['optionPremium'] + report_data['optionUnwindAmount'] + report_data[
        'optionSettleAmount'] + report_data['optionMarketValue']
    report_data['pnl'] = report_data['optionPnl'] + report_data['underlyerPnl']

    report_data = report_data.merge(underlyer_price_all, on='underlyerInstrumentId', how='left')
    report_data.drop(['listMarketValue', 'listPremium'], axis=1, inplace=True)
    report_data['pricingEnvironment'] = pricing_environment
    return report_data


def process_positions(positions):
    multi_asset_position_criteria = positions.productType.isin(['RATIO_SPREAD_EUROPEAN', 'SPREAD_EUROPEAN'])
    single_asset_positions = positions[~multi_asset_position_criteria]
    multi_asset_positions = positions[multi_asset_position_criteria]
    if not multi_asset_positions.empty:
        multi_asset_rows = list()
        for index, row in multi_asset_positions.iterrows():
            row2 = row.copy()
            row['underlyerPrice'] = get_val(row.get('underlyerPrices'), 0)
            row2['underlyerPrice'] = get_val(row.get('underlyerPrices'), 1)
            row['underlyerInstrumentId'] = get_val(row.get('underlyerInstrumentIds'), 0)
            row2['underlyerInstrumentId'] = get_val(row.get('underlyerInstrumentIds'), 1)
            multi_asset_rows.append(row)
            multi_asset_rows.append(row2)
        multi_asset_positions = pd.DataFrame(multi_asset_rows)
        single_asset_positions = pd.concat([multi_asset_positions, single_asset_positions], sort=False)
    return single_asset_positions


def process_position_map_and_cash_flows(position_map, cash_flows):
    position_map.rename(columns={'asset.underlyerInstrumentId': 'underlyerInstrumentId'}, inplace=True)
    multi_asset_position_criteria = position_map.productType.isin(['RATIO_SPREAD_EUROPEAN', 'SPREAD_EUROPEAN'])
    single_asset_positions = position_map[~multi_asset_position_criteria]
    multi_asset_positions = position_map[multi_asset_position_criteria]

    if not multi_asset_positions.empty:
        multi_asset_positions.reset_index(inplace=True)
        single_asset_positions.reset_index(inplace=True)
        multi_asset_position_ids = list(multi_asset_positions.index.unique())
        multi_asset_rows = list()
        for index, row in multi_asset_positions.iterrows():
            row2 = row.copy()
            row['positionId'] = row['positionId'] + '_1'
            row2['positionId'] = row2['positionId'] + '_2'
            row['underlyerInstrumentId'] = row['asset.underlyerInstrumentId1']
            row2['underlyerInstrumentId'] = row['asset.underlyerInstrumentId2']
            multi_asset_rows.append(row)
            multi_asset_rows.append(row2)
        multi_asset_positions = pd.DataFrame(multi_asset_rows)
        single_asset_positions = pd.concat([multi_asset_positions, single_asset_positions], sort=False)
        single_asset_positions.set_index('positionId', inplace=True)
        single_asset_positions.drop(['asset.underlyerInstrumentId1', 'asset.underlyerInstrumentId2',
                                     'asset.underlyerMultiplier1', 'asset.underlyerMultiplier2'], axis=1, inplace=True)

        if not cash_flows.empty:
            multi_asset_cash_flow_criteria = cash_flows.positionId.isin(multi_asset_position_ids)
            multi_asset_cash_flow = cash_flows[multi_asset_cash_flow_criteria]
            cash_flows = cash_flows[~multi_asset_cash_flow_criteria]
            if not multi_asset_cash_flow.empty:
                multi_asset_rows = list()
                for index, row in multi_asset_cash_flow.iterrows():
                    row2 = row.copy()
                    row['positionId'] = row['positionId'] + '_1'
                    row2['positionId'] = row2['positionId'] + '_2'
                    multi_asset_rows.append(row)
                    multi_asset_rows.append(row2)
                multi_asset_cash_flow = pd.DataFrame(multi_asset_rows)
                cash_flows = pd.concat([cash_flows, multi_asset_cash_flow], sort=False)
    return single_asset_positions, cash_flows
