from datetime import datetime, timedelta, date
from market_data.utils import *
from market_data.table_templates import *
from market_data.data_config import *
from utils.airflow_utils import *

_datetime_fmt = '%Y-%m-%dT%H:%M:%S'
_date_fmt = '%Y%m%d'
_date_fmt2 = '%Y-%m-%d'

vol_surface_name = 'TRADER_VOL'
dividend_curve_name = 'TRADER_DIVIDEND_CURVE'
strike_percent = [0.8, 0.9, 0.95, 1, 1.05, 1.1, 1.2]
labels = ['80% SPOT', '90% SPOT', '95% SPOT', '100% SPOT', '105% SPOT', '110% SPOT', '120% SPOT']
vols = [0.2, 0.22, 0.24, 0.26, 0.28, 0.3, 0.32, 0.34, 0.36]
qs = [0, 0, 0, 0, 0, 0, 0, 0, 0]
tenors = ['1D', '1W', '1M', '3M', '6M', '9M', '1Y']
num_days = [1, 7, 30, 92, 180, 270, 365]
val = date(2017, 1, 1)
vol_instrument_default_spot = 100
BATCH_SIZE = 1000

EXCHANGE_SUPPORTED_LIST = ['SSE', 'SZSE', 'SGE', 'SHFE', 'DCE', 'CZCE', 'CFFEX', 'INE']
OTHER_EXCHANGE = 'OTHER_EXCHANGE'
NOTIONAL_LIMIT = 100000000000
ASSET_CLASS_COMMODITY = 'COMMODITY'
ASSET_CLASS_EQUITY = 'EQUITY'
INSTRUMENT_TYPE_SPOT = 'SPOT'
INSTRUMENT_TYPE_STOCK = 'STOCK'
INSTRUMENT_TYPE_INDEX = 'INDEX'
INSTRUMENT_TYPE_FUTURES = 'FUTURES'
INSTRUMENT_TYPE_INDEX_FUTURES = 'INDEX_FUTURES'
FUTURES_SUB_TYPE_CODE = {  # 品种细类代码
    '703001001': 'PRECIOUS_METAL',  # 贵金属
    '703001002': 'METAL',  # 有色
    '703001003': 'BLACK',  # 煤焦钢矿
    '703001004': None,  # 非金属建材
    '703001005': 'RESOURCE',  # 能源
    '703001006': 'RESOURCE',  # 化工
    '703001007': 'AGRICULTURE',  # 谷物
    '703001008': None,  # 油脂油料
    '703001009': None,  # 软商品
    '703001010': 'AGRICULTURE',  # 农副产品
}
TASK_UPDATE_INSTRUMENT_FLAG = 'TASK_UPDATE_INSTRUMENT_FLAG'
TASK_SYNC_INSTRUMENT_FLAG = 'TASK_SYNC_INSTRUMENT_FLAG'
TASK_UPDATE_EOD_PRICE_FLAG = 'TASK_UPDATE_EOD_PRICE_FLAG'
TASK_SUCCESS = 'SUCCESS'
TASK_FAILED = 'FAILED'
INSTRUMENT_SUFFIX_NOT_REQUIRED = ['WI', 'MI']  # TODO:需要过滤掉的标的后缀


def is_instrument_not_required(instance):
    instrument_id = instance.get('instrumentId').upper()
    if '.' in instrument_id and instrument_id.split('.')[-1] in INSTRUMENT_SUFFIX_NOT_REQUIRED:
        return True
    return False


def fetch_instrument_info(ip, token):
    instruments = call('mktInstrumentsListPaged', {}, 'market-data-service', ip, token)
    instruments_dict = {}
    if instruments is not None and len(instruments.get('page')) > 0:
        for x in instruments.get('page'):
            instruments_dict[x.get('instrumentId')] = x
    return instruments.get('page'), instruments_dict


def create_close_quote(instrument_id, valuation_date, settle, close, ip, token):
    params = {
        'instrumentId': instrument_id,
        'instance': 'close',
        'valuationDate': valuation_date,
        'quote': {
            'settle': settle,
            'close': close
        }
    }
    return call('mktQuoteSave', params, 'market-data-service', ip, token)


def create_instrument_white_list(exchange, instrument_id, ip, token):
    params = {
        'venueCode': exchange,
        'instrumentId': instrument_id,
        'notionalLimit': NOTIONAL_LIMIT
    }
    return call('mktInstrumentWhitelistSave', params, 'market-data-service', ip, token)


