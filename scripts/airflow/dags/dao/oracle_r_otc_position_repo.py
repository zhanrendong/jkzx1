from dags.dbo.oracle_trade_snapshot_model import ROTCPosition, Oracle_Session
from terminal.utils import DateTimeUtils
from terminal.utils import Logging

logger = Logging.getLogger(__name__)


class OracleROTCPositionRepo:

    @staticmethod
    def get_otc_position_by_date(start_date, end_date):
        """
        根据时间段获取数据
        :param start_date:
        :param end_date:
        :return:
        """
        logger.info('开始获取%s 至 %s时间段内r_otc_postion表的数据' % (start_date, end_date))
        oracle_session = Oracle_Session()
        r_otc_positions = oracle_session.query(ROTCPosition).filter(ROTCPosition.REPORTDATE >=
                                                                    DateTimeUtils.date2str(start_date),
                                                                    ROTCPosition.REPORTDATE <=
                                                                    DateTimeUtils.date2str(end_date)).all()
        oracle_session.close()
        if len(r_otc_positions) == 0 or r_otc_positions is None:
            logger.info('在%s 至 %s时间段内r_otc_postion表没有数据' % (start_date, end_date))
            return []
        logger.info('在%s 至 %s时间段内的获取了 %d条r_otc_postion表的数据' % (start_date, end_date, len(r_otc_positions)))

        return r_otc_positions
