# -*- coding: utf-8 -*-

from datetime import datetime, timedelta
from json import JSONEncoder, JSONDecoder

from report.basic_cashflows import get_cash_flow, get_cash_flows_today
from report.basic_listed_positions import get_underlyer_positions
from report.basic_positions import get_positions
from report.basic_positions import get_all_positions
from report.basic_risks import get_risks
from report.eod.eod_daily_pnl_by_underlyer_report import eod_daily_pnl_by_underlyer_report
from report.eod.eod_finance_client_fund_report import get_finanical_otc_client_fund
from report.eod.eod_finance_fund_report import get_financial_otc_fund_detail
from report.eod.eod_finance_trade_report import get_financial_otc_trade
from report.eod.eod_historical_pnl_by_underlyer_report import eod_historical_pnl_by_underlyer_report
from report.eod.eod_position_report import eod_position_report
from report.eod.eod_risk_by_underlyer_report import eod_risk_by_underlyer_report
from report.eod.eod_hedge_pnl_report import get_eod_hedge_pnl
from report.eod.eod_profit_statistics_report import eod_profit_statistics_report
from report.eod.eod_statistics_of_risk_report import get_eod_statics_of_risk
from report.eod.eod_subject_computing_report import get_eod_subject_computing
from report.eod.eod_market_risk_summary_report import eod_market_risk_summary_report
from market_data.real_time_market_data import update_market_data
from model import shift_model
import time
from config.bct_config import bct_password, bct_user

from utils import utils

ip = 'localhost'
login_body = {
    'userName': bct_user,
    'password': bct_password
}

PE_DEFAULT_CLOSE = 'DEFAULT_CLOSE_CALENDARS'
PE_RISK_CLOSE = 'RISK_CLOSE_CALENDARS'

EOD_BASIC_POSITIONS = 'eod:basic:positions'
EOD_BASIC_POSITION_MAP = 'eod:basic:positionMap'
EOD_BASIC_CASH_FLOW = 'eod:basic:cashFlow'
EOD_BASIC_CASH_FLOW_TODAY = 'eod:basic:cashFlowToday'
EOD_BASIC_RISKS_ = 'eod:basic:risks:'
EOD_BASIC_UNDELRYER_POSITION_ = 'eod:basic:underlyerPosition:'
EOD_CUSTOM_POSITION_ = 'eod:custom:position:'
EOD_CUSTOM_HST_PNL_ = 'eod:custom:hstPnl:'
POSITION_REPORT_ = '持仓明细_'
RISK_REPORT_ = '标的风险_'
HST_PNL_REPORT_ = '历史盈亏_'
PNL_REPORT_ = '当日盈亏_'
RISK_REPORT_TYPE = 'CUSTOM'
FINANCIAL_OTC_TRADE_REPORT_ = '交易报表_'
MARKET_RISK_REPORT_ = '全市场汇总风险_'

vol_surfaces = [{'name': 'TRADER_VOL', 'instance': 'intraday'},
                {'name': 'TRADER_VOL', 'instance': 'close'}]
risk_free_curves = [{'name': 'TRADER_RISK_FREE_CURVE', 'instance': 'intraday'},
                    {'name': 'TRADER_RISK_FREE_CURVE', 'instance': 'close'}]
dividend_curves = [{'name': 'TRADER_DIVIDEND_CURVE', 'instance': 'intraday'},
                   {'name': 'TRADER_DIVIDEND_CURVE', 'instance': 'close'}]


