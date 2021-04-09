# -*- coding: utf-8 -*-


def eod_market_risk_by_book_underlyer_report(positions, pricing_environment):
    sub_positions_df = positions[['bookName', 'underlyerInstrumentId', 'delta', 'deltaCash', 'gamma', 'gammaCash', 'vega', 'theta', 'rho']].fillna(0)
    risk_report = sub_positions_df.groupby(['bookName', 'underlyerInstrumentId']).sum().reset_index()
    risk_report['pricingEnvironment'] = pricing_environment
    return list(risk_report.fillna(0).to_dict(orient='index').values())
