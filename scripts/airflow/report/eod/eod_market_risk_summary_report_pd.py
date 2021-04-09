# -*- coding: utf-8 -*-


def eod_market_risk_summary_report(positions, pricing_environment):
    sub_positions_df = positions[['deltaCash', 'gammaCash', 'vega', 'theta', 'rho']].fillna(0)
    risk_report = sub_positions_df.sum()
    risk_report['pricingEnvironment'] = pricing_environment
    return [risk_report.fillna(0).to_dict()]


def eod_subsidiary_market_risk_report(positions):
    columns = ['bookName', 'deltaCash', 'gammaCash', 'vega', 'theta', 'rho']
    vanilla_positions_greeks = positions[columns].fillna(0)
    vanilla_positions_greeks.rename(columns={'bookName': 'subsidiary'}, inplace=True)
    risk_report = vanilla_positions_greeks.groupby('subsidiary').sum()
    return list(risk_report.reset_index().to_dict(orient='index').values())


def eod_counter_party_market_risk_report(positions, all_sub_companies):
    columns = ['partyName', 'deltaCash', 'gammaCash', 'vega', 'theta', 'rho']
    # filter out positions which counter party not exist
    greeks = positions[positions.partyName == positions.partyName][columns].fillna(0)
    greeks['isSubsidiary'] = greeks.apply(lambda row: is_subsidiary(row['partyName'], all_sub_companies), axis=1)
    risk_report = greeks[~greeks.isSubsidiary][columns].groupby('partyName').sum()
    risk_report[risk_report.select_dtypes(include=['number']).columns] *= -1
    return list(risk_report.reset_index().to_dict(orient='index').values())


def eod_counter_party_market_risk_by_underlyer_report(positions, all_sub_companies):
    columns = ['partyName', 'underlyerInstrumentId', 'delta', 'deltaCash', 'gamma', 'gammaCash', 'vega', 'theta', 'rho']
    greeks = positions[positions.partyName == positions.partyName][columns].fillna(0)
    greeks['isSubsidiary'] = greeks.apply(lambda row: is_subsidiary(row['partyName'], all_sub_companies), axis=1)
    risk_report = greeks[~greeks.isSubsidiary][columns].groupby(['partyName', 'underlyerInstrumentId']).sum()
    risk_report[risk_report.select_dtypes(include=['number']).columns] *= -1
    return list(risk_report.reset_index().to_dict(orient='index').values())


def is_subsidiary(party_name, all_sub_companies):
    # place holder, will be implemented when related api is ready
    # use this function to find out if a counter party is a subsidiary
    if all_sub_companies is None:
        return False
    return party_name in all_sub_companies
