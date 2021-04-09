# -*- coding: utf-8 -*-
import pandas as pd
import numpy as np
from timeit import default_timer as timer
from utils import utils
from config.bct_config import MAX_PRICING_TRADES_NUM

_datetime_fmt = '%Y-%m-%dT%H:%M:%S'
_PRODUCT_TYPE_SPREADS = ['RATIO_SPREAD_EUROPEAN', 'SPREAD_EUROPEAN']


def get_risks(positions, pricing_environment, valuation_datetime, domain, headers):
    """Return pricing results of given positions.
    positions_list: list of basic positions(live, and possibly expiring positions)"""

    start = timer()
    positions_trade_id = list(positions.tradeId.unique())
    trade_batches = list(utils.chunks(positions_trade_id, MAX_PRICING_TRADES_NUM))
    pricing_result = []
    pricing_diagnostics = []
    for trade_batch in trade_batches:
        params = {
            'requests': ['price', 'delta', 'gamma', 'vega', 'theta', 'rho_r'],
            'tradeIds': trade_batch,
            'pricingEnvironmentId': pricing_environment,
            'valuationDateTime': '{}T00:00:00'.format(valuation_datetime),
            'timezone': None
        }
        res = utils.call_request(domain, 'pricing-service', 'prcPrice', params, headers)
        pricing_result.extend(res.get('result') if 'result' in res else [])
        pricing_diagnostics.extend(res.get('diagnostics') if 'diagnostics' in res else [])
    end = timer()
    print('pricing all trades take ' + str(end - start) + ' seconds')
    if len(positions_trade_id) > 0 and len(pricing_result) > 0:
        start = timer()
        price_data = pd.DataFrame(pricing_result, dtype='double')
        risk_df = price_data
        # fill missing elements of cash flows
        risk_df[['vol', 'q']].fillna(0, inplace=True)
        # risk_df['underlyerPrice'].fillna(-1, inplace=True)  # impossible value
        diagnostics_df = pd.DataFrame(pricing_diagnostics)
        if not diagnostics_df.empty:
            diagnostics_df.rename(columns={'key': 'positionId'}, inplace=True)
            diagnostics_df = diagnostics_df[~diagnostics_df.positionId.duplicated(keep='last')]
            risk_df = risk_df.merge(diagnostics_df, on='positionId', how='outer')
        risk_df['message'] = ''
        # pointless in jkzx
        # multi_asset_position_ids = list(positions[positions.productType.isin(_PRODUCT_TYPE_SPREADS)].tradeId.unique())
        # risk_df['message'] = risk_df.apply(lambda row: get_message(row, multi_asset_position_ids), axis=1)

        pe_description = utils.get_pricing_env_description(pricing_environment, domain, headers)
        risk_df['pricing_environment'] = pe_description
        risk_df.set_index('positionId', inplace=True)
        for prc in ['qs', 'vols', 'underlyerPrices', 'deltas', 'gammas', 'vegas']:
            if prc not in risk_df.columns:
                risk_df[prc] = np.nan
        end = timer()
        print('normalize risk results take ' + str(end - start) + ' seconds')
        return risk_df
    else:
        raise RuntimeError('Failed to price trades.')


def get_message(row, multi_asset_position_ids):
    message = row.get('message', '')
    pid = row.get('positionId', '')
    requests = ['price', 'deltas', 'gammas', 'vegas', 'theta'] if pid in multi_asset_position_ids \
        else ['price', 'delta', 'gamma', 'vega', 'theta', 'rhoR']
    if utils.is_nan(message):
        for req in requests:
            if utils.is_nan(np.float64(row[req])):
                return '定价返回空'
        return ''
    else:
        return message
