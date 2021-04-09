# -*- encoding: utf-8 -*-
from utils import utils
from report import basic_positions, basic_risks, basic_cashflows, basic_listed_positions
from report.intraday import intraday_position_report, intraday_expiring_position_report
from report.intraday import intraday_risk_by_underlyer_report, intraday_daily_pnl_by_underlyer_report
from report.eod import eod_position_report, eod_risk_by_underlyer_report
from report.eod import eod_historical_pnl_by_underlyer_report, eod_daily_pnl_by_underlyer_report
from datetime import datetime
from json import JSONDecoder
from config.bct_config import bct_password, bct_user

_tol = 1e-3

books = ['options1', 'options2']


def filter_dict(dic):
    return {k: v for k, v in dic.items() if v['bookName'] in books}


def filter_list(lis):
    return [i for i in lis if i['bookName'] in books]


def filter_underlyer(dic):
    return {k: v for k, v in dic.items() if k in books}


def find_in_dict(dic, position_ids):
    return {k: v for k, v in dic.items() if k in position_ids}


def compare_position_report(report, ref):
    ref = {r['tradeId']: r for r in ref}
    diff = []
    for r in report:
        rr = ref[r['tradeId']]
        same = True
        for i in ['initialNumber', 'unwindNumber', 'number', 'premium', 'unwindAmount', 'marketValue', 'pnl',
                  'delta', 'gamma', 'gammaCash', 'vega', 'theta', 'rho', 'deltaDecay', 'deltaWithDecay']:
            if rr[i] is None or r[i] is None:
                if r[i] == rr[i]:
                    continue
                else:
                    same = False
                    break
            if (rr[i] != 0 and abs((r[i] - rr[i]) / rr[i]) > _tol) or (rr[i] == 0 and abs(r[i]) > _tol):
                same = False
                break
        if not same:
            diff.append({'result': r, 'ref': rr})
    return diff


def compare_risk_by_underlyer_report(report, ref):
    ref = {r['underlyerInstrumentId']: r for r in ref}
    diff = []
    for r in report:
        rr = ref[r['underlyerInstrumentId']]
        same = True
        for i in ['underlyerPrice', 'underlyerPriceChangePercent', 'underlyerNetPosition', 'delta', 'netDelta',
                  'deltaCash','deltaDecay', 'deltaWithDecay', 'gamma', 'gammaCash', 'vega', 'theta', 'rho']:
            if rr[i] is None or r[i] is None:
                if r[i] == rr[i]:
                    continue
                else:
                    same = False
                    break
            if (rr[i] != 0 and abs((r[i] - rr[i]) / rr[i]) > _tol) or (rr[i] == 0 and abs(r[i]) > _tol):
                same = False
                break
        if not same:
            diff.append({'result': r, 'ref': rr})
    return diff


def compare_daily_pnl_report(report, ref):
    ref = {r['underlyerInstrumentId']: r for r in ref}
    diff = []
    for r in report:
        rr = ref[r['underlyerInstrumentId']]
        same = True
        for i in ['dailyPnl', 'dailyOptionPnl', 'dailyUnderlyerPnl', 'pnlContributionNew', 'pnlContributionSettled',
                  'pnlContributionDelta', 'pnlContributionGamma', 'pnlContributionVega', 'pnlContributionTheta',
                  'pnlContributionRho']:
            if rr[i] is None or r[i] is None:
                if r[i] == rr[i]:
                    continue
                else:
                    same = False
                    break
            if (rr[i] != 0 and abs((r[i] - rr[i]) / rr[i]) > _tol) or (rr[i] == 0 and abs(r[i]) > _tol):
                same = False
                break
        if not same:
            diff.append({'result': r, 'ref': rr})
    return diff


def compare_historical_pnl_report(report, ref):
    ref = {r['underlyerInstrumentId']: r for r in ref}
    diff = []
    for r in report:
        rr = ref[r['underlyerInstrumentId']]
        same = True
        for i in ['pnl', 'optionPremium', 'optionUnwindAmount', 'optionSettleAmount', 'optionMarketValue', 'optionPnl',
                  'underlyerBuyAmount', 'underlyerSellAmount', 'underlyerNetPosition', 'underlyerPrice',
                  'underlyerMarketValue', 'underlyerPnl']:
            if rr[i] is None or r[i] is None:
                if r[i] == rr[i]:
                    continue
                else:
                    same = False
                    break
            if (rr[i] != 0 and abs((r[i] - rr[i]) / rr[i]) > _tol) or (rr[i] == 0 and abs(r[i]) > _tol):
                same = False
                break
        if not same:
            diff.append({'result': r, 'ref': rr})
    return diff


