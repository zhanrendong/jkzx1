# -*- coding: utf-8 -*-

from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import timedelta
from utils.utils import trans_utc_datetime
from utils.airflow_utils import get_manual_trigger_start_date, get_manual_trigger_end_date
from market_report_import.market_report_import import *

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
    'manual_hive_market_report_import_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval=None,
    dagrun_timeout=timedelta(hours=1),
    description='manual import hive market report dag')
# -------------------------------------------------------------------------------

PythonOperator(
    task_id='import_summary_task',
    python_callable=import_summary,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

PythonOperator(
    task_id='import_trade_summary_task',
    python_callable=import_trade_summary,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

PythonOperator(
    task_id='import_position_summary_task',
    python_callable=import_position_summary,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

PythonOperator(
    task_id='import_asset_tool_task',
    python_callable=import_asset_tool,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

PythonOperator(
    task_id='import_cus_type_task',
    python_callable=import_cus_type,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

PythonOperator(
    task_id='import_market_dist_task',
    python_callable=import_market_dist,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

PythonOperator(
    task_id='import_et_commodity_task',
    python_callable=import_et_commodity,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

PythonOperator(
    task_id='import_et_cus_task',
    python_callable=import_et_cus,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

PythonOperator(
    task_id='import_et_sub_company_task',
    python_callable=import_et_sub_company,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

PythonOperator(
    task_id='import_market_manipulate_task',
    python_callable=import_market_manipulate,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

PythonOperator(
    task_id='import_comp_propagate_task',
    python_callable=import_comp_propagate,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)

PythonOperator(
    task_id='import_cus_pos_percentage_task',
    python_callable=import_cus_pos_percentage,
    op_kwargs={'start_date': get_manual_trigger_start_date(), 'end_date': get_manual_trigger_end_date()},
    execution_timeout=timedelta(minutes=30),
    dag=dag)