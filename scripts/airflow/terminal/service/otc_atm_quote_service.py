from terminal.dto.heat_map_dto import HeatMapSchema
from terminal.utils import RedisUtils
from terminal.dbo import OtcAtmQuote
from terminal.dto import OtcAtmQuoteDTO, CustomException, RealizedVolDTO, DiagnosticDTO, DiagnosticType, HeatMapDTO
from terminal.dao import OtcAtmQuoteRepo, InstrumentRepo, CommodityFutureContractRepo
from terminal.service import InstrumentService, HistoricalVolService
from terminal.utils import logging
from datetime import timedelta
from dateutil.relativedelta import relativedelta
from terminal.utils import DateTimeUtils
from terminal.service import TradingDayService
import numpy as np


class OtcAtmQuoteService:
    @staticmethod
    def get_effective_expire_date_set(valuation_date, threshold=4):
        # TODO: 默认是一个月，需要可以传入tenor进行计算
        dest_date = valuation_date + relativedelta(months=+1)
        effective_date_set = set()
        for step in range(0, threshold):
            effective_date_set.add(dest_date + timedelta(days=step))
            effective_date_set.add(dest_date + timedelta(days=-1 * step))
        return effective_date_set

    @staticmethod
    def calc_one_atm_vol(valuation_date, atm_quote_list):
        effective_vol_list = []
        effective_date_set = OtcAtmQuoteService.get_effective_expire_date_set(valuation_date)
        for quote in atm_quote_list:
            expire_date = quote.expireDate
            if expire_date not in effective_date_set:
                logging.info('ATM quote不在有效的到期范围内，当前日期： %s，估值日期： %s, Quote UUID: %s' %
                             (DateTimeUtils.date2str(expire_date), DateTimeUtils.date2str(valuation_date), quote.uuid))
                continue
            if quote.askVol is not None:
                effective_vol_list.append(quote.askVol)
            if quote.bidVol is not None:
                effective_vol_list.append(quote.bidVol)
        if len(effective_vol_list) == 0:
            return None
        atm_vol = RealizedVolDTO()
        atm_vol.tradeDate = valuation_date
        atm_vol.vol = np.mean(effective_vol_list)
        return atm_vol

    @staticmethod
    def calc_instrument_atm_vol_list(db_session, underlyer, start_date, end_date, is_primary):
        atm_quote_list = OtcAtmQuoteService.get_instrument_atm_quote_list_by_period(
            db_session, underlyer, start_date, end_date, is_primary)
        # TODO：去掉重复数据,相同valuation date, underlyer, source, expire date的数据需要去重
        # 根据valuation date进行分类
        atm_quote_dict = {}
        for atm_quote in atm_quote_list:
            if atm_quote.valuationDate not in atm_quote_dict:
                atm_quote_dict[atm_quote.valuationDate] = []
            atm_quote_dict[atm_quote.valuationDate].append(atm_quote)
        # 取一个自然月后到期日的前后3天的ask_vol和bid_vol的平均值作为当天的atm_vol
        atm_vol_dict = {}
        for valuation_date in atm_quote_dict:
            quote_list = atm_quote_dict[valuation_date]
            atm_vol = OtcAtmQuoteService.calc_one_atm_vol(valuation_date, quote_list)
            if atm_vol is not None:
                atm_vol_dict[valuation_date] = atm_vol
        # 检测哪些日期缺失
        missing_dates = []
        holidays = TradingDayService.get_holiday_list(db_session, start_date, end_date)
        trade_date = start_date
        while trade_date <= end_date:
            if DateTimeUtils.is_trading_day(trade_date, holidays):
                if trade_date not in atm_vol_dict:
                    # 将缺失的日期添加到诊断中
                    missing_dates.append(trade_date)
                    # TODO： 是否需要将缺失的数据按0补足
            trade_date += timedelta(days=1)
        # 添加诊断信息
        diagnostics = []
        if len(missing_dates) != 0:
            message = '标的物%s在[%s, %s]时段内中缺失%d条ATM Vol数据' % (
                underlyer, DateTimeUtils.date2str(start_date), DateTimeUtils.date2str(end_date), len(missing_dates))
            diagnostics.append(DiagnosticDTO(DiagnosticType.WARNING, message, missing_dates))
            logging.error(message)
        return atm_vol_dict.values(), diagnostics

    @staticmethod
    def get_instrument_atm_quote_list_by_period(db_session, underlyer, start_date, end_date, is_primary):
        if not InstrumentService.is_commodity_variety_type(db_session, underlyer):
            variety_type = None
            if is_primary:
                # 如果是大宗商品标的，则直接使用variety_type
                variety_type = InstrumentService.get_commodity_variety_type(db_session, underlyer)
            if variety_type is None:
                instrument = InstrumentRepo.get_instrument(db_session, underlyer)
                if instrument is None:
                    raise CustomException('暂不支持标的物%s，请导入该标的物行情' % underlyer)
                atm_quote_dbo_list = OtcAtmQuoteRepo.\
                    get_instrument_atm_quote_list_by_period(db_session, underlyer, start_date, end_date)
            else:
                atm_quote_dbo_list = OtcAtmQuoteRepo.\
                    get_commodity_future_atm_quote_list_by_period(db_session, variety_type, start_date, end_date)
        else:
            atm_quote_dbo_list = OtcAtmQuoteRepo.\
                get_commodity_future_atm_quote_list_by_period(db_session, underlyer, start_date, end_date)
        return OtcAtmQuoteService.to_dtos(atm_quote_dbo_list)

    @staticmethod
    def save_otc_atm_quote_list(db_session, atm_quote_list):
        saved_dbo_list = OtcAtmQuoteRepo.save_otc_atm_quote_list(db_session, OtcAtmQuoteService.to_dbos(atm_quote_list))
        return OtcAtmQuoteService.to_dtos(saved_dbo_list)

    @staticmethod
    def to_dto(dbo):
        if dbo is None:
            return None
        dto = OtcAtmQuoteDTO()
        dto.uuid = dbo.uuid
        dto.varietyType = dbo.varietyType
        dto.underlyer = dbo.underlyer
        dto.valuationDate = dbo.valuationDate
        dto.optionType = dbo.optionType
        dto.expireDate = dbo.expireDate
        dto.source = dbo.source
        dto.legalEntityName = dbo.legalEntityName
        dto.askVol = dbo.askVol
        dto.bidVol = dbo.bidVol
        dto.volEdge = dbo.volEdge
        dto.ptm = dbo.ptm
        dto.updatedAt = dbo.updatedAt
        return dto

    @staticmethod
    def to_dtos(dbo_list):
        return [OtcAtmQuoteService.to_dto(dbo) for dbo in dbo_list]

    @staticmethod
    def to_dbo(dto):
        if dto is None:
            return None
        dbo = OtcAtmQuote()
        dbo.uuid = dto.uuid
        dbo.varietyType = dto.varietyType
        dbo.underlyer = dto.underlyer
        dbo.valuationDate = dto.valuationDate
        dbo.optionType = dto.optionType
        dbo.expireDate = dto.expireDate
        dbo.legalEntityName = dto.legalEntityName
        dbo.source = dto.source
        dbo.askVol = dto.askVol
        dbo.bidVol = dto.bidVol
        dbo.volEdge = dto.volEdge
        dbo.ptm = dto.ptm
        dbo.updatedAt = dto.updatedAt
        return dbo

    @staticmethod
    def to_dbos(dto_list):
        return [OtcAtmQuoteService.to_dbo(dto) for dto in dto_list]

    @staticmethod
    def calc_one_instrument_heat_map(db_session, short_name, contract_type, instrument_id, start_date, end_date):
        logging.info('开始计算标的: %s的热力图' % instrument_id)
        diagnostic_list = []
        # 计算realied vol序列的percentile与vol
        realized_vol_dto_list, diagnostics = HistoricalVolService. \
            calc_instrument_rolling_vol(db_session, instrument_id, start_date, end_date, 22, is_primary=True)
        realized_vol = None
        realized_vol_percentile = None
        if len(realized_vol_dto_list) != 0:
            realized_vol_dto = OtcAtmQuoteService.get_last_vol(realized_vol_dto_list)
            realized_vol_list = [_.vol for _ in realized_vol_dto_list]
            realized_vol = realized_vol_dto.vol
            realized_vol_percentile = OtcAtmQuoteService.calc_percentile(realized_vol_list, realized_vol)
        if len(diagnostics) != 0:
            diagnostic_list.extend(diagnostics)

        # 计算implied vol序列的percentile与vol
        implied_vol_dto_list, diagnostics = OtcAtmQuoteService. \
            calc_instrument_atm_vol_list(db_session, instrument_id, start_date, end_date, is_primary=True)
        implied_vol = None
        implied_vol_percentile = None
        if len(implied_vol_dto_list) != 0:
            implied_vol_dto = OtcAtmQuoteService.get_last_vol(implied_vol_dto_list)
            implied_vol_list = [_.vol for _ in implied_vol_dto_list]
            implied_vol = implied_vol_dto.vol
            implied_vol_percentile = OtcAtmQuoteService.calc_percentile(implied_vol_list, implied_vol)
        if len(diagnostics) != 0:
            diagnostic_list.extend(diagnostics)

        heat_map_dto = OtcAtmQuoteService. \
            to_heat_map_dto(short_name, contract_type, realized_vol_percentile, realized_vol, implied_vol,
                            implied_vol_percentile)
        return heat_map_dto, diagnostic_list

    @staticmethod
    def get_heat_map(db_session, trade_date):
        redis_session = RedisUtils.get_redis_session()
        # 获取当天所主力合约
        primary_contracts = CommodityFutureContractRepo.get_all_primary_contracts_by_date(db_session, trade_date)
        primary_contract_ids = [primary_contract.primaryContractId for primary_contract in primary_contracts]
        # 获取instruments
        instruments = InstrumentRepo.get_instruments_by_ids(db_session, primary_contract_ids)
        # 默认开始日期向前半年
        start_date = trade_date - timedelta(days=183)
        end_date = trade_date

        heat_map_list, diagnostic_list = [], []
        for index, instrument in enumerate(instruments):
            instrument_id = instrument.instrumentId
            short_name = instrument.shortName
            contract_type = instrument.contractType
            key = (contract_type + DateTimeUtils.date2str(trade_date))
            heat_map_bytes = redis_session.get(key)

            if heat_map_bytes:
                heat_map_schema = HeatMapSchema(many=False, exclude=[])
                heat_map_dto = heat_map_schema.loads(heat_map_bytes).data
                heat_map_list.append(heat_map_dto)
            else:
                heat_map_dto, diagnostics = OtcAtmQuoteService.calc_one_instrument_heat_map(db_session, short_name,
                                                                                            contract_type, instrument_id,
                                                                                            start_date, end_date)
                heat_map_schema = HeatMapSchema(many=False, exclude=[])
                heat_map_str = heat_map_schema.dumps(heat_map_dto).data
                redis_session.set(key, heat_map_str)
                heat_map_list.append(heat_map_dto)
                diagnostic_list.append(diagnostics)
        return heat_map_list, diagnostic_list

    @staticmethod
    def to_heat_map_dto(short_name, contract_type, realized_vol_percentile, realized_vol, implied_vol, implied_vol_percentile):
        """
        组建返回数据
        :param short_name:
        :param contract_type:
        :param realized_vol_percentile:
        :param realized_vol:
        :param implied_vol:
        :param implied_vol_percentile:
        :return:
        """
        dto = HeatMapDTO()
        dto.name = short_name
        dto.contract = contract_type
        dto.realizedVol = realized_vol
        dto.realizedVolPercentile = realized_vol_percentile
        dto.impliedVol = implied_vol
        dto.impliedVolPercentile = implied_vol_percentile
        return dto

    @staticmethod
    def calc_percentile(vol_list, vol):
        """
        计算vol百分位
        :param vol_list:
        :param vol:
        :return:
        """
        vol_list.sort()
        index = vol_list.index(vol) + 1
        length = len(vol_list)
        return index / length

    @staticmethod
    def get_last_vol(vol_dto_list):
        """
        获取最近一次波动率
        :param vol_dto_list:
        :return:
        """
        trade_date_dict = {}
        for vol_dto in vol_dto_list:
            trade_date_dict[vol_dto.tradeDate] = vol_dto
        last_vol_dto = trade_date_dict[max(trade_date_dict)]
        return last_vol_dto
