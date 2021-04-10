# -*- coding: utf-8 -*-
from dags.conf.settings import DBConfig as TerminalDBConfig, BCTServerConfig, TerminalServerConfig

# postgres
pg_host = 'localhost'
pg_port = '5432'
pg_database = 'bct'
pg_user = 'bct'
pg_password = 'kEaLJ9ZERLLN!'
pg_url = 'postgresql+psycopg2://%s:%s@%s:%s' % (pg_user, pg_password, pg_host, pg_port)
pg_terminal_database = 'terminal_data'
pg_terminal_schema = TerminalDBConfig.default_schema
pg_terminal_connection = TerminalDBConfig.db_connection
pg_terminal_show_sql = TerminalDBConfig.show_sql

# bct server
bct_host = BCTServerConfig.host
bct_user = BCTServerConfig.username
bct_password = BCTServerConfig.password
bct_port = BCTServerConfig.port
special_captcha = BCTServerConfig.special_captcha
bct_login_body = {
    'userName': bct_user,
    'password': bct_password
}

# terminal
terminal_host = TerminalServerConfig.host
terminal_port = TerminalServerConfig.port
ENV_TERMINAL_SERVICE_HOST = 'ENV_TERMINAL_SERVICE_HOST'

# 定价，情景分析接口数量限制
MAX_PRICING_TRADES_NUM = 10000
