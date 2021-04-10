# -*- coding: utf-8 -*-
from market_data.data_config import terminal_hostport
from utils import utils
from config.bct_config import ENV_TERMINAL_SERVICE_HOST, MAX_PRICING_TRADES_NUM
import numpy as np
from pandas.io.json import json_normalize
import os

CLASSIC_SCENARIOS_2008 = 'FINANCIAL_CRISIS_2008'
CLASSIC_SCENARIOS_2015 = 'STOCK_CRASH_2015'
CLASSIC_SCENARIOS_2018 = 'TRADE_WAR_2018'
CLASSIC_SCENARIOS = [CLASSIC_SCENARIOS_2015, CLASSIC_SCENARIOS_2018, CLASSIC_SCENARIOS_2008]
# POOL_SIZE = 4


def price_scenarios(trades, shock, scenario_type, ip, headers, valuation_date, pricing_environment):
    params = {'trades': trades, 'min': min(shock, 1), 'max': max(shock, 1), 'num': 2,
              'isPercentage': True, 'valuationDateTime': valuation_date, 'pricingEnvironmentId': pricing_environment}
    res = utils.call_request(ip, 'pricing-service', 'prcSpotScenarios', params, headers)
    price_result = [] if 'error' in res else res.get('result')
    result = []
    position_scenarios_count = {}
    for scenario in price_result:
        position_scenarios_count[scenario.get('positionId')] = (position_scenarios_count.get(scenario.get('positionId')) or 0) + 1
    for item in price_result:
        if item['scenarioId'] != 'scenario_100%' or position_scenarios_count.get(item.get('positionId')) == 1:
            item['scenarioType'] = scenario_type
            result.append(item)
    return result


def get_all_instrument_scenario_shock_dict(instrument_ids, headers):
    host = terminal_hostport
    if ENV_TERMINAL_SERVICE_HOST in os.environ:
        host = os.getenv(ENV_TERMINAL_SERVICE_HOST)
    classic_scenarios_dates = ['2008-09-16', '2015-06-26', '2018-03-22']
    classic_scenarios_quotes = utils.get_terminal_quotes_list_by_instruments_and_dates(instrument_ids,
                                                                                       classic_scenarios_dates,
                                                                                       host, headers)
    scenario_shock_dict = {}
    for quote in classic_scenarios_quotes:
        instrument_id = quote.get('instrumentId').upper()
        trade_date = quote.get('tradeDate')
        shock = 1
        if quote.get('closePrice') is not None and quote.get('preClosePrice') is not None:
            shock = quote.get('closePrice') / quote.get('preClosePrice')
        if trade_date == classic_scenarios_dates[0]:
            scenario_shock_dict[CLASSIC_SCENARIOS_2008 + '_' + instrument_id] = shock
        elif trade_date == classic_scenarios_dates[1]:
            scenario_shock_dict[CLASSIC_SCENARIOS_2015 + '_' + instrument_id] = shock
        elif trade_date == classic_scenarios_dates[2]:
            scenario_shock_dict[CLASSIC_SCENARIOS_2018 + '_' + instrument_id] = shock
        else:
            print('该行情不属于经典场景：' + str(quote))
    return scenario_shock_dict


def get_scenario_shock(scenario_type, instrument, instrument_scenario_shock_dict):
    # use this function to get instrument price shock percentage in a specific scenario
    shock = instrument_scenario_shock_dict.get(scenario_type + '_' + instrument.upper())
    return shock or 1


def is_subsidiary(party_name, all_sub_companies):
    # place holder, will be implemented when related api is ready
    # use this function to find out if a counter party is a subsidiary
    if all_sub_companies is None:
        return False
    return party_name in all_sub_companies


