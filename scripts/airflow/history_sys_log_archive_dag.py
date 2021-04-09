# -*- coding: utf-8 -*-

from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import timedelta
from utils.utils import trans_utc_datetime
from history_data_archive.history_sys_log_archive import archive_sys_log_run

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
    'history_sys_log_archive_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval='0,1 20 * * *',
    dagrun_timeout=timedelta(hours=3),
    description='history_sys_log_archive dag')
# -------------------------------------------------------------------------------


archive__sys_log_run_operator = PythonOperator(
    task_id='archive__sys_log_run_task',
    python_callable=archive_sys_log_run,
    execution_timeout=timedelta(hours=3),
    dag=dag)


