# -*- coding: utf-8 -*-
from utils import utils


def get_portfolio_trades(domain, headers):
    portfolio_trades = {}
    portfolio_data = utils.call_request(domain, 'trade-service', 'trdPortfolioTradesList', {}, headers)
    if 'result' in portfolio_data:
        portfolio_trades = portfolio_data['result']
    return portfolio_trades
