import warnings

from pandas.core.common import SettingWithCopyWarning

from market_data.eod_market_data import *
from regression.ImportBCTCalendarTest import ImportBCTCalendarTest
from regression.ImportBCTTradeTest import ImportBCTTradeTest
from regression.ImportTerminalCalendarTest import ImportTerminalCalendarTest
from regression.ImportTerminalMarketDataTest import ImportTerminalMarketDataTest
from regression.ImportTerminalTradeTest import ImportTerminalTradeTest
from regression.SyncTerminalInstrumentTest import SyncTerminalInstrumentTest
from regression.UpdateBCTInstrumentTest import UpdateBCTInstrumentTest
from regression.UpdateBCTQuoteTest import UpdateBCTQuoteTest
from regression.UpdateImpliedVolTest import UpdateImpliedVolTest
from trade_import.trade_import_fuc import trade_data_import

# 第一次调用data-service的时候zuul总会失败，通过warmup来workaround这个问题
def warm_up():
    try:
        bct_token = login_token(user, password, host)
        fetch_instrument_info(host, bct_token)
        get_terminal_instruments_list(bct_token)
    except Exception as e:
        logging.warning(str(e))


if __name__ == '__main__':
    warnings.simplefilter(action='ignore', category=FutureWarning)
    warnings.simplefilter(action='ignore', category=SettingWithCopyWarning)
    current_date = '2020-08-27'
    eod_end_date = datetime.strptime(current_date, '%Y-%m-%d')
    eod_start_date = eod_end_date - timedelta(days=1)
    dump = False
    # dump = True
    warm_up()
    test_suite = [
        ImportBCTCalendarTest(),
        ImportTerminalCalendarTest(),
        ImportTerminalMarketDataTest(eod_start_date, eod_end_date),
        UpdateBCTInstrumentTest(),
        SyncTerminalInstrumentTest(),
        UpdateBCTQuoteTest(current_date),
        ImportBCTTradeTest(current_date),
        ImportTerminalTradeTest(eod_start_date, eod_end_date),
        UpdateImpliedVolTest(eod_start_date, eod_end_date)
    ]
    for test_case in test_suite:
        test_case.run(dump)
    # # 10. eod_otc_future_contract_update_task(terminal_data), 需要更多的RB/IF标的和行情
    # FutureContractInfoService.update_all_future_contract_info(eod_start_date - timedelta(days=300), eod_end_date,
    #                                                           force_update=True)
    # # 11. calc realized(historical) vol
    # RealizedVolService.update_days_instrument_realized_vol(eod_end_date.date(), eod_end_date.date(), force_update=True)
    # # 12. calc implied vol
    # VolSurfaceService.update_all_vol_surface(eod_end_date.date(), eod_end_date.date(), 4)
    # # 13.basic_otc_company_type_run, used for categorizing clients
    # basic_otc_company_type_run()
    # basic_cash_flow_pd_run()
    # basic_cash_flow_today_pd_run()
    # # 14. fetch listed positions (no need for now)
    # basic_underlyer_position_default_close_pd_run()
    # # 15. fetch instrument and contract type mapping
    # basic_instrument_contract_type_run()
    # # 16. fetch otc position
    # basic_position_pd_run(eod_end_date)
    # # 17.run pv & greeks for all positions
    # basic_risks_default_close_pd_run(eod_end_date.date())
    # # 18. merge position and risk
    # eod_position_default_close_pd_run(current_date)
    # # 19. 各子公司分品种风险
    # eod_market_risk_by_book_underlyer_default_close_pd_run(current_date)
    # # 20. 交易对手分品种风险报告
    # eod_counter_party_market_risk_by_underlyer_default_close_pd_run(current_date)
    # # 21. 交易对手风险报告
    # eod_counter_party_market_risk_default_close_pd_run(current_date)
    # # 22. 各子公司整体风险报告
    # eod_subsidiary_market_risk_default_close_pd_run(current_date)
    # # 23. 全市场整体风险汇总报告
    # eod_market_risk_summary_default_close_pd_run(current_date)
    # # 24. 全市场分品种风险报告
    # eod_market_risk_detail_default_close_pd_run(current_date)
