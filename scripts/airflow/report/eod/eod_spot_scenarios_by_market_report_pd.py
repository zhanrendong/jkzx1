# -*- coding: utf-8 -*-
import numpy as np
from pandas.io.json import json_normalize
from timeit import default_timer as timer
from utils import utils
from config.bct_config import MAX_PRICING_TRADES_NUM

SPOT_SCENARIOS_BY_MARKET_REPORT_ = '全市场分品种情景分析报告_'
SPOT_SCENARIOS_BY_SUBSIDIARY_REPORT_ = '子公司分品种情景分析报告_'
SPOT_SCENARIOS_BY_COUNTER_PARTY_REPORT_ = '交易对手分品种情景分析报告_'


def price_scenarios(trades, ip, headers, valuation_date, pricing_environment):
    params = {'trades': trades, 'min': 0.9, 'max': 1.1, 'num': 21, 'pricingEnvironmentId': pricing_environment,
              'isPercentage': True, 'valuationDateTime': valuation_date}
    res = utils.call_request(ip, 'pricing-service', 'prcSpotScenarios', params, headers)
    return [] if 'error' in res else res.get('result')


def eod_spot_scenarios_by_market_report(positions, ip, headers, pe_description, all_sub_companies,
                                        valuation_date, pricing_environment):
    sub_positions = positions[
        ['positionId', 'bookName', 'asset.underlyerInstrumentId', 'asset.underlyerMultiplier', 'counterPartyName']]
    sub_positions.rename(columns={'bookName': 'subsidiary', 'asset.underlyerInstrumentId': 'underlyerInstrumentId',
                                  'asset.underlyerMultiplier': 'multiplier', 'counterPartyName': 'partyName'},
                         inplace=True)
    sub_positions.fillna({'multiplier': 1}, inplace=True)
    trades = list(positions['tradeId'].dropna().unique())

    scenarios = []
    trade_batches = list(utils.chunks(trades, MAX_PRICING_TRADES_NUM))
    for trade_batch in trade_batches:
        print("\tstart pricing "+str(len(trade_batch))+" trades")
        start = timer()
        scenario_batch = price_scenarios(trade_batch, ip, headers, valuation_date, pricing_environment)
        end = timer()
        print('\tpricing takes:'+str(end-start)+' seconds')
        scenarios.extend(scenario_batch)
    print('finish pricing all scenarios')
    start = timer()
    scenarios_df = json_normalize(scenarios)
    end = timer()
    print('\tconvert scenario results takes:'+str(end-start)+' seconds')
    start = timer()
    scenarios_df = scenarios_df[scenarios_df.positionId.isin(sub_positions.positionId)]
    scenarios_df.rename(
        columns={'scenarioResult.delta': 'delta', 'scenarioResult.gamma': 'gamma', 'scenarioResult.theta': 'theta',
                 'scenarioResult.vega': 'vega', 'scenarioResult.rhoR': 'rhoR', 'scenarioResult.pnlChange': 'pnlChange',
                 'scenarioResult.underlyerPrice': 'underlyerPrice'}, inplace=True)
    scenarios_df = scenarios_df[['positionId', 'scenarioId', 'delta', 'gamma', 'theta', 'vega', 'rhoR', 'pnlChange',
                                 'underlyerPrice']].fillna(0)
    scenarios_df.replace([np.inf, -np.inf, 'Infinity', '-Infinity', 'NaN', 'nan'], np.nan, inplace=True)
    scenarios_df.fillna(0, inplace=True)
    end = timer()
    print('\tpostprocess scenario results takes:'+str(end-start)+' seconds')
    start = timer()
    scenarios_df = scenarios_df.merge(sub_positions, on='positionId', how='left')
    end = timer()
    print('\tmerge scenario results takes:'+str(end-start)+' seconds')
    start = timer()
    scenarios_df['deltaCash'] = scenarios_df['delta'] * scenarios_df['underlyerPrice']
    scenarios_df['gammaCash'] = scenarios_df['gamma'] * scenarios_df['underlyerPrice'] * scenarios_df[
        'underlyerPrice'] / 100

    scenarios_df['delta'] = scenarios_df.apply(lambda x: np.float64(x['delta']) / np.float64(x['multiplier']), axis=1)
    scenarios_df['gamma'] = scenarios_df['gamma'] * scenarios_df['underlyerPrice'] / scenarios_df['multiplier'] / 100
    scenarios_df['theta'] = scenarios_df['theta'] / 365
    scenarios_df['vega'] = scenarios_df['vega'] / 100
    scenarios_df['rhoR'] = scenarios_df['rhoR'] / 100
    end = timer()
    print('\tcalc scenario results takes:'+str(end-start)+' seconds')
    start = timer()
    all_market_scenarios = scenarios_df[
        ['scenarioId', 'underlyerInstrumentId', 'delta', 'deltaCash', 'gamma', 'gammaCash', 'theta', 'vega', 'rhoR',
         'pnlChange']].groupby(['scenarioId', 'underlyerInstrumentId']).sum().reset_index()
    subsidiary_scenarios = scenarios_df[
        ['scenarioId', 'underlyerInstrumentId', 'subsidiary', 'delta', 'deltaCash', 'gamma', 'theta', 'vega', 'rhoR',
         'gammaCash', 'pnlChange']].groupby(['scenarioId', 'subsidiary', 'underlyerInstrumentId']).sum().reset_index()
    counter_party_scenarios = scenarios_df[
        ['scenarioId', 'underlyerInstrumentId', 'partyName', 'delta', 'deltaCash', 'gamma', 'theta', 'vega', 'rhoR',
         'gammaCash', 'pnlChange']].groupby(['scenarioId', 'partyName', 'underlyerInstrumentId']).sum().reset_index()
    counter_party_scenarios[counter_party_scenarios.select_dtypes(include=['number']).columns] *= -1
    end = timer()
    print('\tgroup scenario results takes:'+str(end-start)+' seconds')
    start = timer()

    reports = []
    key = ['underlyerInstrumentId']
    for instrument, scenario in all_market_scenarios.groupby(key):
        reports.append({
            'reportType': 'MARKET',
            'reportName': SPOT_SCENARIOS_BY_MARKET_REPORT_ + pe_description,
            'instrumentId': instrument,
            'scenarios': scenario.drop(key, axis=1).to_dict(orient='records')
        })

    key = ['subsidiary', 'underlyerInstrumentId']
    for (subsidiary, instrument), scenario in subsidiary_scenarios.groupby(key):
        reports.append({
            'reportType': 'SUBSIDIARY',
            'reportName': SPOT_SCENARIOS_BY_SUBSIDIARY_REPORT_ + pe_description,
            'contentName': subsidiary,
            'instrumentId': instrument,
            'scenarios': scenario.drop(key, axis=1).to_dict(orient='records')
        })

    key = ['partyName', 'underlyerInstrumentId']
    for (party_name, instrument), scenario in counter_party_scenarios.groupby(key):
        if all_sub_companies is not None and party_name in all_sub_companies:
            continue
        reports.append({
            'reportType': 'PARTY',
            'reportName': SPOT_SCENARIOS_BY_COUNTER_PARTY_REPORT_ + pe_description,
            'contentName': party_name,
            'instrumentId': instrument,
            'scenarios': scenario.drop(key, axis=1).to_dict(orient='records')
        })
    end = timer()
    print('\tgenerate scenario reports takes:'+str(end-start)+' seconds')
    return reports

