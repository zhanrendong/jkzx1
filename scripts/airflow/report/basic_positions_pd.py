# -*- coding: utf-8 -*-
from pandas.io.json import json_normalize

from utils import utils
import pandas as pd
import numpy as np
from timeit import default_timer as timer

_TRADE_STATUS_LIVE = 'LIVE'
_UNIT_TYPE_LOT = 'LOT'
_UNIT_TYPE_PERCENT = 'PERCENT'
_DIRECTION_BUYER = 'BUYER'
_PAYMENT_DIRECTION_PAY = 'PAY'
_PRODUCT_TYPE_CASH_FLOW = 'CASH_FLOW'
_PRODUCT_TYPE_SPREADS = ['RATIO_SPREAD_EUROPEAN', 'SPREAD_EUROPEAN']
_LCM_EVENT_SETTLE = 'SETTLE'
_LCM_EVENT_UNWIND = 'UNWIND'
_LCM_EVENT_EXPIRATION = 'EXPIRATION'
_LCM_EVENT_EXERCISE = 'EXERCISE'
_LCM_EVENT_KNOCK_OUT = 'KNOCK_OUT'
_LCM_EVENT_PAYMENT = 'PAYMENT'
_LCM_EVENT_SNOW_BALL_EXERCISE = 'SNOW_BALL_EXERCISE'
_LCM_EVENT_PHOENIX_EXERCISE = 'PHOENIX_EXERCISE'
_datetime_fmt = '%Y-%m-%dT%H:%M:%S'
_date_fmt = '%Y-%m-%d'
IGNORED_FIELDS = ['createdAt', 'updatedAt']
IGNORED_FIELDS_REGEX = 'asset.fixing|asset.observationDates|asset.knockInObservationDates|asset.constituents|asset.observationBarriers'
CLOSED_POSITION_STATUS = [
    _LCM_EVENT_SETTLE, _LCM_EVENT_UNWIND, _LCM_EVENT_EXPIRATION, _LCM_EVENT_EXERCISE,
    _LCM_EVENT_KNOCK_OUT, _LCM_EVENT_SNOW_BALL_EXERCISE, _LCM_EVENT_PHOENIX_EXERCISE,
    _LCM_EVENT_PAYMENT]
VALID_PRODUCT_TYPES = ['VANILLA_EUROPEAN', 'VANILLA_AMERICAN']
VALID_ASSET_CLASS = ['EQUITY', 'COMMODITY']
BATCH_SIZE = 20000
PAGE_SIZE = 1000000


def positions_dataframe(trades):
    position_list = []
    for trade in trades:
        for position in trade.pop('positions'):
            if position.get('assetClass') not in VALID_ASSET_CLASS \
                    or position.get('productType') not in VALID_PRODUCT_TYPES:
                continue
            ps = trade.copy()
            del position['bookName']
            ps.update(position)
            position_list.append(ps)
    positions_df = json_normalize(position_list)
    col_to_drop = list(positions_df.filter(regex=IGNORED_FIELDS_REGEX).columns)
    col_to_drop.extend(IGNORED_FIELDS)
    positions_df.drop(col_to_drop, axis=1, inplace=True)
    # pointless in jkzx
    # cash flows are special: underlyerInstrumntId = CNY，underlyerMultiplier = 1
    # cf_idx = positions_df[positions_df['productType'] == _PRODUCT_TYPE_CASH_FLOW].index
    # if len(cf_idx) > 0:
    #     positions_df.loc[cf_idx, 'asset.underlyerInstrumentId'] = 'CNY'
    #     positions_df.loc[cf_idx, 'asset.underlyerMultiplier'] = 1
    #     positions_df.loc[cf_idx, 'asset.notionalAmount'] = positions_df.loc[cf_idx, 'asset.paymentAmount']
    #     positions_df.loc[cf_idx, 'asset.notionalAmountType'] = positions_df.loc[cf_idx, 'asset.currency']
    #     positions_df.loc[cf_idx, 'asset.expirationDate'] = positions_df.loc[cf_idx, 'asset.settlementDate']
    return positions_df


def get_eod_positions(domain, headers, valuation_date):
    start = timer()
    positions, _, index = get_positions(domain, headers, valuation_date)
    end = timer()
    print('get eod positions takes ' + str(end - start) + ' seconds')
    return positions, index


def get_intraday_positions(domain, headers, valuation_date):
    return get_positions(domain, headers, valuation_date)


def actual_notional(data):
    """Return actual notional in origin currency."""
    if data['productType'] == _PRODUCT_TYPE_CASH_FLOW:
        return data['asset.paymentAmount']
    notional = np.float64(data['asset.notionalAmount'])
    notional_type = data['asset.notionalAmountType']
    annualized = data['asset.annualized']
    term = np.float64(data['asset.term'])
    days_in_year = np.float64(data['asset.daysInYear'])
    init_spot = np.float64(data['asset.initialSpot'])
    multiplier = np.float64(data['asset.underlyerMultiplier'])

    if utils.is_nan(notional):
        return 0
    notional_in_currency = notional if notional_type != _UNIT_TYPE_LOT else notional * init_spot * multiplier
    return notional_in_currency * term / days_in_year if annualized else notional_in_currency


