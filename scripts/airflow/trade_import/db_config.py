# -*- coding: utf-8 -*-
from config.bct_config import bct_password, bct_user, bct_host, bct_port

bct_ip = bct_host + ':' + bct_port
login_body = {
    'userName': bct_user,
    'password': bct_password
}
