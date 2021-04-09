# -*- coding: utf-8 -*-
from datetime import datetime

_datetime_fmt = '%Y-%m-%dT%H:%M:%S'


def intraday_portfolio_trades_report(risks, position_index, portfolio_trades, pricing_environment):
    # 整理risk报告，增加交易编号tradeId
    timestamp = str(datetime.now())
    risk_reports = []
    for risk in risks.values():
        risk['tradeId'] = position_index[risk['positionId']]['tradeId']
        risk_reports.append(risk)
    portfolio_reports = []
    for portfolio_name in portfolio_trades:
        rho_total = 0
        vega_total = 0
        theta_total = 0
        delta_cash_total = 0
        gamma_cash_total = 0
        trade_ids = portfolio_trades[portfolio_name]
        for trade_id in trade_ids:
            for risk in risk_reports:
                if not risk['isSuccess']:
                    continue
                if trade_id == risk['tradeId']:
                    rho_total = rho_total + risk['rhoR']
                    vega_total = vega_total + risk['vega']
                    theta_total = theta_total + risk['theta']
                    delta_cash = risk['delta'] * risk['underlyerPrice']
                    gamma_cash = risk['gamma'] * risk['underlyerPrice'] * risk['underlyerPrice'] / 100
                    delta_cash_total = delta_cash_total + delta_cash
                    gamma_cash_total = gamma_cash_total + gamma_cash
        portfolio_report = {
            'portfolioName': portfolio_name,
            'deltaCash': delta_cash_total,
            'gammaCash': gamma_cash_total,
            'theta': theta_total,
            'vega': vega_total,
            'rho': rho_total,
            'pricingEnvironment': pricing_environment,
            'createdAt': timestamp
        }
        portfolio_reports.append(portfolio_report)

    return portfolio_reports
