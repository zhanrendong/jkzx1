# -*- coding: utf-8 -*-

from datetime import datetime
from json import JSONEncoder, JSONDecoder

from report import basic_positions_pd, basic_risks_pd, basic_listed_positions_pd, basic_cashflows_pd, \
    basic_otc_company_type, basic_instrument_contract_type
from report.eod import eod_position_report_pd, eod_market_risk_summary_report_pd, eod_market_risk_detail_report_pd, \
    eod_market_risk_by_book_underlyer_report_pd, eod_spot_scenarios_by_market_report_pd, eod_classic_scenarios_report_pd

import pandas as pd
import warnings
from timeit import default_timer as timer
from pandas.core.common import SettingWithCopyWarning
from utils import utils
from config.bct_config import bct_user, bct_password, bct_host

date_fmt = '%Y-%m-%d'
data_resource_ip = bct_host
report_ip = bct_host
redis_ip = 'localhost'
login_body = {
    'userName': bct_user,
    'password': bct_password
}
PE_DEFAULT_CLOSE = 'DEFAULT_CLOSE'

EOD_BASIC_POSITIONS = 'eod:basic:positions'
EOD_BASIC_POSITION_MAP = 'eod:basic:positionMap'
EOD_BASIC_CASH_FLOW = 'eod:basic:cashFlow'
EOD_BASIC_CASH_FLOW_TODAY = 'eod:basic:cashFlowToday'
EOD_BASIC_RISKS_ = 'eod:basic:risks:'
EOD_BASIC_UNDELRYER_POSITION_ = 'eod:basic:underlyerPosition:'
EOD_BASIC_SUB_COMPANIES_FROM_ORACLE = 'eod:basic:subCompanyList'
EOD_BASIC_INSTRUMENT_CONTRACT_TYPE = 'eod:basic:instrumentContractType'
EOD_BASIC_LISTED_OPTION_POSITION_ = 'eod:basic:listedOptionPosition:'
EOD_CUSTOM_POSITION_ = 'eod:custom:position:'
EOD_CUSTOM_HST_PNL_ = 'eod:custom:hstPnl:'
EOD_CUSTOM_MARKET_RISK_ = 'eod:custom:marketRisk:'
POSITION_REPORT_ = '持仓明细_'
RISK_REPORT_ = '标的风险_'
HST_PNL_REPORT_ = '历史盈亏_'
PNL_REPORT_ = '当日盈亏_'
MARKET_RISK_REPORT_ = '全市场整体风险汇总报告_'
CLASSIC_SCENARIO_MARKET_RISK_REPORT_ = '经典情景下全市场情景分析报告_'
MARKET_RISK_BY_BOOK_UNDERLYER_REPORT_ = '各子公司分品种风险报告_'
FINANCIAL_OTC_TRADE_REPORT_ = '交易报表_'
RISK_REPORT_TYPE = 'CUSTOM'

vol_surfaces = [{'name': 'TRADER_VOL', 'instance': 'intraday'},
                {'name': 'TRADER_VOL', 'instance': 'close'}]
risk_free_curves = [{'name': 'TRADER_RISK_FREE_CURVE', 'instance': 'intraday'},
                    {'name': 'TRADER_RISK_FREE_CURVE', 'instance': 'close'}]
dividend_curves = [{'name': 'TRADER_DIVIDEND_CURVE', 'instance': 'intraday'},
                   {'name': 'TRADER_DIVIDEND_CURVE', 'instance': 'close'}]


# -------------------------------------------------------------------------------
# PythonOperator To Ready Position Data

