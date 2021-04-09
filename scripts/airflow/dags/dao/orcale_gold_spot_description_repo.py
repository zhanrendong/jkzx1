from dags.dbo.oracle_market_data_model import GoldSpotDescription
from dags.dbo.oracle_market_data_model import Oracle_Session
from terminal import Logging

logger = Logging.getLogger(__name__)


class OracleGoldSpotDescriptionRepo:

    @staticmethod
    def get_all_gold_spot_description():
        """
        全量查询
        :return:
        """
        logger.info('开始从gold_spot_description表获取数据')
        oracle_session = Oracle_Session()
        gold_spot_descriptions = oracle_session.query(GoldSpotDescription).all()
        oracle_session.close()
        if len(gold_spot_descriptions) == 0 or gold_spot_descriptions is None:
            logger.info('从gold_spot_description表没有获取到数据')
            return []

        logger.info('在gold_spot_description表查询到了%d条数据' %
                    (len(gold_spot_descriptions)))

        return gold_spot_descriptions

