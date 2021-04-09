# -*- coding: utf-8 -*-

from airflow import DAG
from airflow.operators.python_operator import PythonOperator

from market_data.eod_market_data import update_market_instruments,update_eod_price,synchronize_bct_and_terminal_market_data,task_status_check
from utils.utils import *
from utils.airflow_utils import get_valuation_date
from dags.task.eod_terminal_data_task import eod_wind_market_data_update_run

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
    'eod_market_data_sync_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval='10,11 17 * * 1-5',
    dagrun_timeout=timedelta(minutes=30),
    description='market_data synchronize dag')
# -------------------------------------------------------------------------------

eod_wind_market_data_update_operator = PythonOperator(
    task_id='eod_wind_market_data_update_task',
    python_callable=eod_wind_market_data_update_run,
    execution_timeout=timedelta(minutes=10),
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

eod_wind_market_data_update_operator.set_downstream(update_market_instruments_operator)
update_market_instruments_operator.set_downstream(synchronize_market_instruments_operator)
synchronize_market_instruments_operator.set_downstream(update_eod_price_operator)
update_eod_price_operator.set_downstream(task_status_check_operator)
