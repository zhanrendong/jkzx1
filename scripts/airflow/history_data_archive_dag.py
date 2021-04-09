# -*- coding: utf-8 -*-

from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import timedelta
from utils.utils import trans_utc_datetime
from history_data_archive.history_report_archive import *

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
    'history_data_archive_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval='0,1 17 1 1 *',
    dagrun_timeout=timedelta(hours=6),
    description='history_data_archive dag')
# -------------------------------------------------------------------------------


archive__market_risk_report_run_operator = PythonOperator(
    task_id='archive__market_risk_report_run_task',
    python_callable=archive__market_risk_report_run,
    execution_timeout=timedelta(hours=6),
    dag=dag)

archive__market_risk_detail_report_run_operator = PythonOperator(
    task_id='archive__market_risk_detail_report_run_task',
    python_callable=archive__market_risk_detail_report_run,
    execution_timeout=timedelta(hours=6),
    dag=dag)

archive__subsidiary_market_risk_report_run_operator = PythonOperator(
    task_id='archive__subsidiary_market_risk_report_run_task',
    python_callable=archive__subsidiary_market_risk_report_run,
    execution_timeout=timedelta(hours=6),
    dag=dag)

archive__subsidiary_market_risk_by_underlyer_report_run_operator = PythonOperator(
    task_id='archive__subsidiary_market_risk_by_underlyer_report_run_task',
    python_callable=archive__subsidiary_market_risk_by_underlyer_report_run,
    execution_timeout=timedelta(hours=6),
    dag=dag)

archive__counter_party_market_risk_report_run_operator = PythonOperator(
    task_id='archive__counter_party_market_risk_report_run_task',
    python_callable=archive__counter_party_market_risk_report_run,
    execution_timeout=timedelta(hours=6),
    dag=dag)

archive__counter_party_market_risk_underlyer_report_run_operator = PythonOperator(
    task_id='archive__counter_party_market_risk_underlyer_report_run_task',
    python_callable=archive__counter_party_market_risk_underlyer_report_run,
    execution_timeout=timedelta(hours=6),
    dag=dag)

archive__table_spot_scenarios_report_run_operator = PythonOperator(
    task_id='archive__table_spot_scenarios_report_run_task',
    python_callable=archive__table_spot_scenarios_report_run,
    execution_timeout=timedelta(hours=6),
    dag=dag)

