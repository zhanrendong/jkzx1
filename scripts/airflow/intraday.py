# -*- coding: utf-8 -*-

from datetime import datetime, timedelta
from json import JSONEncoder, JSONDecoder

from report import basic_cashflows
from report import basic_lcm_notifications
from report import basic_listed_positions
from report import basic_portfolios
from report import basic_positions
from report import basic_risks
from report.eod import eod_client_valuation_report
from report.intraday.intraday_daily_pnl_by_underlyer_report import intraday_daily_pnl_by_underlyer_report
from report.intraday.intraday_portfolio_trades_report import intraday_portfolio_trades_report
from report.intraday.intraday_position_report import intraday_position_report
from report.intraday.intraday_expiring_position_report import intraday_expiring_position_report
from report.intraday.intraday_risk_by_underlyer_report import intraday_risk_by_underlyer_report
from market_data.real_time_market_data import update_market_data
from utils import utils
from config.bct_config import bct_password, bct_user

# -------------------------------------------------------------------------------
# Some Parameter Be Defined
ip = 'localhost'
login_body = {
    'userName': bct_user,
    'password': bct_password
}

PE_DEFAULT_INTRADAY = 'DEFAULT_INTRADAY_CALENDARS'

INTRADAY_BASIC_POSITIONS = 'intraday:basic:positions'
INTRADAY_BASIC_EXPIRING = 'intraday:basic:expiring'
INTRADAY_BASIC_POSITION_INDEX = 'intraday:basic:position_index'
INTRADAY_BASIC_CASH_FLOW = 'intraday:basic:cashFlow'
INTRADAY_BASIC_CASH_FLOW_TODAY = 'intraday:basic:cashFlowToday'
INTRADAY_BASIC_RISKS = 'intraday:basic:risks'
INTRADAY_BASIC_UNDELRYER_POSITION = 'intraday:basic:underlyerPosition'
INTRADAY_BASIC_PORTFOLIO_TRADES = 'intraday:basic:portfolioTrades'
TRADE_QUEUE = 'trade:queue'
INTRADAY_NOTIFY = 'intraday:notify'
TRADE_EXPIRING_QUEUE = 'tradeExpiring:queue'
INTRADAY_CUSTOM_POSITION = 'intraday:custom:position'
INTRADAY_CUSTOM_EXPIRING_POSITION = 'intraday:custom:expiringPosition'
RISK_QUEUE = 'risk:queue'
PNL_QUEUE = 'pnl:queue'
PORTFOLIO_RISK_QUEUE = 'portfolioRisk:queue'
POSITION_REPORT = 'pos_rpt_default_close'
HST_PNL_REPORT = 'hst_pnl_rpt_default_close'


