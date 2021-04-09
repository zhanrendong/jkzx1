# -*- coding: utf-8 -*-
import utils

domain = 'localhost'
login_body = {
    'userName': 'admin',
    'password': '12345'
}


def trade_upgrade():
    headers = utils.login(domain, login_body)
    utils.call_request(domain, 'trade-service', 'trdTradeGenerateHistoryIndex', {}, headers)
    utils.call_request(domain, 'trade-service', 'tradeRepairHistoryCashFlow', {}, headers)


trade_upgrade()
