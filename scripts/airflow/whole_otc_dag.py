# -*- coding: utf-8 -*-
from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import timedelta
from utils.utils import trans_utc_datetime
from utils.airflow_utils import get_valuation_date
from trade_import.trade_import_fuc import trade_data_import
from market_data.eod_market_data import update_market_instruments, update_eod_price, \
    synchronize_bct_and_terminal_market_data, task_status_check
from dags.task.eod_terminal_data_task import eod_wind_market_data_update_run, eod_otc_trade_snapshot_update_run, \
    eod_otc_position_update_run, eod_otc_future_contract_update_run, eod_realized_vol_update_run, \
    eod_vol_surface_update_run
from eod_pd import *
from report_export.report_export import *

# -------------------------------------------------------------------------------
# dag
# these args will get passed on to each operator
# you can override them on a per-task basis during operator initialization
default_args = {
    'owner': 'TongYu',
    'catchup': False,
    'start_date': trans_utc_datetime('0:00:00'),
}
dag = DAG(
    'whole_otc_system_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval='30,31 17 * * 0-4',
    dagrun_timeout=timedelta(hours=5),
    description='whole otc system dag')
# -------------------------------------------------------------------------------

# market data
eod_wind_market_data_update_operator = PythonOperator(
    task_id='eod_wind_market_data_update_task',
    python_callable=eod_wind_market_data_update_run,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

update_market_instruments_operator = PythonOperator(
    task_id='market_instruments_update_run',
    python_callable=update_market_instruments,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

synchronize_market_instruments_operator = PythonOperator(
    task_id='market_instruments_synchronize_run',
    python_callable=synchronize_bct_and_terminal_market_data,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

update_eod_price_operator = PythonOperator(
    task_id='eod_price_update_run',
    python_callable=update_eod_price,
    op_kwargs={'current_date': get_valuation_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

task_status_check_operator = PythonOperator(
    task_id='task_status_check_run',
    python_callable=task_status_check,
    execution_timeout=timedelta(minutes=30),
    dag=dag)


# trade import
trade_import_operator = PythonOperator(
    task_id='trade_import_task',
    python_callable=trade_data_import,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(hours=2),
    dag=dag)


# terminal calc
eod_otc_trade_snapshot_update_operator = PythonOperator(
    task_id='eod_otc_trade_snapshot_update_task',
    python_callable=eod_otc_trade_snapshot_update_run,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

eod_otc_position_update_operator = PythonOperator(
    task_id='eod_otc_position_update_task',
    python_callable=eod_otc_position_update_run,
    execution_timeout=timedelta(hours=1),
    dag=dag)

eod_otc_future_contract_update_operator = PythonOperator(
    task_id='eod_otc_future_contract_update_task',
    python_callable=eod_otc_future_contract_update_run,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

eod_realized_vol_update_operator = PythonOperator(
    task_id='eod_realized_vol_update_task',
    python_callable=eod_realized_vol_update_run,
    execution_timeout=timedelta(hours=2),
    dag=dag)

eod_vol_surface_update_operator = PythonOperator(
    task_id='eod_vol_surface_update_task',
    python_callable=eod_vol_surface_update_run,
    execution_timeout=timedelta(hours=2),
    dag=dag)

export_vol_surface_operator = PythonOperator(
    task_id='export_vol_surface_task',
    python_callable=export_vol_surface_run,
    execution_timeout=timedelta(hours=1),
    op_kwargs={'valuation_date': get_valuation_date()},
    dag=dag)

export_realized_vol_operator = PythonOperator(
    task_id='export_realized_vol_task',
    python_callable=export_realized_vol_run,
    execution_timeout=timedelta(hours=1),
    op_kwargs={'valuation_date': get_valuation_date()},
    dag=dag)


# eod report
basic_position_operator = PythonOperator(
    task_id='basic_position_task',
    python_callable=basic_position_pd_run,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(minutes=5),
    dag=dag)

basic_cash_flow_operator = PythonOperator(
    task_id='basic_cash_flow_task',
    python_callable=basic_cash_flow_pd_run,
    execution_timeout=timedelta(minutes=5),
    dag=dag)

basic_cash_flow_today_operator = PythonOperator(
    task_id='basic_cash_flow_today_task',
    python_callable=basic_cash_flow_today_pd_run,
    execution_timeout=timedelta(minutes=5),
    dag=dag)

basic_risks_default_close_operator = PythonOperator(
    task_id='basic_risks_default_close_task',
    python_callable=basic_risks_default_close_pd_run,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(minutes=60),
    dag=dag)

basic_underlyer_position_default_close_operator = PythonOperator(
    task_id='basic_underlyer_position_default_close_task',
    python_callable=basic_underlyer_position_default_close_pd_run,
    execution_timeout=timedelta(minutes=5),
    dag=dag)

basic_otc_company_type_operator = PythonOperator(
    task_id='basic_otc_company_type_task',
    python_callable=basic_otc_company_type_run,
    execution_timeout=timedelta(minutes=5),
    dag=dag)

basic_instrument_contract_type_operator = PythonOperator(
    task_id='basic_instrument_contract_type_task',
    python_callable=basic_instrument_contract_type_run,
    execution_timeout=timedelta(minutes=5),
    dag=dag)

eod_position_default_close_operator = PythonOperator(
    task_id='eod_position_default_close_task',
    python_callable=eod_position_default_close_pd_run,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(minutes=5),
    dag=dag)

eod_market_risk_summary_default_close_operator = PythonOperator(
    task_id='eod_market_risk_summary_default_close_task',
    python_callable=eod_market_risk_summary_default_close_pd_run,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(minutes=5),
    dag=dag)

eod_market_risk_detail_default_close_operator = PythonOperator(
    task_id='eod_market_risk_detail_default_close_task',
    python_callable=eod_market_risk_detail_default_close_pd_run,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(minutes=5),
    dag=dag)

eod_subsidiary_market_risk_default_close_operator = PythonOperator(
    task_id='eod_subsidiary_market_risk_default_close_task',
    python_callable=eod_subsidiary_market_risk_default_close_pd_run,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(minutes=5),
    dag=dag)

eod_counter_party_market_risk_default_close_operator = PythonOperator(
    task_id='eod_counter_party_market_risk_default_close_task',
    python_callable=eod_counter_party_market_risk_default_close_pd_run,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(minutes=5),
    dag=dag)

eod_counter_party_market_risk_by_underlyer_default_close_operator = PythonOperator(
    task_id='eod_counter_party_market_risk_by_underlyer_default_close_task',
    python_callable=eod_counter_party_market_risk_by_underlyer_default_close_pd_run,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(minutes=5),
    dag=dag)

eod_market_risk_by_book_underlyer_default_close_operator = PythonOperator(
    task_id='eod_market_risk_by_book_underlyer_default_close_task',
    python_callable=eod_market_risk_by_book_underlyer_default_close_pd_run,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(minutes=5),
    dag=dag)

eod_spot_scenarios_by_market_default_close_operator = PythonOperator(
    task_id='eod_spot_scenarios_by_market_default_close_task',
    python_callable=eod_spot_scenarios_by_market_default_close_pd_run,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(minutes=120),
    dag=dag)

# eod_classic_scenarios_operator = PythonOperator(
#     task_id='eod_classic_scenarios_task',
#     python_callable=eod_classic_scenarios_pd_run,
#     op_kwargs={'valuation_date': get_valuation_date()},
#     execution_timeout=timedelta(minutes=120),
#     dag=dag)


export_market_risk_operator = PythonOperator(
    task_id='export_market_risk_task',
    python_callable=export_market_risk_run,
    execution_timeout=timedelta(hours=1),
    op_kwargs={'valuation_date': get_valuation_date()},
    dag=dag)

export_market_risk_detail_operator = PythonOperator(
    task_id='export_market_risk_detail_task',
    python_callable=export_market_risk_detail_run,
    execution_timeout=timedelta(hours=1),
    op_kwargs={'valuation_date': get_valuation_date()},
    dag=dag)

export_subsidiary_market_risk_operator = PythonOperator(
    task_id='export_subsidiary_market_risk_task',
    python_callable=export_subsidiary_market_risk_run,
    execution_timeout=timedelta(hours=1),
    op_kwargs={'valuation_date': get_valuation_date()},
    dag=dag)

export_subsidiary_market_risk_detail_operator = PythonOperator(
    task_id='export_subsidiary_market_risk_detail_task',
    python_callable=export_subsidiary_market_risk_detail_run,
    execution_timeout=timedelta(hours=1),
    op_kwargs={'valuation_date': get_valuation_date()},
    dag=dag)

export_counter_party_market_risk_operator = PythonOperator(
    task_id='export_counter_party_market_risk_task',
    python_callable=export_counter_party_market_risk_run,
    execution_timeout=timedelta(hours=1),
    op_kwargs={'valuation_date': get_valuation_date()},
    dag=dag)

export_counter_party_market_risk_detail_operator = PythonOperator(
    task_id='export_counter_party_market_risk_detail_task',
    python_callable=export_counter_party_market_risk_detail_run,
    execution_timeout=timedelta(hours=1),
    op_kwargs={'valuation_date': get_valuation_date()},
    dag=dag)

export_spot_scenarios_operator = PythonOperator(
    task_id='export_spot_scenarios_task',
    python_callable=export_spot_scenarios_run,
    execution_timeout=timedelta(hours=1),
    op_kwargs={'valuation_date': get_valuation_date()},
    dag=dag)


# -----------------------------------------------------------------------------------------
# 标的行情更新
eod_wind_market_data_update_operator.set_downstream(update_market_instruments_operator)
update_market_instruments_operator.set_downstream(synchronize_market_instruments_operator)
synchronize_market_instruments_operator.set_downstream(update_eod_price_operator)
update_eod_price_operator.set_downstream(task_status_check_operator)
# 交易导入
task_status_check_operator.set_downstream(trade_import_operator)
trade_import_operator.set_downstream(eod_otc_trade_snapshot_update_operator)
trade_import_operator.set_downstream(eod_otc_future_contract_update_operator)
# terminal相关计算
eod_otc_trade_snapshot_update_operator.set_downstream(eod_otc_position_update_operator)
eod_otc_position_update_operator.set_downstream(eod_vol_surface_update_operator)
eod_otc_future_contract_update_operator.set_downstream(eod_realized_vol_update_operator)
eod_realized_vol_update_operator.set_downstream(eod_vol_surface_update_operator)
eod_realized_vol_update_operator.set_downstream(export_realized_vol_operator)
eod_vol_surface_update_operator.set_downstream(export_vol_surface_operator)
# eod报告
basic_position_operator.set_upstream(eod_vol_surface_update_operator)
basic_cash_flow_operator.set_upstream(eod_vol_surface_update_operator)
basic_cash_flow_today_operator.set_upstream(eod_vol_surface_update_operator)
basic_underlyer_position_default_close_operator.set_upstream(eod_vol_surface_update_operator)

basic_instrument_contract_type_operator.set_downstream(basic_position_operator)
basic_instrument_contract_type_operator.set_upstream(eod_vol_surface_update_operator)

# basic_otc_company_type_operator.set_downstream(eod_classic_scenarios_operator)
basic_otc_company_type_operator.set_downstream(eod_spot_scenarios_by_market_default_close_operator)
basic_otc_company_type_operator.set_downstream(eod_counter_party_market_risk_default_close_operator)
basic_otc_company_type_operator.set_downstream(eod_counter_party_market_risk_by_underlyer_default_close_operator)

# eod_classic_scenarios_operator.set_upstream(basic_position_operator)
eod_spot_scenarios_by_market_default_close_operator.set_upstream(basic_position_operator)

basic_risks_default_close_operator.set_upstream(basic_position_operator)
eod_position_default_close_operator.set_upstream(basic_risks_default_close_operator)
eod_position_default_close_operator.set_upstream(basic_cash_flow_operator)

eod_position_default_close_operator.set_downstream(eod_market_risk_summary_default_close_operator)
eod_position_default_close_operator.set_downstream(eod_market_risk_detail_default_close_operator)
eod_position_default_close_operator.set_downstream(eod_subsidiary_market_risk_default_close_operator)
eod_position_default_close_operator.set_downstream(eod_counter_party_market_risk_default_close_operator)
eod_position_default_close_operator.set_downstream(eod_counter_party_market_risk_by_underlyer_default_close_operator)
eod_position_default_close_operator.set_downstream(eod_market_risk_by_book_underlyer_default_close_operator)

export_market_risk_operator.set_upstream(eod_market_risk_summary_default_close_operator)
export_market_risk_detail_operator.set_upstream(eod_market_risk_detail_default_close_operator)
# export_market_risk_detail_operator.set_upstream(eod_classic_scenarios_operator)
export_subsidiary_market_risk_operator.set_upstream(eod_subsidiary_market_risk_default_close_operator)
export_subsidiary_market_risk_detail_operator.set_upstream(eod_market_risk_by_book_underlyer_default_close_operator)
export_counter_party_market_risk_operator.set_upstream(eod_counter_party_market_risk_default_close_operator)
export_counter_party_market_risk_detail_operator.set_upstream(eod_counter_party_market_risk_by_underlyer_default_close_operator)
export_spot_scenarios_operator.set_upstream(eod_spot_scenarios_by_market_default_close_operator)
