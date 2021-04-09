# -*- coding: utf-8 -*-

from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from datetime import timedelta
from mock_data.mock_generate_eod_price import *
from mock_data.mock_trade_data import mock_trades
from utils.utils import trans_utc_datetime

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
    'mock_dag',
    catchup=False,
    default_args=default_args,
    schedule_interval='0,1 12 * * 1-5',
    dagrun_timeout=timedelta(minutes=30),
    description='mock dag')
# -------------------------------------------------------------------------------


generate_today_gold_spot_eod_price_operator = PythonOperator(
    task_id='generate_today_gold_spot_eod_price',
    python_callable=generate_today_gold_spot_eod_price,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

generate_today_index_eod_price_operator = PythonOperator(
    task_id='generate_today_index_eod_price',
    python_callable=generate_today_index_eod_price,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

generate_today_share_eod_price_operator = PythonOperator(
    task_id='generate_today_share_eod_price',
    python_callable=generate_today_share_eod_price,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

generate_today_bond_future_eod_price_operator = PythonOperator(
    task_id='generate_today_bond_future_eod_price',
    python_callable=generate_today_bond_future_eod_price,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

generate_today_commodity_future_eod_price_operator = PythonOperator(
    task_id='generate_today_commodity_future_eod_price',
    python_callable=generate_today_commodity_future_eod_price,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

generate_today_index_future_eod_price_operator = PythonOperator(
    task_id='generate_today_index_future_eod_price',
    python_callable=generate_today_index_future_eod_price,
    execution_timeout=timedelta(minutes=30),
    dag=dag)

mock_trades_dag = PythonOperator(
    task_id='mock_trades',
    python_callable=mock_trades,
    execution_timeout=timedelta(hours=8),
    dag=dag)