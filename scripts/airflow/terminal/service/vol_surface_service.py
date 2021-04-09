from terminal.dao import OptionStructureRepo, WingModelRepo, QuoteCloseRepo, \
    TradingCalendarRepo, InstrumentRepo, VolSurfaceRepo
from terminal.dto import OptionStructureDTO, SpotDTO, VolSurfaceUnderlyerDTO,\
    VolGridItemDTO, VolItemDTO, VolSurfaceDTO, VolSurfaceModelInfoDTO, FittingModelDTO,\
    VolSurfaceUnderlyerSchema, FittingModelSchema, VolSurfaceModelInfoSchema, FittingModelStrikeType,\
    OptionTradedType, InstanceType, VolSurfaceStrikeType, FutureContractOrder, InstrumentType, AssetClassType
from terminal.utils import Logging
from terminal.utils.datetime_utils import DateTimeUtils
from terminal.algo import WingModelAlgo, WingModelDataAlgo, HistoricalVolAlgo
from terminal.conf.settings import WingModelConfig
from terminal.service import OptionStructureService, HistoricalVolService, TradingDayService, QuoteCloseService
import terminal.dbo.db_model as std
from datetime import timedelta, date, datetime
import uuid

logging = Logging.getLogger(__name__)


class VolSurfaceService:

    @staticmethod
    def save_vol_surface(db_session, vol_surface_dto):
        vol_surface_dbo = VolSurfaceService.to_dbo(vol_surface_dto)
        VolSurfaceRepo.insert_vol_surface(db_session, vol_surface_dbo)
        # TODO： 需要將dbo转为dto
        return vol_surface_dto

    @staticmethod
    def to_dbo(vol_surface_dto):
        logging.debug("%s转成dbo中..." % vol_surface_dto.instrumentId)
        vol_surface_dbo = std.VolSurface()
        vol_surface_dbo.uuid = uuid.uuid1()
        vol_surface_dbo.instrumentId = vol_surface_dto.instrumentId
        vol_surface_dbo.valuationDate = vol_surface_dto.valuationDate
        vol_surface_dbo.strikeType = vol_surface_dto.strikeType
        vol_surface_dbo.instance = vol_surface_dto.instance
        vol_surface_dbo.source = vol_surface_dto.source
        vol_surface_dbo.tag = vol_surface_dto.tag
        vol_surface_dbo.updatedAt = datetime.now()
        vol_surface_model_info = VolSurfaceModelInfoSchema()
        vol_surface_dbo.modelInfo = vol_surface_model_info.dump(vol_surface_dto.modelInfo).data
        vol_surface_fitting_models = FittingModelSchema(many=True)
        vol_surface_dbo.fittingModels = vol_surface_fitting_models.dump(vol_surface_dto.fittingModels).data
        return vol_surface_dbo

    @staticmethod
    def calc_instrument_fitting_model(db_session, instrument_obj, trade_date, strike_type):
        strike_type = FittingModelStrikeType.valueOf(strike_type)
        instrument = instrument_obj.instrumentId
        if (instrument_obj.optionTradedType == OptionTradedType.OTC.name and instrument_obj.assetClass == AssetClassType.EQUITY.name and instrument_obj.instrumentType == InstrumentType.STOCK.name)\
                or (instrument_obj.instrumentType == InstrumentType.FUTURE.name and instrument_obj.optionTradedType == OptionTradedType.OTC.name and instrument_obj.assetClass == AssetClassType.COMMODITY.name):
            vol_surface_dto = OtcModelService.calc_instrument_otc_implied_vol_points(db_session, instrument, trade_date, strike_type)
        elif instrument_obj.instrumentType == InstrumentType.FUND.name:
            vol_surface_dto = WingModelService.calc_fund_vol_surface(db_session, instrument, trade_date, strike_type=strike_type)
            vol_surface_dto.instrumentId = instrument
            vol_surface_dto.valuationDate = trade_date
            vol_surface_dto.instance = InstanceType.INTRADAY.name
            vol_surface_dto.strikeType = strike_type.name
            vol_surface_dto.modelInfo.modelName = "TRADER_VOL"
            underlyer = VolSurfaceUnderlyerDTO()
            underlyer.instance = InstanceType.INTRADAY.name
            underlyer.instrumentId = instrument
            underlyer.quote = QuoteCloseRepo.get_instrument_quote_close_list_by_period(db_session, instrument, trade_date, trade_date)[0].closePrice
            vol_surface_dto.modelInfo.underlyer = underlyer
        elif instrument_obj.instrumentType == InstrumentType.FUTURE.name and instrument_obj.assetClass == AssetClassType.COMMODITY.name and instrument_obj.optionTradedType == OptionTradedType.EXCHANGE_TRADED.name:
            vol_surface_dto = WingModelService.calc_commodity_vol_surface(db_session, instrument_obj.contractType, trade_date, strike_type=strike_type)
            vol_surface_dto.instrumentId = instrument
            vol_surface_dto.valuationDate = trade_date
            vol_surface_dto.instance = InstanceType.INTRADAY.name
            vol_surface_dto.strikeType = strike_type.name
            underlyer = VolSurfaceUnderlyerDTO()
            underlyer.instance = InstanceType.INTRADAY.name
            underlyer.instrumentId = instrument
            underlyer.quote = QuoteCloseRepo.get_instrument_quote_close_list_by_period(db_session, instrument, trade_date, trade_date)[0].closePrice
            vol_surface_dto.modelInfo.underlyer = underlyer

        elif instrument_obj.instrumentType == InstrumentType.OPTION.name or InstrumentType.INDEX.name:
            return None
        else:
            raise RuntimeError("不支持Volsurface的标的物 : %s" % instrument)
        return vol_surface_dto


