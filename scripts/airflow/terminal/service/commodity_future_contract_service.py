from terminal.dao import CommodityFutureContractRepo, TradingCalendarRepo, InstrumentRepo, QuoteCloseRepo
from terminal.dbo import Instrument, FutureContractInfo
from terminal.dto import CommodityFutureContractDTO
from terminal.utils import Logging, DateTimeUtils
from terminal.algo import FutureContractInfoAlgo
from dateutil.rrule import rrule, DAILY


logging = Logging.getLogger(__name__)


class CommodityFutureContractService:
    @staticmethod
    def get_commodity_future_contract_list(db_session, variety_type, start_date, end_date):
        contract_dbo_list = CommodityFutureContractRepo.get_commodity_future_contract_list(db_session, variety_type,
                                                                                           start_date, end_date)
        return CommodityFutureContractService.to_dtos(contract_dbo_list)

    @staticmethod
    def to_dtos(dbos):
        return [CommodityFutureContractService.to_dto(dbo) for dbo in dbos]

    @staticmethod
    def to_dto(dbo):
        if dbo is not None:
            dto = CommodityFutureContractDTO()
            dto.varietyType = dbo.contractType
            dto.primaryContractId = dbo.primaryContractId
            dto.secondaryContractId = dbo.secondaryContractId
            dto.tradeDate = dbo.tradeDate
        else:
            dto = None
        return dto


    @staticmethod
    def get_future_contract_order_dto(db_session, contract_type, exist_dates, start_date, end_date):
        return CommodityFutureContractService.to_dtos(CommodityFutureContractService.get_future_contract_order(db_session, contract_type, exist_dates, start_date, end_date))

    @staticmethod
    def get_future_contract_order(db_session, contract_type, exist_dates, start_date, end_date):
        holidays = TradingCalendarRepo.get_holiday_list(db_session)
        special_dates = []
        if start_date > end_date:
            logging.error("日期指定非法:%s 大于 %s" % DateTimeUtils.date2str(start_date), DateTimeUtils.date2str(end_date))
            return None
        else:
            all_dates = [DateTimeUtils.str2date(date.strftime("%Y-%m-%d")) for date in rrule(DAILY, dtstart=start_date, until=end_date)]
            if exist_dates:
                missing_dates = list(set(all_dates)- set(exist_dates))
            else:
                missing_dates = all_dates
            listed_date, delisted_date = DateTimeUtils.offset_trade_date(start_date, end_date, holidays, special_dates)
            close_dict = CommodityFutureContractService.get_corresponding_close(
                db_session, contract_type, listed_date, delisted_date)
            instruments_list = db_session.query(Instrument).filter(Instrument.contractType == contract_type)

            fc_list = []
            if close_dict:
                for trade_date in missing_dates:
                    if DateTimeUtils.is_trading_day(trade_date, holidays, special_dates):
                        logging.info("处理交易日%s" % DateTimeUtils.date2str(trade_date))
                        previous_trade_date, _ = DateTimeUtils.offset_trade_date(
                            trade_date, trade_date, holidays, special_dates)
                        pri, sec = FutureContractInfoAlgo.calc_one_future_contract_info(instruments_list, close_dict, contract_type, previous_trade_date)
                        if pri:
                            fc_info = FutureContractInfo(contractType=contract_type,
                                                             tradeDate=trade_date, primaryContractId=pri,
                                                             secondaryContractId=sec)
                            logging.info(
                                '合约的主力和次主力为：%s %s %s %s' % (fc_info.contractType, DateTimeUtils.date2str(fc_info.tradeDate),
                                                            fc_info.primaryContractId, fc_info.secondaryContractId))
                            fc_list.append(fc_info)
                    else:
                        logging.info("跳过非交易日%s" % DateTimeUtils.date2str(trade_date))
            else:
                fc_list = None
            return fc_list

    @staticmethod
    def get_corresponding_close(db_session, contract_type, listed_date, delisted_date):
        contract_instruments = InstrumentRepo.get_instrument_by_contract_in_range(
            db_session, contract_type, listed_date, delisted_date)
        # 找到上面找出的instrmentsId所对应的QuoteClose数据
        quote_close_list = QuoteCloseRepo.get_instrument_quote_close_list_by_range(
            db_session, contract_instruments, listed_date, delisted_date)
        return quote_close_list
