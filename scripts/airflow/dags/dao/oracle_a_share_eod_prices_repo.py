from dags.dbo.oracle_market_data_model import AShareEODPrices, Oracle_Session
from terminal.utils import DateTimeUtils
from terminal import Logging

logger = Logging.getLogger(__name__)


class OracleAShareEODPricesRepo:

    @staticmethod
    def get_a_share_eod_prices_by_date(trade_date):
        """
        根据时间获取数据
        :param trade_date:
        :return:
        """
        logger.info('开始从a_share_eod_prices表获取日期为: %s的数据' % trade_date)
        oracle_session = Oracle_Session()
        a_share_eod_prices = oracle_session.query(AShareEODPrices). \
            filter(AShareEODPrices.TRADE_DT == DateTimeUtils.date2str(trade_date, '%Y%m%d')).all()
        oracle_session.close()
        if len(a_share_eod_prices) == 0 or a_share_eod_prices is None:
            logger.info('从a_share_eod_prices表没有获取到日期为: %s的数据' % trade_date)
            return []

        logger.info('时间为: %s,在a_share_eod_prices表查询到了%d条数据' %
                    (trade_date, len(a_share_eod_prices)))

        return a_share_eod_prices
