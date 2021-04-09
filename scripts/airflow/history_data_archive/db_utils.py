# -*- coding: utf-8 -*-
import psycopg2
import os

from config.bct_config import *

os.environ['NLS_LANG'] = 'SIMPLIFIED CHINESE_CHINA.UTF8'


def get_pg_connection():
    return psycopg2.connect(database=pg_database, user=pg_user, password=pg_password, host=pg_host, port=pg_port)





