from dags.dbo.oracle_trade_snapshot_model import ROTCTradeData, Oracle_Session
from terminal.utils import DateTimeUtils
from terminal.utils import Logging

logger = Logging.getLogger(__name__)


class OracleROTCTradedataRepo:

    @staticmethod
    def get_otc_tradedata_by_date(start_date, end_date):
        """
        根据时间段获取数据
        :param start_date:
        :param end_date:
        :return:
        """
        logger.info('开始获取%s 至 %s时间段内r_otc_tradedata表的数据' % (start_date, end_date))
        oracle_session = Oracle_Session()
        r_otc_tradedatas = oracle_session.query(ROTCTradeData).filter(ROTCTradeData.REPORTDATE >=
                                                                      DateTimeUtils.date2str(start_date),
                                                                      ROTCTradeData.REPORTDATE <=
                                                                      DateTimeUtils.date2str(end_date)).all()
        oracle_session.close()
        if len(r_otc_tradedatas) == 0 or r_otc_tradedatas is None:
            logger.info('在%s 至 %s时间段内r_otc_tradedata表没有数据' % (start_date, end_date))
            return []
        logger.info('在%s 至 %s时间段内的获取了 %d条r_otc_tradedata表的数据' % (start_date, end_date, len(r_otc_tradedatas)))

        return r_otc_tradedatas
