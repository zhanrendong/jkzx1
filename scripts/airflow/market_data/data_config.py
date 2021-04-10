# -*- encoding: utf-8 -*-
from config.bct_config import bct_user, bct_password, bct_host, bct_port, terminal_port, terminal_host

# database_wind_url = 'wbsj/Cfmmc_wbsj_7s8X@10.240.22.23:1521/wbsjdb.cfmmc.com'  # wind oracle db
# database_wind_url = 'bct/bct@10.1.5.41:1521/helowinXDB'  # 仿真环境oracle
# wind_oracle_url = 'oracle+cx_oracle://bct:bct@10.1.5.41:1521/helowin'

host = bct_host + ':' + bct_port
user = bct_user
password = bct_password

terminal_hostport = terminal_host + ':' + terminal_port