class OtcModelService:
    @staticmethod
    def calc_instrument_otc_implied_vol_points(db_session, instrument, trade_date, strike_type,
                                               contract_order=FutureContractOrder.PRIMARY.name,
                                               percents=[0.8, 0.9, 0.95, 1, 1.05, 1.1, 1.2]):
        last_trade_date = trade_date
        logging.info("现在计算%s的vol surface" % instrument)
        quote_close_instrument = QuoteCloseService.get_instrument_quote_close_list_by_period(db_session, instrument, last_trade_date, last_trade_date, is_primary=True)[0]
        instrument = InstrumentRepo.get_instrument(db_session, instrument)
        if not quote_close_instrument:
            raise Exception(
                'trading_date: %s, instrument_id: %s的收盘价信息为空' % (instrument.instrumentId, last_trade_date))

        if not quote_close_instrument.closePrice:
            raise Exception(
                '标的instrument_id: %s,trading_date: %s没有收盘价' % (instrument.instrumentId, last_trade_date))

        if strike_type is None:
            strike_type = VolSurfaceStrikeType.PERCENT


        if strike_type == VolSurfaceStrikeType.PERCENT:
            # 根据 percentage 计算vor grid
            percents = percents
        elif strike_type == VolSurfaceStrikeType.STRIKE:
            # 根据 strike 计算 vor grid ,需要获取instrument last_trade_date时的spot_price 也就是 quote_close表的close_price
            percents = [quote_close_instrument.closePrice * i for i in percents]

        instrument_id = instrument.instrumentId
        start_date = TradingDayService.go_trading_day(db_session, last_trade_date, 132, -1)
        end_date = last_trade_date
        default_windows = [44, 66, 132]
        default_percentiles = [50]
        realised_vol_dto_list, realised_vol_log = HistoricalVolService.calc_instrument_realized_vol(db_session, instrument_id, last_trade_date,
                                                                    [1, 3, 5, 10, 22], contract_order)
        if realised_vol_log:
            logging.warning(realised_vol_log)
        realised_vol = {}
        for item in realised_vol_dto_list:
            realised_vol[item.window] = item.vol
        rolling_vol_dto_list, rolling_vol_log = HistoricalVolService.calc_instrument_rolling_vol(db_session, instrument_id, start_date, end_date, 22, contract_order)
        if rolling_vol_log:
            logging.warning(rolling_vol_log)
        vol_cone_dto_list, vol_cone_log = HistoricalVolService.calc_instrument_vol_cone(db_session, instrument_id, start_date, end_date, default_windows,
                                                            default_percentiles, contract_order)
        if vol_cone_log:
            logging.warning(vol_cone_log)
        vol_cone = {}
        for item in vol_cone_dto_list:
            vol_cone[item.window] = {}
            for vol_item in item.vols:
                vol_cone[item.window][vol_item.percentile] = vol_item.vol
        # 按日期从小到大排序
        rolling_vol_list = [item.vol for item in sorted(rolling_vol_dto_list, key=lambda x:x.tradeDate, reverse=True)]
        vol_surface = {
            '2W': (realised_vol[1] * 40 + realised_vol[3] * 20 + realised_vol[5] * 20 + realised_vol[10] * 10 +
                   realised_vol[22] * 10) / 100,
            '1M': ((sum(rolling_vol_list) / len(rolling_vol_list)) * 30 + realised_vol[1] * 30 + realised_vol[3]
                   * 20 + realised_vol[5] * 20) / 100,
            '2M': (vol_cone[44][50] * 60 + realised_vol[1] * 10 + realised_vol[22] * 30) / 100,
            '3M': (vol_cone[66][50] * 80 + realised_vol[1] * 10 + realised_vol[22] * 10) / 100,
            '6M': (vol_cone[132][50] * 90 + realised_vol[1] * 5 + realised_vol[22] * 5) / 100
        }

        dto_vol_surface = VolSurfaceDTO()
        dto_vol_surface.instrumentId = instrument_id
        dto_vol_surface.valuationDate = last_trade_date
        dto_vol_surface.strikeType = strike_type.name
        dto_vol_surface.instance = InstanceType.INTRADAY.name
        # TODO WHAT IS T-A-G?
        # dto_vol_surface.tag =
        # get model_info
        dto_model_info = VolSurfaceModelInfoDTO()
        dto_model_info.daysInYear = 245
        dto_model_info.modelName = 'TRADER_VOL'

        dto_underlyer = VolSurfaceUnderlyerDTO()
        dto_underlyer.instrumentId = instrument_id
        dto_underlyer.quote = quote_close_instrument.closePrice
        # dto_underlyer.field =
        dto_underlyer.instance = InstanceType.INTRADAY.name

        dto_model_info.underlyer = dto_underlyer
        dto_model_info.save = True
        dto_vol_grids = OtcModelService.calc_vol_grid_item(vol_surface, percents)
        dto_vol_grid_list = []
        for tenor, quote in vol_surface.items():
            dto_vol_grid = VolGridItemDTO()
            # vol_grid_item.expiry =
            dto_vol_grid.tenor = tenor
            dto_vol_grid.vols = [x.vols for x in dto_vol_grids if x.tenor == tenor][0]
            dto_vol_grid_list.append(dto_vol_grid)


        dto_model_info.instruments = dto_vol_grid_list
        dto_vol_surface.model_name = 'TRADER_VOL'
        dto_vol_surface.field = 'LAST'
        dto_vol_surface.modelInfo = dto_model_info

        dto_fitting_model_list = []
        for item in dto_model_info.instruments:
            dto_fitting_model = FittingModelDTO()
            dto_fitting_model.scatter = item.vols
            dto_fitting_model.daysInYear = dto_model_info.daysInYear
            dto_fitting_model_list.append(dto_fitting_model)

        dto_vol_surface.fittingModels = dto_fitting_model_list
        return dto_vol_surface

    @staticmethod
    def calc_vol_grid_item(vol_surface, percents):
        vol_grid_items = []
        for day, quote in vol_surface.items():
            vol_grid_item = VolGridItemDTO()
            vol_grid_item.tenor = day
            vol_grid_item.vols = OtcModelService.calc_vol_item(quote, percents)
            vol_grid_items.append(vol_grid_item)
        return vol_grid_items

    @staticmethod
    def calc_vol_item(quote, percents):
        vol_items = []
        for percent in percents:
            vol_item = VolItemDTO()
            vol_item.label = str(int(percent * 100)) + '% SPOT'
            vol_item.quote = quote
            vol_item.percent = percent
            if percent is not None and quote is not None:
                vol_item.strike = vol_item.percent * quote
            vol_items.append(vol_item)

        return vol_items


