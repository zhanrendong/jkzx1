# -*- coding: utf-8 -*-

from datetime import datetime, timedelta
from pandas.io.json import json_normalize
from utils.utils import get_val, is_nan, call_request, get_pricing_env_description, remove_nan_and_inf, get_correlation

import numpy as np
import pandas as pd

_PRODUCT_TYPE_SPREADS = ['RATIO_SPREAD_EUROPEAN', 'SPREAD_EUROPEAN']
_PRODUCT_TYPE_CASH_FLOW = 'CASH_FLOW'
_datetime_fmt = '%Y-%m-%dT%H:%M:%S'


def intraday_position_report(positions, risks, cash_flows, listed_option_positions, pricing_environment, domain,
                             headers):
    """Return intraday position report.

    positions: basic position report, live positions(not expiring)
    risks: basic risk report
    cash_flows: basic cash flow report
    equity_postions: basic underlyer equity positions
    """

    multi_asset_position_criteria = positions.productType.isin(_PRODUCT_TYPE_SPREADS)
    single_asset_positions = positions[~multi_asset_position_criteria]
    multi_asset_positions = positions[multi_asset_position_criteria]

    decay_time = (datetime.now() + timedelta(days=1)).replace(hour=9, minute=0, second=0, microsecond=0)
    params = {
        'requests': ['delta'],
        'pricingEnvironmentId': pricing_environment,
        'valuationDateTime': decay_time.strftime(_datetime_fmt),
        'timezone': None}
    report = process_single_asset_pos(single_asset_positions, cash_flows, risks, domain, headers, params)

    if not multi_asset_positions.empty:
        multi_asset_report = process_multi_asset_pos(multi_asset_positions, cash_flows, risks, domain, headers, params)
        report = pd.concat([report, multi_asset_report], sort=False)

    # Exchange positions
    if not listed_option_positions.empty:
        listed_report = process_listed_pos(listed_option_positions, domain, headers, params)
        report = pd.concat([report, listed_report], sort=False)

    pe_description = get_pricing_env_description(pricing_environment, domain, headers)
    report['pricingEnvironment'] = pe_description
    report['createdAt'] = str(datetime.now())
    return remove_nan_and_inf(report.to_dict(orient='records'))


