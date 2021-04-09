# -*- coding: utf-8 -*-
import warnings

import pandas as pd
from datetime import datetime, timedelta
from json import JSONEncoder, JSONDecoder
from timeit import default_timer as timer
from pandas.core.common import SettingWithCopyWarning
from report.eod import eod_client_valuation_report_pd
from market_data.real_time_market_data import update_market_data

from report import basic_positions_pd, basic_risks_pd, basic_listed_positions_pd, basic_cashflows_pd, \
    basic_portfolios_pd, basic_lcm_notifications
from report.intraday import intraday_risk_by_underlyer_report_pd, intraday_position_report_pd, \
    intraday_daily_pnl_by_underlyer_report_pd, intraday_expiring_position_report_pd, intraday_portfolio_trades_report_pd
from utils import utils
from config.bct_config import bct_password, bct_user

# -------------------------------------------------------------------------------
# Some Parameter Be Defined
ip = utils.get_bct_host()
redis_ip = utils.get_bct_host()
login_body = {
    'userName': bct_user,
    'password': bct_password
}

PE_DEFAULT_INTRADAY = 'DEFAULT_INTRADAY_CALENDARS'

INTRADAY_BASIC_POSITIONS = 'intraday:basic:positions'
INTRADAY_BASIC_EXPIRING = 'intraday:basic:expiring'
INTRADAY_BASIC_POSITION_INDEX = 'intraday:basic:position_index'
INTRADAY_BASIC_TERMINATED_POSITION_INDEX = 'intraday:basic:today_terminated_position_index'
INTRADAY_BASIC_CASH_FLOW = 'intraday:basic:cashFlow'
INTRADAY_BASIC_CASH_FLOW_TODAY = 'intraday:basic:cashFlowToday'
INTRADAY_BASIC_RISKS = 'intraday:basic:risks'
INTRADAY_BASIC_UNDELRYER_POSITION = 'intraday:basic:underlyerPosition'
INTRADAY_BASIC_LISTED_OPTION_POSITION = 'intraday:basic:listedOptionPositiony'
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
def basic_position_pd_run():
    headers = utils.login(ip, login_body)
    position, expiring, position_index = basic_positions_pd.get_intraday_positions(ip, headers)
    terminated_position_index = basic_positions_pd.get_today_terminated_positions(ip, headers)
    position_result = position.to_msgpack(compress='zlib')
    expiring_result = expiring.to_msgpack(compress='zlib')
    position_index_result = position_index.to_msgpack(compress='zlib')
    terminated_position_index_result = terminated_position_index.to_msgpack(compress='zlib')

    r = utils.get_redis_conn(redis_ip)
    r.set(INTRADAY_BASIC_EXPIRING, expiring_result)
    r.set(INTRADAY_BASIC_POSITIONS, position_result)
    r.set(INTRADAY_BASIC_POSITION_INDEX, position_index_result)
    r.set(INTRADAY_BASIC_TERMINATED_POSITION_INDEX, terminated_position_index_result)
    print('Basic Position Pd Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready UnderlyerPosition Data
def basic_underlyer_position_pd_run():
    headers = utils.login(ip, login_body)
    underlyer_positions, listed_option_positions = basic_listed_positions_pd.get_underlyer_positions(
        ip, headers, PE_DEFAULT_INTRADAY)
    underlyer_positions_result = underlyer_positions.to_msgpack(compress='zlib')
    listed_option_positions_result = listed_option_positions.to_msgpack(compress='zlib')

    r = utils.get_redis_conn(redis_ip)
    r.set(INTRADAY_BASIC_UNDELRYER_POSITION, underlyer_positions_result)
    r.set(INTRADAY_BASIC_LISTED_OPTION_POSITION, listed_option_positions_result)
    print('Basic UnderlyerPosition Pd Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready CashFlow Data
def basic_cash_flow_pd_run():
    headers = utils.login(ip, login_body)
    cash_flow = basic_cashflows_pd.get_cash_flow(ip, headers)
    cash_flow_result = cash_flow.to_msgpack(compress='zlib')

    r = utils.get_redis_conn(redis_ip)
    r.set(INTRADAY_BASIC_CASH_FLOW, cash_flow_result)
    print('Basic CashFlow Pd Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready CashFlowToday Data
def basic_cash_flow_today_pd_run():
    headers = utils.login(ip, login_body)
    cash_flow_today = basic_cashflows_pd.get_cash_flows_today(ip, headers)
    cash_flow_today_result = cash_flow_today.to_msgpack(compress='zlib')

    r = utils.get_redis_conn(redis_ip)
    r.set(INTRADAY_BASIC_CASH_FLOW_TODAY, cash_flow_today_result)
    print('Basic CashFlowToday Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready portfolio trades Data
def basic_portfolio_trades_pd_run():
    headers = utils.login(ip, login_body)
    portfolio_trades = basic_portfolios_pd.get_portfolio_trades(ip, headers)
    portfolio_trades_result = portfolio_trades.to_msgpack(compress='zlib')

    r = utils.get_redis_conn(redis_ip)
    r.set(INTRADAY_BASIC_PORTFOLIO_TRADES, portfolio_trades_result)
    print('Basic portfolioTrades Pd Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready Risk Data
def basic_risks_pd_run():
    valuation_time = datetime.now()
    pricing_environment = PE_DEFAULT_INTRADAY
    headers = utils.login(ip, login_body)
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(INTRADAY_BASIC_POSITIONS)
    positions = pd.read_msgpack(position_result)
    expiring_result = r.get(INTRADAY_BASIC_EXPIRING)
    expirings = pd.read_msgpack(expiring_result)
    all_positions = positions if expirings.empty else pd.concat([positions, expirings])
    risk = basic_risks_pd.get_risks(all_positions, pricing_environment, valuation_time, ip, headers)
    risk_result = risk.to_msgpack(compress='zlib')

    r.set(INTRADAY_BASIC_RISKS, risk_result)
    print('Basic Risk Pd Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Generate RealTimePositionReport
def real_time_position_pd_run():
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(INTRADAY_BASIC_POSITIONS)
    position = pd.read_msgpack(position_result)

    risk_result = r.get(INTRADAY_BASIC_RISKS)
    risk = pd.read_msgpack(risk_result)

    cash_flow_result = r.get(INTRADAY_BASIC_CASH_FLOW)
    cash_flow = pd.read_msgpack(cash_flow_result)

    listed_option_positions_result = r.get(INTRADAY_BASIC_LISTED_OPTION_POSITION)
    listed_option_positions = pd.read_msgpack(listed_option_positions_result)

    headers = utils.login(ip, login_body)
    reports = intraday_position_report_pd.intraday_position_report(
        position, risk, cash_flow, listed_option_positions, PE_DEFAULT_INTRADAY, ip, headers)
    position_result = JSONEncoder().encode(reports)
    r.set(TRADE_QUEUE, str(position_result))
    r.publish(TRADE_QUEUE, str(position_result))
    r.set(INTRADAY_CUSTOM_POSITION, str(position_result))
    print('RealTimePositionReport Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodRiskReport
def real_time_risk_pd_run():
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(INTRADAY_CUSTOM_POSITION)
    position = JSONDecoder().decode(bytes.decode(position_result))

    underlyer_position_result = r.get(INTRADAY_BASIC_UNDELRYER_POSITION)
    underlyer_position = pd.read_msgpack(underlyer_position_result)

    headers = utils.login(ip, login_body)
    pe_description = utils.get_pricing_env_description(PE_DEFAULT_INTRADAY, ip, headers)
    reports = intraday_risk_by_underlyer_report_pd.intraday_risk_by_underlyer_report(
        pd.DataFrame(position), underlyer_position, ip, headers, pe_description)
    risk_result = JSONEncoder().encode(reports)
    r.set(RISK_QUEUE, str(risk_result))
    r.publish(RISK_QUEUE, str(risk_result))


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodPnlReport
def real_time_pnl_pd_run():
    r = utils.get_redis_conn(redis_ip)
    risk_result = r.get(INTRADAY_BASIC_RISKS)
    risk = pd.read_msgpack(risk_result)

    underlyer_position_result = r.get(INTRADAY_BASIC_UNDELRYER_POSITION)
    underlyer_position = pd.read_msgpack(underlyer_position_result)

    list_position_result = r.get(INTRADAY_BASIC_LISTED_OPTION_POSITION)
    list_position = pd.read_msgpack(list_position_result)

    cash_flow_today_result = r.get(INTRADAY_BASIC_CASH_FLOW_TODAY)
    cash_flow_today = pd.read_msgpack(cash_flow_today_result)

    live_position_index_result = r.get(INTRADAY_BASIC_POSITION_INDEX)
    live_position_index = pd.read_msgpack(live_position_index_result)

    today_terminated_position_index_result = r.get(INTRADAY_BASIC_TERMINATED_POSITION_INDEX)
    today_terminated_position_index = pd.read_msgpack(today_terminated_position_index_result)

    now_date = datetime.now().date()
    yst_date = now_date + timedelta(days=-1)
    yst_params = {'reportName': POSITION_REPORT, 'valuationDate': str(yst_date)}
    headers = utils.login(ip, login_body)
    yst_position = utils.call_request(
        ip, 'report-service', 'rptLatestPositionReportByNameAndDate', yst_params, headers)['result']
    yst_params['reportName'] = HST_PNL_REPORT
    yst_historical_pnl = utils.call_request(
        ip, 'report-service', 'rptLatestPnlHstReportByNameAndDate', yst_params, headers)['result']
    yst_position = pd.DataFrame(yst_position)
    yst_historical_pnl = pd.DataFrame(yst_historical_pnl)
    pe_description = utils.get_pricing_env_description(PE_DEFAULT_INTRADAY, ip, headers)
    reports = intraday_daily_pnl_by_underlyer_report_pd.intraday_daily_pnl_by_underlyer_report(
        risk, cash_flow_today, underlyer_position, list_position, live_position_index, today_terminated_position_index,
        yst_position, yst_historical_pnl, pe_description)
    reports = utils.remove_nan_and_inf(reports.to_dict(orient='records'))
    pnl_result = JSONEncoder().encode(reports)
    r.set(PNL_QUEUE, str(pnl_result))
    r.publish(PNL_QUEUE, str(pnl_result))


# -------------------------------------------------------------------------------
# PythonOperator To Generate RealTimeExpiringPositionReport
def real_time_expiring_position_pd_run():
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(INTRADAY_BASIC_EXPIRING)
    position = pd.read_msgpack(position_result)

    risk_result = r.get(INTRADAY_BASIC_RISKS)
    risk = pd.read_msgpack(risk_result)

    cash_flow_result = r.get(INTRADAY_BASIC_CASH_FLOW)
    cash_flow = pd.read_msgpack(cash_flow_result)

    reports = intraday_expiring_position_report_pd.intraday_expiring_position_report(position, risk, cash_flow)
    position_result = JSONEncoder().encode(reports)
    r.set(TRADE_EXPIRING_QUEUE, str(position_result))
    r.publish(TRADE_EXPIRING_QUEUE, str(position_result))
    r.set(INTRADAY_CUSTOM_EXPIRING_POSITION, str(position_result))


# -------------------------------------------------------------------------------
# PythonOperator To Generate portfolio risk Report


def real_time_portfolio_trades_pd_run():
    r = utils.get_redis_conn(redis_ip)
    risk_result = r.get(INTRADAY_BASIC_RISKS)
    risk = pd.read_msgpack(risk_result)

    position_index_result = r.get(INTRADAY_BASIC_POSITION_INDEX)
    position_index = pd.read_msgpack(position_index_result)

    portfolio_trades_result = r.get(INTRADAY_BASIC_PORTFOLIO_TRADES)
    portfolio_trades = pd.read_msgpack(portfolio_trades_result)
    headers = utils.login(ip, login_body)
    pe_description = utils.get_pricing_env_description(PE_DEFAULT_INTRADAY, ip, headers)
    portfolio_report = intraday_portfolio_trades_report_pd.intraday_portfolio_trades_report(
        risk, position_index, portfolio_trades, pe_description)
    portfolio_report_result = JSONEncoder().encode(portfolio_report)
    r.set(PORTFOLIO_RISK_QUEUE, str(portfolio_report_result))
    r.publish(PORTFOLIO_RISK_QUEUE, str(portfolio_report_result))


# -------------------------------------------------------------------------------
# PythonOperator To Generate valuation Report

def real_time_valuation_pd_run():
    r = utils.get_redis_conn(redis_ip)
    risk_result = r.get(INTRADAY_BASIC_RISKS)
    risk = pd.read_msgpack(risk_result)

    position_result = r.get(INTRADAY_BASIC_POSITIONS)
    position = pd.read_msgpack(position_result)

    cash_flow_result = r.get(INTRADAY_BASIC_CASH_FLOW)
    cash_flow = pd.read_msgpack(cash_flow_result)

    pnls = eod_client_valuation_report_pd.option_total_pnl(position, risk, cash_flow)

    headers = utils.login(ip, login_body)
    client_valuations = eod_client_valuation_report_pd.client_valuation(position, pnls, datetime.now(), ip, headers)
    eod_client_valuation_report_pd.process_and_save_report(client_valuations, ip, headers)


def trade_notification_run():
    headers = utils.login(ip, login_body)
    basic_lcm_notifications.trade_notification_all(ip, headers)


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


def update_ctp_quotes_run():
    headers = utils.login(ip, login_body)
    # sync_ctp_market_data(ip, headers)


if __name__ == '__main__':
    start = timer()
    warnings.simplefilter(action='ignore', category=FutureWarning)
    warnings.simplefilter(action='ignore', category=SettingWithCopyWarning)
    basic_position_pd_run()
    basic_underlyer_position_pd_run()
    basic_cash_flow_pd_run()
    basic_cash_flow_today_pd_run()
    basic_portfolio_trades_pd_run()
    basic_risks_pd_run()

    real_time_position_pd_run()
    real_time_risk_pd_run()
    real_time_expiring_position_pd_run()
    real_time_portfolio_trades_pd_run()
    real_time_pnl_pd_run()
    real_time_valuation_pd_run()
    # update_ctp_quotes_run()
    end = timer()
    print('###Total Execution Time of Intraday Job:' + str(end - start) + ' seconds###')