def create_vol_instruments(tenors, vols):
    return [{'tenor': tenor,
             'vols': [{'percent': p, 'label': l, 'quote': vol} for (p, l) in zip(strike_percent, labels)]}
            for tenor, vol in zip(tenors, vols)]


def create_vol(name, underlyer, instance, spot, tenors, vols, val, ip, token):
    params = {
        'save': True,
        'modelName': name,
        'valuationDate': val.strftime(_date_fmt2),
        'instance': instance,
        'underlyer': {
            'instrumentId': underlyer,
            'instance': instance,
            'field': 'close' if instance.upper() == 'CLOSE' else 'last',
            'quote': spot
        },
        'instruments': create_vol_instruments(tenors, vols),
        'daysInYear': 365
    }
    return call('mdlVolSurfaceInterpolatedStrikeCreate', params, 'model-service', ip, token)


def create_instruments(tenors, values):
    return [{'tenor': tenor, 'quote': value, 'use': True} for tenor, value in zip(tenors, values)]


def create_dividend_curve(name, underlyer, instance, tenors, rates, val, ip, token):
    params = {
        'save': True,
        'underlyer': underlyer,
        'modelName': name,
        'valuationDate': val.strftime(_date_fmt2),
        'instance': instance,
        'instruments': create_instruments(tenors, rates)
    }
    return call('mdlCurveDividendCreate', params, 'model-service', ip, token)


def create_instrument(instrument_id, asset_class, instrument_type, asset_sub_class, instrument_info, ip, token):
    params = {
        'assetClass': asset_class,
        'assetSubClass': asset_sub_class,
        'instrumentId': instrument_id,
        'instrumentType': instrument_type,
        'instrumentInfo': instrument_info
    }
    return save_instrument(params, ip, token)


def save_instrument(instance, ip, token):
    return call('mktInstrumentCreate', instance, 'market-data-service', ip, token)


def delete_instrument(instrument_id, ip, token):
    params = {
        'instrumentId': instrument_id,
    }
    return call('mktInstrumentDelete', params, 'market-data-service', ip, token)


def delete_instrument_white(instrument_ids, ip, token):
    params = {
        'instrumentIds': instrument_ids,
    }
    return call('mktInstrumentWhitelistDelete', params, 'market-data-service', ip, token)


def get_terminal_instruments_list(token):
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
    }
    params = {
        'tradeDate': datetime.now().strftime(_date_fmt2),
        'filtering': False
    }
    return call_terminal_request(terminal_hostport, 'data-service', 'getInstrumentIdList', params, headers)


def get_terminal_quotes_list_by_instruments_and_dates(instrument_ids, trade_dates, terminal_host, token):
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
    }
    params = {
        'instrumentIds': instrument_ids,
        'tradeDates': trade_dates
    }
    return call_terminal_request(terminal_host, 'data-service', 'getInstrumentQuoteCloseBatch', params, headers)


def covert_exchange(exchange):  # BCT暂不支持的交易所，保存为 OTHER_EXCHANGE
    return exchange if exchange in EXCHANGE_SUPPORTED_LIST else OTHER_EXCHANGE


def save_quote_list(quote_list, cur_instrument_dict, ip, token, has_settle_attr):
    if quote_list is None:
        return []
    error_list = []
    # TODO: BATCH SAVE
    for p in quote_list:
        try:
            instrument_id = p.S_INFO_WINDCODE.upper()
            if cur_instrument_dict.get(instrument_id) is not None:
                date_str = p.TRADE_DT
                cur_date = date_str[0:4] + '-' + date_str[4:6] + '-' + date_str[6:]
                if has_settle_attr:
                    settle = p.S_DQ_SETTLE or p.S_DQ_CLOSE
                else:
                    settle = p.S_DQ_CLOSE
                settle = settle or -1
                close = p.S_DQ_CLOSE or settle or -1  # TODO: close settle 字段值为空时处理
                create_close_quote(instrument_id, cur_date, settle, close, ip, token)
        except Exception as error:
            logging.info(error)
            logging.info(p)
            error_list.append({'instrumentId': instrument_id,
                               'error_msg': error})
    return error_list


