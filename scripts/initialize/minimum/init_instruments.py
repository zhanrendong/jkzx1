import utils
import io
from init_params import *

method = 'mktInstrumentCreate'


def create_commodity_futures(id, name, exchange, multiplier, maturity, host, token):
    params = {
        'instrumentId': id,
        'assetClass': 'commodity',
        'instrumentType': 'futures',
        'assetSubClass': 'others',
        'instrumentInfo': {
            'name': name,
            'multiplier': multiplier,
            'exchange': exchange,
            'maturity': maturity
        }
    }
    utils.call(method, params, service='market-data-service', host=host, token=token)


def create_commodity_spot(id, name, exchange, multiplier, host, token):
    params = {
        'instrumentId': id,
        'assetClass': 'commodity',
        'instrumentType': 'spot',
        'assetSubClass': 'others',
        'instrumentInfo': {
            'name': name,
            'multiplier': multiplier,
            'exchange': exchange
        }
    }
    utils.call(method, params, service='market-data-service', host=host, token=token)


def create_equity_stock(id, name, exchange, multiplier, host, token):
    params = {
        'instrumentId': id,
        'assetClass': 'equity',
        'instrumentType': 'stock',
        'assetSubClass': 'equity',
        'instrumentInfo': {
            'name': name,
            'multiplier': multiplier,
            'exchange': exchange
        }
    }
    utils.call(method, params, service='market-data-service', host=host, token=token)


def create_equity_index(id, name, exchange, host, token):
    params = {
        'instrumentId': id,
        'assetClass': 'equity',
        'instrumentType': 'index',
        'assetSubClass': 'index',
        'instrumentInfo': {
            'name': name,
            'exchange': exchange
        }
    }
    utils.call(method, params, service='market-data-service', host=host, token=token)


def create_equity_index_futures(id, name, exchange, multiplier, maturity, host, token):
    params = {
        'instrumentId': id,
        'assetClass': 'equity',
        'instrumentType': 'index_futures',
        'instrumentInfo': {
            'name': name,
            'exchange': exchange,
            'multiplier': multiplier,
            'maturity': maturity
        }
    }
    utils.call(method, params, service='market-data-service', host=host, token=token)


def save_whitelist(id, exchange, limit, host, token):
    utils.call('mktInstrumentWhitelistSave', {
        'venueCode': exchange,
        'instrumentId': id,
        'notionalLimit': limit},
               'market-data-service', host, token)


if __name__ == '__main__':
    import init_auth

    token = utils.login(init_auth.trader_names[0], init_auth.trader_password, host)
    file = io.open('instrument.csv', 'r', encoding='utf8')
    lines = file.readlines()
    for line in lines:
        instrument = line.replace('\n', '').split(',')
        id = instrument[0]
        asset = instrument[1]
        instrument_type = instrument[2]
        sub_asset = instrument[3]
        name = instrument[4]
        exchange = instrument[5]
        multiplier = instrument[6]
        maturity = instrument[7]
        trade_category = instrument[8]
        trade_unit = instrument[9]
        unit = instrument[10]
        info = {
            'name': name,
            'tradeCategory' : trade_category,
            'tradeUnit' : trade_unit,
            'unit' : unit
        }
        if maturity != 'None':
            info.update({'maturity': maturity})
        if exchange != 'None':
            info.update({'exchange': exchange})
        if instrument_type != 'INDEX':
            if multiplier != 'None':
                info.update({'multiplier': multiplier})
            else:
                info.update({'multiplier': 1})
        params = {
            'instrumentId': id,
            'assetClass': asset,
            'assetSubClass': sub_asset,
            'instrumentType': instrument_type,
            'instrumentInfo': info
        }
        res = utils.call('mktInstrumentCreate', params, 'market-data-service', host, token)
        print(res)
        res = utils.call('mktInstrumentWhitelistSave',
                         {'venueCode': exchange, 'instrumentId': id, 'notionalLimit': 100000000000.0},
                         'market-data-service', host, token)
        print(res)

