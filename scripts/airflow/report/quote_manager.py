# -*- coding: utf-8 -*-
import random

from utils import utils


def get_tickers_from_file():
    f = open("/root/airflow/win_feed/whitelist.csv")
    line = f.readline()
    instruments = []
    i = 0
    while line:
        if i > 0 and len(line.strip('\n')) > 0:
            instruments.append(line.strip('\n'))
        line = f.readline()
        i = i + 1
    f.close()
    return instruments


def get_latest_quotes(ip, headers, tickers):
    params = {
        'instrumentIds': tickers,
        'instance': 'close'
    }
    latest_quotes = utils.call_request(ip, 'market-data-service', 'mktQuotesList', params, headers)['result']
    latest_map = {}
    for quote in latest_quotes:
        key = quote['instrumentId']
        latest_map[key] = quote
    return latest_map


def generate_intraday_quote(map, ticker):
    quote = map[ticker]
    close = quote['fields']['CLOSE']
    param = {}
    bid = close * (1+random.randint(-10, 0)/100)
    ask = close * (1+random.randint(0, 10)/100)
    last = (bid+ask)/2
    print('generate intraday quote for '+str(ticker)+':bid='+str(bid)+',ask='+str(ask)+',last='+str(last))
    param['BID'] = bid
    param['ASK'] = ask
    param['LAST'] = last
    body = {
        'instrumentId': ticker,
        'instance': 'intraday',
        'quote': param,
    }
    return body


def generate_close_quote(map, ticker):
    quote = map[ticker]
    last = quote['fields']['CLOSE']
    param = {}
    open_quote = last * (1+random.randint(-10, 10)/100)
    high_quote = last * (1+random.randint(0, 10)/100)
    low_quote = last * (1+random.randint(-10, 0)/100)
    close_quote = (high_quote+low_quote)/2
    settle_quote = close_quote
    param['OPEN'] = open_quote
    param['HIGH'] = high_quote
    param['LOW'] = low_quote
    param['CLOSE'] = close_quote
    param['SETTLE'] = settle_quote
    body = {
        'instrumentId': ticker,
        'instance': 'close',
        'quote': param,
    }
    return body


def mkt_quote_save_batch(ip, headers, quotes):
    params = {
        'quotes': quotes
    }
    result = utils.call_request(ip, 'market-data-service', 'mktQuoteSaveBatch', params, headers)
    return result