def convert_instrument_instance(data_type, instruments):
    instances = []
    if instruments is None or len(instruments) == 0:
        return []
    for i in instruments:
        instance = {}
        instrument_id = i.S_INFO_WINDCODE.upper()
        exchange = covert_exchange(i.S_INFO_EXCHMARKET)
        instrument_name = i.S_INFO_NAME
        if data_type == INSTRUMENT_TYPE_SPOT:
            asset_class = ASSET_CLASS_COMMODITY
            asset_sub_class = 'PRECIOUS_METAL'
            instrument_type = INSTRUMENT_TYPE_SPOT
            instrument_info = {
                'multiplier': 10,  # TODO: 黄金现货合约乘数 待修正
                'name': instrument_name,
                'exchange': exchange,
                'unit': i.S_INFO_PUNIT
            }
        elif data_type == INSTRUMENT_TYPE_STOCK:
            asset_class = ASSET_CLASS_EQUITY
            asset_sub_class = 'EQUITY'
            instrument_type = INSTRUMENT_TYPE_STOCK
            instrument_info = {
                'name': instrument_name,
                'exchange': exchange,
                'multiplier': 1
            }
        elif data_type == INSTRUMENT_TYPE_INDEX:
            asset_class = ASSET_CLASS_EQUITY
            asset_sub_class = 'INDEX'
            instrument_type = INSTRUMENT_TYPE_INDEX
            instrument_info = {
                'name': instrument_name,
                'exchange': exchange
            }
        elif data_type == INSTRUMENT_TYPE_FUTURES:
            if i.FS_INFO_PUNIT == '人民币元':  # TODO:以此判断为国债，暂不收录
                continue
            delist_date = i.S_INFO_DELISTDATE
            maturity = None
            if delist_date is not None:
                maturity = delist_date[:4] + '-' + delist_date[4:6] + '-' + delist_date[6:]
            multiplier = (i.S_INFO_CEMULTIPLIER or 1) * (i.S_INFO_PUNIT or 1) * (i.S_INFO_RTD or 1)
            instrument_info = {
                'multiplier': multiplier,
                'name': instrument_name,
                'exchange': exchange,
                'maturity': maturity,
                'unit': i.FS_INFO_PUNIT
            }
            if i.FS_INFO_PUNIT == '指数点':
                instrument_type = INSTRUMENT_TYPE_INDEX_FUTURES
                asset_class = ASSET_CLASS_EQUITY
                asset_sub_class = 'INDEX'
            else:
                instrument_type = INSTRUMENT_TYPE_FUTURES
                asset_class = ASSET_CLASS_COMMODITY
                asset_sub_class = FUTURES_SUB_TYPE_CODE.get(
                    str(i.S_SUB_TYPCODE) if i.S_SUB_TYPCODE is not None else None) or 'OTHERS'
        else:
            continue
        instance = {
            'assetClass': asset_class,
            'assetSubClass': asset_sub_class,
            'instrumentId': instrument_id,
            'instrumentType': instrument_type,
            'instrumentInfo': instrument_info
        }
        instances.append(instance)
    return instances


def filter_available_instruments(instance, terminal_instrument_ids, instrument_dict, force_update):
    instrument_id = instance.get('instrumentId').upper()
    if instrument_id not in terminal_instrument_ids \
            or (instrument_dict.get(instrument_id) is not None and not force_update) \
            or is_instrument_not_required(instance):
        return False
    return True


def roll_back_instrument_save(failed_ids, ip, token):
    if failed_ids is not None and len(failed_ids) > 0:
        deleted_ids = []
        for instrument_id in failed_ids:
            try:
                delete_instrument(instrument_id, ip, token)
                deleted_ids.append(instrument_id)
            except Exception as error:
                print(error)
                logging.info({'instrumentId': instrument_id, 'error_msg': error})
        try:
            delete_instrument_white(deleted_ids, ip, token)
        except Exception as error:
            print(error)
            logging.info({'instrumentId': instrument_id, 'error_msg': error})


