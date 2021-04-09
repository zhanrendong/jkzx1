# -*- coding: utf-8 -*-
from datetime import datetime

from report.basic_quotes_pd import get_underlyer_quotes
from utils import utils

_TRADE_STATUS_LIVE = 'LIVE'
_UNIT_TYPE_PERCENT = 'PERCENT'
_UNIT_TYPE_LOT = 'LOT'
_DIRECTION_BUYER = 'BUYER'
_date_fmt = '%Y-%m-%d'

_EVENT_TYPE_OPEN = 'OPEN'
_EVENT_TYPE_UNWIND = 'UNWIND'


def option_total_pnl(positions, risks, cash_flows):
    """Return option total PnL's (per position).
    positions: result of get_positions() in report_eod_basic
    risks: result of get_risks() in report_eod_basic
    cash_flows: result of get_cash_flows() in report_eod_basic
    """
    # option pnl
    position_pnl = positions[['positionId', 'tradeId', 'asset.underlyerInstrumentId', 'lcmEventType']]
    position_pnl = position_pnl.merge(risks[['price']].reset_index(), on='positionId', how='left')
    position_pnl.rename(columns={'asset.underlyerInstrumentId': 'underlyerInstrumentId', 'price': 'marketValue'},
                        inplace=True)
    position_pnl = position_pnl.merge(cash_flows[['open', 'unwind', 'settle', 'positionId']], on='positionId',
                                      how='left').fillna(0)
    position_pnl['pnl'] = position_pnl['marketValue'] + position_pnl['open'] + position_pnl['unwind'] + position_pnl[
        'settle']
    return position_pnl


def premium_amount(direction, premium, premium_type, notional, notional_type, annualized,
                   term, days_in_year, init_spot, multiplier):
    """Return actual premium amount."""
    if premium is None or notional is None:
        return 0
    sign = -1 if direction == _DIRECTION_BUYER else 1
    if premium_type != _UNIT_TYPE_PERCENT:
        return premium * sign
    notional_amount = notional if notional_type != _UNIT_TYPE_LOT else notional * multiplier * init_spot
    if annualized:
        notional_amount = notional_amount * term / days_in_year
    return premium * notional_amount * sign


def quantity(notional, notional_type, annualized, term, days_in_year, init_spot, multiplier):
    """Return quantity of position."""
    if notional is None:
        return 0
    if notional_type == _UNIT_TYPE_LOT:
        return notional
    notional_amount = notional * term / days_in_year if annualized else notional
    return notional_amount / init_spot / multiplier


def client_valuation(positions, pnls, valuation_date, domain, headers):
    """Return client valuation report.

    positions: result of get_positions() in report_eod_basic
    risks: result of get_risks() in report_eod_basic
    pnls: result of option_total_pnl() in report_eod_pnl

    Only take into account clients having LIVE trades.
    """
    val = valuation_date.strftime(_date_fmt)
    # find all clients
    clients = set(positions.counterPartyCode.unique())
    position2client = positions[['counterPartyCode', 'positionId']].set_index('positionId').to_dict(orient='index')
    instrument_ids = set(positions['asset.underlyerInstrumentId'].dropna().unique())
    # empty report
    valuations = {}
    for c in clients:
        valuations[c] = {
            'legalName': c,
            'valuationDate': val,
            'price': 0.0,
            'positions': {}
        }
    # position info
    quotes = get_underlyer_quotes(list(instrument_ids), datetime.now(), domain, headers).to_dict(orient='index')
    for index, p in positions.fillna('').iterrows():
        p_id = p['positionId']
        client = position2client.get(p_id, {}).get('counterPartyCode', None)
        if client is None:
            continue
        valuations[client]['positions'][p_id] = {
            'positionId': p_id,
            'tradeId': p['tradeId'],
            'productType': p['productType'],
            'direction': p['asset.direction'],
            'underlyerInstrumentId': p['asset.underlyerInstrumentId'],
            'quantity': quantity(p['asset.notionalAmount'], p['asset.notionalAmountType'], p['asset.annualized'],
                                 p.get('asset.term', None), p.get('asset.daysInYear', 365), p['asset.initialSpot'],
                                 p['asset.underlyerMultiplier']),
            'tradeDate': p['tradeDate'],
            'expirationDate': p.get('asset.expirationDate', None),
            'premium': premium_amount(p['asset.direction'], p['asset.premium'], p['asset.premiumType'],
                                      p['asset.notionalAmount'],
                                      p['asset.notionalAmountType'], p['asset.annualized'], p.get('asset.term', None),
                                      p.get('asset.daysInYear', 365), p['asset.initialSpot'],
                                      p['asset.underlyerMultiplier']),
            'lcmEventType': p['lcmEventType'],
            'tradeStatus': 'LIVE',
            'optionType': p.get('asset.optionType', None),
            'strike': p.get('asset.strike', None),
            'initialSpot': p['asset.initialSpot'],
            'underlyerPrice': quotes.get(p['asset.underlyerInstrumentId'], {'close', None})['close'],
            'settle': None,
            'settlePnl': None,
            'marketValue': None,
            'marketValuePnl': None
        }
    # market value and pnl
    for index, p in pnls.iterrows():
        p_id = p['positionId']
        client = position2client.get(p_id, {}).get('counterPartyCode', None)
        if client is None:
            continue
        price = p['marketValue']
        valuations[client]['price'] = valuations[client]['price'] + price
        valuation = valuations[client]['positions'][p_id]
        valuation['settle'] = p['unwind'] + p['settle']
        valuation['settlePnl'] = valuation['premium'] + valuation['settle']
        valuation['marketValue'] = price
        valuation['marketValuePnl'] = valuation['settlePnl'] + price
    # make positions list
    for c in valuations:
        valuation = valuations[c]
        valuation['positions'] = list(valuation['positions'].values())
    return list(valuations.values())


