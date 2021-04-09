# -*- coding: utf-8 -*-

from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import timedelta
from utils.utils import trans_utc_datetime
from calendar_import.calendar_import import calendar_import
from dags.task.eod_terminal_data_task import eod_trading_calendar_update_run
from utils.airflow_utils import get_calendar_import_year


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
    'calendar_import_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval='0,1 17 1 1 *',
    dagrun_timeout=timedelta(hours=1),
    description='trading calendar and vol calendar dag')
# ----------------------------------------


bct_calendar_update_operator = PythonOperator(
    task_id='calendar_import_task',
    python_callable=calendar_import,
    op_kwargs={'current_year': get_calendar_import_year()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

terminal_trading_calendar_update_operator = PythonOperator(
    task_id='eod_trading_calendar_update_task',
    python_callable=eod_trading_calendar_update_run,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

terminal_trading_calendar_update_operator.set_upstream(bct_calendar_update_operator)
