from terminal.dbo import FutureContractInfo
from sqlalchemy import and_
from terminal.utils import Logging


logging = Logging.getLogger(__name__)


class CommodityFutureContractRepo:
    @staticmethod
    def get_commodity_future_contract_list(db_session, variety_type, start_date, end_date):
        contract_dbo_list = db_session.query(FutureContractInfo).filter(
            and_(
                FutureContractInfo.contractType == variety_type,
                FutureContractInfo.tradeDate >= start_date,
                FutureContractInfo.tradeDate <= end_date
            )
        ).all()
        return contract_dbo_list

    @staticmethod
    def get_exist_future_contract_info(db_session, force_update, start_date, end_date, variety_types):
        exist_dict = {}
        if not force_update:
            exist_contract = db_session.query(FutureContractInfo).filter(
                FutureContractInfo.tradeDate <= end_date, FutureContractInfo.tradeDate >= start_date).all()
            for item in exist_contract:
                exist_dict[item.contractType] = exist_dict.get(item.contractType, []) + [item.tradeDate]
        else:
            db_session.query(FutureContractInfo).\
                filter(FutureContractInfo.contractType.in_(variety_types),
                       FutureContractInfo.tradeDate >= start_date,
                       FutureContractInfo.tradeDate <= end_date).delete(synchronize_session=False)
            db_session.commit()
        return exist_dict

    @staticmethod
    def insert_fc_info_list(db_session, fc_list):
        if fc_list:
            db_session.bulk_save_objects(fc_list)
            db_session.commit()

    @staticmethod
    def get_commodity_future_contract(db_session, variety_type, trade_date):
        future_contract = db_session.query(FutureContractInfo).filter(
            and_(
                FutureContractInfo.contractType == variety_type,
                FutureContractInfo.tradeDate == trade_date
            )
        ).one_or_none()
        return future_contract

    @staticmethod
    def get_all_primary_contracts_by_date(db_session, trade_date):
        """
        获取日期内所有主力合约
        :param db_session:
        :param trade_date:
        :return:
        """
        logging.info('开始获取trade_date为: %s的主力合约' % trade_date)
        primary_contracts = db_session.query(FutureContractInfo).filter(FutureContractInfo.tradeDate == trade_date).all()
        if len(primary_contracts) == 0:
            logging.info('没有获取到主力合约信息')
            return []
        logging.info('获取主力合约的长度为: %d,primary_contract_id为: %s' %
                     (len(primary_contracts), [_.primaryContractId for _ in primary_contracts]))
        return primary_contracts
