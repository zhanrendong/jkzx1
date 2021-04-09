import logging
import json
from terminal.server.application import init_redis
from terminal.dto import Constant


class RealTimeMarketDataService:
    @staticmethod
    def get_real_time_market_data(redis_session ,instrument_ids):
        results = []
        # instrument_ids为空直接返回
        if instrument_ids is None or len(instrument_ids) == 0:
            return results
        # 获取Redis中标的物行情
        market_data_items = redis_session.hmget(Constant.REAL_TIME_MARKET_DATA, instrument_ids)
        for index in range(0, len(market_data_items)):
            item = market_data_items[index]
            if item is None:
                logging.error('标的物没有实时行情数据：%s' % instrument_ids[index])
            else:
                results.append(json.loads(item))
        return results

    @staticmethod
    def get_real_time_instruments():
        results = []
        conn = init_redis()
        market_data_items = conn.hgetall(Constant.REAL_TIME_MARKET_DATA)
        for item in market_data_items:
            value = market_data_items[item]
            results.append(json.loads(value))
        result = []
        for index in range(0, len(results)):
            item = results[index]
            ret = {}
            ret['instrumentId'] = item.get('instrumentId', None)
            ret['assetClass'] = item.get('assetClass', None)
            ret['instrumentType'] = item.get('instrumentType', None)
            ret['shortName'] = item.get('shortName', None)
            result.append(ret)
        return result