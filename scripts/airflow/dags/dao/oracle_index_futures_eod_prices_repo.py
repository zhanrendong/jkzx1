from dags.dbo.oracle_market_data_model import IndexFuturesEODPrices, Oracle_Session
from terminal.utils import DateTimeUtils
from terminal import Logging

logger = Logging.getLogger(__name__)


class OracleIndexFuturesEODPricesRepo:

    @staticmethod
    def get_index_futures_eod_prices_by_date(trade_date):
        """
        根据时间获取数据
        :param trade_date:
        :return:
        """
        logger.info('开始从index_futures_eod_prices表获取日期为: %s的数据' % trade_date)
        oracle_session = Oracle_Session()
        index_futures_eod_prices = oracle_session.query(IndexFuturesEODPrices). \
            filter(IndexFuturesEODPrices.TRADE_DT == DateTimeUtils.date2str(trade_date, '%Y%m%d')).all()
        oracle_session.close()
        if len(index_futures_eod_prices) == 0 or index_futures_eod_prices is None:
            logger.info('从index_futures_eod_prices表没有获取到日期为: %s的数据' % trade_date)
            return []

        logger.info('时间为: %s,在index_futures_eod_prices表查询到了%d条数据' %
                    (trade_date, len(index_futures_eod_prices)))

        return index_futures_eod_prices
