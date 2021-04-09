import pandas as pd
from datetime import datetime

def intraday_daily_pnl_by_underlyer_report(risks, cash_flows, underlyer_positions, list_position,
                                           live_positions_index, today_terminated_positions_index,
                                           yst_positions, yst_historical_pnl, pricing_environment):
    positions_index = pd.concat([live_positions_index, today_terminated_positions_index])
    risks, yst_positions, cash_flows, positions_index = massage_data(risks, yst_positions, cash_flows, positions_index)
    group_rule = ['bookName', 'underlyerInstrumentId']
    positions_index = positions_index.reset_index()
    positions_index.rename(columns={'asset.underlyerInstrumentId': 'underlyerInstrumentId',
                                    'asset.underlyerMultiplier': 'underlyerMultiplier'}, inplace=True)
    positions_risk_index = positions_index[['positionId', 'bookName', 'underlyerMultiplier']]
    positions_cashflow_index = positions_index[['positionId', 'bookName', 'underlyerInstrumentId']]

    risks = risks.reset_index()
    risks = pd.merge(risks, positions_risk_index, how='left', on='positionId')
    risks = risks[['positionId', 'bookName', 'underlyerInstrumentId', 'price', 'quantity', 'underlyerPrice',
                   'vol', 'r', 'underlyerMultiplier']]

    yst_positions_columns = ['positionId', 'number', 'underlyerPrice', 'marketValue', 'delta', 'gamma', 'vega', 'theta',
                             'rho', 'vol', 'r']
    if yst_positions.empty:
        yst_positions = pd.DataFrame(columns=yst_positions_columns)
    yst_positions = yst_positions[yst_positions_columns]
    yst_positions.rename(columns={'number': 'yst_number', 'underlyerPrice': 'yst_underlyerPrice',
                                  'marketValue': 'yst_marketValue', 'delta': 'yst_delta', 'gamma': 'yst_gamma',
                                  'vega': 'yst_vega', 'theta': 'yst_theta', 'rho': 'yst_rho', 'vol': 'yst_vol',
                                  'r': 'yst_r'}, inplace=True)

    yst_risks = pd.merge(yst_positions, risks, how='inner', on='positionId')
    yst_risks.fillna(0, inplace=True)
    yst_risks['fraction'] = abs(yst_risks['quantity'] / yst_risks['yst_number'] / yst_risks['underlyerMultiplier'])
    yst_risks['d_spot'] = yst_risks['underlyerPrice'] - yst_risks['yst_underlyerPrice']
    yst_risks['pnlContributionDelta'] = yst_risks['yst_delta'] * yst_risks['underlyerMultiplier'] * yst_risks[
        'd_spot'] * yst_risks['fraction']
    yst_risks['pnlContributionGamma'] = yst_risks['yst_gamma'] * yst_risks['underlyerMultiplier'] / yst_risks[
        'yst_underlyerPrice'] * yst_risks['d_spot'] * yst_risks['d_spot'] * 50 * yst_risks['fraction']
    yst_risks['pnlContributionVega'] = yst_risks['yst_vega'] * (yst_risks['vol'] - yst_risks['yst_vol']) * 100 * \
                                       yst_risks['fraction']
    yst_risks['pnlContributionTheta'] = yst_risks['yst_theta'] * yst_risks['fraction']
    yst_risks['pnlContributionRho'] = yst_risks['yst_rho'] * (yst_risks['r'] - yst_risks['yst_r']) * 100 * yst_risks[
        'fraction']

    yst_risks = yst_risks.groupby(group_rule).sum().reset_index()
    yst_risks_columns = ['bookName', 'underlyerInstrumentId', 'pnlContributionDelta', 'pnlContributionGamma',
                         'pnlContributionVega', 'pnlContributionTheta', 'pnlContributionRho']
    if yst_risks.empty:
        yst_risks = pd.DataFrame(columns=yst_risks_columns)
    yst_risks = yst_risks[yst_risks_columns]

    cash_flows.fillna(0, inplace=True)
    if cash_flows.empty:
        cash_flows['positionId'] = ''
    cash_flows['total'] = 0 if cash_flows.empty else cash_flows['open'] + cash_flows['unwind'] + cash_flows['settle']
    cash_flows['pnlContributionNew'] = 0
    cash_flows['pnlContributionSettled'] = 0
    cash_flows = pd.merge(cash_flows, positions_cashflow_index, how='left', on='positionId')

    cash_flow_yst = yst_positions[['positionId', 'yst_marketValue', 'yst_number']]
    cash_flow_risks = risks[['positionId', 'quantity', 'underlyerMultiplier', 'price']]

    pos_in_risk = cash_flows['positionId'].isin(cash_flow_risks['positionId'])
    pos_in_yst = cash_flows['positionId'].isin(cash_flow_yst['positionId'])

    pos_in_yst_in_risk = cash_flows[(pos_in_risk) & (pos_in_yst)]
    pos_in_yst_not_risk = cash_flows[(~pos_in_risk) & (pos_in_yst)]
    pos_not_yst_in_risk = cash_flows[(pos_in_risk) & (~pos_in_yst)]
    cash_flows_not_yst_not_risk = cash_flows[(~pos_in_risk) & (~pos_in_yst)]

    # in yst in risk
    cash_flows_in_yst = pd.merge(pos_in_yst_in_risk, cash_flow_yst, how='left', on='positionId')
    cash_flows_in_risk_in_yst = pd.merge(cash_flows_in_yst, cash_flow_risks, how='left', on='positionId')
    cash_flows_in_risk_in_yst['pnlContributionSettled'] = \
        cash_flows_in_risk_in_yst['total'] - cash_flows_in_risk_in_yst['yst_marketValue'] \
        * (cash_flows_in_risk_in_yst['yst_number'] - cash_flows_in_risk_in_yst['quantity'] / cash_flows_in_risk_in_yst[
            'underlyerMultiplier']) / cash_flows_in_risk_in_yst['yst_number']
    # in yst not in risk
    cash_flows_in_yst_not_risk = pd.merge(pos_in_yst_not_risk, cash_flow_yst, how='left', on='positionId')
    cash_flows_in_yst_not_risk['pnlContributionSettled'] = cash_flows_in_yst_not_risk['total'] \
                                                           - cash_flows_in_yst_not_risk['yst_marketValue']
    # not in yst in risk
    cash_flows_not_yst_in_risk = pd.merge(pos_not_yst_in_risk, cash_flow_risks, how='left', on='positionId')
    cash_flows_not_yst_in_risk['pnlContributionNew'] = cash_flows_not_yst_in_risk['total'] \
                                                       + cash_flows_not_yst_in_risk['price']
    # not in risk not in yst
    cash_flows_not_yst_not_risk['pnlContributionSettled'] = cash_flows_not_yst_not_risk['total']

    cash_flows_all = [cash_flows_in_risk_in_yst, cash_flows_in_yst_not_risk, cash_flows_not_yst_in_risk,
                      cash_flows_not_yst_not_risk]
    cash_flows_risk = pd.concat(cash_flows_all)
    cash_flows_risk = cash_flows_risk[['bookName', 'underlyerInstrumentId', 'total', 'pnlContributionNew',
                                       'pnlContributionSettled']]
    cash_flows_risk.fillna(0, inplace=True)
    cash_flows_group = cash_flows_risk.groupby(group_rule).sum().reset_index()

    risks_info = risks[['bookName', 'underlyerInstrumentId', 'price']]
    risks_info = risks_info.groupby(group_rule).sum().reset_index()
    cash_flows_risk_yst = pd.merge(cash_flows_group, yst_risks, how='outer', on=group_rule)
    cash_flows_risks_data = pd.merge(cash_flows_risk_yst, risks_info, how='outer', on=group_rule)

    underlyer_positions = underlyer_positions[['bookId', 'totalPnl', 'underlyerInstrumentId']]
    underlyer_positions.rename(columns={'bookId': 'bookName', 'totalPnl': 'dailyUnderlyerPnl'}, inplace=True)
    underlyer_risk = pd.merge(cash_flows_risks_data, underlyer_positions, how='outer', on=group_rule)

    yst_historical_pnl_columns = ['bookName', 'underlyerInstrumentId', 'optionMarketValue', 'underlyerPnl']
    if yst_historical_pnl.empty:
        yst_historical_pnl = pd.DataFrame(columns=yst_historical_pnl_columns)
    yst_historical_pnl = yst_historical_pnl[yst_historical_pnl_columns]
    reports = pd.merge(underlyer_risk, yst_historical_pnl, how='outer', on=group_rule)
    reports.fillna(0, inplace=True)
    reports['dailyOptionPnl'] = reports['price'] + reports['total'] - reports['optionMarketValue']
    reports['dailyUnderlyerPnl'] = reports['dailyUnderlyerPnl'] - reports['underlyerPnl']
    reports['dailyPnl'] = reports['dailyOptionPnl'] + reports['dailyUnderlyerPnl']
    reports['pnlContributionUnexplained'] = reports['dailyOptionPnl'] - reports['pnlContributionDelta'] \
                                            - reports['pnlContributionGamma'] - reports['pnlContributionVega'] \
                                            - reports['pnlContributionTheta'] - reports['pnlContributionRho'] \
                                            - reports['pnlContributionNew'] - reports['pnlContributionSettled']
    right_data = reports['underlyerInstrumentId'] != 0
    reports = reports[right_data]
    reports = reports[['bookName', 'underlyerInstrumentId', 'dailyPnl', 'dailyOptionPnl', 'dailyUnderlyerPnl',
                       'pnlContributionNew', 'pnlContributionSettled', 'pnlContributionDelta', 'pnlContributionGamma',
                       'pnlContributionVega', 'pnlContributionTheta', 'pnlContributionRho',
                       'pnlContributionUnexplained']]
    reports = reports.groupby(group_rule).sum().reset_index()
    reports['pricingEnvironment'] = pricing_environment
    reports['createdAt'] = str(datetime.now())
    return reports
