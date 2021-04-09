# -*- coding: utf-8 -*-

from utils import utils
import numpy as np
from datetime import datetime


def intraday_portfolio_trades_report(risks, position_index, portfolio_trades, pricing_environment):
    # 整理risk报告，增加交易编号tradeId
    if portfolio_trades.empty:
        return []
    risks = risks.join(position_index['tradeId'])
    risks_data = risks[['tradeId', 'rhoR', 'vega', 'theta', 'delta', 'gamma', 'underlyerPrice']].fillna(0)
    risks_data.rename(columns={'rhoR': 'rho'}, inplace=True)
    risks_data['deltaCash'] = np.float64(risks_data['delta']) * np.float64(risks_data['underlyerPrice'])
    risks_data['gammaCash'] = np.float64(risks_data['gamma']) * np.float64(risks_data['underlyerPrice']) * np.float64(
        risks_data['underlyerPrice']) / 100
    risks_data = risks_data.groupby('tradeId').sum().reset_index()

    report = portfolio_trades.merge(risks_data, on='tradeId', how='left').groupby('portfolioName').sum().reset_index()[
        ['portfolioName', 'deltaCash', 'gammaCash', 'theta', 'vega', 'rho']]
    report['pricingEnvironment'] = pricing_environment
    report['createdAt'] = str(datetime.now())
    return utils.remove_nan_and_inf(list(report.to_dict(orient='index').values()))
