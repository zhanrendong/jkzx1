import pandas as pd
import numpy as np
from utils.utils import get_val


def eod_daily_pnl_by_underlyer_report(risks, yst_positions, historical_pnl, yst_historical_pnl, cash_flows_today,
                                      live_positions_index, today_terminated_positions_index, pricing_environment):
    """Return daily pnl by underlyer and book.

    risks: eod basic risk report
    yst_positions: eod position report yesterday
    historical_pnl: historical pnl by underlyer report today
    yst_historical_pnl: historical pnl by underlyer report yesterday
    cash_flows_today: basic cash flows today
    position_index: second result of basic position report
    """
    positions_index = pd.concat([live_positions_index, today_terminated_positions_index])
    risks, yst_positions, cash_flows_today, position_index = massage_data(risks, yst_positions, cash_flows_today,
                                                                          positions_index)
    key = ['bookName', 'underlyerInstrumentId']
    report_data = historical_pnl[['bookName', 'underlyerInstrumentId', 'optionPnl', 'underlyerPnl', 'pnl']].fillna(0)
    report_data.rename(columns={'optionPnl': 'dailyOptionPnl', 'underlyerPnl': 'dailyUnderlyerPnl', 'pnl': 'dailyPnl'},
                       inplace=True)
    if not yst_historical_pnl.empty:
        report_data2 = yst_historical_pnl[
            ['bookName', 'underlyerInstrumentId', 'optionPnl', 'underlyerPnl', 'pnl']].fillna(0)
        report_data = report_data.merge(report_data2, on=key, how='left')
    else:
        report_data['pnl'] = 0
        report_data['optionPnl'] = 0
        report_data['underlyerPnl'] = 0

    report_data['dailyOptionPnl'] -= report_data['optionPnl']
    report_data['dailyUnderlyerPnl'] -= report_data['underlyerPnl']
    report_data['dailyPnl'] -= report_data['pnl']
    report_data.drop(columns=['optionPnl', 'underlyerPnl', 'pnl'], inplace=True)

    yst_positions_fields = ['positionId', 'marketValue', 'number', 'underlyerPrice', 'delta', 'gamma', 'vega', 'theta',
                            'rho', 'r', 'vol']
    yst_positions_data = pd.DataFrame(columns=yst_positions_fields).set_index('positionId')
    if not yst_positions.empty:
        yst_positions = yst_positions[yst_positions['productType'] != 'CASH_FLOW']
        yst_positions_data = yst_positions[yst_positions_fields].set_index('positionId')
    # remove CASH_FLOW
    cash_flow_position_ids = position_index[position_index.productType == 'CASH_FLOW'].index
    position_index = position_index.drop(cash_flow_position_ids)
    risks = risks[~risks.index.isin(cash_flow_position_ids)]
    position_index_data = position_index[
        ['bookName', 'asset.underlyerInstrumentId', 'asset.underlyerMultiplier']].fillna('')
    position_index_data.columns = ['bookName', 'underlyerInstrumentId', 'underlyerMultiplier']
    risks_data = risks[['quantity', 'r', 'vol', 'underlyerPrice', 'price']]
    risks_data.columns = ['quantity', 'risk.r', 'risk.vol', 'risk.underlyerPrice', 'price']

    # greeks contribution
    greeks_data = yst_positions_data.join(position_index_data).join(risks_data).fillna(0)
    greeks_data['fraction'] = abs(np.float64(greeks_data['quantity']) / np.float64(greeks_data['number']) / np.float64(
        greeks_data['underlyerMultiplier']))
    greeks_data.replace(np.inf, np.nan, inplace=True)
    greeks_data['pnlContributionDelta'] = np.float64(greeks_data['delta']) * np.float64(
        greeks_data['underlyerMultiplier']) * (np.float64(greeks_data['risk.underlyerPrice']) - np.float64(
        greeks_data['underlyerPrice'])) * np.float64(greeks_data['fraction'])
    greeks_data['pnlContributionGamma'] = np.float64(greeks_data['gamma']) * np.float64(
        greeks_data['underlyerMultiplier']) / np.float64(greeks_data['underlyerPrice']) * pow(
        (np.float64(greeks_data['risk.underlyerPrice']) - np.float64(greeks_data['underlyerPrice'])),
        2) * 50 * np.float64(greeks_data['fraction'])
    greeks_data['pnlContributionVega'] = np.float64(greeks_data['vega']) * (
            np.float64(greeks_data['risk.vol']) - np.float64(greeks_data['vol'])) * 100 * np.float64(
        greeks_data['fraction'])
    greeks_data['pnlContributionTheta'] = np.float64(greeks_data['theta']) * np.float64(greeks_data['fraction'])
    greeks_data['pnlContributionRho'] = np.float64(greeks_data['rho']) * (
            np.float64(greeks_data['risk.r']) - np.float64(greeks_data['r'])) * 100 * np.float64(
        greeks_data['fraction'])

    report_data3 = greeks_data[
        ['bookName', 'underlyerInstrumentId', 'pnlContributionDelta', 'pnlContributionGamma', 'pnlContributionVega',
         'pnlContributionTheta', 'pnlContributionRho']].groupby(key).sum().reset_index()
    report_data = report_data.merge(report_data3, on=key, how='left').fillna(0)

    # new and settled positions contribution
    if cash_flows_today.empty:
        report_data['pnlContributionSettled'] = 0
        report_data['pnlContributionNew'] = 0
    else:
        cash_flows_today = cash_flows_today[~cash_flows_today.positionId.isin(cash_flow_position_ids)]
        settle = ['pnlContributionSettled', 'pnlContributionNew']
        report_data4 = pd.DataFrame()
        pos_in_risk = cash_flows_today['positionId'].isin(risks_data.index)
        pos_in_yst_position = cash_flows_today['positionId'].isin(yst_positions_data.index)
        cash_flows_without_risk = cash_flows_today[~pos_in_risk]
        cash_flows_with_risk_without_yst_position = cash_flows_today[pos_in_risk & ~pos_in_yst_position]
        cash_flows_with_risk_with_yst_position = cash_flows_today[pos_in_risk & pos_in_yst_position]

        if not cash_flows_without_risk.empty:
            cash_flows_data = cash_flows_without_risk[['positionId', 'open', 'unwind', 'settle']].set_index(
                'positionId')
            settled_data = cash_flows_data.join(position_index_data[key]).join(
                yst_positions_data['marketValue']).set_index(key).fillna(0)
            settled_data['pnlContributionSettled'] = np.float64(settled_data['open']) + np.float64(
                settled_data['unwind']) + np.float64(settled_data['settle']) - np.float64(settled_data['marketValue'])
            settled_data['pnlContributionNew'] = 0
            report_data4 = pd.concat([settled_data[settle].reset_index(), report_data4])

        if not cash_flows_with_risk_without_yst_position.empty:
            cash_flows_data = cash_flows_with_risk_without_yst_position[
                ['positionId', 'open', 'unwind', 'settle']].set_index('positionId')
            settled_data = cash_flows_data.join(position_index_data[key]).join(
                risks_data['price']).set_index(key).fillna(0)
            settled_data['pnlContributionNew'] = np.float64(settled_data['open']) + np.float64(
                settled_data['unwind']) + np.float64(settled_data['settle']) + np.float64(settled_data['price'])
            settled_data['pnlContributionSettled'] = 0
            report_data4 = pd.concat([settled_data[settle].reset_index(), report_data4])

        if not cash_flows_with_risk_with_yst_position.empty:
            cash_flows_data = cash_flows_with_risk_with_yst_position[
                ['positionId', 'open', 'unwind', 'settle']].set_index('positionId')
            settled_data = cash_flows_data.join(position_index_data).join(
                risks_data['quantity']).join(yst_positions_data[['number', 'marketValue']]).set_index(key).fillna(0)
            settled_data['oldPnlContributionSettled'] = np.float64(settled_data['marketValue']) * (
                    np.float64(settled_data['number']) - np.float64(settled_data['quantity']) / np.float64(
                settled_data['underlyerMultiplier'])) / np.float64(settled_data['number'])
            settled_data.fillna(0, inplace=True)
            settled_data['pnlContributionSettled'] = np.float64(settled_data['open']) + np.float64(
                settled_data['unwind']) + np.float64(settled_data['settle']) - np.float64(
                settled_data['oldPnlContributionSettled'])
            settled_data['pnlContributionNew'] = 0
            report_data4 = pd.concat([settled_data[settle].reset_index(), report_data4])
        report_data4 = report_data4.groupby(key).sum().fillna(0).reset_index()
        report_data = report_data.merge(report_data4, on=key, how='left')
        report_data = report_data.groupby(key).sum().reset_index()
    report_data['pnlContributionUnexplained'] = \
        report_data['dailyOptionPnl'] - report_data['pnlContributionDelta'] - report_data['pnlContributionGamma'] - \
        report_data['pnlContributionVega'] - report_data['pnlContributionTheta'] - report_data['pnlContributionRho'] - \
        report_data['pnlContributionNew'] - report_data['pnlContributionSettled']
    report_data['pricingEnvironment'] = pricing_environment

    return report_data.to_dict(orient='records')


