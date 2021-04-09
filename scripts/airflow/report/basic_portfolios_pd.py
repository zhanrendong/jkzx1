# -*- coding: utf-8 -*-
from utils import utils
import pandas as pd


def get_portfolio_trades(domain, headers):
    result = []
    portfolio_data = utils.call_request(domain, 'trade-service', 'trdPortfolioTradesList', {}, headers)
    if 'result' in portfolio_data:
        portfolio_trades = portfolio_data['result']
        for portfolio_name in portfolio_trades:
            trade_ids = portfolio_trades[portfolio_name]
            for trade_id in trade_ids:
                result.append({'tradeId': trade_id, 'portfolioName': portfolio_name})
    return pd.DataFrame(result)