# -------------------------------------------------------------------------------
# PythonOperator To Ready Position Data
def basic_position_run():
    headers = utils.login(ip, login_body)
    position, expiring, position_index = basic_positions.get_intraday_positions(ip, headers)
    position_index_result = JSONEncoder().encode(position_index)
    position_result = JSONEncoder().encode(position)
    expiring_result = JSONEncoder().encode(expiring)

    r = utils.get_redis_conn(ip)
    r.set(INTRADAY_BASIC_EXPIRING, str(expiring_result))
    r.set(INTRADAY_BASIC_POSITIONS, str(position_result))
    r.set(INTRADAY_BASIC_POSITION_INDEX, str(position_index_result))
    print('Basic Position Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready CashFlow Data
def basic_cash_flow_run():
    headers = utils.login(ip, login_body)
    cash_flow = basic_cashflows.get_cash_flow(ip, headers)
    cash_flow_result = JSONEncoder().encode(cash_flow)

    r = utils.get_redis_conn(ip)
    r.set(INTRADAY_BASIC_CASH_FLOW, str(cash_flow_result))
    print('Basic CashFlow Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready CashFlowToday Data
def basic_cash_flow_today_run():
    headers = utils.login(ip, login_body)
    cash_flow_today = basic_cashflows.get_cash_flows_today(ip, headers)
    cash_flow_today_result = JSONEncoder().encode(cash_flow_today)

    r = utils.get_redis_conn(ip)
    r.set(INTRADAY_BASIC_CASH_FLOW_TODAY, str(cash_flow_today_result))
    print('Basic CashFlowToday Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready Risk Data
def basic_risks_run():
    valuation_time = datetime.now()
    pricing_environment = PE_DEFAULT_INTRADAY
    headers = utils.login(ip, login_body)
    r = utils.get_redis_conn(ip)
    position_result = r.get(INTRADAY_BASIC_POSITIONS)
    positions = JSONDecoder().decode(bytes.decode(position_result))
    expiring_result = r.get(INTRADAY_BASIC_EXPIRING)
    expirings = JSONDecoder().decode(bytes.decode(expiring_result))
    risk = basic_risks.get_risks([positions, expirings], pricing_environment, valuation_time, ip, headers)[0]
    risk_result = JSONEncoder().encode(risk)

    r.set(INTRADAY_BASIC_RISKS, str(risk_result))
    print('Basic Risk Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready UnderlyerPosition Data
def basic_underlyer_position_run():
    headers = utils.login(ip, login_body)
    underlyer_position = basic_listed_positions.get_underlyer_positions(ip, headers, PE_DEFAULT_INTRADAY)
    underlyer_position_result = JSONEncoder().encode(underlyer_position)

    r = utils.get_redis_conn(ip)
    r.set(INTRADAY_BASIC_UNDELRYER_POSITION, str(underlyer_position_result))
    print('Basic UnderlyerPosition Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Generate RealTimePositionReport
def real_time_position_run():
    r = utils.get_redis_conn(ip)
    position_result = r.get(INTRADAY_BASIC_POSITIONS)
    position = JSONDecoder().decode(bytes.decode(position_result))

    risk_result = r.get(INTRADAY_BASIC_RISKS)
    risk = JSONDecoder().decode(bytes.decode(risk_result))

    cash_flow_result = r.get(INTRADAY_BASIC_CASH_FLOW)
    cash_flow = JSONDecoder().decode(bytes.decode(cash_flow_result))

    headers = utils.login(ip, login_body)
    reports = intraday_position_report(position, risk, cash_flow, PE_DEFAULT_INTRADAY, ip, headers)
    position_result = JSONEncoder().encode(reports)
    r.set(TRADE_QUEUE, str(position_result))
    r.publish(TRADE_QUEUE, str(position_result))
    r.set(INTRADAY_CUSTOM_POSITION, str(position_result))


# -------------------------------------------------------------------------------
# PythonOperator To Generate RealTimeExpiringPositionReport
def real_time_expiring_position_run():
    r = utils.get_redis_conn(ip)
    position_result = r.get(INTRADAY_BASIC_EXPIRING)
    position = JSONDecoder().decode(bytes.decode(position_result))

    risk_result = r.get(INTRADAY_BASIC_RISKS)
    risk = JSONDecoder().decode(bytes.decode(risk_result))

    cash_flow_result = r.get(INTRADAY_BASIC_CASH_FLOW)
    cash_flow = JSONDecoder().decode(bytes.decode(cash_flow_result))

    reports = intraday_expiring_position_report(position, risk, cash_flow)
    position_result = JSONEncoder().encode(reports)
    r.set(TRADE_EXPIRING_QUEUE, str(position_result))
    r.publish(TRADE_EXPIRING_QUEUE, str(position_result))
    r.set(INTRADAY_CUSTOM_EXPIRING_POSITION, str(position_result))


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodRiskReport
def real_time_risk_run():
    r = utils.get_redis_conn(ip)
    position_result = r.get(INTRADAY_CUSTOM_POSITION)
    position = JSONDecoder().decode(bytes.decode(position_result))

    underlyer_position_result = r.get(INTRADAY_BASIC_UNDELRYER_POSITION)
    underlyer_position = JSONDecoder().decode(bytes.decode(underlyer_position_result))

    headers = utils.login(ip, login_body)
    pe_description = utils.get_pricing_env_description(PE_DEFAULT_INTRADAY, ip, headers)
    reports = intraday_risk_by_underlyer_report(position, underlyer_position, ip, headers, pe_description)

    risk_result = JSONEncoder().encode(reports)
    r.set(RISK_QUEUE, str(risk_result))
    r.publish(RISK_QUEUE, str(risk_result))


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodPnlReport
def real_time_pnl_run():
    r = utils.get_redis_conn(ip)
    risk_result = r.get(INTRADAY_BASIC_RISKS)
    risk = JSONDecoder().decode(bytes.decode(risk_result))

    position_index_result = r.get(INTRADAY_BASIC_POSITION_INDEX)
    position_index = JSONDecoder().decode(bytes.decode(position_index_result))

    underlyer_position_result = r.get(INTRADAY_BASIC_UNDELRYER_POSITION)
    underlyer_position = JSONDecoder().decode(bytes.decode(underlyer_position_result))

    cash_flow_today_result = r.get(INTRADAY_BASIC_CASH_FLOW_TODAY)
    cash_flow_today = JSONDecoder().decode(bytes.decode(cash_flow_today_result))

    now_date = datetime.now().date()
    yst_date = now_date + timedelta(days=-1)
    yst_params = {
        'reportName': POSITION_REPORT,
        'valuationDate': str(yst_date)
    }
    headers = utils.login(ip, login_body)
    yst_position = utils.call_request(ip, 'report-service', 'rptLatestPositionReportByNameAndDate',
                                      yst_params, headers)['result']
    yst_params['reportName'] = HST_PNL_REPORT
    yst_historical_pnl = utils.call_request(ip, 'report-service', 'rptLatestPnlHstReportByNameAndDate',
                                            yst_params, headers)['result']
    pe_description = utils.get_pricing_env_description(PE_DEFAULT_INTRADAY, ip, headers)
    reports = intraday_daily_pnl_by_underlyer_report(
        risk, cash_flow_today, underlyer_position, position_index, yst_position, yst_historical_pnl, pe_description)
    pnl_result = JSONEncoder().encode(reports)
    r.set(PNL_QUEUE, str(pnl_result))
    r.publish(PNL_QUEUE, str(pnl_result))


def real_time_valuation_run():
    r = utils.get_redis_conn(ip)
    risk_result = r.get(INTRADAY_BASIC_RISKS)
    risk = JSONDecoder().decode(bytes.decode(risk_result))

    position_result = r.get(INTRADAY_BASIC_POSITIONS)
    position = JSONDecoder().decode(bytes.decode(position_result))

    cash_flow_result = r.get(INTRADAY_BASIC_CASH_FLOW)
    cash_flow = JSONDecoder().decode(bytes.decode(cash_flow_result))

    pnls = eod_client_valuation_report.option_total_pnl(position, risk, cash_flow)

    headers = utils.login(ip, login_body)
    client_valuations = eod_client_valuation_report.client_valuation(position, pnls, datetime.now(), ip, headers)
    eod_client_valuation_report.process_and_save_report(client_valuations, ip, headers)


def trade_notification_run():
    headers = utils.login(ip, login_body)
    basic_lcm_notifications.trade_notification_all(ip, headers)


def basic_portfolio_trades_run():
    headers = utils.login(ip, login_body)
    portfolio_trades = basic_portfolios.get_portfolio_trades(ip, headers)
    portfolio_trades_result = JSONEncoder().encode(portfolio_trades)

    r = utils.get_redis_conn(ip)
    r.set(INTRADAY_BASIC_PORTFOLIO_TRADES, str(portfolio_trades_result))
    print('Basic portfolioTrades Data Has Save To Redis')


def real_time_portfolio_trades_run():
    r = utils.get_redis_conn(ip)
    risk_result = r.get(INTRADAY_BASIC_RISKS)
    risk = JSONDecoder().decode(bytes.decode(risk_result))

    position_index_result = r.get(INTRADAY_BASIC_POSITION_INDEX)
    position_index = JSONDecoder().decode(bytes.decode(position_index_result))

    portfolio_trades_result = r.get(INTRADAY_BASIC_PORTFOLIO_TRADES)
    portfolio_trades = JSONDecoder().decode(bytes.decode(portfolio_trades_result))
    headers = utils.login(ip, login_body)
    pe_description = utils.get_pricing_env_description(PE_DEFAULT_INTRADAY, ip, headers)
    portfolio_report = intraday_portfolio_trades_report(risk, position_index, portfolio_trades, pe_description)
    portfolio_report_result = JSONEncoder().encode(portfolio_report)
    r.set(PORTFOLIO_RISK_QUEUE, str(portfolio_report_result))
    r.publish(PORTFOLIO_RISK_QUEUE, str(portfolio_report_result))


# -------------------------------------------------------------------------------
# PythonOperator To update market data
def real_time_market_data():
    update_market_data('intraday')


# -------------------------------------------------------------------------------
# Intraday report notification
def intraday_expiring_position_report_notifier():
    intraday_report_notifier("EXPIRING_POSITION")


def intraday_pnl_report_notifier():
    intraday_report_notifier("PNL")


def intraday_risk_report_notifier():
    intraday_report_notifier("RISK")


def intraday_valuation_report_notifier():
    intraday_report_notifier("VALUATION")


def intraday_portfolio_risk_report_notifier():
    intraday_report_notifier("PORTFOLIO_RISK")


def intraday_report_notifier(notification_type):
    r = utils.get_redis_conn(ip)
    valuation_time = str(datetime.now().isoformat())
    msg = {"reportType": notification_type, "valuationTime": valuation_time}
    msg_json = JSONEncoder().encode(msg)
    r.set(INTRADAY_NOTIFY, str(msg_json))
    r.publish(INTRADAY_NOTIFY, str(msg_json))


if __name__ == '__main__':
    basic_position_run()
    basic_cash_flow_run()
    basic_cash_flow_today_run()
    basic_risks_run()
    basic_underlyer_position_run()
    basic_portfolio_trades_run()
    real_time_position_run()
    real_time_expiring_position_run()
    real_time_risk_run()
    real_time_pnl_run()
    real_time_valuation_run()
    real_time_portfolio_trades_run()
