import utils
import io
import datetime
from init_params import *


def save_quote(id, instance, quote, date, host, token):
    utils.call('mktQuoteSave', {
        'instrumentId': id,
        'instance': instance,
        'quote': quote,
        'valuationDate': date.strftime('%Y-%m-%d')
    }, 'market-data-service', host, token)


if __name__ == '__main__':
    import init_auth

    token = utils.login(init_auth.trader_names[0], init_auth.trader_password, host)
    yesterday = datetime.date.today() - datetime.timedelta(days=1)
    method = 'mktQuoteSave'
    file = io.open('market.csv', 'r', encoding='utf8')
    lines = file.readlines()
    existing_instruments = utils.call('mktInstrumentIdsList', {}, 'market-data-service', host, token)
    for line in lines:
        instrument = line.replace('\n', '').split(',')
        id = instrument[0]
        if id in existing_instruments:
            quote = float(instrument[1])
            eod_params = {
                'instrumentId': id,
                'instance': 'close',
                'valuationDate': str(yesterday),
                'quote': {
                    'close': quote,
                    'settle': quote
                }
            }
            intraday_params = {
                'instrumentId': id,
                'instance': 'intraday',
                'valuationDate': str(yesterday),
                'quote': {
                    'bid': quote,
                    'ask': quote,
                    'last': quote
                }
            }
            res = utils.call(method, eod_params, 'market-data-service', host, token)
            print(res)
            res = utils.call(method, intraday_params, 'market-data-service', host, token)
            print(res)
