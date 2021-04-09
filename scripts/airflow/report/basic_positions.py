# -*- coding: utf-8 -*-

from utils import utils
from datetime import datetime

_TRADE_STATUS_LIVE = 'LIVE'
_UNIT_TYPE_LOT = 'LOT'
_UNIT_TYPE_PERCENT = 'PERCENT'
_DIRECTION_BUYER = 'BUYER'
_LCM_EVENT_SETTLE = 'SETTLE'
_LCM_EVENT_UNWIND = 'UNWIND'
_LCM_EVENT_EXPIRATION = 'EXPIRATION'
_LCM_EVENT_EXERCISE = 'EXERCISE'
_LCM_EVENT_KNOCK_OUT = 'KNOCK_OUT'
_LCM_EVENT_SNOW_BALL_EXERCISE = 'SNOW_BALL_EXERCISE'
_LCM_EVENT_PHOENIX_EXERCISE = 'PHOENIX_EXERCISE'
_datetime_fmt = '%Y-%m-%dT%H:%M:%S'
_date_fmt = '%Y-%m-%d'


def get_eod_positions(domain, headers):
    positions, _, index = get_positions(domain, headers)
    return positions, index


def get_intraday_positions(domain, headers):
    return get_positions(domain, headers)


def actual_notional(notional, notional_type, annualized, term, days_in_year, init_spot, multiplier):
    """Return actual notional in origin currency."""
    if notional is None:
        return 0
    notional_in_currency = notional if notional_type != _UNIT_TYPE_LOT else notional * init_spot * multiplier
    return notional_in_currency * term / days_in_year if annualized else notional_in_currency


def actual_premium(premium, premium_type, notional, direction):
    """Return actual premium in origin currency."""
    if premium is None:
        return 0
    sign = -1 if direction == _DIRECTION_BUYER else 1
    return sign * premium * notional if premium_type == _UNIT_TYPE_PERCENT else sign * premium


def get_positions(domain, headers):
    """Return LIVE(not expiring) positions, expiring positions and position index."""
    # retrieve all trades.
    trade_data = utils.call_request(domain, 'trade-service', 'trdTradeSearch', {}, headers)
    if 'result' in trade_data:
        trades = trade_data['result']
        positions = {}
        expiring = {}
        position_index = {}
        trade_position_ids = []
        timestamp = datetime.now().strftime(_date_fmt)
        for t in trades:
            for p in t['positions']:
                position_index[p['positionId']] = {
                    'positionId': p['positionId'],
                    'tradeId': t['tradeId'],
                    'bookName': t['bookName'],
                    'underlyerInstrumentId': p['asset']['underlyerInstrumentId'],
                    'underlyerMultiplier': p['asset']['underlyerMultiplier']
                }
            if t['tradeStatus'] != _TRADE_STATUS_LIVE:
                continue
            for p in t['positions']:
                if bad_data(p) or closed(p):
                    continue
                trade_position_ids.append({
                    'tradeId': t['tradeId'],
                    'positionId': p['positionId']
                })
                p['bookName'] = t['bookName']
                p['tradeId'] = t['tradeId']
                p['tradeDate'] = t['tradeDate']
                asset = p.pop('asset')
                for a in asset:
                    p[a] = asset[a]
                act_notional = actual_notional(p['notionalAmount'], p['notionalAmountType'], p['annualized'],
                                               p.get('term', None), p.get('daysInYear', 365), p['initialSpot'],
                                               p['underlyerMultiplier'])
                p['actualNotional'] = act_notional
                p['actualPremium'] = actual_premium(p['premium'], p['premiumType'], act_notional, p['direction'])
                p['quantity'] = act_notional / p['initialSpot']
                p['initialNumber'] = 0
                p['unwindNumber'] = 0
                if p['expirationDate'] == timestamp:
                    expiring[p['positionId']] = p
                else:
                    positions[p['positionId']] = p
        # unwind number
        unwind_data = utils.call_request(domain, 'trade-service', 'trdTradeLCMUnwindAmountGetAll', {
            'positionIds': trade_position_ids
        }, headers)['result']
        for u in unwind_data:
            if u['positionId'] in expiring:
                p = expiring[u['positionId']]
            else:
                p = positions[u['positionId']]
            scale = p['quantity'] / p['underlyerMultiplier'] / u['remainValue']
            p['initialNumber'] = u['initialValue'] * scale
            p['unwindNumber'] = u['historyValue'] * scale
        return positions, expiring, position_index
    else:
        raise RuntimeError('Failed to fetch trades.')


def get_all_positions(domain, headers):
    """Return LIVE(not expiring) positions, expiring positions and position index."""
    # retrieve all trades.
    trade_data = utils.call_request(domain, 'trade-service', 'trdTradeSearch', {}, headers)
    if 'result' in trade_data:
        trades = trade_data['result']
        positions = {}
        position_index = {}
        trade_position_ids = []
        for t in trades:
            for p in t['positions']:
                position_index[p['positionId']] = {
                    'positionId': p['positionId'],
                    'tradeId': t['tradeId'],
                    'bookName': t['bookName'],
                    'underlyerInstrumentId': p['asset']['underlyerInstrumentId'],
                    'underlyerMultiplier': p['asset']['underlyerMultiplier']
                }
            for p in t['positions']:
                if bad_data(p):
                    continue
                trade_position_ids.append({
                    'tradeId': t['tradeId'],
                    'positionId': p['positionId']
                })
                p['bookName'] = t['bookName']
                p['tradeId'] = t['tradeId']
                p['status'] = t['tradeStatus']
                p['tradeDate'] = t['tradeDate']
                asset = p.pop('asset')
                for a in asset:
                    p[a] = asset[a]
                act_notional = actual_notional(p['notionalAmount'], p['notionalAmountType'], p['annualized'],
                                               p.get('term', None), p.get('daysInYear', 365), p['initialSpot'],
                                               p['underlyerMultiplier'])
                p['actualNotional'] = act_notional
                p['actualPremium'] = actual_premium(p['premium'], p['premiumType'], act_notional, p['direction'])
                p['quantity'] = act_notional / p['initialSpot']
                p['initialNumber'] = 0
                p['unwindNumber'] = 0
                positions[p['positionId']] = p
        # unwind number
        unwind_data = utils.call_request(domain, 'trade-service', 'trdTradeLCMUnwindAmountGetAll', {
            'positionIds': trade_position_ids
        }, headers)['result']
        for u in unwind_data:
            p = positions[u['positionId']]
            if p['underlyerMultiplier'] == 0 or u['remainValue'] == 0:
                continue
            scale = p['quantity'] / p['underlyerMultiplier'] / u['remainValue']
            p['initialNumber'] = u['initialValue'] * scale
            p['unwindNumber'] = u['historyValue'] * scale
        return positions, position_index
    else:
        raise RuntimeError('Failed to fetch trades.')


def bad_data(p):
    a = p['asset']
    return (a['underlyerMultiplier'] is None) or (a['underlyerMultiplier'] == 0) or (a['initialSpot'] is None) or (
            a['initialSpot'] == 0)


def closed(p):
    return p['lcmEventType'] in [_LCM_EVENT_SETTLE, _LCM_EVENT_UNWIND, _LCM_EVENT_EXPIRATION, _LCM_EVENT_EXERCISE,
                                 _LCM_EVENT_KNOCK_OUT, _LCM_EVENT_SNOW_BALL_EXERCISE, _LCM_EVENT_PHOENIX_EXERCISE]
