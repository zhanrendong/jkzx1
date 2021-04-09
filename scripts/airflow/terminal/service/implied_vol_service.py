from terminal.algo import ImpliedVolAlgo
from terminal.dto import VolSurfaceDTO, VolSurfaceStrikeType, VolSurfaceInstanceType, \
    VolSurfaceSourceType, VolSurfaceModelInfoSchema, FittingModelSchema, DiagnosticDTO, DiagnosticType
from terminal.dao import VolSurfaceRepo, QuoteCloseRepo
from terminal.service import TradingDayService, InstrumentService


class ImpliedVolService:
    @staticmethod
    def calc_implied_q(call_options, put_options, spot, r, expiration_date, valuation_date=None):
        return ImpliedVolAlgo.calc_implied_q(call_options, put_options, spot, r,
                                             expiration_date, valuation_date)

    @staticmethod
    def calc_implied_vol(option_price, spot_price, strike_price, tau, r, q, option_type, product_type):
        return ImpliedVolAlgo.calc_implied_vol(option_price, spot_price, strike_price, tau, r, q, option_type, product_type)

    @staticmethod
    def get_official_vol_surface(db_session, instrument_id, valuation_date, strike_type, instance):
        vol_surface_dbo = VolSurfaceRepo.get_instrument_vol_surface(db_session, instrument_id, valuation_date,
                                                                    strike_type, instance,
                                                                    VolSurfaceSourceType.OFFICIAL.name)
        if vol_surface_dbo is None:
            vol_surface_dbo = VolSurfaceRepo.get_instrument_vol_surface(db_session, instrument_id, valuation_date,
                                                                        strike_type, instance)
        return vol_surface_dbo

    @staticmethod
    def get_latest_vol_surface(db_session, instrument_id, valuation_date, strike_type, instance, is_primary):
        # 如果传入大宗的合约类型, 直接返回主力结果; 如果是具体标的, 看is_primary是否需要主力
        instrument_id = InstrumentService.get_primary_instrument_id(
            db_session, instrument_id, valuation_date, is_primary)
        vol_surface_dbo = VolSurfaceRepo.get_latest_vol_surface(db_session, instrument_id, valuation_date,
                                                                    strike_type, instance)
        return ImpliedVolService.to_dto(vol_surface_dbo)

    @staticmethod
    def get_instrument_vol_surface(db_session, instrument_id, valuation_date, strike_type, instance, is_primary):
        diagnostic = None
        origin_instrument_id = instrument_id
        # 如果不是交易日,则直接返回上一个交易日的波动率曲面
        if not TradingDayService.is_trading_day(db_session, valuation_date):
            valuation_date = TradingDayService.go_trading_day(db_session, valuation_date, step=1, direction=-1)
        # 如果传入大宗的合约类型, 直接返回主力结果; 如果是具体标的, 看is_primary是否需要主力
        instrument_id = InstrumentService.get_primary_instrument_id(
            db_session, instrument_id, valuation_date, is_primary)
        # 判断是要百分比还是绝对值的波动率曲面
        if strike_type == VolSurfaceStrikeType.PERCENT.name:
            vol_surface_dbo = ImpliedVolService.get_official_vol_surface(
                db_session, instrument_id, valuation_date, strike_type, instance)
            if vol_surface_dbo is None:
                message = '标的物%s在%s中时没有波动率曲面' % (instrument_id, valuation_date)
                diagnostic = DiagnosticDTO(DiagnosticType.WARNING, message, instrument_id)
            return ImpliedVolService.to_dto(vol_surface_dbo), diagnostic
        else:
            vol_surface_dbo = ImpliedVolService.get_official_vol_surface(
                db_session, instrument_id, valuation_date, strike_type, instance)
            if vol_surface_dbo is not None:
                return ImpliedVolService.to_dto(vol_surface_dbo), diagnostic
            # 如果取不到Strike的波动率曲面,则尝试取Percent的波动率曲面
            strike_type = VolSurfaceStrikeType.PERCENT.name
            vol_surface_dbo = ImpliedVolService.get_official_vol_surface(
                db_session, instrument_id, valuation_date, strike_type, instance)
            if vol_surface_dbo is None:
                # 如果波动率曲面为空,不需要再获取收盘价
                message = '标的物%s在%s中时没有波动率曲面' % (instrument_id, valuation_date)
                diagnostic = DiagnosticDTO(DiagnosticType.WARNING, message, instrument_id)
                return ImpliedVolService.to_dto(vol_surface_dbo), diagnostic
            if InstrumentService.is_commodity_variety_type(db_session, origin_instrument_id):
                # 如果传入的是合约类型, 获取收盘价时使用主力合约标的id的收盘价
                quote_close = QuoteCloseRepo.get_instrument_quote_close_by_date(db_session, instrument_id,
                                                                                valuation_date)
            else:
                # 如果传入的是标的id, 获取收盘价时使用传入的原始标的id
                quote_close = QuoteCloseRepo.get_instrument_quote_close_by_date(db_session, origin_instrument_id,
                                                                                valuation_date)
            if quote_close is None or quote_close.closePrice is None:
                message = '标的物%s在%s中时有波动率曲面, 但没有收盘价' % (instrument_id, valuation_date)
                diagnostic = DiagnosticDTO(DiagnosticType.WARNING, message, instrument_id)
                return ImpliedVolService.to_dto(None), diagnostic
            # 收盘价和波动率曲面不为空时,将Percent换算成Strike
            vol_surface_dto = ImpliedVolService.to_dto(vol_surface_dbo)
            vol_surface_dto.modelInfo.underlyer.quote = quote_close.closePrice
            for instrument in vol_surface_dto.modelInfo.instruments:
                for vol in instrument.vols:
                    vol.strike = quote_close.closePrice * vol.percent
            # fittingModels可能为空
            if vol_surface_dto.fittingModels:
                for fitting_model in vol_surface_dto.fittingModels:
                    if fitting_model is None:
                        continue
                    for _ in fitting_model.scatter:
                        _.strike = quote_close.closePrice * _.percent
            return vol_surface_dto, diagnostic

    @staticmethod
    def to_dto(dbo):
        if dbo is None:
            return None
        dto = VolSurfaceDTO()
        dto.instrumentId = dbo.instrumentId
        dto.valuationDate = dbo.valuationDate
        dto.strikeType = dbo.strikeType
        dto.instance = VolSurfaceInstanceType.INTRADAY.name \
            if dbo.instance == VolSurfaceInstanceType.INTRADAY.name else VolSurfaceInstanceType.CLOSE.name
        dto.source = dbo.source
        dto.tag = dbo.tag

        # model_info
        model_info_schema = VolSurfaceModelInfoSchema()
        model_info = model_info_schema.load(dbo.modelInfo).data
        dto.modelInfo = model_info

        # fitting model
        fitting_model_schema = FittingModelSchema(many=True)
        fitting_model_list =fitting_model_schema.load(dbo.fittingModels).data
        dto.fittingModels = fitting_model_list
        dto.updatedAt = dbo.updatedAt
        return dto