def process_multi_asset_pos(positions, cash_flows, risks, domain, headers, params):
    all_data = positions.merge(cash_flows, on='positionId', how='left').merge(risks, on='positionId', how='left')
    rpt = all_data[['positionId', 'bookName', 'counterPartyName', 'tradeId', 'asset.underlyerInstrumentId1', 'vegas',
                    'asset.underlyerInstrumentId2', 'productType', 'initialNumber1', 'initialNumber2', 'gammas', 'r',
                    'unwindNumber1', 'unwindNumber2', 'tradeDate', 'asset.expirationDate', 'message', 'deltas', 'qs',
                    'asset.underlyerMultiplier1', 'asset.underlyerMultiplier2', 'price', 'theta', 'settle', 'vols',
                    'quantity1', 'quantity2', 'actualPremium', 'open', 'underlyerPrices', 'rhoR', 'unwind']]
    rpt.rename(columns={'counterPartyName': 'partyName', 'asset.expirationDate': 'expirationDate'}, inplace=True)
    rpt['marketValue'] = rpt['price']
    rpt['underlyerInstrumentIds'] = rpt.apply(
        lambda r: [r['asset.underlyerInstrumentId1'], r['asset.underlyerInstrumentId2']], axis=1)
    # TODO: 这种写法可能会有性能问题，但考虑到只是针对多资产交易，数量少，暂时这样做。若以后交易量大，后台应提供相应批量查询接口以避免频繁调用接口
    rpt['correlation'] = rpt.apply(lambda r: get_correlation(r['underlyerInstrumentIds'], domain, headers), axis=1)
    rpt['underlyerMultipliers'] = rpt.apply(
        lambda r: [r['asset.underlyerMultiplier1'], r['asset.underlyerMultiplier2']], axis=1)
    rpt['initialNumbers'] = rpt.apply(lambda r: [r['initialNumber1'], r['initialNumber2']], axis=1)
    rpt['unwindNumbers'] = rpt.apply(lambda r: [r['unwindNumber1'], r['unwindNumber2']], axis=1)
    rpt['numbers'] = rpt.apply(lambda row: [np.float64(row['quantity1']) / row['underlyerMultipliers'][0],
                                            np.float64(row['quantity2']) / row['underlyerMultipliers'][1]], axis=1)
    rpt['premium'] = rpt.apply(
        lambda row: np.float64(row['actualPremium']) if is_nan(row['open']) else np.float64(row['open']), axis=1)
    rpt['unwindAmount'] = rpt.apply(
        lambda row: 0 if is_nan(row['open']) else np.float64(row['unwind']) + np.float64(row['settle']), axis=1)
    rpt['pnl'] = np.float64(rpt['marketValue']) + rpt['premium'] + rpt['unwindAmount']
    rpt['deltas'] = rpt.apply(lambda r: [get_val(r['deltas'], 0) / get_val(r['underlyerMultipliers'], 0),
                                         get_val(r['deltas'], 1) / get_val(r['underlyerMultipliers'], 1)], axis=1)
    rpt['deltaCashes'] = rpt.apply(lambda r: [np.float64(r['deltas'][0]) * get_val(r['underlyerPrices'], 0),
                                              np.float64(r['deltas'][1]) * get_val(r['underlyerPrices'], 1)], axis=1)
    rpt['gammas'] = rpt.apply(
        lambda r: [get_val(r['gammas'], 0, 0) * get_val(r['underlyerPrices'], 0) / r['underlyerMultipliers'][0] / 100,
                   get_val(r['gammas'], 1, 1) * get_val(r['underlyerPrices'], 1) / r['underlyerMultipliers'][1] / 100],
        axis=1)
    rpt['gammaCashes'] = rpt.apply(lambda r: [np.float64(r['gammas'][0]) * get_val(r['underlyerPrices'], 0),
                                              np.float64(r['gammas'][1]) * get_val(r['underlyerPrices'], 1)], axis=1)
    rpt['vegas'] = rpt.apply(lambda r: [get_val(r['vegas'], 0) / 100, get_val(r['vegas'], 1) / 100], axis=1)
    rpt['theta'] = np.float64(rpt['theta']) / 365
    rpt['rho'] = np.float64(rpt['rhoR']) / 100
    rpt['deltaDecays'] = np.nan
    rpt['deltaWithDecays'] = np.nan

    rpt.drop(['asset.underlyerInstrumentId1', 'asset.underlyerInstrumentId2', 'asset.underlyerMultiplier1', 'quantity1',
              'asset.underlyerMultiplier2', 'initialNumber1', 'initialNumber2', 'rhoR', 'quantity2', 'actualPremium',
              'open', 'settle', 'unwindNumber1', 'unwindNumber2', 'underlyerPrices', 'unwind'], axis=1, inplace=True)
    params['tradeIds'] = list(rpt.tradeId.unique())
    decay_data = call_request(domain, 'pricing-service', 'prcPrice', params, headers)
    if 'result' in decay_data:
        diagnostics_pd = pd.DataFrame(decay_data['diagnostics'])
        if not diagnostics_pd.empty:
            diagnostics_pids = list(diagnostics_pd.key.unique())
            diagnostics_pd.set_index('key', inplace=True)
            diagnostics_pd.drop_duplicates(inplace=True)
            rpt['message'] = rpt.apply(lambda row: get_error_msg(row, diagnostics_pd, diagnostics_pids), axis=1)

        risk_pd = pd.DataFrame(decay_data['result'])
        if not risk_pd.empty:
            risk_pids = list(risk_pd.positionId.unique())
            risk_pd = risk_pd.set_index('positionId')
            rpt['deltaWithDecays'] = rpt.apply(lambda row: calc_delta_with_decays(row, risk_pd, risk_pids), axis=1)
            rpt['deltaDecays'] = rpt.apply(lambda r: [get_val(r['deltaWithDecays'], 0) - get_val(r['deltas'], 0),
                                                      get_val(r['deltaWithDecays'], 1) - get_val(r['deltas'], 1)],
                                           axis=1)
    rpt['listedOption'] = False
    return rpt


