# -*- coding: utf-8 -*-

from dags.utils import model_converter as converter_utils
from dags.utils.dataapi_utils import Client
from dags.dbo import create_redis_session
import json
from datetime import datetime
import dags.dto.tonglian_model as tonglian
from concurrent.futures import ThreadPoolExecutor, as_completed
import dags.utils.server_utils as server_utils
from terminal.utils import Logging
from terminal.dto import InstrumentType, AssetClassType, QuoteIntradayDTO, Constant
from terminal.service import RealTimeMarketDataService as TerminalRealTimeMarketDataService

logging = Logging.getLogger(__name__)


class QuoteIntradayService:

    @staticmethod
    def convert_to_market_data(instrment_id, std_asset_class, std_instrument_type, tl_real_time_data):
        quote_intraday = QuoteIntradayDTO()
        quote_intraday.instrumentId = instrment_id
        quote_intraday.assetClass = std_asset_class
        quote_intraday.instrumentType = std_instrument_type
        if 'askBook' in tl_real_time_data and len(tl_real_time_data['askBook']) > 0:
            quote_intraday.askPrice1 = tl_real_time_data['askBook'][0]['price']
        if 'bidBook' in tl_real_time_data and len(tl_real_time_data['bidBook']) > 0:
            quote_intraday.bidPrice1 = tl_real_time_data['bidBook'][0]['price']
        if 'shortNM' in tl_real_time_data:
            quote_intraday.shortName = tl_real_time_data['shortNM']
        if 'lastPrice' in tl_real_time_data:
            quote_intraday.lastPrice = tl_real_time_data['lastPrice']
        if 'prevClosePrice' in tl_real_time_data:
            quote_intraday.prevClosePrice = tl_real_time_data['prevClosePrice']
        if 'preClosePrice' in tl_real_time_data:
            quote_intraday.prevClosePrice = tl_real_time_data['preClosePrice']
        quote_intraday.timestamp = tl_real_time_data['timestamp']
        return quote_intraday

    @staticmethod
    def request_one_real_time_market_data(param):
        std_asset_class, std_instrument_type, tl_exchange_id = param
        market_data_dict = {}
        try:
            tl_asset_class = converter_utils.get_tl_asset_class(std_asset_class, std_instrument_type)
            tl_instrument_type = std_instrument_type
            # 调API获取数据
            client = Client()
            logging.info('开始处理：%s %s' % (std_asset_class, std_instrument_type))
            if (std_instrument_type == InstrumentType.STOCK.name
                    or std_instrument_type == InstrumentType.FUND.name
                    or std_instrument_type == InstrumentType.INDEX.name):
                url = '/api/market/getSHSZTickRTSnapshot.json?exchangeCD=%s&assetClass=%s' % (tl_exchange_id,
                                                                                              tl_asset_class)
            elif std_instrument_type == InstrumentType.FUTURE.name:
                url = '/api/market/getFutureTickRTSnapshot.json?field='
            elif std_instrument_type == InstrumentType.OPTION.name \
                    and std_asset_class == AssetClassType.EQUITY.name:
                url = '/api/market/getOptionTickRTSnapshot.json?field='
            elif std_instrument_type == InstrumentType.OPTION.name \
                    and std_asset_class == AssetClassType.COMMODITY.name:
                url = '/api/market/getFutureOptTickRTSnapshot.json?field='
            else:
                raise Exception('无法处理类型：%s' % std_instrument_type)
            code, result = client.getData(url)
            # 返回代码如果不是200，则直接跳过处理
            if code != 200:
                logging.error('获取远程数据异常：%s %s %s %s, 远程服务器请求返回错误代码: %d' %
                              (std_asset_class, std_instrument_type, tl_exchange_id, tl_asset_class, code))
            # 解析为JSON数据
            ret = json.loads(result)
            # 返回代码不为1，则直接跳过处理
            if ret['retCode'] != 1:
                logging.error('获取远程数据异常：%s %s %s %s, 远程服务器返回: %s' %
                              (std_asset_class, std_instrument_type, tl_exchange_id, tl_asset_class, ret))
            # 开始处理获取到的标的物数据
            tl_real_time_datas = ret['data']
            for tl_real_time_data in tl_real_time_datas:
                if (std_instrument_type == InstrumentType.STOCK.name
                        or std_instrument_type == InstrumentType.FUND.name
                        or std_instrument_type == InstrumentType.INDEX.name):
                    tl_instrument_id = tl_real_time_data['ticker']
                    tl_exchange_id = tl_real_time_data['exchangeCD']
                elif std_instrument_type == InstrumentType.FUTURE.name:
                    tl_instrument_id = tl_real_time_data['instrumentID']
                    tl_exchange_id = tl_real_time_data['exchangeCD']
                elif std_instrument_type == InstrumentType.OPTION.name \
                        and std_asset_class == AssetClassType.EQUITY.name:
                    tl_instrument_id = tl_real_time_data['optionId']
                    tl_exchange_id = None
                elif std_instrument_type == InstrumentType.OPTION.name \
                        and std_asset_class == AssetClassType.COMMODITY.name:
                    tl_instrument_id = tl_real_time_data['instrumentID']
                    tl_exchange_id = tl_real_time_data['exchangeCD']
                else:
                    raise Exception('无法处理类型：%s' % std_instrument_type)
                # 将通联模型转化为Std模型
                std_instrument_id = converter_utils.convertToStd(tonglian.Instrument(
                    instrumentId=tl_instrument_id, exchangeId=tl_exchange_id,
                    assetClass=tl_asset_class, instrumentType=tl_instrument_type)).upper()
                market_data_dict[std_instrument_id] = QuoteIntradayService.convert_to_market_data(
                    std_instrument_id, std_asset_class, std_instrument_type, tl_real_time_data)
            logging.info('获取到的行情为：%s %s %d' % (std_asset_class, std_instrument_type, len(market_data_dict)))
        except Exception as e:
            logging.info('处理失败：%s %s %s，异常：%s' % (std_asset_class, std_instrument_type,
                                                  tl_exchange_id, str(e)))
            return market_data_dict, False
        return market_data_dict, True

    @staticmethod
    def request_all_real_time_market_data():
        # 请求参数
        params = [(AssetClassType.EQUITY.name, InstrumentType.STOCK.name, tonglian.ExchangeIdType.XSHG.name),
                  (AssetClassType.EQUITY.name, InstrumentType.STOCK.name, tonglian.ExchangeIdType.XSHE.name),
                  (AssetClassType.EQUITY.name, InstrumentType.INDEX.name, tonglian.ExchangeIdType.XSHG.name),
                  (AssetClassType.EQUITY.name, InstrumentType.INDEX.name, tonglian.ExchangeIdType.XSHE.name),
                  (AssetClassType.EQUITY.name, InstrumentType.FUND.name, tonglian.ExchangeIdType.XSHG.name),
                  (AssetClassType.EQUITY.name, InstrumentType.FUND.name, tonglian.ExchangeIdType.XSHE.name),
                  (AssetClassType.EQUITY.name, InstrumentType.OPTION.name, None),
                  (AssetClassType.COMMODITY.name, InstrumentType.FUTURE.name, None),
                  (AssetClassType.COMMODITY.name, InstrumentType.OPTION.name, None)]
        # 并发进行请求
        with ThreadPoolExecutor(max_workers=8) as executor:
            # 开始并行向远程服务器请求数据
            all_task = [executor.submit(QuoteIntradayService.request_one_real_time_market_data, param) for param in params]
            # 处理结果
            failed_count = 0
            result = {}
            for future in as_completed(all_task):
                market_data_dict, status = future.result()
                if status is True:
                    result.update(market_data_dict)
                else:
                    failed_count = failed_count + 1
            if failed_count != 0:
                logging.error('处理失败的条数为：%d of %d' % (failed_count, len(params)))
            logging.info('总共获取到的行情为：%d，标的物：%s' % (len(result), ','.join(result.keys())))
            return result

    @staticmethod
    def update_quote_intraday(redis_session, market_data_dict):
        updated_dict = {}
        now = datetime.now().isoformat()
        for instrument_id in market_data_dict:
            market_data_item = market_data_dict[instrument_id].__dict__
            updated_item = {}
            for key in market_data_item:
                value = market_data_item[key]
                # value为空的情况下进行保存
                if value is None:
                    continue
                updated_item[key] = value
                updated_item['updatedAt'] = now
            updated_dict[instrument_id] = json.dumps(updated_item)
        redis_session.hmset(Constant.REAL_TIME_MARKET_DATA, updated_dict)
        logging.info('保存行情到Redis完成')


    @staticmethod
    def backfill_market_data_to_redis():
        redis_session = create_redis_session()
        market_data_dict = QuoteIntradayService.request_all_real_time_market_data()
        # 开始保存行情数据到Redis
        QuoteIntradayService.update_quote_intraday(redis_session, market_data_dict)

    @staticmethod
    def get_quote_param(market_data_type, market_data):
        bid_price = market_data['bidPrice1'] if 'bidPrice1' in market_data else market_data['lastPrice']
        ask_price = market_data['askPrice1'] if 'askPrice1' in market_data else market_data['lastPrice']
        last_price = market_data['lastPrice']
        if market_data_type == "close":
            return {
                'instrumentId': market_data['instrumentId'],
                'instance': 'close',
                'quote': {
                    'close': last_price,
                    'settle': last_price
                }
            }
        else:
            return {
                'instrumentId': market_data['instrumentId'],
                'instance': 'intraday',
                'quote': {
                    'bid': bid_price,
                    'ask': ask_price,
                    'last': last_price
                }
            }

    @staticmethod
    def update_quote_intraday_to_terminal(redis_session, market_data_type):
        header = server_utils.login_terminal()
        instruments = server_utils.get_instrument_list(header)
        instrument_ids = []
        for instrument in instruments:
            instrument_id = instrument['instrumentId']
            if instrument_id not in instrument_ids:
                instrument_ids.append(instrument_id)
        market_data_dict = {}
        market_data_params = []
        for item in TerminalRealTimeMarketDataService.get_real_time_market_data(redis_session, instrument_ids):
            try:
                instrument_id = item['instrumentId']
                param = QuoteIntradayService.get_quote_param(market_data_type, item)
                market_data_dict[instrument_id] = param
                market_data_params.append(param)
            except Exception as e:
                logging.error('处理标的物失败：%s, 异常：%s' % (item.get('instrumentId'), str(e)))
        logging.info("开始发送保存请求")
        server_utils.call_terminal_request('terminal-service', 'mktQuoteSaveBatch', {'quotes': market_data_params}, header)
        logging.info("成功保存")
        # 失败的标的物
        failed_instrument_ids = []
        for instrument_id in instrument_ids:
            if instrument_id not in market_data_dict:
                failed_instrument_ids.append(instrument_id)
        if len(failed_instrument_ids) > 0:
            logging.error('失败的标的物为：%s' % ','.join(failed_instrument_ids))
        logging.info('导入行情数据到Terminal完成，导入成功：%d of %d' % (len(market_data_dict.keys()), len(instrument_ids)))

    @staticmethod
    def update_eod_market_data_to_terminal():
        redis_session = create_redis_session()
        QuoteIntradayService.update_quote_intraday_to_terminal(redis_session, 'close')

    @staticmethod
    def update_intraday_market_data_to_terminal():
        redis_session = create_redis_session()
        QuoteIntradayService.update_quote_intraday_to_terminal(redis_session, 'intraday')
        logging.info('更新行情数据结束')


if __name__ == '__main__':
    # MarketDataUpdateService.backfill_market_data_to_redis()
    QuoteIntradayService.update_intraday_market_data_to_terminal()