def update_market_instrument(instrument_instances, terminal_instrument_ids, instrument_dict, ip, token,
                             force_update=True):
    if instrument_instances is None or len(instrument_instances) == 0:
        return []
    error_list = []
    failed_ids = []

    all_instruments_to_save = list(
        filter(lambda inst: filter_available_instruments(inst, terminal_instrument_ids, instrument_dict, force_update),
               instrument_instances))
    all_ids_to_save = list(map(lambda x: x.get('instrumentId').upper(), all_instruments_to_save))
    current_date = datetime.now() + timedelta(days=-1)
    terminal_quotes = []
    for start in range(0, len(all_ids_to_save), BATCH_SIZE):
        end = start + BATCH_SIZE
        terminal_quotes.extend(get_terminal_quotes_list_by_instruments_and_dates(all_ids_to_save[start:end],
                                                                                 [current_date.strftime(_date_fmt2)],
                                                                                 terminal_hostport, token))
    terminal_quotes_dict = {}
    for quote in terminal_quotes:
        terminal_quotes_dict[quote.get('instrumentId').upper()] = quote.get('closePrice')
    for instance in all_instruments_to_save:
        instrument_id = instance.get('instrumentId').upper()
        try:
            save_instrument(instance, ip, token)
            create_instrument_white_list(instance.get('instrumentInfo').get('exchange'), instrument_id, ip, token)

            close_price = terminal_quotes_dict.get(instrument_id) or vol_instrument_default_spot
            create_vol(vol_surface_name, instrument_id, 'close', close_price, tenors, vols, val, host, token)
            create_vol(vol_surface_name, instrument_id, 'intraday', close_price, tenors, vols, val, host, token)

            create_dividend_curve(dividend_curve_name, instrument_id, 'close', tenors, qs, val, host, token)
            create_dividend_curve(dividend_curve_name, instrument_id, 'intraday', tenors, qs, val, host, token)
        except Exception as error:
            failed_ids.append(instrument_id)
            logging.info(error)
            logging.info(instance)
            error_list.append({'instrumentId': instrument_id,
                               'error_msg': error})
    roll_back_instrument_save(failed_ids, ip, token)
    return error_list


def update_market_instruments(force_update=False):
    try:
        session = None
        error_info_list = []
        bct_token = login_token(user, password, host)
        instrument_list, instrument_dict = fetch_instrument_info(host, bct_token)
        logging.debug('bct_instrument_ids:' + str(list(instrument_dict.keys())))
        terminal_instrument_ids = get_terminal_instruments_list(bct_token)
        terminal_instrument_ids = list(map(lambda x: x.upper(), terminal_instrument_ids))
        logging.debug('terminal_instrument_ids:' + str(terminal_instrument_ids))
        session = get_oracle_session()
        instrument_instances = []

        gold_spot_instruments = fetch_gold_spot_description(session)
        instrument_instances.extend(convert_instrument_instance(INSTRUMENT_TYPE_SPOT, gold_spot_instruments))

        futures_instruments = fetch_futures_description(session)
        instrument_instances.extend(convert_instrument_instance(INSTRUMENT_TYPE_FUTURES, futures_instruments))

        index_instruments = fetch_index_description(session)
        instrument_instances.extend(convert_instrument_instance(INSTRUMENT_TYPE_INDEX, index_instruments))

        stock_instruments = fetch_stock_description(session)
        instrument_instances.extend(convert_instrument_instance(INSTRUMENT_TYPE_STOCK, stock_instruments))

        error_info_list.extend(
            update_market_instrument(instrument_instances, terminal_instrument_ids, instrument_dict, host, bct_token,
                                     force_update))
    except Exception as error:
        print(error)
        logging.info(error)
        error_info_list.append({'method': 'update_market_instruments', 'error_msg': error})
    if session:
        session.close()
    if len(error_info_list) > 0:
        print(str(error_info_list))
        logging.info('标的更新出现错误：' + str(error_info_list))
        set_airflow_variable(TASK_UPDATE_INSTRUMENT_FLAG, TASK_FAILED)
    else:
        logging.info('标的更新成功')
        set_airflow_variable(TASK_UPDATE_INSTRUMENT_FLAG, TASK_SUCCESS)