def process_single_asset_pos(positions, cash_flows, risks, domain, headers, params):
    all_data = positions.merge(cash_flows, on='positionId', how='left') \
        .merge(risks.drop(['quantity'], axis=1), on='positionId', how='left')
    report = all_data[['positionId', 'bookName', 'counterPartyName', 'tradeId', 'asset.underlyerInstrumentId',
                       'productType', 'initialNumber', 'unwindNumber', 'tradeDate', 'asset.expirationDate',
                       'asset.underlyerMultiplier', 'price', 'delta', 'gamma', 'vega', 'theta', 'message', 'q', 'r',
                       'quantity', 'actualPremium', 'open', 'underlyerPrice', 'rhoR', 'unwind', 'settle', 'vol']]
    report.rename(columns={'counterPartyName': 'partyName', 'asset.underlyerInstrumentId': 'underlyerInstrumentId',
                           'asset.expirationDate': 'expirationDate', 'asset.underlyerMultiplier': 'underlyerMultiplier'}
                  , inplace=True)

    # for cash flows: underlyerPrice = 1
    cf_idx = report[report['productType'] == _PRODUCT_TYPE_CASH_FLOW].index
    if len(cf_idx) > 0:
        report.loc[cf_idx, 'underlyerPrice'] = 1
    report['marketValue'] = report['price']
    report['number'] = np.float64(report['quantity']) / np.float64(report['underlyerMultiplier'])
    report['premium'] = report.apply(
        lambda row: np.float64(row['actualPremium']) if is_nan(row['open']) else np.float64(row['open']), axis=1)
    report['unwindAmount'] = report.apply(
        lambda row: 0 if is_nan(row['open']) else np.float64(row['unwind']) + np.float64(row['settle']), axis=1)
    report['pnl'] = np.float64(report['marketValue']) + report['premium'] + report['unwindAmount']
    calc_delta_and_gamma(report)
    report['vega'] = np.float64(report['vega']) / 100
    report['theta'] = np.float64(report['theta']) / 365
    report['rho'] = np.float64(report['rhoR']) / 100
    report['deltaDecay'] = np.nan
    report['deltaWithDecay'] = np.nan

    report.drop(['quantity', 'actualPremium', 'open', 'underlyerPrice', 'rhoR', 'unwind', 'settle'], axis=1,
                inplace=True)
    params['tradeIds'] = list(report.tradeId.unique())
    decay_data = call_request(domain, 'pricing-service', 'prcPrice', params, headers)
    if 'result' in decay_data:
        diagnostics_pd = json_normalize(decay_data['diagnostics'])
        diagnostics_pd = pd.DataFrame(decay_data['diagnostics'])
        if not diagnostics_pd.empty:
            diagnostics_pids = list(diagnostics_pd.key.unique())
            diagnostics_pd.set_index('key', inplace=True)
            diagnostics_pd.drop_duplicates(inplace=True)
            report['message'] = report.apply(lambda row: get_error_msg(row, diagnostics_pd, diagnostics_pids), axis=1)

        risk_pd = json_normalize(decay_data['result'])
        risk_pd = pd.DataFrame(decay_data['result'])
        if not risk_pd.empty:
            risk_pids = list(risk_pd.positionId.unique())
            risk_pd = risk_pd.set_index('positionId')
            report['deltaWithDecay'] = report.apply(lambda row: calc_delta_with_decay(row, risk_pd, risk_pids), axis=1)
            report['deltaDecay'] = report['deltaWithDecay'] - report['delta']
    report['listedOption'] = False
    return report


