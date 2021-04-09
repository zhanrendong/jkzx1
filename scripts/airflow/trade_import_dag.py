# -*- coding: utf-8 -*-

from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from trade_import.trade_import_fuc import trade_data_import
from trade_import import utils
from utils.airflow_utils import get_valuation_date
from datetime import timedelta


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
    'trade_import_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval='30,31 17 * * 0-4',
    dagrun_timeout=timedelta(hours=6),
    description='trade import dag')
# ----------------------------------------


trade_import_operator = PythonOperator(
    task_id='trade_import_task',
    python_callable=trade_data_import,
    op_kwargs={'valuation_date': get_valuation_date()},
    execution_timeout=timedelta(hours=6),
    dag=dag)
