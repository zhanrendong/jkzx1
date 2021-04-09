from terminal.dbo.db_model import *
from terminal.api import *
from terminal.server import *
from tornado import web
from terminal.utils import Logging, RedisUtils, ServerUtils
import os


def create_app(config_name='default'):
    config_name = os.environ.get('TERMINAL_ENV') or config_name
    # TODO: 去掉函数
    session_factory = init_db(config_name)()
    dispatch_params = init_dispatch(config_name)
    logging_params = init_logging(config_name)
    redis_params = init_redis(config_name)
    server_params = init_server(config_name)
    port = init_tornado(config_name)
    # 初始化dispatch参数
    JsonRpcHandler.set_dispatcher(dispatch_params)
    # 初始化Logging参数
    Logging.set_logging_params(logging_params)
    RedisUtils.set_redis_params(redis_params)
    ServerUtils.set_server_params(server_params)
    # 初始化Handler
    handlers = [(r"/api/rpc", QuoteCloseHandler),
                (r"/api/rpc", HistoricalVolHandler),
                (r"/api/rpc", OptionStructureHandler),
                (r"/api/rpc", OTCPositionSnapshotHandler),
                (r"/api/rpc", RealTimeMarketHandler),
                (r"/api/rpc", FittingModelHandler),
                (r"/api/rpc", CommodityFutureContractHandler),
                (r"/api/rpc", OtcAtmQuoteHandler),
                (r"/api/rpc", OTCReportHandler),
                (r"/api/rpc", InstrumentHandler)]
    # 初始化Server
    app = web.Application(handlers=handlers, session_factory=session_factory)
    app.listen(port)
    return app