def process_listed_pos(positions, domain, headers, params):
    report = positions[
        ['bookId', 'delta', 'gamma', 'netPosition', 'expirationDate', 'historySellAmount', 'historyBuyAmount',
         'totalPnl', 'marketValue', 'positionId', 'vega', 'vol', 'q', 'r', 'theta', 'underlyerInstrumentId',
         'underlyerInstrumentMultiplier', 'rhoR', 'instrumentId', 'underlyerPrice', 'price']].fillna(0)
    report.rename(columns={'bookId': 'bookName', 'netPosition': 'number', 'totalPnl': 'pnl', 'rhoR': 'rho',
                           'underlyerInstrumentMultiplier': 'underlyerMultiplier'}, inplace=True)
    # partyName,message and tradeDate has no value
    report['unwindNumber'] = 0
    report['unwindAmount'] = 0
    calc_delta_and_gamma(report)
    report['tradeId'] = report.apply(lambda row: row['bookName'] + '_' + row['instrumentId'], axis=1)
    report['initialNumber'] = report['number']
    report['premium'] = report['historySellAmount'] - report['historyBuyAmount']
    report['listedOption'] = True

    instrument_data = call_request(domain, 'market-data-service', 'mktInstrumentsListPaged',
                                   {'instrumentIds': list(report.instrumentId.dropna().unique())}, headers)
    instrument_df = pd.DataFrame(instrument_data['result']['page'])
    instrument_df['productType'] = instrument_df.apply(
        lambda row: 'VANILLA_EUROPEAN' if row.get('exerciseType') == 'EUROPEAN' else 'VANILLA_AMERICAN', axis=1)
    report = report.merge(instrument_df[['instrumentId', 'productType']], on='instrumentId', how='left')

    params['books'] = list(report.bookName.dropna().unique())
    listed_decay_data = call_request(domain, 'pricing-service', 'prcPriceOnExchange', params, headers)
    listed_decay = pd.DataFrame(listed_decay_data['result'])
    listed_decay = listed_decay[listed_decay.positionId.isin(report.positionId)][['positionId', 'delta']]
    listed_decay.columns = ['positionId', 'deltaWithDecay']
    report = report.merge(listed_decay, on='positionId', how='left')
    report['deltaWithDecay'] = np.float64(report['deltaWithDecay']) / np.float64(report['underlyerMultiplier'])
    report['deltaDecay'] = report['deltaWithDecay'] - report['delta']
    report.drop(['historySellAmount', 'historyBuyAmount', 'underlyerPrice', 'instrumentId'], axis=1, inplace=True)
    report['positionId'] = report.apply(
        lambda row: row['positionId'][:-len('_portfolio_null')] if row['positionId'].endswith('_portfolio_null') else
        row['positionId'], axis=1)
    return report


def calc_delta_and_gamma(report):
    report['delta'] = np.float64(report['delta']) / np.float64(report['underlyerMultiplier'])
    report['deltaCash'] = np.float64(report['delta']) * np.float64(report['underlyerPrice'])
    report['gamma'] = np.float64(report['gamma']) * np.float64(report['underlyerPrice']) / 100 / np.float64(
        report['underlyerMultiplier'])
    report['gammaCash'] = np.float64(report['gamma']) * np.float64(report['underlyerPrice'])


def get_error_msg(row, diagnostics_pd, diagnostics_pids):
    msg = row['message']
    pid = row['positionId']
    if pid in diagnostics_pids and msg == '':
        msg = 'Delta decay ' + diagnostics_pd.loc[pid, 'message']
    return msg


def calc_delta_with_decay(row, risk_pd, risk_pids):
    pid = row['positionId']
    delta_with_decay = row['deltaWithDecay']
    if pid in risk_pids:
        delta_with_decay = np.float64(risk_pd.ix[pid]['delta']) / np.float64(row['underlyerMultiplier'])
    return delta_with_decay


def calc_delta_with_decays(row, risk_pd, risk_pids):
    pid = row['positionId']
    result = row['deltaWithDecays']
    if pid in risk_pids:
        result = [get_val(risk_pd.loc[pid]['deltas'], 0) / get_val(row['underlyerMultipliers'], 0),
                  get_val(risk_pd.loc[pid]['deltas'], 1) / get_val(row['underlyerMultipliers'], 1)]
    return [np.nan, np.nan] if is_nan(result) else result