class WingModelService:
    # 参数优先级:
    # step + point_num : step
    # None + point_num : point_num
    # step + None : step
    # None + None : point_num = 10
    @staticmethod
    # 大宗入口
    def calc_commodity_vol_surface(db_session, contract_type, observed_date, start_strike=None,
                                   end_strike=None, point_num=10, step=None, strike_type=None, days_in_year=365, calendar_name=None):
        if contract_type:
            underlyers = OptionStructureRepo.get_underlyers_by_contract_type(db_session, contract_type, observed_date)
            if underlyers:
                return WingModelService.get_wing_model(db_session, underlyers, observed_date, contract_type,
                                                       start_strike, end_strike, point_num, step,
                                                       strike_type,days_in_year, calendar_name)
        else:
            return None

    # 510050入口
    @staticmethod
    def calc_fund_vol_surface(db_session, underlyer, observed_date, start_strike=None, end_strike=None,
                              point_num=None, step=None, strike_type=None, days_in_year=365, calendar_name=None):
        return WingModelService.get_wing_model(db_session, [underlyer], observed_date, underlyer, start_strike,
                                               end_strike, point_num, step, strike_type, days_in_year, calendar_name)


    @staticmethod
    def get_wing_model(db_session, underlyer_list, observed_date, contract_type,
                       start_strike=None, end_strike=None, point_num=10, step=None, strike_type=None, days_in_year=365, calendar_name=None):
        try:
            spots_dict = {}
            holidays = TradingCalendarRepo.get_holiday_list(db_session, calendar_name=calendar_name)
            if type(underlyer_list) == type([]) and len(underlyer_list):
                logging.info("Step 1 : option数据查询(现价、行权价、到期日等")
                # get option_data,,spots
                option_data_list = WingModelDataService.get_all_option_data(db_session, underlyer_list, observed_date)
                spots = WingModelDataService.get_all_close_price(db_session, underlyer_list, observed_date)
                for item in spots:
                    spots_dict[item.effectiveDays] = item.spot
                logging.info("Step 2 : 聚合计算利率、散点图、wingmodel")
                wing_model_list = WingModelAlgo.calc_multiple_wing_models(
                    option_data_list, spots, observed_date, contract_type,
                    days_in_year, holidays, WingModelConfig.bounds_dict)
                logging.info("Step 3 : wingmodel求解vol surface")
                origin = WingModelDataAlgo.fit_all_wing_models(
                    spots, wing_model_list, start_strike, end_strike, strike_type, point_num, step)
                # 处理数据为对象
                origin_key_sort = sorted(origin, key=lambda x: x)
                origin_new = {}
                for item in origin_key_sort:
                    origin_new[item] = origin[item]
                origin = origin_new
                logging.info("Step 4 : 组装DTO")
                vol_surface_dto = VolSurfaceDTO()
                vol_surface_dto.fittingModels = wing_model_list
                model_info_dto = VolSurfaceModelInfoDTO()
                model_info_dto.instruments = []
                for expiry, value in origin.items():
                    dto_vol_grid = VolGridItemDTO()
                    # expire time
                    dto_vol_grid.tenor = str(DateTimeUtils.get_effective_days_num(observed_date, expiry, holidays)) + 'D'
                    dto_vol_grid.expiry = expiry
                    dto_vol_grid.vols = []
                    vol_set = []
                    for point in value:
                        vol_item_dto = VolItemDTO()
                        percent = round(float(point[0]), 3)
                        if percent not in vol_set:
                            vol_item_dto.percent = percent
                            vol_item_dto.strike = round(point[0], 3)
                            vol_item_dto.quote = round(point[1], 3)
                            vol_item_dto.label = str(vol_item_dto.percent * 100) + '% SPOT'
                            # TODO LOGMONEYLESS: vol_item_dto.log = math.log(percent)
                            dto_vol_grid.vols.append(vol_item_dto)
                            vol_set.append(percent)
                            model_info_dto.instruments.append(dto_vol_grid)
                model_info_dto.modelName = "TRADED_VOL"
                model_info_dto.save = True
                model_info_dto.daysInYear = days_in_year
                vol_surface_dto.modelInfo = model_info_dto
                # 这里将model info中instruments的值拷贝给了scatter
                for item in vol_surface_dto.fittingModels:
                    scatters = [x.vols for x in model_info_dto.instruments if x.tenor == item.tenor]
                    item.scatter = [x.vols for x in model_info_dto.instruments if x.tenor == item.tenor][0]
                return vol_surface_dto
            else:
                logging.error("错误的输入类型")
                return None
        except Exception as ex:
            logging.error(ex)
            return None


