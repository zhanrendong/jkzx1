# -*- coding: utf-8 -*-
from pandas.io.json import json_normalize

from utils import utils
from datetime import datetime
import numpy as np
import pandas as pd
from timeit import default_timer as timer

_EVENT_TYPE_OPEN = 'OPEN'
_EVENT_TYPE_UNWIND = 'UNWIND'
_EVENT_TYPE_UNWIND_PARTIAL = 'UNWIND_PARTIAL'
_EVENT_TYPE_AMEND = 'AMEND'
_date_fmt = '%Y-%m-%d'


def process_cash_flow(data, cash_flows_today):
    if len(data) == 0:
        return pd.DataFrame()
    required_fields = ['positionId', 'cashFlow', 'lcmEventType', 'premium', 'createdAt']
    cfs = list(map(lambda cf: {k: v for k, v in cf.items() if k in required_fields}, data))
    cashflow_df = json_normalize(cfs)
    cashflow_df = cashflow_df[cashflow_df.createdAt == cashflow_df.createdAt].fillna(0)
    open_cashflow_rows = cashflow_df['lcmEventType'] == _EVENT_TYPE_OPEN
    unwind_cashflow_rows = cashflow_df['lcmEventType'].isin([_EVENT_TYPE_UNWIND, _EVENT_TYPE_UNWIND_PARTIAL])
    amend_cashflow_rows = cashflow_df['lcmEventType'] == _EVENT_TYPE_AMEND
    open_cashflow_df = cashflow_df[open_cashflow_rows]
    unwind_cashflow_df = cashflow_df[unwind_cashflow_rows]
    amend_cashflow_df = cashflow_df[amend_cashflow_rows]
    other_cashflow_df = cashflow_df[~(open_cashflow_rows | unwind_cashflow_rows | amend_cashflow_rows)]
    open_sum = open_cashflow_df.groupby('positionId')[['cashFlow']].agg(np.sum).reset_index()
    open_sum.rename(columns={'cashFlow': 'open'}, inplace=True)
    unwind_sum = unwind_cashflow_df.groupby('positionId')[['cashFlow']].agg(np.sum).reset_index()
    unwind_sum.rename(columns={'cashFlow': 'unwind'}, inplace=True)
    settle_sum = other_cashflow_df.groupby('positionId')[['cashFlow']].agg(np.sum).reset_index()
    settle_sum.rename(columns={'cashFlow': 'settle'}, inplace=True)
    amend_sum = amend_cashflow_df.groupby('positionId')[['cashFlow']].agg(np.sum).reset_index()
    if cash_flows_today:
        amend_sum.rename(columns={'cashFlow': 'settle'}, inplace=True)
    else:
        amend_sum.rename(columns={'cashFlow': 'open'}, inplace=True)

    all_sum = pd.concat([open_sum, unwind_sum, settle_sum, amend_sum], sort=False).fillna(0).groupby('positionId').agg(
        np.sum).reset_index()
    latest_cf_idx = cashflow_df.groupby(['positionId'])['createdAt'].transform(np.max) == cashflow_df['createdAt']
    latest_cf_df = cashflow_df[latest_cf_idx][['positionId', 'createdAt', 'lcmEventType']]
    latest_cf_df.rename(columns={'createdAt': 'timestamp'}, inplace=True)
    return all_sum.fillna(0).merge(latest_cf_df, on='positionId', how='left')


def get_cash_flow(domain, headers):
    """Return total cash flows of each position.
    Information other than cash flow amount is according to the last cash flow."""
    start = timer()
    cash_flow_data = utils.call_request(domain, 'trade-service', 'trdTradeCashFlowListAll', {}, headers)
    end = timer()
    print('fetch trade cashflow takes ' + str(end - start) + ' seconds')
    if 'result' in cash_flow_data:
        start = timer()
        cf_df = process_cash_flow(cash_flow_data['result'], False)
        end = timer()
        print('process cashflow takes ' + str(end - start) + ' seconds')
        return cf_df
    else:
        raise RuntimeError('Failed to fetch cash flows.')


def get_cash_flows_today(domain, headers):
    """Return today's cash flow for each position."""
    cash_flow_data = utils.call_request(domain, 'trade-service', 'trdTradeLCMEventSearchByDate',
                                        {'date': datetime.now().strftime(_date_fmt)}, headers)
    if 'result' in cash_flow_data:
        return process_cash_flow(cash_flow_data['result'], True)
    else:
        raise RuntimeError("Failed to fetch today's cash flows.")
