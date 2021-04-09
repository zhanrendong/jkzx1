from terminal.dao import QuoteCloseRepo, InstrumentRepo
from terminal.dto import QuoteCloseDTO, CustomException
from .commodity_future_contract_service import CommodityFutureContractService
from .instrument_service import InstrumentService
from .trading_day_service import TradingDayService
from terminal.dto import FutureContractOrder, DiagnosticDTO, DiagnosticType
from terminal.utils import DateTimeUtils
import logging
from datetime import timedelta


class QuoteCloseService:

    @staticmethod
    def get_instrument_quote_close_dict_by_period(db_session, instrument_id, start_date, end_date, is_primary=False):
        quote_close_dto_list = QuoteCloseService.get_instrument_quote_close_list_by_period(db_session, instrument_id,
                                                                                           start_date, end_date,
                                                                                           is_primary)
        # 转化为字典类型
        quote_close_dto_dict = {}
        for quote_close in quote_close_dto_list:
            quote_close_dto_dict[quote_close.tradeDate] = quote_close
        # 校验缺失的收盘价数据
        missing_dates = []
        holidays = TradingDayService.get_holiday_list(db_session, start_date, end_date)
        trade_date = start_date
        while trade_date <= end_date:
            if DateTimeUtils.is_trading_day(trade_date, holidays):
                if trade_date not in quote_close_dto_dict:
                    # 将缺失的日期添加到诊断中
                    missing_dates.append(trade_date)
                    # 是否需要将缺失的数据按None补齐
                    quote_close_dto_dict[trade_date] = None
            trade_date += timedelta(days=1)
        # 添加诊断信息
        diagnostics = []
        if len(missing_dates) != 0:
            message = '标的物%s在[%s, %s]时段内中缺失%d条收盘数据' % (
                instrument_id, DateTimeUtils.date2str(start_date), DateTimeUtils.date2str(end_date), len(missing_dates))
            diagnostics.append(DiagnosticDTO(DiagnosticType.WARNING, message, missing_dates))
            logging.debug(message)
        return quote_close_dto_dict, diagnostics


    @staticmethod
    def get_commodity_future_quote_close_dict_by_period(db_session, variety_type, start_date, end_date,
                                                        variety_order=FutureContractOrder.PRIMARY.name):
        quote_close_dto_list = QuoteCloseService.get_commodity_future_quote_close_list_by_period(
            db_session, variety_type, start_date, end_date, variety_order)
        quote_close_dto_dict = {}
        # 转化为字典类型
        for quote_close in quote_close_dto_list:
            quote_close_dto_dict[quote_close.tradeDate] = quote_close
        return quote_close_dto_dict

    @staticmethod
    def get_instrument_quote_close_list_by_period(db_session, instrument_id, start_date, end_date, is_primary=False):
        if not InstrumentService.is_commodity_variety_type(db_session, instrument_id):
            variety_type = None
            if is_primary:
                # 如果是大宗商品标的，则直接使用variety_type
                variety_type = InstrumentService.get_commodity_variety_type(db_session, instrument_id)
            if variety_type is None:
                instrument = InstrumentRepo.get_instrument(db_session, instrument_id)
                if instrument is None:
                    raise CustomException('暂不支持标的物%s，请导入该标的物行情' % instrument_id)
                quote_close_dbo_list = QuoteCloseRepo.\
                    get_instrument_quote_close_list_by_period(db_session, instrument_id, start_date, end_date)
            else:
                quote_close_dbo_list = QuoteCloseService.\
                    get_commodity_future_quote_close_list_by_period(db_session, variety_type, start_date, end_date)
        else:
            quote_close_dbo_list = QuoteCloseService.\
                get_commodity_future_quote_close_list_by_period(db_session, instrument_id, start_date, end_date)
        return QuoteCloseService.to_dtos(quote_close_dbo_list)

    @staticmethod
    def get_instrument_quote_close_list_by_days(db_session, instrument_ids, trade_dates):
        quote_close_dbo_list = QuoteCloseRepo.get_instrument_quote_close_list_by_days(db_session,
                                                                                      instrument_ids, trade_dates)
        return QuoteCloseService.to_dtos(quote_close_dbo_list)

    @staticmethod
    def get_commodity_future_quote_close_list_by_period(db_session, variety_type, start_date, end_date,
                                                        variety_order=FutureContractOrder.PRIMARY.name):
        contract_dto_list = CommodityFutureContractService.get_commodity_future_contract_list(db_session, variety_type,
                                                                                              start_date, end_date)
        instrument_ids = []
        trade_dates = []
        for dto in contract_dto_list:
            if variety_order == FutureContractOrder.PRIMARY.name:
                instrument_ids.append(dto.primaryContractId)
            else:
                instrument_ids.append(dto.secondaryContractId)
            trade_dates.append(dto.tradeDate)
        quote_close_list = QuoteCloseService.get_instrument_quote_close_list_by_days(db_session, instrument_ids,
                                                                                     trade_dates)
        # 构建收盘价字典
        quote_close_dto_dict = {}
        for dto in quote_close_list:
            if quote_close_dto_dict.get(dto.tradeDate) is None:
                quote_close_dto_dict[dto.tradeDate] = {}
            quote_close_dto_dict[dto.tradeDate][dto.instrumentId] = dto
        # 获取对应的收盘价
        result = []
        for dto in contract_dto_list:
            if quote_close_dto_dict.get(dto.tradeDate) is not None:
                if variety_order == FutureContractOrder.PRIMARY.name:
                    quote_close_dto = quote_close_dto_dict.get(dto.tradeDate).get(dto.primaryContractId)
                else:
                    quote_close_dto = quote_close_dto_dict.get(dto.tradeDate).get(dto.secondaryContractId)
                if quote_close_dto is not None:
                    result.append(quote_close_dto)
        return result

    @staticmethod
    def to_dtos(dbos):
        return [QuoteCloseService.to_dto(dbo) for dbo in dbos]

    @staticmethod
    def to_dto(dbo):
        if dbo is not None:
            dto = QuoteCloseDTO()
            dto.instrumentId = dbo.instrumentId
            dto.tradeDate = dbo.tradeDate
            dto.closePrice = dbo.closePrice
            dto.preClosePrice = dbo.preClosePrice
            dto.returnRate = dbo.returnRate
        else:
            dto = None
        return dto

