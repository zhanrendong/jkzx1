----client_service----

----account

alter table client_service.account alter column margin type numeric(19,4);
alter table client_service.account alter column cash type numeric(19,4);
alter table client_service.account alter column premium type numeric(19,4);
alter table client_service.account alter column credit_used type numeric(19,4);
alter table client_service.account alter column debt type numeric(19,4);
alter table client_service.account alter column net_deposit type numeric(19,4);
alter table client_service.account alter column realized_pnl type numeric(19,4);
alter table client_service.account alter column credit type numeric(19,4);
alter table client_service.account alter column counter_party_fund type numeric(19,4);
alter table client_service.account alter column counter_party_margin type numeric(19,4);
alter table client_service.account alter column counter_party_credit type numeric(19,4);
alter table client_service.account alter column counter_party_credit_balance type numeric(19,4);

----account_op_record

alter table client_service.account_op_record alter column margin_change type numeric(19,4);
alter table client_service.account_op_record alter column cash_change type numeric(19,4);
alter table client_service.account_op_record alter column premium_change type numeric(19,4);
alter table client_service.account_op_record alter column credit_used_change type numeric(19,4);
alter table client_service.account_op_record alter column debt_change type numeric(19,4);
alter table client_service.account_op_record alter column net_deposit_change type numeric(19,4);
alter table client_service.account_op_record alter column realized_pnlchange type numeric(19,4);
alter table client_service.account_op_record alter column credit_change type numeric(19,4);
alter table client_service.account_op_record alter column counter_party_credit_change type numeric(19,4);
alter table client_service.account_op_record alter column counter_party_credit_balance_change type numeric(19,4);
alter table client_service.account_op_record alter column counter_party_fund_change type numeric(19,4);
alter table client_service.account_op_record alter column counter_party_margin_change type numeric(19,4);
alter table client_service.account_op_record alter column margin type numeric(19,4);
alter table client_service.account_op_record alter column cash type numeric(19,4);
alter table client_service.account_op_record alter column premium type numeric(19,4);
alter table client_service.account_op_record alter column credit_used type numeric(19,4);
alter table client_service.account_op_record alter column debt type numeric(19,4);
alter table client_service.account_op_record alter column net_deposit type numeric(19,4);
alter table client_service.account_op_record alter column realized_pnl type numeric(19,4);
alter table client_service.account_op_record alter column credit type numeric(19,4);
alter table client_service.account_op_record alter column counter_party_credit type numeric(19,4);
alter table client_service.account_op_record alter column counter_party_credit_balance type numeric(19,4);
alter table client_service.account_op_record alter column counter_party_fund type numeric(19,4);
alter table client_service.account_op_record alter column counter_party_margin type numeric(19,4);


----fund_event_record

alter table client_service.fund_event_record alter column payment_amount type numeric(19,4);


----report_service---

----financial_otc_fund_detail_report

alter table report_service.financial_otc_fund_detail_report alter column payment_in type numeric(19,4);
alter table report_service.financial_otc_fund_detail_report alter column payment_out type numeric(19,4);
alter table report_service.financial_otc_fund_detail_report alter column payment_amount type numeric(19,4);

----financial_otc_trade_report

alter table report_service.financial_otc_trade_report alter column nominal_price type numeric(19,4);
alter table report_service.financial_otc_trade_report alter column begin_premium type numeric(19,4);
alter table report_service.financial_otc_trade_report alter column end_premium type numeric(19,4);
alter table report_service.financial_otc_trade_report alter column total_premium type numeric(19,4);

----financial_otc_client_fund_report

alter table report_service.finanical_otc_client_fund_report alter column payment_in type numeric(19,4);
alter table report_service.finanical_otc_client_fund_report alter column payment_out type numeric(19,4);
alter table report_service.finanical_otc_client_fund_report alter column premium_buy type numeric(19,4);
alter table report_service.finanical_otc_client_fund_report alter column premium_sell type numeric(19,4);
alter table report_service.finanical_otc_client_fund_report alter column profit_amount type numeric(19,4);
alter table report_service.finanical_otc_client_fund_report alter column loss_amount type numeric(19,4);
alter table report_service.finanical_otc_client_fund_report alter column fund_total type numeric(19,4);


----hedge_pnl_report