def actual_premium(data):
    """Return actual premium in origin currency."""
    if data['productType'] == _PRODUCT_TYPE_CASH_FLOW:
        return 0
    premium = np.float64(data['asset.premium'])
    premium_type = data['asset.premiumType']
    notional = np.float64(data['asset.actualNotional'])
    direction = data['asset.direction']

    if utils.is_nan(premium):
        return 0
    sign = -1 if direction == _DIRECTION_BUYER else 1
    return sign * premium * notional if premium_type == _UNIT_TYPE_PERCENT else sign * premium


def process_single_asset_positions(positions, domain, headers, valuation_date):
    def chunks(l, n):
        """Yield successive n-sized chunks from l."""
        for i in range(0, len(l), n):
            yield l[i:i + n]

    live_positions_df = positions[~positions['lcmEventType'].isin(CLOSED_POSITION_STATUS)]
    live_positions_df['asset.actualNotional'] = live_positions_df.apply(lambda row: actual_notional(row), axis=1)
    live_positions_df['actualPremium'] = live_positions_df.apply(lambda row: actual_premium(row), axis=1)
    live_positions_df['quantity'] = np.float64(live_positions_df['asset.actualNotional']) / np.float64(
        live_positions_df['asset.initialSpot'])

    #     trade_position_ids = list(map(
    #         lambda item: {'tradeId': item[1], 'positionId': item[0]}, live_positions_df['tradeId'].iteritems()))
    #     batch = list(chunks(trade_position_ids, BATCH_SIZE))
    #     unwind_data = []
    #     for ids in batch:
    #         data = utils.call_request(domain, 'trade-service', 'trdTradeLCMUnwindAmountGetAll', {
    #             'positionIds': ids}, headers)
    #         print('finish querying ' + str(len(ids)) + ' trades')
    #         if 'result' in data:
    #             unwind_data.extend(data['result'])
    #         else:
    #             raise RuntimeError('Failed to fetch cash flow.')
    #     print('fetch ' + str(len(unwind_data)) + ' events')
    #     unwind_df = pd.DataFrame(unwind_data).drop('tradeId', axis=1)
    #     live_pos_with_unwind = pd.merge(live_positions_df, unwind_df, on='positionId')
    #     live_pos_with_unwind['scalingFactor'] = np.float64(live_pos_with_unwind['quantity']) / np.float64(
    #         live_pos_with_unwind['asset.underlyerMultiplier']) / np.float64(live_pos_with_unwind['remainValue'])
    #     live_pos_with_unwind['initialNumber'] = np.float64(live_pos_with_unwind['initialValue']) * np.float64(
    #         live_pos_with_unwind['scalingFactor'])
    #     live_pos_with_unwind['unwindNumber'] = np.float64(live_pos_with_unwind['historyValue']) * np.float64(
    #         live_pos_with_unwind['scalingFactor'])

    # future cash flows
    # cf_idx = live_pos_with_unwind[live_pos_with_unwind['productType'] == _PRODUCT_TYPE_CASH_FLOW].index
    # if len(cf_idx) > 0:
    #     live_pos_with_unwind.loc[cf_idx, 'quantity'] = live_pos_with_unwind.loc[
    #         cf_idx, 'asset.paymentDirection'].map(lambda d: -1 if d == _PAYMENT_DIRECTION_PAY else 1)
    #     live_pos_with_unwind.loc[cf_idx, 'initialNumber'] = live_pos_with_unwind.loc[cf_idx, 'quantity']
    #     live_pos_with_unwind.loc[cf_idx, 'unwindNumber'] = 0

    expiring_cond = live_positions_df['asset.expirationDate'] == valuation_date
    expiring_live_positions_df = live_positions_df[expiring_cond]
    non_expiring_live_positions_df = live_positions_df[~expiring_cond]
    return non_expiring_live_positions_df, expiring_live_positions_df