def synchronize_bct_and_terminal_market_data():
    try:
        error_list = []
        bct_token = login_token(user, password, host)
        instrument_list, instrument_dict = fetch_instrument_info(host, bct_token)
        bct_instrument_ids = list(instrument_dict.keys())
        logging.debug('bct_instrument_ids:' + str(bct_instrument_ids))
        terminal_instrument_ids = get_terminal_instruments_list(bct_token)
        terminal_instrument_ids = list(map(lambda x: x.upper(), terminal_instrument_ids))
        logging.debug('terminal_instrument_ids:' + str(terminal_instrument_ids))
        if terminal_instrument_ids is None or len(terminal_instrument_ids) == 0:
            raise Exception('标的同步终止，未获取到终端服务标的信息列表！')
        extra_instrument_ids = list(set(bct_instrument_ids).difference(set(terminal_instrument_ids)))
        logging.info('BCT和终端待同步标的数量:%s' % len(extra_instrument_ids))
        if extra_instrument_ids is not None and len(extra_instrument_ids) > 0:
            deleted_ids = []
            for instrument_id in extra_instrument_ids:
                try:
                    delete_instrument(instrument_id, host, bct_token)
                    deleted_ids.append(instrument_id)
                except Exception as error:
                    print(error)
                    logging.info(error)
                    error_list.append({'instrumentId': instrument_id,
                                       'error_msg': error})
            delete_instrument_white(deleted_ids, host, bct_token)
    except Exception as error:
        print(error)
        logging.info(error)
        error_list.append({'method': 'synchronize_bct_and_terminal_market_data', 'error_msg': error})
    if len(error_list) > 0:
        print(str(error_list))
        logging.info('标的同步出错，错误信息：' + str(error_list))
        set_airflow_variable(TASK_SYNC_INSTRUMENT_FLAG, TASK_FAILED)
    else:
        logging.info('标的同步成功')
        set_airflow_variable(TASK_SYNC_INSTRUMENT_FLAG, TASK_SUCCESS)


def update_eod_price(current_date=(datetime.now() + timedelta(days=-1)).strftime(_date_fmt2)):
    try:
        current_date = datetime.strptime(current_date, _date_fmt2)
        session = None
        error_info_list = []
        bct_token = login_token(user, password, host)
        instrument_list, instrument_dict = fetch_instrument_info(host, bct_token)
        logging.info('在BCT中找到%s个标的物' % len(instrument_dict))
        session = get_oracle_session()

        gold_spot_eod_price_list = fetch_gold_spot_eod_price(session, current_date)
        error_info_list.extend(save_quote_list(gold_spot_eod_price_list, instrument_dict, host, bct_token, True))

        commodity_futures_eod_price_list = fetch_commodity_futures_eod_price(session, current_date)
        error_info_list.extend(
            save_quote_list(commodity_futures_eod_price_list, instrument_dict, host, bct_token, True))

        index_futures_eod_price_list = fetch_index_futures_eod_price(session, current_date)
        error_info_list.extend(save_quote_list(index_futures_eod_price_list, instrument_dict, host, bct_token, True))

        # bond_futures_eod_price_list = fetch_bond_futures_eod_price(current_date)
        # save_quote_list(bond_futures_eod_price_list, instrument_dict, host, bct_token, True)

        share_eod_price_list = fetch_share_eod_price(session, current_date)
        error_info_list.extend(save_quote_list(share_eod_price_list, instrument_dict, host, bct_token, False))

        index_eod_price_list = fetch_index_eod_price(session, current_date)
        error_info_list.extend(save_quote_list(index_eod_price_list, instrument_dict, host, bct_token, False))
    except Exception as error:
        print(error)
        logging.info(error)
        error_info_list.append({'method': 'update_eod_price', 'error_msg': error})
    if session:
        session.close()
    if len(error_info_list) > 0:
        print(str(error_info_list))
        logging.info('行情更新出现错误：' + str(error_info_list))
        set_airflow_variable(TASK_UPDATE_EOD_PRICE_FLAG, TASK_FAILED)
    else:
        logging.info('行情更新成功')
        set_airflow_variable(TASK_UPDATE_EOD_PRICE_FLAG, TASK_SUCCESS)


def task_status_check():
    is_success = True
    if get_airflow_variable(TASK_UPDATE_INSTRUMENT_FLAG) == TASK_FAILED:
        is_success = False
        logging.info('标的更新出现异常,详细信息请查看任务日志！')
    if get_airflow_variable(TASK_SYNC_INSTRUMENT_FLAG) == TASK_FAILED:
        is_success = False
        logging.info('标的同步出现异常,详细信息请查看任务日志！')
    if get_airflow_variable(TASK_UPDATE_EOD_PRICE_FLAG) == TASK_FAILED:
        is_success = False
        logging.info('行情更新出现异常,详细信息请查看任务日志！')
    if not is_success:
        raise RuntimeError('市场数据更新任务运行出现异常，请查看日志了解更多！')


if __name__ == '__main__':
    print('start:' + datetime.now().strftime(_datetime_fmt))
    print('start update instruments')
    update_market_instruments()
    print('start synchronize ')
    synchronize_bct_and_terminal_market_data()
    print('start update price')
    update_eod_price()
    print('over:' + datetime.now().strftime(_datetime_fmt))
