# -*- coding: utf-8 -*-

from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import timedelta
from utils.utils import trans_utc_datetime
from utils.airflow_utils import init_manual_trigger_date, init_hive_market_report_import_days, \
    init_fair_vol_marking_process_num


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
    'airflow_variables_init_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval=None,
    dagrun_timeout=timedelta(minutes=10),
    description='airflow variables init dag')
# ----------------------------------------


PythonOperator(
    task_id='init_manual_trigger_date_task',
    python_callable=init_manual_trigger_date,
    execution_timeout=timedelta(minutes=10),
    dag=dag)

PythonOperator(
    task_id='init_hive_market_report_import_days_task',
    python_callable=init_hive_market_report_import_days,
    execution_timeout=timedelta(minutes=10),
    dag=dag)

PythonOperator(
    task_id='init_fair_vol_marking_process_num_task',
    python_callable=init_fair_vol_marking_process_num,
    execution_timeout=timedelta(minutes=10),
    dag=dag)