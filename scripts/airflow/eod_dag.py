# -*- coding: utf-8 -*-

from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import timedelta
from eod_pd import *
from report_export.report_export import *
from utils.airflow_utils import get_valuation_date


# -------------------------------------------------------------------------------
# dag
# these args will get passed on to each operator
# you can override them on a per-task basis during operator initialization
default_args = {
    'owner': 'TongYu',
    'catchup': False,
    'start_date': utils.trans_utc_datetime('0:00:00'),
}
dag = DAG(
    'eod_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval='30,31 18 * * 0-4',
    dagrun_timeout=timedelta(minutes=15),
    description='close_task dag')
# -------------------------------------------------------------------------------


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
    execution_timeout=timedelta(minutes=5),
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
    execution_timeout=timedelta(minutes=30),
    dag=dag)

# eod_classic_scenarios_operator = PythonOperator(
#     task_id='eod_classic_scenarios_task',
#     python_callable=eod_classic_scenarios_pd_run,
#     op_kwargs={'valuation_date': get_valuation_date()},
#     execution_timeout=timedelta(minutes=30),
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


# -----------------------------------------------------------------------------------
# Operator Dependency Relationship
# default close tasks
basic_risks_default_close_operator.set_upstream(basic_position_operator)
basic_instrument_contract_type_operator.set_downstream(basic_position_operator)

# basic_otc_company_type_operator.set_downstream(eod_classic_scenarios_operator)
basic_otc_company_type_operator.set_downstream(eod_spot_scenarios_by_market_default_close_operator)
basic_otc_company_type_operator.set_downstream(eod_counter_party_market_risk_default_close_operator)
basic_otc_company_type_operator.set_downstream(eod_counter_party_market_risk_by_underlyer_default_close_operator)

# eod_classic_scenarios_operator.set_upstream(basic_position_operator)
eod_spot_scenarios_by_market_default_close_operator.set_upstream(basic_position_operator)

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