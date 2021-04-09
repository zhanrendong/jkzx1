# -*- coding: utf-8 -*-

from utils import utils
import numpy as np
from datetime import datetime

_datetime_fmt = '%Y-%m-%dT%H:%M:%S'


def intraday_expiring_position_report(expirings, risks, cash_flows):
    """Return report on positions expire today.

    expirings: expiring positions from basic position report
    risks: basic risk report
    cash_flows: basic cash flow report
    """
    if expirings.empty:
        return []
    # cash flow
    cash_flows_data = cash_flows[['positionId', 'open', 'unwind', 'settle']].set_index('positionId')
    expirings_data = expirings[
        ['positionId', 'bookName', 'counterPartyName', 'tradeId', 'asset.underlyerInstrumentId', 'productType',
         'initialNumber', 'unwindNumber', 'tradeDate', 'asset.expirationDate', 'asset.underlyerMultiplier',
         'quantity']].set_index('positionId')
    risks_data = risks[[
        'price', 'delta', 'underlyerPrice', 'gamma', 'vega', 'theta', 'rhoR', 'message', 'pricing_environment']]
    report_data = expirings_data.join(risks_data).join(cash_flows_data)
    report_data.rename(columns={'counterPartyName': 'partyName', 'asset.underlyerInstrumentId': 'underlyerInstrumentId',
                                'asset.underlyerMultiplier': 'underlyerMultiplier', 'price': 'marketValue',
                                'asset.expirationDate': 'expirationDate', 'pricing_environment': 'pricingEnvironment',
                                'rhoR': 'rho'}, inplace=True)

    report_data['premium'] = report_data.apply(
        lambda row: np.float64(row['actualPremium']) if utils.is_nan(row['open']) else np.float64(row['open']), axis=1)
    report_data['unwindAmount'] = report_data.apply(
        lambda row: 0 if utils.is_nan(row['open']) else np.float64(row['unwind']) + np.float64(row['settle']), axis=1)

    report_data['number'] = np.float64(report_data['quantity']) / np.float64(report_data['underlyerMultiplier'])
    report_data['pnl'] = np.float64(report_data['marketValue']) + report_data['premium'] + report_data['unwindAmount']
    report_data['delta'] = np.float64(report_data['delta']) / np.float64(report_data['underlyerMultiplier'])
    report_data['deltaCash'] = np.float64(report_data['delta']) * np.float64(report_data['underlyerPrice'])
    report_data['gamma'] = np.float64(report_data['gamma']) * np.float64(report_data['underlyerPrice']) / np.float64(
        report_data['underlyerMultiplier']) / 100
    report_data['gammaCash'] = report_data['gamma'] * np.float64(report_data['underlyerPrice']) * np.float64(
        report_data['underlyerMultiplier'])
    report_data['vega'] = np.float64(report_data['vega']) / 100
    report_data['theta'] = np.float64(report_data['theta']) / 365
    report_data['rho'] = np.float64(report_data['rho']) / 100
    report_data['deltaDecay'] = np.float64(report_data['delta']) * -1
    report_data['deltaWithDecay'] = 0
    report_data['createdAt'] = str(datetime.now())

    report_data.drop(['open', 'unwind', 'settle', 'quantity', 'underlyerPrice'], inplace=True, axis=1)
    report_data.fillna('', inplace=True)

    return utils.remove_nan_and_inf(list(report_data.reset_index().to_dict(orient='index').values()))
