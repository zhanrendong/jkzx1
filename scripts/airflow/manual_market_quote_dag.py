# -*- coding: utf-8 -*-

from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import timedelta
from utils.utils import trans_utc_datetime
from market_data.eod_market_data import update_eod_price, task_status_check
from utils.airflow_utils import get_valuation_date

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
    'manual_market_quote_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval=None,
    dagrun_timeout=timedelta(hours=1),
    description='manual import quote dag')
# -------------------------------------------------------------------------------

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

update_eod_price_operator.set_downstream(task_status_check_operator)

