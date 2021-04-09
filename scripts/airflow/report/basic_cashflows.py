# -*- coding: utf-8 -*-

from utils import utils
from datetime import datetime

_EVENT_TYPE_OPEN = 'OPEN'
_EVENT_TYPE_UNWIND = 'UNWIND'
_EVENT_TYPE_UNWIND_PARTIAL = 'UNWIND_PARTIAL'
_EVENT_TYPE_AMEND = 'AMEND'
_date_fmt = '%Y-%m-%d'


def get_cash_flow(domain, headers):
    """Return total cash flows of each position.

    Information other than cash flow amount is according to the last cash flow."""
    cash_flow_data = utils.call_request(
        domain, 'trade-service', 'trdTradeCashFlowListAll', {}, headers)
    if 'result' in cash_flow_data:
        position_ids = set([c['positionId'] for c in cash_flow_data['result']])
        # set up empty map
        cash_flows = {p: {
            'positionId': p,
            'open': 0,
            'unwind': 0,
            'settle': 0,
            'timestamp': None,  # last event time
            'lcmEventType': None  # last event type
        } for p in position_ids}
        # fill in
        for c in cash_flow_data['result']:
            p_id = c['positionId']
            cf = cash_flows[p_id]
            if c['cashFlow'] is not None:
                if c['lcmEventType'] == _EVENT_TYPE_OPEN:
                    cf['open'] += c['cashFlow']
                elif c['lcmEventType'] in [_EVENT_TYPE_UNWIND, _EVENT_TYPE_UNWIND_PARTIAL]:
                    cf['unwind'] += c['cashFlow']
                elif c['lcmEventType'] == _EVENT_TYPE_AMEND:
                    cf['open'] += c['cashFlow']
                else:
                    cf['settle'] += c['cashFlow']
                if cf['timestamp'] is None or cf['timestamp'] < c['createdAt']:
                    cf['timestamp'] = c['createdAt']
                    cf['lcmEventType'] = c['lcmEventType']
            else:
                print(p_id + "'s " + c['lcmEventType'] + " does not have cashFlow")
        return cash_flows
    else:
        raise RuntimeError('Failed to fetch cash flows.')


def get_cash_flows_today(domain, headers):
    """Return today's cash flow for each position."""
    cash_flow_data = utils.call_request(domain, 'trade-service', 'trdTradeLCMEventSearchByDate',
                                        {'date': datetime.now().strftime(_date_fmt)}, headers)
    if 'result' in cash_flow_data:
        position_ids = set([c['positionId'] for c in cash_flow_data['result']])
        # set up empty map
        cash_flows = {p: {
            'positionId': p,
            'open': 0,
            'unwind': 0,
            'settle': 0,
            'timestamp': None,  # last event time
            'lcmEventType': None  # last event type
        } for p in position_ids}
        # fill in
        for c in cash_flow_data['result']:
            p_id = c['positionId']
            cf = cash_flows[p_id]
            if c['cashFlow'] is not None:
                if c['lcmEventType'] == _EVENT_TYPE_OPEN:
                    cf['open'] += c['cashFlow']
                elif c['lcmEventType'] in [_EVENT_TYPE_UNWIND, _EVENT_TYPE_UNWIND_PARTIAL]:
                    cf['unwind'] += c['cashFlow']
                else:
                    cf['settle'] += c['cashFlow']
                if cf['timestamp'] is None or cf['timestamp'] < c['createdAt']:
                    cf['timestamp'] = c['createdAt']
                    cf['lcmEventType'] = c['lcmEventType']
            else:
                print(p_id + "'s " + c['lcmEventType'] + " does not have cashFlow")
        return cash_flows
    else:
        raise RuntimeError("Failed to fetch today's cash flows.")
