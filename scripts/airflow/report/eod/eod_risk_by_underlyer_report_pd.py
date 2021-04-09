# -*- coding: utf-8 -*-
from datetime import datetime, timedelta
from report.basic_quotes_pd import get_underlyer_quotes
import numpy as np
import pandas as pd


def process_underlyer_position(row):
    spot = row['underlyerPrice']
    multiplier = np.float64(row['underlyerInstrumentMultiplier'])
    row['underlyerNetPosition'] = np.float64(row['netPosition']) if row['instrumentId'] == row[
        'underlyerInstrumentId'] else 0
    row['delta'] = 0 if row['instrumentId'] == row['underlyerInstrumentId'] else np.float64(row['delta']) / multiplier
    row['gamma'] = np.float64(row['gamma']) * spot / 100 / multiplier
    row['gammaCash'] = row['gamma'] * spot
    row['vega'] = np.float64(row['vega']) / 100
    row['theta'] = np.float64(row['theta']) / 365
    row['rho'] = np.float64(row['rhoR']) / 100
    return row


def eod_risk_by_underlyer_report(positions, underlyer_positions, domain, headers, pricing_environment):
    """Return Eod risk collected by book and underlyer.

    positions: eod position report
    underlyer_positions: basic underlyer position report"""
    # find books and underlyers
    underlyer_positions_multiplier = underlyer_positions[['instrumentId', 'underlyerInstrumentMultiplier']]
    sub_underlyer_positions = underlyer_positions.apply(lambda row: process_underlyer_position(row), axis=1)[
        ['bookId', 'instrumentId', 'underlyerNetPosition', 'delta', 'gamma', 'gammaCash', 'vega', 'theta', 'rho']]
    sub_underlyer_positions.rename(columns={'bookId': 'bookName', 'instrumentId': 'underlyerInstrumentId'},
                                   inplace=True)
    sub_positions_df = positions[
        ['bookName', 'underlyerInstrumentId', 'delta', 'deltaDecay', 'deltaWithDecay', 'gamma', 'gammaCash', 'vega',
         'theta', 'rho']]
    book_underlyers = pd.concat([sub_positions_df, sub_underlyer_positions], ignore_index=True, sort=False)
    risk_report_data = book_underlyers[book_underlyers.bookName == book_underlyers.bookName].fillna(0)

    underlyers = list(risk_report_data['underlyerInstrumentId'].dropna().unique())

    risk_report = risk_report_data.groupby(['bookName', 'underlyerInstrumentId']).sum().reset_index()

    quotes = get_underlyer_quotes(underlyers, datetime.now(), domain, headers)[['close']]
    quotes.reset_index(inplace=True)
    quotes.rename(columns={'instrumentId': 'underlyerInstrumentId', 'close': 'underlyerPrice'}, inplace=True)
    risk_report = risk_report.merge(quotes, on='underlyerInstrumentId', how='left')

    yst_quotes = get_underlyer_quotes(underlyers, datetime.now() - timedelta(days=1), domain, headers)[['close']]
    yst_quotes.reset_index(inplace=True)
    yst_quotes.rename(columns={'instrumentId': 'underlyerInstrumentId'}, inplace=True)
    risk_report = risk_report.merge(yst_quotes, on='underlyerInstrumentId', how='left')
    risk_report['underlyerPriceChangePercent'] = (np.float64(risk_report['underlyerPrice']) - np.float64(
        risk_report['close'])) / np.float64(risk_report['underlyerPrice'])

    risk_report['netDelta'] = np.float64(risk_report['underlyerNetPosition']) + np.float64(risk_report['delta'])
    risk_report['pricingEnvironment'] = pricing_environment

    # deltaCash
    underlyer_positions_multiplier.columns = ['underlyerInstrumentId', 'underlyerMultiplier']
    positions_multiplier = positions[['underlyerInstrumentId', 'underlyerMultiplier']]
    multiplier = pd.concat([underlyer_positions_multiplier, positions_multiplier], ignore_index=True, sort=False)
    multiplier.drop_duplicates(inplace=True)
    risk_report = risk_report.merge(multiplier, on='underlyerInstrumentId')
    risk_report['deltaCash'] = np.float64(risk_report['netDelta']) * np.float64(risk_report['underlyerPrice'])
    risk_report.drop(columns=['close', 'underlyerMultiplier'], inplace=True)
    risk_report.drop_duplicates(inplace=True)
    return list(risk_report.fillna(0).to_dict(orient='index').values())
