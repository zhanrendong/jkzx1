# -*- coding: utf-8 -*-

from utils import utils
from datetime import datetime
import os
import io
from config.bct_config import bct_password, bct_user

instrument_type_map = {
    'INDEX_FUTURES': 'future',
    'FUTURES': 'future',
    'STOCK': 'stock',
    'OPTION': 'option',
    'FUND': 'fund',
    'INDEX': 'index'
}

login_body = {
    'userName': bct_user,
    'password': bct_password
}


def get_ip_list():
    ip_list_file = 'ip_list'
    ip_list = []
    if os.path.exists(ip_list_file):
        file = io.open(ip_list_file, 'r', encoding='utf8')
        lines = file.readlines()
        for line in lines:
            if line != '\n':
                ip_list.append(line.replace('\n', '').strip())
    return ip_list


def real_time_market_data_get(instrument_type, instrument_ids):
    param = {
        'assetClass': instrument_type,
        'instrumentIds': instrument_ids
    }
    res = utils.get_real_time_market_data(param)
    if res != "error":
        return res['result']
    else:
        return None


def prepare_quote_param(market_data_type, market_data, instrument_id, instrument_type):
    ask = market_data.get('askPrice1', None)
    bid = market_data.get('bidPrice1', None)
    last = market_data.get('lastPrice', None)
    quote = last if instrument_type == 'index' else (ask + bid) / 2
    if market_data_type == "close":
        return {
            'instrumentId': instrument_id,
            'instance': 'close',
            'quote': {
                'close': quote,
                'settle': quote
            }
        }
    else:
        return {
            'instrumentId': instrument_id,
            'instance': 'intraday',
            'quote': {
                'bid': quote if instrument_type == 'index' else bid,
                'ask': quote if instrument_type == 'index' else ask,
                'last': last
            }
        }


def not_valid_instrument(instrument):
    instrument_type = instrument['instrumentType']
    maturity = instrument.get('maturity', None)
    expired = False
    if maturity is not None:
        maturity_time = datetime.strptime(maturity, '%Y-%m-%d')
        expired = datetime.now() >= maturity_time
    return instrument_type is None or instrument_type_map.get(instrument_type, None) is None or expired


def get_instrument_id(instrument):
    instrument_id = instrument['instrumentId']
    instrument_type = instrument_type_map[instrument['instrumentType']]
    index = instrument_id.find(".")
    if instrument_type == "fund" or instrument_type == "stock" or instrument_type == "index":
        if index == -1:
            instrument_id = instrument_id + '.'
        return instrument_id
    if index != -1:
        instrument_id = instrument_id[0:index]
    return instrument_id


def update_market_data(market_data_type):
    real_time_market_data_map = {}
    bad_instrument = set()
    instrument_map = {}
    instrument_id_map = {}
    header = utils.login('10.1.5.16', login_body)
    market_data_ip_list = get_ip_list()
    for this_ip in market_data_ip_list:
        instrument_map.clear()
        try:
            instrument_list = \
                utils.call_request(this_ip, 'market-data-service', 'mktInstrumentsListPaged', {}, header)['result'][
                    'page']
        except Exception as e:
            print("failed update market data for: " + this_ip + ",Exception:" + str(e))
            continue
        quote_list = []
        for instrument in instrument_list:
            if not_valid_instrument(instrument):
                continue
            instrument_id = get_instrument_id(instrument).upper()
            instrument_id_map[instrument_id] = instrument['instrumentId']
            instrument_type = instrument_type_map[instrument['instrumentType']]
            quote_param = real_time_market_data_map.get(instrument_id, None)
            if quote_param is None:
                if instrument_id not in bad_instrument:
                    id_list = instrument_map.get(instrument_type, [])
                    id_list.append(instrument_id)
                    instrument_map[instrument_type] = id_list
                    bad_instrument.add(instrument_id)
            else:
                quote_list.append(quote_param)

        for (ins_type, ids) in instrument_map.items():
            if len(ids) == 0:
                continue
            market_datas = real_time_market_data_get(ins_type, ids)
            for market_data in market_datas:
                temp_id = market_data['instrumentId'].upper()
                instrument_id = instrument_id_map.get(temp_id, None)
                if instrument_id is None:
                    continue
                temp_quote_param = prepare_quote_param(market_data_type, market_data, instrument_id, ins_type)
                bad_instrument.discard(temp_id)
                real_time_market_data_map[temp_id] = temp_quote_param
                quote_list.append(temp_quote_param)
        utils.call_request(this_ip, 'market-data-service', 'mktQuoteSaveBatch', {'quotes': quote_list}, header)
        print('done for ' + this_ip)
    print('all done')
