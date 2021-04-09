# -*- coding: utf-8 -*-


def eod_market_risk_detail_report(positions):
    columns = ['underlyerInstrumentId', 'delta', 'deltaCash', 'gamma', 'gammaCash', 'vega', 'theta', 'rho']
    vanilla_positions_greeks = positions[columns].fillna(0)
    risk_report = vanilla_positions_greeks.groupby('underlyerInstrumentId').sum()
    risk_report['scenarioType'] = 'BASE'
    return risk_report.reset_index().to_dict(orient='records')