def massage_data(risks, yst_positions, cash_flows_today, position_index):
    spread_types = ['RATIO_SPREAD_EUROPEAN', 'SPREAD_EUROPEAN']
    if not position_index.empty:
        multi_asset_pid_criteria = position_index.productType.isin(spread_types)
        multi_asset_pids = position_index[multi_asset_pid_criteria]
        position_index = position_index[~multi_asset_pid_criteria]
        if not multi_asset_pids.empty:
            multi_asset_rows = list()
            multi_asset_pids.reset_index(inplace=True)
            multi_asset_position_ids = list(multi_asset_pids.positionId.unique())
            for index, row in multi_asset_pids.iterrows():
                row2 = row.copy()
                row['positionId'] = row['positionId'] + '_1'
                row2['positionId'] = row2['positionId'] + '_2'
                row['asset.underlyerInstrumentId'] = row['asset.underlyerInstrumentId1']
                row2['asset.underlyerInstrumentId'] = row['asset.underlyerInstrumentId2']
                row['asset.underlyerMultiplier'] = row['asset.underlyerMultiplier1']
                row2['asset.underlyerMultiplier'] = row['asset.underlyerMultiplier2']
                multi_asset_rows.append(row)
                multi_asset_rows.append(row2)
            multi_asset_pids = pd.DataFrame(multi_asset_rows).set_index('positionId')
            position_index = pd.concat([position_index, multi_asset_pids], sort=False)

            if not risks.empty:
                multi_asset_risks_criteria = risks.index.isin(multi_asset_position_ids)
                multi_asset_risks = risks[multi_asset_risks_criteria]
                risks = risks[~multi_asset_risks_criteria]
                if not multi_asset_risks.empty:
                    multi_asset_rows = list()
                    multi_asset_risks.reset_index(inplace=True)
                    for index, row in multi_asset_risks.iterrows():
                        row2 = row.copy()
                        row['positionId'] = row['positionId'] + '_1'
                        row2['positionId'] = row2['positionId'] + '_2'
                        row['vol'] = get_val(row.get('vols'), 0)
                        row2['vol'] = get_val(row.get('vols'), 1)
                        row['underlyerPrice'] = get_val(row.get('underlyerPrices'), 0)
                        row2['underlyerPrice'] = get_val(row.get('underlyerPrices'), 1)
                        multi_asset_rows.append(row)
                        multi_asset_rows.append(row2)
                    multi_asset_risks = pd.DataFrame(multi_asset_rows).set_index('positionId')
                    risks = pd.concat([risks, multi_asset_risks], sort=False)

            if not cash_flows_today.empty:
                multi_asset_cash_flow_criteria = cash_flows_today.positionId.isin(multi_asset_position_ids)
                multi_asset_cash_flow = cash_flows_today[multi_asset_cash_flow_criteria]
                cash_flows_today = cash_flows_today[~multi_asset_cash_flow_criteria]
                if not multi_asset_cash_flow.empty:
                    multi_asset_rows = list()
                    for index, row in multi_asset_cash_flow.iterrows():
                        row2 = row.copy()
                        row['positionId'] = row['positionId'] + '_1'
                        row2['positionId'] = row2['positionId'] + '_2'
                        multi_asset_rows.append(row)
                        multi_asset_rows.append(row2)
                    multi_asset_cash_flow = pd.DataFrame(multi_asset_rows)
                    cash_flows_today = pd.concat([cash_flows_today, multi_asset_cash_flow], sort=False)

    if not yst_positions.empty:
        multi_asset_position_criteria = yst_positions.productType.isin(spread_types)
        multi_asset_positions = yst_positions[multi_asset_position_criteria]
        yst_positions = yst_positions[~multi_asset_position_criteria]
        if not multi_asset_positions.empty:
            multi_asset_rows = list()
            for index, row in multi_asset_positions.iterrows():
                row2 = row.copy()
                row['positionId'] = row['positionId'] + '_1'
                row2['positionId'] = row2['positionId'] + '_2'
                row['vol'] = get_val(row.get('vols'), 0)
                row2['vol'] = get_val(row.get('vols'), 1)
                row['delta'] = get_val(row.get('deltas'), 0)
                row2['delta'] = get_val(row.get('deltas'), 1)
                row['gamma'] = get_val(row.get('gammas'), 0)
                row2['gamma'] = get_val(row.get('gammas'), 1)
                row['vega'] = get_val(row.get('vegas'), 0)
                row2['vega'] = get_val(row.get('vegas'), 1)
                row['number'] = get_val(row.get('numbers'), 0)
                row2['number'] = get_val(row.get('numbers'), 1)
                row['underlyerPrice'] = get_val(row.get('underlyerPrices'), 0)
                row2['underlyerPrice'] = get_val(row.get('underlyerPrices'), 1)
                row['underlyerInstrumentId'] = get_val(row.get('underlyerInstrumentIds'), 0)
                row2['underlyerInstrumentId'] = get_val(row.get('underlyerInstrumentIds'), 1)
                row['underlyerMultiplier'] = get_val(row.get('underlyerMultipliers'), 0)
                row2['underlyerMultiplier'] = get_val(row.get('underlyerMultipliers'), 1)
                multi_asset_rows.append(row)
                multi_asset_rows.append(row2)
            multi_asset_positions = pd.DataFrame(multi_asset_rows)
            yst_positions = pd.concat([yst_positions, multi_asset_positions], sort=False)
    return risks, yst_positions, cash_flows_today, position_index