def process_and_save_report(client_valuations, ip, headers):
    for v in client_valuations:
        print(v['legalName'], v['price'], v['valuationDate'])
        content = {
            'positions': v['positions'],
            'legalName': v['legalName']
        }
        # fund records
        fund_event_list_data = get_cli_fund_event_list_by_client_id(ip, v['legalName'], headers)
        # sum of funds
        content['fundSum'] = fund_event_list_data[1]
        content['fundEventList'] = fund_event_list_data[0]
        accounts_data = get_accounts_by_legal_name(ip, v['legalName'], headers)
        if accounts_data is None:
            continue
        # credit
        content['credit'] = accounts_data['credit']
        # credit balance
        counterPartyCreditBalance = accounts_data['counterPartyCreditBalance']
        margin_data = get_mgn_margin_by_account_id(ip, accounts_data['accountId'], headers)
        if margin_data is None:
            content['maintenanceMargin'] = 0
        else:
            # maintenance margin
            content['maintenanceMargin'] = margin_data['maintenanceMargin']
        premium_and_settle_sum = 0
        market_value_sum = 0
        for p in v['positions']:
            premium_and_settle_sum += (p['premium'] + p['settle'])
            market_value_sum += p['marketValue']
        # sum of premium and settle
        content['premiumAndSettleSum'] = premium_and_settle_sum
        # market value
        content['marketValueSum'] = market_value_sum
        # prepaid balance
        content['prepaymentBalance'] = content['fundSum'] + premium_and_settle_sum
        # net assets
        content['netAssets'] = content['prepaymentBalance'] + market_value_sum
        # available prepaid balance
        content['prepaymentAvailableBalance'] = content['prepaymentBalance'] + market_value_sum + content[
            'maintenanceMargin']
        content['prepaymentAvailableBalanceWithCredit'] = content[
                                                              'prepaymentAvailableBalance'] + counterPartyCreditBalance
        v['content'] = content
        v.pop('positions')
        save_client_valuation_report(ip, v, headers)


def get_cli_fund_event_list_by_client_id(domain, clientId, headers):
    params = {
        'clientIds': [clientId]
    }
    fund_event_list_data = utils.call_request(domain, 'reference-data-service', 'cliFundEventListByClientIds', params,
                                              headers)
    if 'result' in fund_event_list_data:
        fund_event_list = []
        fund_sum = 0
        for l in fund_event_list_data['result']:
            fund_event = {
                'paymentDate': l['paymentDate'],
                'paymentAmount': l['paymentAmount'] if l['paymentDirection'] == 'IN' else l['paymentAmount'] * -1
            }
            fund_sum += l['paymentAmount'] if l['paymentDirection'] == 'IN' else l['paymentAmount'] * -1
            fund_event_list.append(fund_event)
        return fund_event_list, fund_sum
    else:
        raise RuntimeError('Failed to fetch fund_event_list.')


def get_accounts_by_legal_name(domain, legalName, headers):
    """Return client accounts."""
    params = {
        'legalName': legalName
    }
    try:
        accounts_data = utils.call_request(domain, 'reference-data-service', 'clientAccountSearch', params, headers)
        return accounts_data['result'][0]
    except Exception as e:
        return None


def get_mgn_margin_by_account_id(domain, accountId, headers):
    """Return client accounts."""
    params = {
        'accountIds': [accountId]
    }
    try:
        margin_data = utils.call_request(domain, 'reference-data-service', 'mgnMarginList', params, headers)
        return margin_data['result'][0]
    except Exception as e:
        return None


def save_client_valuation_report(domain, report, headers):
    params = {
        'valuationReports': [report]
    }
    result = utils.call_request(domain, 'report-service', 'rptValuationReportCreateOrUpdate', params, headers)
    if 'error' in result:
        raise RuntimeError('Failed to save valuation report.')