alter table report_service.hedge_pnl_report alter column stock_hedge_pnl type numeric(19,4);
alter table report_service.hedge_pnl_report alter column commodity_hedge_pnl type numeric(19,4);
alter table report_service.hedge_pnl_report alter column floor_options_hedge_pnl type numeric(19,4);
alter table report_service.hedge_pnl_report alter column hedge_all_pnl type numeric(19,4);

----pnl_hst_report

alter table report_service.pnl_hst_report alter column underlyer_market_value type numeric(19,4);
alter table report_service.pnl_hst_report alter column underlyer_net_position type numeric(19,4);
alter table report_service.pnl_hst_report alter column underlyer_sell_amount type numeric(19,4);
alter table report_service.pnl_hst_report alter column underlyer_buy_amount type numeric(19,4);
alter table report_service.pnl_hst_report alter column underlyer_price type numeric(19,4);
alter table report_service.pnl_hst_report alter column underlyer_pnl type numeric(19,4);
alter table report_service.pnl_hst_report alter column option_unwind_amount type numeric(19,4);
alter table report_service.pnl_hst_report alter column option_settle_amount type numeric(19,4);
alter table report_service.pnl_hst_report alter column option_market_value type numeric(19,4);
alter table report_service.pnl_hst_report alter column option_premium type numeric(19,4);
alter table report_service.pnl_hst_report alter column option_pnl type numeric(19,4);
alter table report_service.pnl_hst_report alter column pnl type numeric(19,4);

----pnl_report

alter table report_service.pnl_report alter column daily_pnl type numeric(19,4);
alter table report_service.pnl_report alter column daily_option_pnl type numeric(19,4);
alter table report_service.pnl_report alter column daily_underlyer_pnl type numeric(19,4);
alter table report_service.pnl_report alter column pnl_contribution_new type numeric(19,4);
alter table report_service.pnl_report alter column pnl_contribution_settled type numeric(19,4);
alter table report_service.pnl_report alter column pnl_contribution_delta type numeric(19,4);
alter table report_service.pnl_report alter column pnl_contribution_gamma type numeric(19,4);
alter table report_service.pnl_report alter column pnl_contribution_vega type numeric(19,4);
alter table report_service.pnl_report alter column pnl_contribution_theta type numeric(19,4);
alter table report_service.pnl_report alter column pnl_contribution_rho type numeric(19,4);
alter table report_service.pnl_report alter column pnl_contribution_unexplained type numeric(19,4);

-----position_report

alter table report_service.position_report alter column underlyer_price type numeric(19,4);
alter table report_service.position_report alter column initial_number type numeric(19,4);
alter table report_service.position_report alter column unwind_number type numeric(19,4);
alter table report_service.position_report alter column number type numeric(19,4);
alter table report_service.position_report alter column premium type numeric(19,4);
alter table report_service.position_report alter column unwind_amount type numeric(19,4);
alter table report_service.position_report alter column market_value type numeric(19,4);
alter table report_service.position_report alter column pnl type numeric(19,4);
alter table report_service.position_report alter column delta type numeric(19,4);
alter table report_service.position_report alter column delta_decay type numeric(19,4);
alter table report_service.position_report alter column delta_with_decay type numeric(19,4);
alter table report_service.position_report alter column gamma type numeric(19,4);
alter table report_service.position_report alter column gamma_cash type numeric(19,4);
alter table report_service.position_report alter column vega type numeric(19,4);
alter table report_service.position_report alter column theta type numeric(19,4);
alter table report_service.position_report alter column rho type numeric(19,4);
alter table report_service.position_report alter column r type numeric(19,4);
alter table report_service.position_report alter column q type numeric(19,4);
alter table report_service.position_report alter column vol type numeric(19,4);
alter table report_service.position_report alter column days_in_year type numeric(19,4);

----profit_statisics_report

alter table report_service.profit_statisics_report alter column stock_hold_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column commodity_hold_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column exotic_hold_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column stock_today_to_end_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column stock_histroy_to_end_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column commodity_today_to_end_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column commodity_histroy_to_end_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column exotic_today_to_end_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column exotic_histroy_to_end_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column stock_to_end_all_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column commodity_to_end_all_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column exotic_to_end_all_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column stock_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column commodity_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column exotic_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column option_today_to_end_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column option_histroy_to_end_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column option_to_end_all_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column option_hold_pnl type numeric(19,4);
alter table report_service.profit_statisics_report alter column option_pnl type numeric(19,4);

----risk_report