def eod_classic_scenarios_report(positions, all_sub_companies, ip, headers, valuation_date, pricing_environment):
    sub_positions = positions[
        ['positionId', 'tradeId', 'asset.underlyerInstrumentId', 'asset.underlyerMultiplier', 'counterPartyName',
         'bookName']]
    sub_positions.rename(columns={'bookName': 'subsidiary', 'asset.underlyerInstrumentId': 'underlyerInstrumentId',
                                  'asset.underlyerMultiplier': 'multiplier', 'counterPartyName': 'partyName'},
                         inplace=True)
    sub_positions.fillna({'multiplier': 1}, inplace=True)

    instrument_ids = list(set(sub_positions['underlyerInstrumentId']))
    instrument_scenario_shock_dict = get_all_instrument_scenario_shock_dict(instrument_ids, headers)
    # api prcSpotScenarios takes lots of time due to too many trades and too many scenarios, use thread pool
    # but we need to decide which combination of pool size and batch size is the optimum solution
    # pool = Pool(POOL_SIZE)
    scenarios = []
    for scenario_type in CLASSIC_SCENARIOS:
        scenario_group = sub_positions.groupby('underlyerInstrumentId')
        print('pricing scenario '+scenario_type+' with '+str(len(scenario_group))+' instruments')
        cnt = 0
        for instrument, positions_df in sub_positions.groupby('underlyerInstrumentId'):
            cnt = cnt + 1
            print('\t'+str(cnt)+': pricing '+instrument+' with '+str(len(positions_df))+' positions')
            shock = get_scenario_shock(scenario_type, instrument, instrument_scenario_shock_dict)
            trade_batches = list(utils.chunks(list(positions_df['tradeId'].dropna().unique()), MAX_PRICING_TRADES_NUM))
            for trade_batch in trade_batches:
                res = price_scenarios(trade_batch, shock, scenario_type, ip, headers, valuation_date, pricing_environment)
                scenarios.extend(res)
            #pool.apply_async(func=price_scenarios, args=(trades, shock, scenario_type, ip, headers),
            #                 callback=lambda res: scenarios.extend(res))
    # pool.close()
    # pool.join()
    scenarios_df = json_normalize(scenarios)
    scenarios_df = scenarios_df[scenarios_df.positionId.isin(sub_positions.positionId)]
    scenarios_df.rename(
        columns={'scenarioResult.delta': 'delta', 'scenarioResult.gamma': 'gamma', 'scenarioResult.theta': 'theta',
                 'scenarioResult.vega': 'vega', 'scenarioResult.rhoR': 'rhoR',
                 'scenarioResult.pnlChange': 'pnlChange', 'scenarioResult.underlyerPrice': 'underlyerPrice'},
        inplace=True)
    scenarios_df = scenarios_df[['positionId', 'scenarioType', 'delta', 'gamma', 'theta', 'vega', 'rhoR', 'pnlChange',
                                 'underlyerPrice']].fillna(0)
    scenarios_df.replace([np.inf, -np.inf, 'Infinity', '-Infinity', 'NaN', 'nan'], np.nan, inplace=True)
    scenarios_df.fillna(0, inplace=True)
    scenarios_df = scenarios_df.merge(sub_positions, on='positionId', how='left')
    scenarios_df['deltaCash'] = scenarios_df['delta'] * scenarios_df['underlyerPrice']
    scenarios_df['gammaCash'] = scenarios_df['gamma'] * scenarios_df['underlyerPrice'] * scenarios_df[
        'underlyerPrice'] / 100

    scenarios_df['delta'] = scenarios_df.apply(lambda x: np.float64(x['delta']) / np.float64(x['multiplier']), axis=1)
    scenarios_df['gamma'] = scenarios_df['gamma'] * scenarios_df['underlyerPrice'] / scenarios_df['multiplier'] / 100
    scenarios_df['theta'] = scenarios_df['theta'] / 365
    scenarios_df['vega'] = scenarios_df['vega'] / 100
    scenarios_df['rho'] = scenarios_df['rhoR'] / 100

    reports = []
    columns = ['scenarioType', 'underlyerInstrumentId', 'delta', 'deltaCash', 'gamma', 'gammaCash', 'theta', 'vega',
               'rho', 'pnlChange']

    key = ['scenarioType', 'underlyerInstrumentId']
    all_market_scenarios = scenarios_df[columns].groupby(key).sum().reset_index()
    all_market_scenarios['reportType'] = 'MARKET'

    key = ['scenarioType', 'subsidiary', 'underlyerInstrumentId']
    subsidiary_scenarios = scenarios_df[columns + ['subsidiary']].groupby(key).sum().reset_index()
    subsidiary_scenarios['reportType'] = 'SUBSIDIARY'

    key = ['scenarioType', 'partyName', 'underlyerInstrumentId']
    party_scenarios = scenarios_df[columns + ['partyName']]
    party_scenarios['isSubsidiary'] = party_scenarios.apply(
        lambda row: is_subsidiary(row['partyName'], all_sub_companies), axis=1)
    party_scenarios = party_scenarios[~party_scenarios.isSubsidiary].groupby(key).sum().reset_index()
    party_scenarios[party_scenarios.select_dtypes(include=['number']).columns] *= -1
    party_scenarios['reportType'] = 'PARTY'
    party_scenarios.drop('isSubsidiary', axis=1, inplace=True)

    reports.extend(all_market_scenarios.to_dict(orient='records'))
    reports.extend(subsidiary_scenarios.to_dict(orient='records'))
    reports.extend(party_scenarios.to_dict(orient='records'))

    return reports