if __name__ == '__main__':
    domain = 'localhost'
    headers = utils.login(domain, {
        'userName': bct_user,
        'password': bct_password
    })

    mismatch = False

    # intraday
    pricing_environment = 'DEFAULT_INTRADAY'
    val_time = datetime.now().replace(hour=13, minute=0, second=0, microsecond=0)

    positions, expirings, index = basic_positions.get_intraday_positions(domain, headers)
    positions = filter_dict(positions)
    expirings = filter_dict(expirings)
    index = filter_dict(index)

    risks = basic_risks.get_risks([positions, expirings], pricing_environment, val_time, domain, headers)[0]
    risks = find_in_dict(risks, index)

    cash_flows = basic_cashflows.get_cash_flow(domain, headers)
    cash_flows = find_in_dict(cash_flows, index)

    cash_flows_today = basic_cashflows.get_cash_flows_today(domain, headers)
    cash_flows_today = find_in_dict(cash_flows_today, index)

    listed_positions = basic_listed_positions.get_underlyer_positions(domain, headers, pricing_environment)
    listed_positions = filter_underlyer(listed_positions)

    intraday_position_report = intraday_position_report.intraday_position_report(
        positions, risks, cash_flows, pricing_environment, domain, headers)
    intraday_position_report = filter_list(intraday_position_report)

    intraday_expiring_report = intraday_expiring_position_report.intraday_expiring_position_report(
        expirings, risks, cash_flows)
    intraday_expiring_report = filter_list(intraday_expiring_report)

    intraday_risk_by_underlyer_report = intraday_risk_by_underlyer_report.intraday_risk_by_underlyer_report(
        intraday_position_report, listed_positions, domain, headers, pricing_environment)
    intraday_risk_by_underlyer_report = filter_list(intraday_risk_by_underlyer_report)

    intraday_daily_pnl_report = intraday_daily_pnl_by_underlyer_report.intraday_daily_pnl_by_underlyer_report(
        risks, cash_flows_today, listed_positions, index, [], [], pricing_environment)
    intraday_daily_pnl_report = filter_list(intraday_daily_pnl_report)

    with open('report/report_test_intraday_position_report.json', encoding='utf-8') as f:
        intraday_position_report_json = f.read()
    intraday_position_report_ref = JSONDecoder().decode(intraday_position_report_json)
    intraday_position_report_diff = compare_position_report(intraday_position_report, intraday_position_report_ref)
    if len(intraday_position_report_diff) > 0:
        print('Mismatch in intraday position report---------------------------')
        for d in intraday_position_report_diff:
            print(d['result'])
            print(d['ref'])
        mismatch = True
    else:
        print('Intraday position report test success.')

    with open('report/report_test_intraday_risk_by_underlyer_report.json') as f:
        intraday_risk_by_underlyer_report_json = f.read()
    intraday_risk_by_underlyer_report_ref = JSONDecoder().decode(intraday_risk_by_underlyer_report_json)
    intraday_risk_by_underlyer_report_diff = compare_risk_by_underlyer_report(
        intraday_risk_by_underlyer_report, intraday_risk_by_underlyer_report_ref)
    if len(intraday_risk_by_underlyer_report_diff) > 0:
        print('Mismatch in intraday risk report-------------------------------')
        for d in intraday_risk_by_underlyer_report_diff:
            print(d['result'])
            print(d['ref'])
        mismatch = True
    else:
        print('Intraday risk report test success.')

    with open('report/report_test_intraday_daily_pnl_report.json') as f:
        intraday_daily_pnl_report_json = f.read()
    intraday_daily_pnl_report_ref = JSONDecoder().decode(intraday_daily_pnl_report_json)
    intraday_daily_pnl_report_diff = compare_daily_pnl_report(intraday_daily_pnl_report, intraday_daily_pnl_report_ref)
    if len(intraday_daily_pnl_report_diff) > 0:
        print('Mismatch in intraday daily pnl report--------------------------')
        for d in intraday_daily_pnl_report_diff:
            print(d['result'])
            print(d['ref'])
        mismatch = True
    else:
        print('Intraday daily PnL report test success.')

    # end of day
    pricing_environment = 'DEFAULT_CLOSE'
    val_time = datetime.now().replace(hour=15, minute=0, second=0, microsecond=0)

    positions, index = basic_positions.get_eod_positions(domain, headers)
    positions = filter_dict(positions)
    index = filter_dict(index)

    risks = basic_risks.get_risks([positions], pricing_environment, val_time, domain, headers)[0]
    risks = find_in_dict(risks, index)

    cash_flows = basic_cashflows.get_cash_flow(domain, headers)
    cash_flows = find_in_dict(cash_flows, index)

    cash_flows_today = basic_cashflows.get_cash_flows_today(domain, headers)
    cash_flows_today = find_in_dict(cash_flows_today, index)

    listed_positions = basic_listed_positions.get_underlyer_positions(domain, headers, pricing_environment)
    listed_positions = filter_underlyer(listed_positions)

    positions, index = basic_positions.get_eod_positions(domain, headers)
    positions = filter_dict(positions)
    index = filter_dict(index)

    risks = basic_risks.get_risks([positions], pricing_environment, val_time, domain, headers)[0]
    risks = find_in_dict(risks, index)

    eod_position_report = eod_position_report.eod_position_report(
        positions, risks, cash_flows, pricing_environment, domain, headers)
    eod_position_report = filter_list(eod_position_report)

    eod_risk_by_underlyer_report = eod_risk_by_underlyer_report.eod_risk_by_underlyer_report(
        eod_position_report, listed_positions, domain, headers, pricing_environment)
    eod_risk_by_underlyer_report = filter_list(eod_risk_by_underlyer_report)

    eod_historical_pnl_report = eod_historical_pnl_by_underlyer_report.eod_historical_pnl_by_underlyer_report(
        eod_position_report, cash_flows, listed_positions, index, pricing_environment)
    eod_historical_pnl_report = filter_list(eod_historical_pnl_report)

    eod_daily_pnl_report = eod_daily_pnl_by_underlyer_report.eod_daily_pnl_by_underlyer_report(
        risks, [], eod_historical_pnl_report, [], cash_flows_today, index, pricing_environment)
    eod_daily_pnl_report = filter_list(eod_daily_pnl_report)

    with open('report/report_test_eod_position_report.json', encoding='utf-8') as f:
        eod_position_report_json = f.read()
    eod_position_report_ref = JSONDecoder().decode(eod_position_report_json)
    eod_position_report_diff = compare_position_report(eod_position_report, eod_position_report_ref)
    if len(eod_position_report_diff) > 0:
        print('Mismatch in EoD position report--------------------------------')
        for d in eod_position_report_diff:
            print(d['result'])
            print(d['ref'])
        mismatch = True
    else:
        print('EoD position report test success.')

    with open('report/report_test_eod_risk_by_underlyer_report.json') as f:
        eod_risk_by_underlyer_report_json = f.read()
    eod_risk_by_underlyer_report_ref = JSONDecoder().decode(eod_risk_by_underlyer_report_json)
    eod_risk_by_underlyer_report_diff = compare_risk_by_underlyer_report(
        eod_risk_by_underlyer_report, eod_risk_by_underlyer_report_ref)
    if len(eod_risk_by_underlyer_report_diff) > 0:
        print('Mismatch in EoD risk report------------------------------------')
        for d in eod_risk_by_underlyer_report_diff:
            print(d['result'])
            print(d['ref'])
        mismatch = True
    else:
        print('EoD risk report test success.')

    with open('report/report_test_eod_hst_pnl_report.json') as f:
        eod_historical_pnl_report_json = f.read()
    eod_historical_pnl_report_ref = JSONDecoder().decode(eod_historical_pnl_report_json)
    eod_historical_pnl_report_diff = compare_historical_pnl_report(
        eod_historical_pnl_report, eod_historical_pnl_report_ref)
    if len(eod_historical_pnl_report_diff) > 0:
        print('Mismatch in EoD historical PnL report------------------------------')
        for d in eod_historical_pnl_report_diff:
            print(d['result'])
            print(d['ref'])
        mismatch = True
    else:
        print('EoD historical PnL report test success.')

    with open('report/report_test_eod_daily_pnl_report.json') as f:
        eod_daily_pnl_report_json = f.read()
    eod_daily_pnl_report_ref = JSONDecoder().decode(eod_daily_pnl_report_json)
    eod_daily_pnl_report_diff = compare_daily_pnl_report(eod_daily_pnl_report, eod_daily_pnl_report_ref)
    if len(eod_daily_pnl_report_diff) > 0:
        print('Mismatch in EoD daily pnl report--------------------------')
        for d in eod_daily_pnl_report_diff:
            print(d['result'])
            print(d['ref'])
        mismatch = True
    else:
        print('EoD daily PnL report test success.')

    if mismatch:
        raise RuntimeError('Report test failed.')