class WingModelDataService:
    @staticmethod
    def get_all_option_data(db_session, underlyer_list, observed_date):
        option_data = []
        for underlyer in underlyer_list:
            logging.info("现在计算%s的相关数据" % underlyer)
            option_data_1_underlyer = WingModelDataService.get_one_option_data(db_session, underlyer, observed_date)
            option_data += option_data_1_underlyer
        return option_data

    @staticmethod
    def get_one_option_data(db_session, underlyer, observed_date):
        option_structure_list = OptionStructureService.get_instrument_option_chain(db_session, underlyer, observed_date, has_dividend=False)
        option_dto_list = WingModelRepo.get_option_close(db_session, underlyer, observed_date, option_structure_list)
        return option_dto_list


    @staticmethod
    def get_all_close_price(db_session, underlyer_list, observed_date):
        spot_dto_list = []
        for underlyer in underlyer_list:
            quote_close = QuoteCloseRepo.get_instrument_quote_close_by_date(db_session, underlyer, observed_date)
            option_data_1_underlyer = WingModelDataService.get_one_option_data(db_session, underlyer, observed_date)
            expire_days_1_underlyer = list(set([x.expirationDate for x in option_data_1_underlyer]))

            for expire in expire_days_1_underlyer:
                spot_dto = SpotDTO()
                spot_dto.spot = quote_close.closePrice
                spot_dto.effectiveDays = expire
                spot_dto.underlyer = underlyer
                spot_dto_list.append(spot_dto)
        return spot_dto_list


if __name__ == '__main__':
    pass