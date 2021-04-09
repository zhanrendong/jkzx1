from terminal.service import RealTimeMarketDataService
from .json_rpc_handler import JsonRpcHandler
from terminal.jsonrpc import method
from terminal.utils import RedisUtils


class RealTimeMarketHandler(JsonRpcHandler):
    @method
    async def get_real_time_market_data(self, instrument_ids):
        redis_session = RedisUtils.get_redis_session()
        ret = RealTimeMarketDataService.get_real_time_market_data(redis_session, instrument_ids)
        return ret

    @method
    async def get_real_time_instruments(self):
        ret = RealTimeMarketDataService.get_real_time_instruments()
        return ret


