import dags.dbo.db_model as std
from terminal.dao import InstrumentRepo, CommodityFutureContractRepo
from terminal.service import CommodityFutureContractService
from terminal.utils import Logging
from datetime import date

logging = Logging.getLogger(__name__)


class FutureContractInfoService(object):
    @staticmethod
    def update_all_future_contract_info(start_date, end_date, force_update=False, active_only=True):
        db_session = std.create_db_session()
        variety_types = InstrumentRepo.get_commodity_variety_types(db_session, active_only)
        exist_dict = CommodityFutureContractRepo.get_exist_future_contract_info(db_session, force_update, start_date,
                                                                                end_date, variety_types)
        for variety_type in variety_types:
            logging.info('正在处理的合约类型为：%s' % variety_type)
            fc_list = CommodityFutureContractService.get_future_contract_order(db_session, variety_type,
                                                                               exist_dict.get(variety_type), start_date,
                                                                               end_date)
            CommodityFutureContractRepo.insert_fc_info_list(db_session, fc_list)


if __name__ == '__main__':
    FutureContractInfoService.update_all_future_contract_info(date(2019, 8, 29), date(2019, 8, 29))
