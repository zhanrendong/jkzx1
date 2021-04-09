from dags.dbo.oracle_market_data_model import AIndexDescription
from dags.dbo.oracle_market_data_model import Oracle_Session
from terminal import Logging

logger = Logging.getLogger(__name__)


class OracleAIndexDescriptionRepo:

    @staticmethod
    def get_all_a_index_description():
        """
        全量查询
        :return:
        """
        logger.info('开始从a_index_description表获取数据')
        oracle_session = Oracle_Session()
        a_index_descriptions = oracle_session.query(AIndexDescription).all()
        oracle_session.close()

        if len(a_index_descriptions) == 0 or a_index_descriptions is None:
            logger.info('从a_index_description表没有获取到数据')
            return []

        logger.info('在a_index_description表查询到了%d条数据' %
                    (len(a_index_descriptions)))

        return a_index_descriptions

