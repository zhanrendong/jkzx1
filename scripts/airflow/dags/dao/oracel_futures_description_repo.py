from dags.dbo.oracle_market_data_model import FuturesDescription
from dags.dbo.oracle_market_data_model import Oracle_Session
from terminal import Logging

logger = Logging.getLogger(__name__)


class OracleFuturesDescriptionRepo:

    @staticmethod
    def get_all_futures_description():
        """
        全量查询
        :return:
        """
        logger.info('开始从futures_description表获取数据')
        oracle_session = Oracle_Session()
        futures_descriptions = oracle_session.query(FuturesDescription).all()
        oracle_session.close()

        if len(futures_descriptions) == 0 or futures_descriptions is None:
            logger.info('从futures_description表没有获取到数据')
            return []

        logger.info('在futures_description表查询到了%d条数据' %
                    (len(futures_descriptions)))

        return futures_descriptions

    @staticmethod
    def get_futures_description_by_fs_info_type(fs_info_type):
        """
        根据字段过滤查询
        :param fs_info_type:
        :return:
        """
        logger.info('开始从futures_description表获取数据')
        oracle_session = Oracle_Session()
        futures_descriptions = oracle_session.query(FuturesDescription).filter(FuturesDescription.
                                                                               FS_INFO_TYPE.in_(fs_info_type)).all()
        oracle_session.close()

        if len(futures_descriptions) == 0 or futures_descriptions is None:
            logger.info('从futures_description表没有获取到数据')
            return []

        logger.info('在futures_description表查询到了%d条数据' %
                    (len(futures_descriptions)))

        return futures_descriptions

