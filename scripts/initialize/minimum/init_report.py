import utils
from init_params import *
from datetime import datetime, timedelta

PE_DEFAULT_CLOSE = 'DEFAULT_CLOSE'
PE_RISK_CLOSE = 'RISK_CLOSE'

POSITION_REPORT_ = 'pos_rpt_'
RISK_REPORT_ = 'risk_rpt_'
HST_PNL_REPORT_ = 'hst_pnl_rpt_'
PNL_REPORT_ = 'pnl_rpt_'


def create_eod_position(pricing_environment):
    params = {
        'reportName': POSITION_REPORT_ + pricing_environment.lower(),
        'valuationDate': now_date,
        'positionReports': [{}]
    }
    return utils.call_request('rptPositionReportCreateBatch', params, 'report-service', host, token)


def create_eod_risk(pricing_environment):
    params = {
        'reportName': RISK_REPORT_ + pricing_environment.lower(),
        'valuationDate': now_date,
        'riskReports': [{}]
    }
    return utils.call_request('rptRiskReportCreateBatch', params, 'report-service', host, token)


def create_eod_hst_pnl(pricing_environment):
    params = {
        'reportName': HST_PNL_REPORT_ + pricing_environment.lower(),
        'valuationDate': now_date,
        'pnlHstReports': [{}]
    }
    return utils.call_request('rptPnlHstReportCreateBatch', params, 'report-service', host, token)


def create_financial_otc_trade_risk():
    params = {
        'reportName': 'financial_otc_trade_report_risk_close',
        'valuationDate': now_date,
        'otcTradeReports': [{}]
    }
    return utils.call_request('rptOtcTradeReportCreateBatch', params, 'report-service', host, token)


def create_financial_otc_trade():
    params = {
        'reportName': 'financial_otc_trade_report_default_close',
        'valuationDate': now_date,
        'otcTradeReports': [{}]
    }
    return utils.call_request('rptOtcTradeReportCreateBatch', params, 'report-service', host, token)


def create_financial_otc_fund_detail():
    params = {
        'reportName': 'financial_otc_fund_detail_report',
        'valuationDate': now_date,
        'financialOtcFundDetailReports': [{}]
    }
    return utils.call_request('rptFinancialOtcFundDetailReportCreateBatch', params, 'report-service', host, token)


def create_finanical_otc_client_fund():
    params = {
        "reportName": "finanical_otc_client_fund_report",
        "valuationDate": now_date,
        "finanicalOtcClientFundReports": [{}]
    }
    return utils.call_request('rptFinanicalOtcClientFundReportCreateBatch', params, 'report-service', host, token)


def create_eod_daily_pnl(pricing_environment):
    params = {
        'reportName': PNL_REPORT_ + pricing_environment.lower(),
        'valuationDate': now_date,
        'pnlReports': [{}]
    }
    return utils.call_request('rptPnlReportCreateBatch', params, 'report-service', host, token)


if __name__ == '__main__':
    token = utils.login(admin_user, admin_password, host)
    now_date = '1900-01-01'
    print('=== create default report name ===')
    create_eod_position(PE_DEFAULT_CLOSE)
    create_eod_position(PE_RISK_CLOSE)
    create_eod_risk(PE_DEFAULT_CLOSE)
    create_eod_risk(PE_RISK_CLOSE)
    create_eod_hst_pnl(PE_DEFAULT_CLOSE)
    create_eod_hst_pnl(PE_RISK_CLOSE)
    create_financial_otc_trade_risk()
    create_financial_otc_trade()
    create_financial_otc_fund_detail()
    create_finanical_otc_client_fund()
    create_eod_daily_pnl(PE_DEFAULT_CLOSE)
    create_eod_daily_pnl(PE_RISK_CLOSE)