def process_multi_asset_positions(positions, domain, headers, valuation_date):
    live_positions_df = positions[~positions['lcmEventType'].isin(CLOSED_POSITION_STATUS)]
    live_positions_df['asset.actualNotional'] = live_positions_df.apply(lambda row: actual_notional(row), axis=1)
    live_positions_df['actualPremium'] = live_positions_df.apply(lambda row: actual_premium(row), axis=1)
    live_positions_df['quantity1'] = np.float64(live_positions_df['asset.actualNotional']) / np.float64(
        live_positions_df['asset.initialSpot1'])
    live_positions_df['quantity2'] = np.float64(live_positions_df['asset.actualNotional']) / np.float64(
        live_positions_df['asset.initialSpot2'])

    trade_position_ids = list(map(
        lambda item: {'tradeId': item[1], 'positionId': item[0]}, live_positions_df['tradeId'].iteritems()))
    # unwind number
    unwind_data = utils.call_request(domain, 'trade-service', 'trdTradeLCMUnwindAmountGetAll', {
        'positionIds': trade_position_ids}, headers)
    if 'result' in unwind_data:
        unwind_df = pd.DataFrame(unwind_data['result']).drop('tradeId', axis=1)
        live_pos_with_unwind = pd.merge(live_positions_df, unwind_df, on='positionId')
        live_pos_with_unwind['scalingFactor1'] = np.float64(live_pos_with_unwind['quantity1']) / np.float64(
            live_pos_with_unwind['asset.underlyerMultiplier1']) / np.float64(live_pos_with_unwind['remainValue'])
        live_pos_with_unwind['initialNumber1'] = np.float64(live_pos_with_unwind['initialValue']) * np.float64(
            live_pos_with_unwind['scalingFactor1'])
        live_pos_with_unwind['unwindNumber1'] = np.float64(live_pos_with_unwind['historyValue']) * np.float64(
            live_pos_with_unwind['scalingFactor1'])

        live_pos_with_unwind['scalingFactor2'] = np.float64(live_pos_with_unwind['quantity2']) / np.float64(
            live_pos_with_unwind['asset.underlyerMultiplier2']) / np.float64(live_pos_with_unwind['remainValue'])
        live_pos_with_unwind['initialNumber2'] = np.float64(live_pos_with_unwind['initialValue']) * np.float64(
            live_pos_with_unwind['scalingFactor2'])
        live_pos_with_unwind['unwindNumber2'] = np.float64(live_pos_with_unwind['historyValue']) * np.float64(
            live_pos_with_unwind['scalingFactor2'])

        expiring_cond = live_pos_with_unwind['asset.expirationDate'] == valuation_date
        expiring_live_positions_df = live_pos_with_unwind[expiring_cond]
        non_expiring_live_positions_df = live_pos_with_unwind[~expiring_cond]
        return non_expiring_live_positions_df, expiring_live_positions_df
    else:
        raise RuntimeError('Failed to fetch trades.')


def get_positions(domain, headers, valuation_date):
    """Return LIVE(not expiring) positions, expiring positions and position index."""
    # retrieve all trades.
    trade_data = utils.call_request(domain, 'trade-service', 'trdTradeLivedList', {}, headers)
    if 'result' in trade_data:
        trades = trade_data['result']
        if not trades:
            return pd.DataFrame(), pd.DataFrame(), pd.DataFrame()
        start = timer()
        positions_df = positions_dataframe(trades)
        end = timer()
        print('convert ' + str(len(trades)) + ' takes ' + str(end - start) + ' seconds')
        position_index = positions_df[
            ['tradeId', 'bookName', 'asset.underlyerInstrumentId', 'asset.underlyerMultiplier', 'productType']]
        # multi_asset_position_criteria = positions_df.productType.isin(_PRODUCT_TYPE_SPREADS)
        # single_asset_positions = positions_df[~multi_asset_position_criteria]
        # multi_asset_positions = positions_df[multi_asset_position_criteria]
        start = timer()
        non_expiring_pos, expiring_pos = process_single_asset_positions(positions_df, domain, headers,
                                                                        valuation_date)
        end = timer()
        print('process single asset positions take ' + str(end - start) + ' seconds')
        # if not multi_asset_positions.empty:
        #     start = timer()
        #     position_index = positions_df[
        #         ['tradeId', 'bookName', 'asset.underlyerInstrumentId', 'asset.underlyerInstrumentId1',
        #          'asset.underlyerInstrumentId2', 'asset.underlyerMultiplier', 'asset.underlyerMultiplier1',
        #          'asset.underlyerMultiplier2', 'productType']]
        #     non_expiring_pos2, expiring_pos2 = process_multi_asset_positions(multi_asset_positions, domain, headers,
        #                                                                      valuation_date)
        #     if not non_expiring_pos2.empty:
        #         non_expiring_pos = pd.concat([non_expiring_pos, non_expiring_pos2], sort=False)
        #     if not expiring_pos2.empty:
        #         expiring_pos = pd.concat([expiring_pos, expiring_pos2], sort=False)
        #     end = timer()
        #     print('process single asset positions take ' + str(end - start) + ' seconds')
        return non_expiring_pos, expiring_pos, position_index
    else:
        raise RuntimeError('Failed to fetch trades.')


def get_today_terminated_positions(domain, headers):
    param_terminated = {
        'page': 0,
        'pageSize': PAGE_SIZE,
        'status': 'TERMINIATED_TODAY'
    }
    today_terminated_trade_data = utils.call_request(domain, 'trade-service', 'trdTradeSearchIndexPaged',
                                                     param_terminated, headers)
    if 'result' not in today_terminated_trade_data:
        raise RuntimeError('Failed to fetch today terminated trades.')
    trades = today_terminated_trade_data['result']['page']
    if len(trades) == 0:
        return pd.DataFrame()
    terminated_positions_df = positions_dataframe(trades)
    terminated_position_index = terminated_positions_df[
        ['tradeId', 'bookName', 'asset.underlyerInstrumentId', 'asset.underlyerMultiplier', 'productType']]
    return terminated_position_index
