# -*- coding: utf-8 -*-
from dags.conf.settings import OracleDBConfig, HiveConfig

# trade oracle
REPORT_TO_ORACLE_SCHEMA = OracleDBConfig.report_to_oracle_schema
center_oracle_database_url = OracleDBConfig.trade_snapshot_db_connection
# wind oracle
SCHEMA_WIND_MARKET = OracleDBConfig.market_data_schema
wind_oracle_database_url = OracleDBConfig.market_data_db_connection
# hive
HIVE_TRADE_SCHEMA = HiveConfig.hive_trade_schema
HIVE_REPORT_SCHEMA = HiveConfig.hive_report_schema
HIVE_TRADEDATE_SCHEMA = HiveConfig.hive_tradedate_schema
hive_database_url = HiveConfig.hive_connection
hive_host = HiveConfig.hive_host
hive_port = HiveConfig.hive_port
hive_username = HiveConfig.hive_username
hive_password = HiveConfig.hive_password
hive_database = HiveConfig.hive_database
hive_auth = HiveConfig.hive_auth

# wind_oracle_database_url = 'oracle+cx_oracle://bct:bct@10.1.5.41:1521/helowin'
# center_oracle_database_url = 'oracle+cx_oracle://bct:bct@10.1.5.41:1521/helowin'
# hive_database_url = f'hive://hive:hive@10.1.5.41:10000/default?auth=CUSTOM'
