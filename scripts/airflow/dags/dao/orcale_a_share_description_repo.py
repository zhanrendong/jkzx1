from dags.dbo.oracle_market_data_model import AShareDescription
from dags.dbo.oracle_market_data_model import Oracle_Session
from terminal import Logging

logger = Logging.getLogger(__name__)


class OracleAShareDescriptionRepo:

    @staticmethod
    def get_all_a_share_description():
        """
        全量查询
        :return:
        """

        logger.info('开始从a_share_description表获取数据')
        oracle_session = Oracle_Session()
        a_share_descriptions = oracle_session.query(AShareDescription).all()
        oracle_session.close()

        if len(a_share_descriptions) == 0 or a_share_descriptions is None:
            logger.info('从a_share_description表没有获取到数据')
            return []

        logger.info('在a_share_description表查询到了%d条数据' % (len(a_share_descriptions)))

        return a_share_descriptions