def basic_position_pd_run(valuation_date):
    headers = utils.login(data_resource_ip, login_body)
    position, position_map = basic_positions_pd.get_eod_positions(data_resource_ip, headers, valuation_date)

    position_result = position.to_msgpack(compress='zlib')
    position_map_result = position_map.to_msgpack(compress='zlib')

    r = utils.get_redis_conn(redis_ip)
    r.set(EOD_BASIC_POSITIONS, position_result)
    r.set(EOD_BASIC_POSITION_MAP, position_map_result)
    print('Basic Position Pd Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready CashFlow Data
def basic_cash_flow_pd_run():
    headers = utils.login(data_resource_ip, login_body)
    # cash_flow = basic_cashflows_pd.get_cash_flow(data_resource_ip, headers)
    cash_flow = pd.DataFrame()
    cash_flow_result = cash_flow.to_msgpack(compress='zlib')

    r = utils.get_redis_conn(redis_ip)
    r.set(EOD_BASIC_CASH_FLOW, cash_flow_result)
    print('Basic CashFlow Pd Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready CashFlowToday Data
def basic_cash_flow_today_pd_run():
    headers = utils.login(data_resource_ip, login_body)
    # cash_flow_today = basic_cashflows_pd.get_cash_flows_today(data_resource_ip, headers)
    cash_flow_today = pd.DataFrame()
    cash_flow_today_result = cash_flow_today.to_msgpack(compress='zlib')

    r = utils.get_redis_conn(redis_ip)
    r.set(EOD_BASIC_CASH_FLOW_TODAY, cash_flow_today_result)
    print('Basic CashFlowToday Pd Data Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Ready Risk Data
def basic_risks_pd_run(pricing_environment, valuation_date):
    headers = utils.login(data_resource_ip, login_body)
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(EOD_BASIC_POSITIONS)
    position = pd.read_msgpack(position_result)
    if not position.empty:
        risk = basic_risks_pd.get_risks(position, pricing_environment, valuation_date, data_resource_ip, headers)
        risk_result = risk.to_msgpack(compress='zlib')
        r.set(EOD_BASIC_RISKS_ + pricing_environment.lower(), risk_result)
        print('Basic Risk Pd Data In ' + pricing_environment + ' Has Save To Redis')


def basic_risks_default_close_pd_run(valuation_date):
    basic_risks_pd_run(PE_DEFAULT_CLOSE, valuation_date)


# -------------------------------------------------------------------------------
# PythonOperator To Ready UnderlyerPosition Data
def get_basic_underlyer_position_pd(price_env):
    headers = utils.login(data_resource_ip, login_body)
    underlyer_position, listed_option_positions = basic_listed_positions_pd.get_underlyer_positions(
        data_resource_ip, headers, price_env)
    underlyer_position_result = underlyer_position.to_msgpack(compress='zlib')
    listed_option_positions_result = listed_option_positions.to_msgpack(compress='zlib')

    r = utils.get_redis_conn(redis_ip)
    r.set(EOD_BASIC_UNDELRYER_POSITION_ + price_env.lower(), underlyer_position_result)
    r.set(EOD_BASIC_LISTED_OPTION_POSITION_ + price_env.lower(), listed_option_positions_result)
    print(price_env + ' Basic UnderlyerPosition Pd Data Has Save To Redis')


def basic_underlyer_position_default_close_pd_run():
    get_basic_underlyer_position_pd(PE_DEFAULT_CLOSE)


def basic_otc_company_type_run():
    sub_companies = basic_otc_company_type.get_sub_company_from_oracle()
    sub_companies_name = list(set(map(lambda i: i.get('clientName'), sub_companies)))
    r = utils.get_redis_conn(redis_ip)
    r.set(EOD_BASIC_SUB_COMPANIES_FROM_ORACLE, JSONEncoder().encode(sub_companies_name))
    print(' Basic SubCompanies Info from Oracle Has Save To Redis')
    headers = utils.login(data_resource_ip, login_body)
    params = {
        "infos": sub_companies
    }
    utils.call_request(report_ip, 'reference-data-service', 'refCompanyTypeInfoBatchCreate', params, headers)


def basic_instrument_contract_type_run():
    instrument_contract_dict = basic_instrument_contract_type.get_instrument_contract_dict()
    r = utils.get_redis_conn(redis_ip)
    r.set(EOD_BASIC_INSTRUMENT_CONTRACT_TYPE, JSONEncoder().encode(instrument_contract_dict))
    print(' Basic instrument contract type Info from terminal postgres Has Save To Redis')


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodPositionReport
def eod_position_pd_run(pricing_environment, valuation_date):
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(EOD_BASIC_POSITIONS)
    position = pd.read_msgpack(position_result)

    risk_result = r.get(EOD_BASIC_RISKS_ + pricing_environment.lower())
    risk = pd.read_msgpack(risk_result)

    cash_flow_result = r.get(EOD_BASIC_CASH_FLOW)
    cash_flow = pd.read_msgpack(cash_flow_result)

    listed_option_positions_result = r.get(EOD_BASIC_LISTED_OPTION_POSITION_ + pricing_environment.lower())
    listed_option_positions = pd.read_msgpack(listed_option_positions_result)

    headers = utils.login(data_resource_ip, login_body)

    rpt = []
    if not position.empty:
        rpt = eod_position_report_pd.eod_position_report(position, risk, cash_flow, listed_option_positions,
                                                         pricing_environment, data_resource_ip, headers, valuation_date)
    position_result = JSONEncoder().encode(rpt)
    r.set(EOD_CUSTOM_POSITION_ + pricing_environment.lower(), str(position_result))

    # jkzx doesn't need this for now
    # pe_description = utils.get_pricing_env_description(pricing_environment, data_resource_ip, headers)
    # params = {
    #     'reportName': POSITION_REPORT_ + pe_description,
    #     'valuationDate': valuation_date,
    #     'positionReports': reports
    # }
    # utils.call_request(report_ip, 'report-service', 'rptPositionReportCreateBatch', params, headers)


def eod_position_default_close_pd_run(valuation_date):
    eod_position_pd_run(PE_DEFAULT_CLOSE, valuation_date)


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodMarketRiskSummaryReport
def eod_market_risk_summary_pd_run(pricing_environment, valuation_date):
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(EOD_CUSTOM_POSITION_ + pricing_environment.lower())
    position = JSONDecoder().decode(bytes.decode(position_result))

    if position:
        headers = utils.login(data_resource_ip, login_body)
        reports = eod_market_risk_summary_report_pd.eod_market_risk_summary_report(pd.DataFrame(position),
                                                                                   pricing_environment)
        pe_description = utils.get_pricing_env_description(pricing_environment, data_resource_ip, headers)
        params = {
            'reportName': MARKET_RISK_REPORT_ + pe_description,
            'valuationDate': valuation_date,
            'reports': reports
        }
        utils.call_request(report_ip, 'report-service', 'rptMarketRiskReportCreateBatch', params, headers)


def eod_market_risk_summary_default_close_pd_run(valuation_date):
    eod_market_risk_summary_pd_run(PE_DEFAULT_CLOSE, valuation_date)


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodMarketRiskSummaryReport
def eod_market_risk_detail_default_close_pd_run(valuation_date):
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(EOD_CUSTOM_POSITION_ + PE_DEFAULT_CLOSE.lower())
    position = JSONDecoder().decode(bytes.decode(position_result))

    if position:
        headers = utils.login(data_resource_ip, login_body)
        reports = eod_market_risk_detail_report_pd.eod_market_risk_detail_report(pd.DataFrame(position))

        instrument_contract_result = r.get(EOD_BASIC_INSTRUMENT_CONTRACT_TYPE)
        instruments_contract_dict = JSONDecoder().decode(bytes.decode(instrument_contract_result))
        for item in reports:
            item['exfsid'] = instruments_contract_dict.get(item.get('underlyerInstrumentId'))

        params = {
            'reportName': '全市场分品种风险报告_交易-收盘-自然日',
            'valuationDate': valuation_date,
            'reports': reports
        }
        utils.call_request(report_ip, 'report-service', 'rptMarketRiskDetailReportCreateBatch', params, headers)


def eod_subsidiary_market_risk_default_close_pd_run(valuation_date):
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(EOD_CUSTOM_POSITION_ + PE_DEFAULT_CLOSE.lower())
    position = JSONDecoder().decode(bytes.decode(position_result))

    if position:
        headers = utils.login(data_resource_ip, login_body)
        reports = eod_market_risk_summary_report_pd.eod_subsidiary_market_risk_report(pd.DataFrame(position))

        params = {
            'reportName': '各子公司整体风险报告_交易-收盘-自然日',
            'valuationDate': valuation_date,
            'reports': reports
        }
        utils.call_request(report_ip, 'report-service', 'rptSubsidiaryMarketRiskReportCreateBatch', params, headers)


def eod_counter_party_market_risk_default_close_pd_run(valuation_date):
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(EOD_CUSTOM_POSITION_ + PE_DEFAULT_CLOSE.lower())
    position = JSONDecoder().decode(bytes.decode(position_result))

    if position:
        sub_companies_result = r.get(EOD_BASIC_SUB_COMPANIES_FROM_ORACLE)
        all_sub_companies = JSONDecoder().decode(bytes.decode(sub_companies_result))

        headers = utils.login(data_resource_ip, login_body)
        reports = eod_market_risk_summary_report_pd.eod_counter_party_market_risk_report(pd.DataFrame(position),
                                                                                         all_sub_companies)

        params = {
            'reportName': '交易对手风险报告_交易-收盘-自然日',
            'valuationDate': valuation_date,
            'reports': reports
        }
        utils.call_request(report_ip, 'report-service', 'rptCounterPartyMarketRiskReportCreateBatch', params, headers)


def eod_counter_party_market_risk_by_underlyer_default_close_pd_run(valuation_date):
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(EOD_CUSTOM_POSITION_ + PE_DEFAULT_CLOSE.lower())
    position = JSONDecoder().decode(bytes.decode(position_result))

    if position:
        sub_companies_result = r.get(EOD_BASIC_SUB_COMPANIES_FROM_ORACLE)
        all_sub_companies = JSONDecoder().decode(bytes.decode(sub_companies_result))

        headers = utils.login(data_resource_ip, login_body)
        reports = eod_market_risk_summary_report_pd.eod_counter_party_market_risk_by_underlyer_report(
            pd.DataFrame(position), all_sub_companies)

        instrument_contract_result = r.get(EOD_BASIC_INSTRUMENT_CONTRACT_TYPE)
        instruments_contract_dict = JSONDecoder().decode(bytes.decode(instrument_contract_result))
        for item in reports:
            item['exfsid'] = instruments_contract_dict.get(item.get('underlyerInstrumentId'))

        params = {
            'reportName': '交易对手分品种风险报告_交易-收盘-自然日',
            'valuationDate': valuation_date,
            'reports': reports
        }
        utils.call_request(report_ip, 'report-service', 'rptCounterPartyMarketRiskByUnderlyerReportCreateBatch',
                           params, headers)


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodMarketRiskByBookUnderlyerReport
def eod_market_risk_by_book_underlyer_pd_run(pricing_environment, valuation_date):
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(EOD_CUSTOM_POSITION_ + pricing_environment.lower())
    position = JSONDecoder().decode(bytes.decode(position_result))

    if position:
        headers = utils.login(data_resource_ip, login_body)
        pe_description = utils.get_pricing_env_description(pricing_environment, data_resource_ip, headers)
        reports = eod_market_risk_by_book_underlyer_report_pd.eod_market_risk_by_book_underlyer_report(
            pd.DataFrame(position), pe_description)

        instrument_contract_result = r.get(EOD_BASIC_INSTRUMENT_CONTRACT_TYPE)
        instruments_contract_dict = JSONDecoder().decode(bytes.decode(instrument_contract_result))
        for item in reports:
            item['exfsid'] = instruments_contract_dict.get(item.get('underlyerInstrumentId'))

        params = {
            'reportName': MARKET_RISK_BY_BOOK_UNDERLYER_REPORT_ + pe_description,
            'valuationDate': valuation_date,
            'reports': reports
        }
        utils.call_request(report_ip, 'report-service', 'rptMarketRiskBySubUnderlyerReportCreateBatch', params, headers)


def eod_market_risk_by_book_underlyer_default_close_pd_run(valuation_date):
    eod_market_risk_by_book_underlyer_pd_run(PE_DEFAULT_CLOSE, valuation_date)


# -------------------------------------------------------------------------------
# PythonOperator To Generate EodMarketSpotScenariosByUnderlyerReport
def eod_spot_scenarios_by_market_pd_run(pricing_environment, valuation_date):
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(EOD_BASIC_POSITIONS)
    position = pd.read_msgpack(position_result)

    if not position.empty:
        sub_companies_result = r.get(EOD_BASIC_SUB_COMPANIES_FROM_ORACLE)
        all_sub_companies = JSONDecoder().decode(bytes.decode(sub_companies_result))

        headers = utils.login(data_resource_ip, login_body)
        pe_description = utils.get_pricing_env_description(pricing_environment, data_resource_ip, headers)
        reports = eod_spot_scenarios_by_market_report_pd.eod_spot_scenarios_by_market_report(
            pd.DataFrame(position), data_resource_ip, headers, pe_description, all_sub_companies,
            valuation_date, pricing_environment)

        instrument_contract_result = r.get(EOD_BASIC_INSTRUMENT_CONTRACT_TYPE)
        instruments_contract_dict = JSONDecoder().decode(bytes.decode(instrument_contract_result))
        for item in reports:
            item['exfsid'] = instruments_contract_dict.get(item.get('instrumentId'))

        utils.call_request(report_ip, 'report-service', 'rptDeleteSpotScenariosReportByDate',
                           {'valuationDate': valuation_date}, headers)
        chunk_size = 1000
        for start in range(0, len(reports), chunk_size):
            end = start + chunk_size
            params = {'valuationDate': valuation_date, 'reports': reports[start:end]}
            utils.call_request(report_ip, 'report-service', 'rptSpotScenariosReportCreateBatch', params, headers)


def eod_spot_scenarios_by_market_default_close_pd_run(valuation_date):
    eod_spot_scenarios_by_market_pd_run(PE_DEFAULT_CLOSE, valuation_date)


# -------------------------------------------------------------------------------
# PythonOperator To Generate classicMarketScenariosReport
def eod_classic_scenarios_pd_run(valuation_date):
    r = utils.get_redis_conn(redis_ip)
    position_result = r.get(EOD_BASIC_POSITIONS)
    position = pd.read_msgpack(position_result)

    if not position.empty:
        sub_companies_result = r.get(EOD_BASIC_SUB_COMPANIES_FROM_ORACLE)
        all_sub_companies = JSONDecoder().decode(bytes.decode(sub_companies_result))

        headers = utils.login(data_resource_ip, login_body)
        pe_description = utils.get_pricing_env_description(PE_DEFAULT_CLOSE, data_resource_ip, headers)
        reports = eod_classic_scenarios_report_pd.eod_classic_scenarios_report(
            position, all_sub_companies, data_resource_ip, headers, valuation_date, PE_DEFAULT_CLOSE)

        instrument_contract_result = r.get(EOD_BASIC_INSTRUMENT_CONTRACT_TYPE)
        instruments_contract_dict = JSONDecoder().decode(bytes.decode(instrument_contract_result))
        for item in reports:
            item['exfsid'] = instruments_contract_dict.get(item.get('underlyerInstrumentId'))

        params = {
            'reportName': CLASSIC_SCENARIO_MARKET_RISK_REPORT_ + pe_description,
            'valuationDate': valuation_date,
            'reports': reports
        }
        utils.call_request(report_ip, 'report-service', 'rptMarketRiskDetailReportCreateBatch', params, headers)


if __name__ == '__main__':
    import sys
    import os

    sys.stdout.flush()
    # 经典场景需获取terminal服务api，此处设置terminal服务地址，使smoke可以正常进行
    os.environ['ENV_TERMINAL_SERVICE_HOST'] = '10.1.5.41'
    os.environ['BCT_PORT'] = '16016'
    start = timer()
    warnings.simplefilter(action='ignore', category=FutureWarning)
    warnings.simplefilter(action='ignore', category=SettingWithCopyWarning)
    r = utils.get_redis_conn(redis_ip)
    r.set(EOD_BASIC_SUB_COMPANIES_FROM_ORACLE, JSONEncoder().encode([]))
    r.set(EOD_BASIC_INSTRUMENT_CONTRACT_TYPE, JSONEncoder().encode({}))

    cur_valuation_date = datetime.now().strftime(date_fmt)
    time_stamp = timer()
    basic_position_pd_run(cur_valuation_date)
    print('###### basic_position_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    basic_cash_flow_pd_run()
    basic_cash_flow_today_pd_run()
    time_stamp = timer()
    basic_risks_default_close_pd_run(cur_valuation_date)
    print('###### basic_risks_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    time_stamp = timer()
    basic_underlyer_position_default_close_pd_run()
    print('###### basic_underlyer_position_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    # basic_otc_company_type_run()
    # basic_instrument_contract_type_run()

    time_stamp = timer()
    eod_position_default_close_pd_run(cur_valuation_date)
    print('###### eod_position_default_close_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    time_stamp = timer()
    eod_market_risk_summary_default_close_pd_run(cur_valuation_date)
    print('###### eod_market_risk_summary_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    time_stamp = timer()
    eod_market_risk_detail_default_close_pd_run(cur_valuation_date)
    print('###### eod_market_risk_detail_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    time_stamp = timer()
    eod_subsidiary_market_risk_default_close_pd_run(cur_valuation_date)
    print('###### eod_subsidiary_market_risk_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    # time_stamp = timer()
    # eod_classic_scenarios_pd_run(cur_valuation_date)
    # print('###### eod_classic_scenarios_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    time_stamp = timer()
    eod_spot_scenarios_by_market_default_close_pd_run(cur_valuation_date)
    print('###### eod_spot_scenarios_by_market_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    time_stamp = timer()
    eod_counter_party_market_risk_default_close_pd_run(cur_valuation_date)
    print('###### eod_counter_party_market_risk_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    time_stamp = timer()
    eod_counter_party_market_risk_by_underlyer_default_close_pd_run(cur_valuation_date)
    print('###### eod_counter_party_market_risk_by_underlyer_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    time_stamp = timer()
    eod_market_risk_by_book_underlyer_default_close_pd_run(cur_valuation_date)
    print('###### eod_market_risk_by_book_underlyer_pd_run takes ' + str(timer() - time_stamp) + ' seconds')
    print('done')
    print('###### all tasks take ' + str(timer() - start) + ' seconds')
