from dags.dbo.oracle_market_data_model import GoldSpotEODPrices, Oracle_Session
from terminal.utils import DateTimeUtils
from terminal import Logging

logger = Logging.getLogger(__name__)


class OracleGoldSpotEODPricesRepo:

    @staticmethod
    def get_gold_spot_eod_prices_by_date(trade_date):
        """
        根据时间段获取数据
        :param trade_date:
        :return:
        """
        logger.info('开始从gold_spot_eod_prices表获取日期为: %s的数据' % trade_date)

        oracle_session = Oracle_Session()
        gold_spot_eod_prices = oracle_session.query(GoldSpotEODPrices). \
            filter(GoldSpotEODPrices.TRADE_DT == DateTimeUtils.date2str(trade_date, '%Y%m%d')).all()
        oracle_session.close()

        if len(gold_spot_eod_prices) == 0 or gold_spot_eod_prices is None:
            logger.info('从gold_spot_eod_prices表没有获取到日期为: %s的数据' % trade_date)
            return []

        logger.info('时间为: %s,在gold_spot_eod_prices表查询到了%d条数据' %
                    (trade_date, len(gold_spot_eod_prices)))

        return gold_spot_eod_prices