# -------------------------------------------------------------------------------
# PythonOperator To Ready Position Data
def basic_position_run():
    headers = utils.login(ip, login_body)
    position, expiring_pos, position_map = get_positions(ip, headers)
    position_map_result = JSONEncoder().encode(position_map)
    position_result = JSONEncoder().encode(position)

    r = utils.get_redis_conn(ip)
    r.set(EOD_BASIC_POSITIONS, str(position_result))
    r.set(EOD_BASIC_POSITION_MAP, str(position_map_result))
    print('Basic Position Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready CashFlow Data
def basic_cash_flow_run():
    headers = utils.login(ip, login_body)
    cash_flow = get_cash_flow(ip, headers)
    cash_flow_result = JSONEncoder().encode(cash_flow)

    r = utils.get_redis_conn(ip)
    r.set(EOD_BASIC_CASH_FLOW, str(cash_flow_result))
    print('Basic CashFlow Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready CashFlowToday Data
def basic_cash_flow_today_run():
    headers = utils.login(ip, login_body)
    cash_flow_today = get_cash_flows_today(ip, headers)
    cash_flow_today_result = JSONEncoder().encode(cash_flow_today)

    r = utils.get_redis_conn(ip)
    r.set(EOD_BASIC_CASH_FLOW_TODAY, str(cash_flow_today_result))
    print('Basic CashFlowToday Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready Risk Data
def basic_risks_run(pricing_environment):
    headers = utils.login(ip, login_body)
    valuation_time = datetime.now()
    r = utils.get_redis_conn(ip)
    position_result = r.get(EOD_BASIC_POSITIONS)
    position = JSONDecoder().decode(bytes.decode(position_result))
    risk, price_data = get_risks([position], pricing_environment, valuation_time, ip, headers)
    risk_result = JSONEncoder().encode(risk)
    r.set(EOD_BASIC_RISKS_ + pricing_environment.lower(), str(risk_result))
    print('Basic Risk Data In ' + pricing_environment + ' Has Save To Redis')


def basic_risks_default_close_run():
    basic_risks_run(PE_DEFAULT_CLOSE)


def basic_risks_risk_close_run():
    basic_risks_run(PE_RISK_CLOSE)


# -------------------------------------------------------------------------------
# PythonOperator To Ready UnderlyerPosition Data
def basic_underlyer_position_risk_close_run():
    get_basic_underlyer_position(PE_RISK_CLOSE)


def basic_underlyer_position_default_close_run():
    get_basic_underlyer_position(PE_DEFAULT_CLOSE)


def get_basic_underlyer_position(price_env):
    headers = utils.login(ip, login_body)
    underlyer_position = get_underlyer_positions(ip, headers, price_env)
    underlyer_position_result = JSONEncoder().encode(underlyer_position)

    r = utils.get_redis_conn(ip)
    r.set(EOD_BASIC_UNDELRYER_POSITION_ + price_env.lower(), str(underlyer_position_result))
    print(price_env + ' Basic UnderlyerPosition Data Has Save To Redis')


def get_pricing_environment_description(pricing_environment, headers):
    return utils.call_request(
        ip, 'pricing-service', 'prcPricingEnvironmentGet',
        {'pricingEnvironmentId': pricing_environment}, headers)['result']['description']


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodPositionReport
def eod_position_run(pricing_environment):
    r = utils.get_redis_conn(ip)
    position_result = r.get(EOD_BASIC_POSITIONS)
    position = JSONDecoder().decode(bytes.decode(position_result))

    risk_result = r.get(EOD_BASIC_RISKS_ + pricing_environment.lower())
    risk = JSONDecoder().decode(bytes.decode(risk_result))

    cash_flow_result = r.get(EOD_BASIC_CASH_FLOW)
    cash_flow = JSONDecoder().decode(bytes.decode(cash_flow_result))

    headers = utils.login(ip, login_body)
    reports = eod_position_report(position, risk, cash_flow, pricing_environment, ip, headers)
    position_result = JSONEncoder().encode(reports)
    r.set(EOD_CUSTOM_POSITION_ + pricing_environment.lower(), str(position_result))

    now_date = datetime.now().date()
    pe_description = get_pricing_environment_description(pricing_environment, headers)
    params = {
        'reportName': POSITION_REPORT_ + pe_description,
        'valuationDate': str(now_date),
        'positionReports': reports
    }
    utils.call_request(ip, 'report-service', 'rptPositionReportCreateBatch', params, headers)


def eod_hedge_pnl_run(pricing_environment):
    headers = utils.login(ip, login_body)
    report = get_eod_hedge_pnl(ip, headers, pricing_environment)
    pe_description = get_pricing_environment_description(pricing_environment, headers)
    params = {
        "reportName": '对冲端盈亏表' + pe_description,
        'reportType': RISK_REPORT_TYPE,
        "valuationDate": time.strftime("%Y-%m-%d", time.localtime()),
        "reports": [report]
    }
    utils.call_request(ip, 'report-service', 'rptCustomReportSaveBatch', params, headers)


def eod_hedge_pnl_close_run():
    eod_hedge_pnl_run(PE_DEFAULT_CLOSE)


def eod_hedge_pnl_risk_run():
    eod_hedge_pnl_run(PE_RISK_CLOSE)


def eod_profit_statistics_report_run(pricing_environment):
    headers = utils.login(ip, login_body)
    r = utils.get_redis_conn(ip)
    price_data = r.get(EOD_BASIC_RISKS_ + pricing_environment.lower())
    price_data = JSONDecoder().decode(bytes.decode(price_data))
    pe_description = get_pricing_environment_description(pricing_environment, headers)
    report = eod_profit_statistics_report(ip, headers, price_data)
    params = {
        "reportName": '利润统计表_' + pe_description,
        'reportType': RISK_REPORT_TYPE,
        "valuationDate": time.strftime("%Y-%m-%d", time.localtime()),
        "reports": [report]
    }
    utils.call_request(ip, 'report-service', 'rptCustomReportSaveBatch', params, headers)


def eod_profit_statistics_report_default_close_run():
    eod_profit_statistics_report_run(PE_DEFAULT_CLOSE)


def eod_profit_statistics_report_risk_close_run():
    eod_profit_statistics_report_run(PE_RISK_CLOSE)


def eod_statics_of_risk_run(price_environment):
    headers = utils.login(ip, login_body)
    r = utils.get_redis_conn(ip)
    price_data = r.get(EOD_BASIC_RISKS_ + price_environment.lower())
    price_data = JSONDecoder().decode(bytes.decode(price_data))
    pe_description = get_pricing_environment_description(price_environment, headers)
    report = get_eod_statics_of_risk(price_data)
    params = {
        "reportName": '风险指标统计表_' + pe_description,
        'reportType': RISK_REPORT_TYPE,
        "valuationDate": time.strftime("%Y-%m-%d", time.localtime()),
        "reports": [report]
    }
    utils.call_request(ip, 'report-service', 'rptCustomReportSaveBatch', params, headers)


def eod_statics_of_risk_risk_close_run():
    eod_statics_of_risk_run(PE_RISK_CLOSE)


def eod_statics_of_risk_default_close_run():
    eod_statics_of_risk_run(PE_DEFAULT_CLOSE)


def eod_subject_computing_run(pricing_environment):
    headers = utils.login(ip, login_body)
    report = get_eod_subject_computing(ip, headers, pricing_environment)
    pe_description = get_pricing_environment_description(pricing_environment, headers)
    params = {
        "reportName": '资产统计表_' + pe_description,
        'reportType': RISK_REPORT_TYPE,
        "valuationDate": time.strftime("%Y-%m-%d", time.localtime()),
        "reports": [report]
    }
    utils.call_request(ip, 'report-service', 'rptCustomReportSaveBatch', params, headers)


def eod_subject_computing_close_run():
    eod_subject_computing_run(PE_DEFAULT_CLOSE)


def eod_subject_computing_risk_run():
    eod_subject_computing_run(PE_RISK_CLOSE)


def eod_position_default_close_run():
    eod_position_run(PE_DEFAULT_CLOSE)


def eod_position_risk_close_run():
    eod_position_run(PE_RISK_CLOSE)


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodRiskReport
def eod_risk_run(pricing_environment):
    r = utils.get_redis_conn(ip)
    position_result = r.get(EOD_CUSTOM_POSITION_ + pricing_environment.lower())
    position = JSONDecoder().decode(bytes.decode(position_result))

    underlyer_position_result = r.get(EOD_BASIC_UNDELRYER_POSITION_ + pricing_environment.lower())
    underlyer_position = JSONDecoder().decode(bytes.decode(underlyer_position_result))

    headers = utils.login(ip, login_body)
    pe_description = get_pricing_environment_description(pricing_environment, headers)
    reports = eod_risk_by_underlyer_report(position, underlyer_position, ip, headers, pe_description)
    now_date = datetime.now().date()
    params = {
        'reportName': RISK_REPORT_ + pe_description,
        'valuationDate': str(now_date),
        'riskReports': reports
    }
    utils.call_request(ip, 'report-service', 'rptRiskReportCreateBatch', params, headers)


def eod_risk_default_close_run():
    eod_risk_run(PE_DEFAULT_CLOSE)


def eod_risk_risk_close_run():
    eod_risk_run(PE_RISK_CLOSE)


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodMarketRiskSummaryReport
def eod_market_risk_summary_run(pricing_environment):
    r = utils.get_redis_conn(ip)
    position_result = r.get(EOD_CUSTOM_POSITION_ + pricing_environment.lower())
    position = JSONDecoder().decode(bytes.decode(position_result))

    headers = utils.login(ip, login_body)
    reports = eod_market_risk_summary_report(position, pricing_environment)
    now_date = datetime.now().date()
    pe_description = get_pricing_environment_description(pricing_environment, headers)
    params = {
        'reportName': MARKET_RISK_REPORT_ + pe_description,
        'valuationDate': str(now_date),
        'reports': reports
    }
    utils.call_request(ip, 'report-service', 'rptMarketRiskReportCreateBatch', params, headers)


def eod_market_risk_summary_default_close_run():
    eod_market_risk_summary_run(PE_DEFAULT_CLOSE)


def eod_market_risk_summary_risk_close_run():
    eod_market_risk_summary_run(PE_RISK_CLOSE)


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodHstPnlReport
def eod_hst_pnl_run(pricing_environment):
    r = utils.get_redis_conn(ip)
    position_result = r.get(EOD_CUSTOM_POSITION_ + pricing_environment.lower())
    position = JSONDecoder().decode(bytes.decode(position_result))

    cash_flow_result = r.get(EOD_BASIC_CASH_FLOW)
    cash_flow = JSONDecoder().decode(bytes.decode(cash_flow_result))

    underlyer_position_result = r.get(EOD_BASIC_UNDELRYER_POSITION_ + pricing_environment.lower())
    underlyer_position = JSONDecoder().decode(bytes.decode(underlyer_position_result))

    position_map_result = r.get(EOD_BASIC_POSITION_MAP)
    position_map = JSONDecoder().decode(bytes.decode(position_map_result))

    headers = utils.login(ip, login_body)
    pe_description = get_pricing_environment_description(pricing_environment, headers)
    reports = eod_historical_pnl_by_underlyer_report(
        position, cash_flow, underlyer_position, position_map, pe_description)
    hst_pnl_result = JSONEncoder().encode(reports)
    r.set(EOD_CUSTOM_HST_PNL_ + pricing_environment.lower(), str(hst_pnl_result))
    now_date = datetime.now().date()
    params = {
        'reportName': HST_PNL_REPORT_ + pe_description,
        'valuationDate': str(now_date),
        'pnlHstReports': reports
    }
    utils.call_request(ip, 'report-service', 'rptPnlHstReportCreateBatch', params, headers)


def eod_hst_pnl_default_close_run():
    eod_hst_pnl_run(PE_DEFAULT_CLOSE)


def eod_hst_pnl_risk_close_run():
    eod_hst_pnl_run(PE_RISK_CLOSE)


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodDailyPnlReport
def eod_daily_pnl_run(pricing_environment):
    r = utils.get_redis_conn(ip)
    position_map_result = r.get(EOD_BASIC_POSITION_MAP)
    position_map = JSONDecoder().decode(bytes.decode(position_map_result))
    risk_result = r.get(EOD_BASIC_RISKS_ + pricing_environment.lower())
    risk = JSONDecoder().decode(bytes.decode(risk_result))
    hst_pnl_result = r.get(EOD_CUSTOM_HST_PNL_ + pricing_environment.lower())
    hst_pnl = JSONDecoder().decode(bytes.decode(hst_pnl_result))
    cash_flow_today_result = r.get(EOD_BASIC_CASH_FLOW_TODAY)
    cash_flow_today = JSONDecoder().decode(bytes.decode(cash_flow_today_result))

    headers = utils.login(ip, login_body)
    pe_description = get_pricing_environment_description(pricing_environment, headers)
    now_date = datetime.now().date()
    yst_date = now_date + timedelta(days=-1)
    yst_params = {
        'reportName': POSITION_REPORT_ + pe_description,
        'valuationDate': str(yst_date)
    }

    yst_position = utils.call_request(ip, 'report-service', 'rptLatestPositionReportByNameAndDate',
                                      yst_params, headers)['result']

    yst_params['reportName'] = HST_PNL_REPORT_ + pe_description
    yst_historical_pnl = utils.call_request(ip, 'report-service', 'rptLatestPnlHstReportByNameAndDate',
                                            yst_params, headers)['result']

    reports = eod_daily_pnl_by_underlyer_report(
        risk, yst_position, hst_pnl, yst_historical_pnl, cash_flow_today, position_map, pe_description)
    params = {
        'reportName': PNL_REPORT_ + pe_description,
        'valuationDate': str(now_date),
        'pnlReports': reports
    }
    utils.call_request(ip, 'report-service', 'rptPnlReportCreateBatch', params, headers)


def eod_daily_pnl_default_close_run():
    eod_daily_pnl_run(PE_DEFAULT_CLOSE)


def eod_daily_pnl_risk_close_run():
    eod_daily_pnl_run(PE_RISK_CLOSE)


# -------------------------------------------------------------------------------
# PythonOperator To Ready businessTrade Data
def financial_otc_trade_run(pricing_environment):
    headers = utils.login(ip, login_body)
    r = utils.get_redis_conn(ip)
    now_date = datetime.now().date()
    position = get_all_positions(ip, headers)
    risk_result = r.get(EOD_BASIC_RISKS_ + pricing_environment.lower())
    risk = JSONDecoder().decode(bytes.decode(risk_result))
    cash_flows_result = r.get(EOD_BASIC_CASH_FLOW)
    cash_flows = JSONDecoder().decode(bytes.decode(cash_flows_result))
    reports = get_financial_otc_trade(position, risk, cash_flows, headers, ip)

    pe_description = utils.get_pricing_env_description(pricing_environment, ip, headers)
    params = {
        'reportName': FINANCIAL_OTC_TRADE_REPORT_ + pe_description,
        'valuationDate': str(now_date),
        'otcTradeReports': reports
    }
    utils.call_request(ip, 'report-service', 'rptOtcTradeReportCreateBatch', params, headers)


def financial_otc_trade_default_close_run():
    financial_otc_trade_run(PE_DEFAULT_CLOSE)


def financial_otc_trade_risk_close_run():
    financial_otc_trade_run(PE_RISK_CLOSE)


def financial_otc_fund_detail_run():
    now_date = datetime.now().date()
    params = {}
    headers = utils.login(ip, login_body)
    funds = utils.call_request(ip, 'reference-data-service', 'cliFundEvnetListAll', params, headers)
    reports = get_financial_otc_fund_detail(funds)
    for report in reports:
        params = {
            "legalName": report['clientName']
        }
        party = utils.call_request(ip, 'reference-data-service', 'refPartyGetByLegalName', params, headers)
        report['masterAgreementId'] = '-'
        if party != 'error':
            report['masterAgreementId'] = party['result']['masterAgreementId']
    params = {
        'reportName': 'financial_otc_fund_detail_report',
        'valuationDate': str(now_date),
        'financialOtcFundDetailReports': reports
    }
    utils.call_request(ip, 'report-service', 'rptFinancialOtcFundDetailReportCreateBatch', params, headers)


def finanical_otc_client_fund_run():
    headers = utils.login(ip, login_body)
    now_date = datetime.now().date()
    reports = get_finanical_otc_client_fund(headers, ip)
    params = {
        "reportName": "finanical_otc_client_fund_report",
        "valuationDate": str(now_date),
        "finanicalOtcClientFundReports": reports
    }
    utils.call_request(ip, 'report-service', 'rptFinanicalOtcClientFundReportCreateBatch', params, headers)


# -------------------------------------------------------------------------------
# PythonOperator To update market data
def eod_market_data():
    update_market_data('close')


# PythonOperator To shift model valuation date
def eod_shift_model_valuation_date():
    headers = utils.login(ip, login_body)
    shift_model.shift_models(ip, headers, vol_surfaces, risk_free_curves, dividend_curves)
    print('Models of instruments in whitelist shifted.')


if __name__ == '__main__':
    basic_position_run()
    basic_cash_flow_run()
    basic_cash_flow_today_run()
    basic_risks_default_close_run()
    basic_risks_risk_close_run()
    basic_underlyer_position_default_close_run()
    basic_underlyer_position_risk_close_run()
    eod_position_default_close_run()
    eod_position_risk_close_run()
    eod_risk_default_close_run()
    eod_risk_risk_close_run()
    eod_market_risk_summary_default_close_run()
    eod_market_risk_summary_risk_close_run()
    eod_hst_pnl_default_close_run()
    eod_daily_pnl_default_close_run()
    eod_hst_pnl_risk_close_run()
    eod_daily_pnl_risk_close_run()
    eod_hedge_pnl_close_run()
    eod_hedge_pnl_risk_run()
    eod_profit_statistics_report_default_close_run()
    eod_profit_statistics_report_risk_close_run()
    eod_statics_of_risk_risk_close_run()
    eod_statics_of_risk_default_close_run()
    eod_subject_computing_risk_run()
    eod_subject_computing_close_run()

    financial_otc_fund_detail_run()
    finanical_otc_client_fund_run()
    financial_otc_trade_default_close_run()
    financial_otc_trade_risk_close_run()
