from terminal.dao import InstrumentRepo
from terminal.dao import CommodityFutureContractRepo
from terminal.dto import CustomException, AssetClassType
from terminal.utils import DateTimeUtils


class InstrumentService:
    @staticmethod
    def is_commodity_variety_type(db_session, instrument_id):
        variety_types = InstrumentRepo.get_commodity_variety_types(db_session)
        if instrument_id in variety_types:
            return True
        else:
            return False

    @staticmethod
    def get_commodity_variety_types(db_session):
        variety_types = InstrumentRepo.get_commodity_variety_types(db_session)
        return variety_types

    @staticmethod
    def get_commodity_variety_type(db_session, instrument_id):
        instrument = InstrumentRepo.get_instrument(db_session, instrument_id)
        # 不是大宗商品主力合约为空
        if instrument is None:
            return None
        return instrument.contractType

    @staticmethod
    def get_instrument_id_list(db_session, trade_date, filtering=True):
        return InstrumentRepo.get_instrument_id_list(db_session, trade_date, filtering)

    @staticmethod
    def get_grouped_instrument_id_list(db_session, trade_date):
        return InstrumentRepo.get_grouped_instrument_id_list(db_session, trade_date)

    @staticmethod
    def get_primary_instrument_id(db_session, instrument_id, trade_date, is_primary):
        if InstrumentService.is_commodity_variety_type(db_session, instrument_id):
            # 如果传入的是大宗合约类型, 则直接获取当天的主力合约代码
            variety_type = instrument_id
        else:
            if is_primary is False:
                return instrument_id
            # 如果传入的是具体的大宗标的代码, 则直接获取当天的主力合约代码
            variety_type = InstrumentService.get_commodity_variety_type(db_session, instrument_id)
        # 如果获取到的合约种类为空, 则说明该标的不是大宗商品, 直接返回标的代码
        if variety_type is None:
            instrument = InstrumentRepo.get_instrument(db_session, instrument_id)
            if instrument is None:
                raise CustomException('不支持标的%s' % instrument_id)
            return instrument_id
        else:
            future_contract = CommodityFutureContractRepo.get_commodity_future_contract(
                db_session, variety_type, trade_date)
            if future_contract is None or future_contract.primaryContractId is None:
                raise CustomException("合约类型%s在%s下无主力合约数据" % (variety_type, DateTimeUtils.date2str(trade_date)))
            return future_contract.primaryContractId




