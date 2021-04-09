# -*- coding: utf-8 -*-

from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import timedelta
from utils.utils import trans_utc_datetime
from utils.airflow_utils import set_valuation_date, set_calendar_import_year



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
    'valuation_date_update_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval='0,1 17 * * *',
    dagrun_timeout=timedelta(minutes=10),
    description='valuation date manager dag')
# ----------------------------------------


PythonOperator(
    task_id='valuation_date_update_task',
    python_callable=set_valuation_date,
    execution_timeout=timedelta(minutes=10),
    dag=dag)

PythonOperator(
    task_id='calendar_import_year_update_task',
    python_callable=set_calendar_import_year,
    execution_timeout=timedelta(minutes=10),
    dag=dag)