alter table report_service.risk_report alter column underlyer_price type numeric(19,4);
alter table report_service.risk_report alter column underlyer_price_change_percent type numeric(19,4);
alter table report_service.risk_report alter column underlyer_net_position type numeric(19,4);
alter table report_service.risk_report alter column delta type numeric(19,4);
alter table report_service.risk_report alter column net_delta type numeric(19,4);
alter table report_service.risk_report alter column delta_decay type numeric(19,4);
alter table report_service.risk_report alter column delta_with_decay type numeric(19,4);
alter table report_service.risk_report alter column gamma type numeric(19,4);
alter table report_service.risk_report alter column gamma_cash type numeric(19,4);
alter table report_service.risk_report alter column vega type numeric(19,4);
alter table report_service.risk_report alter column theta type numeric(19,4);
alter table report_service.risk_report alter column rho type numeric(19,4);


----statistics_of_risk_report

alter table report_service.statistics_of_risk_report alter column stock_delta type numeric(19,4);
alter table report_service.statistics_of_risk_report alter column stock_gamma type numeric(19,4);
alter table report_service.statistics_of_risk_report alter column stock_vega type numeric(19,4);
alter table report_service.statistics_of_risk_report alter column commodity_delta type numeric(19,4);
alter table report_service.statistics_of_risk_report alter column commodity_gamma type numeric(19,4);

----subject_computing_report

alter table report_service.subject_computing_report alter column commodity type numeric(19,4);
alter table report_service.subject_computing_report alter column commodity_forward type numeric(19,4);
alter table report_service.subject_computing_report alter column commodity_all type numeric(19,4);
alter table report_service.subject_computing_report alter column stock type numeric(19,4);
alter table report_service.subject_computing_report alter column stock_swap type numeric(19,4);
alter table report_service.subject_computing_report alter column stock_all type numeric(19,4);
alter table report_service.subject_computing_report alter column exotic type numeric(19,4);
alter table report_service.subject_computing_report alter column net_position type numeric(19,4);
alter table report_service.subject_computing_report alter column absolute_position type numeric(19,4);
alter table report_service.subject_computing_report alter column hedge type numeric(19,4);

----ValuationReport

alter table report_service.valuation_report alter column price type numeric(19,4);

----trade_snapshot_model----

----lcmevent

alter table trade_snapshot_model.lcmevent alter column premium type numeric(19,4);
alter table trade_snapshot_model.lcmevent alter column cash_flow type numeric(19,4);

----position

alter table trade_snapshot_model.position alter column quantity type numeric(19,4);


----margin_service

alter table margin_service.margin alter column maintenance_margin type numeric(19,4);

----exchange_service----

----position_record

alter table exchange_service.position_record alter column long_position type numeric(19,4);
alter table exchange_service.position_record alter column short_position type numeric(19,4);
alter table exchange_service.position_record alter column net_position type numeric(19,4);
alter table exchange_service.position_record alter column total_sell type numeric(19,4);
alter table exchange_service.position_record alter column total_buy type numeric(19,4);
alter table exchange_service.position_record alter column history_buy_amount type numeric(19,4);
alter table exchange_service.position_record alter column history_sell_amount type numeric(19,4);
alter table exchange_service.position_record alter column market_value type numeric(19,4);
alter table exchange_service.position_record alter column total_pnl type numeric(19,4);

----position_snapshot

alter table exchange_service.position_snapshot alter column long_position type numeric(19,4);
alter table exchange_service.position_snapshot alter column short_position type numeric(19,4);
alter table exchange_service.position_snapshot alter column net_position type numeric(19,4);
alter table exchange_service.position_snapshot alter column total_sell type numeric(19,4);
alter table exchange_service.position_snapshot alter column total_buy type numeric(19,4);
alter table exchange_service.position_snapshot alter column history_buy_amount type numeric(19,4);
alter table exchange_service.position_snapshot alter column history_sell_amount type numeric(19,4);
alter table exchange_service.position_snapshot alter column market_value type numeric(19,4);
alter table exchange_service.position_snapshot alter column total_pnl type numeric(19,4);


----trade_record

alter table exchange_service.trade_record alter column multiplier type numeric(19,4);
alter table exchange_service.trade_record alter column deal_amount type numeric(19,4);
alter table exchange_service.trade_record alter column deal_price type numeric(19,4);


----trade_service----

----trade_cash_flow

alter table trade_service.trade_cash_flow alter column premium type numeric(19,4);
alter table trade_service.trade_cash_flow alter column cash_flow type numeric(19,4);
