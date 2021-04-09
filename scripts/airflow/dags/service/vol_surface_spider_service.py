from dags.dao import OTCOptionQuoteRepo
from terminal import dto as enum_dto
from terminal.dbo import Instrument, FutureContractInfo
from dags.dbo import create_db_session
from collections import Counter
from datetime import date
from sqlalchemy import and_, or_
from terminal.dao import TradingCalendarRepo, VolSurfaceRepo
from terminal.dto import VolSurfaceDTO, VolSurfaceModelInfoDTO, Constant, FittingModelDTO, VolSurfaceUnderlyerDTO, \
    VolGridItemDTO, VolItemDTO
from terminal.service import VolSurfaceService
from terminal.utils import DateTimeUtils
import copy
import uuid
from terminal import Logging

logger = Logging.getLogger(__name__)


class VolSurfaceSpiderService:

    @staticmethod
    def get_quote(option_dict, index, flag=True):
        """
        计算出标的的平均quote
        :param option_dict:
        :param index:
        :param flag:
        :return:
        """
        if flag:
            quote = round((float(option_dict[enum_dto.OptionType.PUT.name][index].askVol.split('%')[0]) +
                           float(option_dict[enum_dto.OptionType.PUT.name][index].bidVol.split('%')[0])) / 2, 2)
        else:
            quote = round((float(option_dict[enum_dto.OptionType.CALL.name][index].askVol.split('%')[0]) +
                           float(option_dict[enum_dto.OptionType.CALL.name][index].bidVol.split('%')[0])) / 2, 2)
        return quote

    @staticmethod
    def get_average(list):
        """
        求列表平均数
        :param list:
        :return:
        """
        sum = 0
        for item in list:
            sum += item
        return sum / len(list)

    @staticmethod
    def merge_approximate_vol(vols, scope=0.001):
        """
        percent根据正负scope范围,合并近似的vol
        :param vols:
        :param scope: 给定一个小数值(不能过小, 不能过大)
        :return:
        """
        # 将vol percent值近似的归类
        data = {}
        for index, vol in enumerate(vols):
            if index == 0:
                vol_list = []
                vol_list.append(vol)
                data[index] = vol_list
                continue

            flag = True
            for key, vols_ in data.items():
                if round((vols_[0].percent + scope), 3) >= vol.percent >= round((vols_[0].percent - scope), 3):
                    data[key].append(vol)
                    flag = False
                    break
            if flag is True:
                vol_list = []
                vol_list.append(vol)
                data[index] = vol_list

        # vol percent近似的归好类的取平均值
        new_vols = []
        for vols in data.values():
            quotes = []
            percents = []
            for vol in vols:
                percents.append(vol.percent)
                quotes.append(vol.quote)

            quote = VolSurfaceSpiderService.get_average(quotes)
            percent = VolSurfaceSpiderService.get_average(percents)

            dto_vol_item = VolItemDTO()
            dto_vol_item.label = str(round(percent * 100, 1)) + '% SPOT'
            dto_vol_item.quote = round(round(quote, 2) / 100.0, 4)
            dto_vol_item.percent = round(percent, 3)

            new_vols.append(dto_vol_item)

        return new_vols

    @staticmethod
    def remove_duplicative_vol(vol_grids):
        """
        去重vol中重复的percent
        :param vol_grids:
        :return:
        """
        tenors = {}
        for vol_grid in vol_grids:
            percents = []
            for vol in vol_grid.vols:
                percents.append(vol.percent)
            tenors[vol_grid.tenor] = percents

        for tenor, percents_ in tenors.items():
            for percent, count in Counter(percents_).items():
                if count == 1:
                    continue
                for percent_ in percents_:
                    if count == 1:
                        break
                    if percent_ == percent:
                        for vol_grid_ in vol_grids:
                            if count == 1:
                                break
                            if vol_grid_.tenor == tenor:
                                for index, vol_ in enumerate(vol_grid_.vols):
                                    if vol_.percent == percent:
                                        if count == 1:
                                            break
                                        del vol_grid_.vols[index]
                                        count -= 1
        return vol_grids

    @staticmethod
    def intersection_vol_len(vol_grids):
        """
        求每个tenor中的vols的长度, 长度最小的作为标准, 长度大于的vol, 删除其中的元素, 使每个tenor中的vols的长度一致
        :param vol_grids:
        :return:
        """
        vols_len = []
        for vol_grid in vol_grids:
            vols_len.append(len(vol_grid.vols))

        min_vol_len = min([vol_len for vol_len in vols_len])

        for vol_grid in vol_grids:
            if min_vol_len < len(vol_grid.vols):
                vol_grid.vols = vol_grid.vols[0:-(len(vol_grid.vols) - min_vol_len)]

        return vol_grids

    @staticmethod
    def remove_tenor(vol_grids, tenors=[1, 2, 3, 4]):
        """
        移除tenors list中的的tenor(前提是移除后不能少于三个tenor)
        :param vol_grids:
        :param tenors:
        :return:
        """

        if len(vol_grids) <= 3:
            return vol_grids

        # 删除多余的
        tenors_list = []
        for vol_grid in vol_grids:
            tenors_list.append(int(vol_grid.tenor))
        tenors_list.sort()

        for tenor in tenors_list:
            if tenor in tenors:
                for index, vol_grid in enumerate(vol_grids):
                    if int(vol_grid.tenor) == tenor:
                        del vol_grids[index]
                        break
                if len(vol_grids) == 3:
                    break

        return vol_grids

    @staticmethod
    def convert_to_vol_surface_underlyer_dto(data):
        """
        格式化为vol surface underlyer dto对象
        :param data:
        :return:
        """
        instrument_id = list(list(data.values())[0].values())[0][0].underlier
        spot_price = list(list(data.values())[0].values())[0][0].spotPrice
        vol_surface_underlyer_dto = VolSurfaceUnderlyerDTO()
        vol_surface_underlyer_dto.instrumentId = instrument_id
        vol_surface_underlyer_dto.instance = enum_dto.InstanceType.CLOSE.name
        vol_surface_underlyer_dto.field = None
        vol_surface_underlyer_dto.quote = spot_price

        return vol_surface_underlyer_dto

    @staticmethod
    def convert_to_vol_item_dto(option_dict):
        """
        格式化为vol item dto对象
        :param option_dict:
        :return:
        """

        vol_item_dtos = []
        for index in range(len(option_dict[enum_dto.OptionType.PUT.name])):
            vol_item_dto = VolItemDTO()
            vol_item_dto.strike = None
            vol_item_dto.percent = round((option_dict[enum_dto.OptionType.PUT.name][index].exercisePrice /
                                         option_dict[enum_dto.OptionType.PUT.name][index].spotPrice), 3)
            if option_dict[enum_dto.OptionType.PUT.name][index].exercisePrice >= \
                    option_dict[enum_dto.OptionType.PUT.name][index].spotPrice:
                vol_item_dto.quote = VolSurfaceSpiderService.get_quote(option_dict, index, flag=True)
            else:
                vol_item_dto.quote = VolSurfaceSpiderService.get_quote(option_dict, index, flag=False)
            vol_item_dto.label = str(
                round((option_dict[enum_dto.OptionType.PUT.name][index].exercisePrice /
                       option_dict[enum_dto.OptionType.PUT.name][index].spotPrice) * 100, 1)) + '% SPOT'

            vol_item_dtos.append(vol_item_dto)
        return vol_item_dtos

    @staticmethod
    def convert_to_vol_grid_item_dto(data):
        """
        格式化为vol grid item dto对象
        :param data:
        :return:
        """
        vol_grid_item_dtos = []
        for tenor, option_dict in data.items():
            vol_grid_item_dto = VolGridItemDTO()
            vol_grid_item_dto.tenor = str(tenor) + 'D'
            vol_grid_item_dto.expiry = None
            # List of VolItemDTO
            vol_grid_item_dto.vols = VolSurfaceSpiderService.merge_approximate_vol(
                VolSurfaceSpiderService.convert_to_vol_item_dto(option_dict))
            vol_grid_item_dtos.append(vol_grid_item_dto)

        return vol_grid_item_dtos

    @staticmethod
    def convert_to_fitting_Models_dto(dto_vol_surface, days_in_year=245):
        """
        构建fitting_models_dto对象
        :param dto_vol_surface:
        :param days_in_year:
        :return:
        """
        fitting_models_dtos = []
        for instrument in dto_vol_surface.modelInfo.instruments:
            scatter = instrument.vols
            fitting_model_dto = FittingModelDTO()
            fitting_model_dto.underlyer = None
            fitting_model_dto.expiry = None
            fitting_model_dto.tenor = None
            fitting_model_dto.spotPrice = None
            fitting_model_dto.params = None
            # List of VolItemDTO
            fitting_model_dto.scatter = scatter
            fitting_model_dto.r = None
            fitting_model_dto.q = None
            fitting_model_dto.daysInYear = days_in_year
            fitting_models_dtos.append(fitting_model_dto)

        return fitting_models_dtos

    @staticmethod
    def convert_to_vol_surface_model_info_dto(data, days_in_year=245):
        """
        构造vol surface model info dto对象
        :param data:
        :param days_in_year:
        :return:
        """

        vol_surface_model_info_dto = VolSurfaceModelInfoDTO()
        vol_surface_model_info_dto.daysInYear = days_in_year
        vol_surface_model_info_dto.modelName = Constant.TRADER_VOL
        vol_surface_model_info_dto.save = True

        # Object of VolSurfaceUnderlyerDTO
        vol_surface_model_info_dto.underlyer = VolSurfaceSpiderService.convert_to_vol_surface_underlyer_dto(data)
        # List of VolGridItemDTO
        vol_surface_model_info_dto.instruments = VolSurfaceSpiderService.remove_tenor(
            VolSurfaceSpiderService.intersection_vol_len(
                VolSurfaceSpiderService.remove_duplicative_vol(
                    VolSurfaceSpiderService.convert_to_vol_grid_item_dto(data))))

        return vol_surface_model_info_dto

    @staticmethod
    def convert_to_vol_surface_dto(instrument_id, observed_date, data):
        """
        格式化为vol surface dto对象
        :param instrument_id:
        :param observed_date:
        :param data:
        :return:
        """
        vol_surface_dto = VolSurfaceDTO()
        vol_surface_dto.instrumentId = instrument_id
        vol_surface_dto.valuationDate = observed_date
        vol_surface_dto.strikeType = enum_dto.VolSurfaceStrikeType.PERCENT.name
        vol_surface_dto.instance = enum_dto.InstanceType.CLOSE.name
        vol_surface_dto.source = enum_dto.VolSurfaceSourceType.OFFICIAL.name

        # Object of VolSurfaceModelInfoDTO
        vol_surface_dto.modelInfo = VolSurfaceSpiderService.convert_to_vol_surface_model_info_dto(data)
        # List of FittingModelDTO
        vol_surface_dto.fittingModels = VolSurfaceSpiderService.convert_to_fitting_Models_dto(vol_surface_dto)

        return vol_surface_dto

    @staticmethod
    def process_data(instruments):
        """
        构造vol surface model数据
        :param instruments:
        :return:
        """
        data = {}
        tenors = set([instrument.term for instrument in instruments])
        if len(tenors) <= 1:
            return None
        for tenor in tenors:
            option_dict = {}
            call_list = []
            put_list = []
            for instrument in instruments:
                if instrument.term == tenor:
                    if instrument.optionType == enum_dto.OptionType.CALL.name:
                        call_list.append(instrument)
                    else:
                        put_list.append(instrument)
            option_dict[enum_dto.OptionType.CALL.name] = call_list
            option_dict[enum_dto.OptionType.PUT.name] = put_list

            data[tenor] = option_dict

        return data

    @staticmethod
    def get_last_quote_data(quotes):
        """
        如果爬虫启动多次,只取最后一次
        :param quotes:
        :return:
        """
        spider_records = []
        for quote in quotes:
            if quote.spiderRecord is not None:
                spider_records.append(quote.spiderRecord)

        if len(spider_records) == 0:
            return quotes
        # 当天爬虫启动的最后一次记录
        last_spdier_record = max(spider_records)

        last_quotes = []
        for quote in quotes:
            if quote.spiderRecord == last_spdier_record:
                last_quotes.append(quote)

        return last_quotes

    @staticmethod
    def calc_one_instrument_quote_vol_surface(db_session, instrument_id, observed_date):
        """
        计算单个标的的quote vol_surface
        :param db_session:
        :param instrument_id:
        :param observed_date:
        :return:
        """
        try:
            # 获取标的quote数据
            quotes = OTCOptionQuoteRepo.get_quotes_by_observed_date_and_instrument_id(db_session,
                                                                                      observed_date,
                                                                                      instrument_id)
            if len(quotes) == 0:
                return None
            # 如果爬虫启动多次,只取最后一次
            last_quotes = VolSurfaceSpiderService.get_last_quote_data(quotes)
            # 处理标的数据
            data = VolSurfaceSpiderService.process_data(last_quotes)
            if data is None:
                logger.info('标的为: %s, observed_date为: %s的标的只有一条线,不能画出一个vol_surface面' %
                            (instrument_id, observed_date))
                return None
            # 格式化为vol surface dto对象
            vol_surface_dto = VolSurfaceSpiderService.convert_to_vol_surface_dto(instrument_id, observed_date, data)
            # 构建 vol surface dbo对象
            vol_surface_dbo = VolSurfaceService.to_dbo(vol_surface_dto)

            return vol_surface_dbo
        except Exception as e:
            logger.error('计算标的的vol surface出错标的为: %s ,错误为: %s' % (instrument_id, e))
            return None

    @staticmethod
    def calc_all_instrument_quote_vol_surface(observed_date):
        """
        从otc_option_quote表里取华泰的数据,格式化写入到vol_surface表中
        :return:
        """
        db_session = create_db_session()
        try:
            # 取所有instrument_id
            instrument_ids = OTCOptionQuoteRepo.get_instrument_ids_by_observe_date(db_session, observed_date)
            vol_surface_dbo_dict = {}
            for index, instrument_id in enumerate(instrument_ids):
                # 计算一个标的的vol_surface
                logger.info('准备加载的标的为: %s, 进度为: %d of %d' % (instrument_id, index + 1, len(instrument_ids)))
                vol_surface_dbo = VolSurfaceSpiderService.calc_one_instrument_quote_vol_surface(db_session, instrument_id,
                                                                                                observed_date)
                if vol_surface_dbo is None:
                    continue
                vol_surface_dbo_dict[instrument_id] = vol_surface_dbo
                logger.info('成功计算了标的: %s的vol_surface, 进度为: %d of %d' % (instrument_id, index + 1, len(instrument_ids)))

            # 获取所有的合约种类及其对应的主力合约
            holidays = TradingCalendarRepo.get_holiday_list(db_session)
            special_dates = []
            # 取上一个交易日
            last_trade_date = DateTimeUtils.get_trading_day(observed_date, holidays, special_dates, 1, -1)
            # 获取所有的主力合约
            contract_list = db_session.query(FutureContractInfo).filter(
                FutureContractInfo.tradeDate == last_trade_date).all()
            # 获取当天所有活跃的大宗期货合约
            active_instrument_list = db_session.query(Instrument).filter(
                and_(Instrument.status == enum_dto.InstrumentStatusType.ACTIVE.name,
                     Instrument.assetClass == enum_dto.AssetClassType.COMMODITY.name,
                     Instrument.instrumentType == enum_dto.InstrumentType.FUTURE.name,
                     or_(Instrument.delistedDate is None, Instrument.delistedDate > observed_date))).all()
            active_instrument_dict = {}
            for instrument in active_instrument_list:
                if instrument.contractType not in active_instrument_dict:
                    active_instrument_dict[instrument.contractType] = []
                active_instrument_dict[instrument.contractType].append(instrument.instrumentId)
            # 所有标的都使用主力合约对应的波动率曲面
            vol_surface_dbos = []
            for contract in contract_list:
                primary_contract_id = contract.primaryContractId
                if primary_contract_id not in vol_surface_dbo_dict:
                    logger.warning('标的没有主力合约对应的波动率曲面：%s' % primary_contract_id)
                    continue
                variety_type = contract.contractType
                if variety_type not in active_instrument_dict:
                    logger.warning('主力合约没有活跃的合约：%s' % variety_type)
                    continue
                active_future_list = active_instrument_dict[variety_type]
                for instrument_id in active_future_list:
                    vol_surface_dbo = copy.deepcopy(vol_surface_dbo_dict[primary_contract_id])
                    vol_surface_dbo.uuid = uuid.uuid1()
                    vol_surface_dbo.instrumentId = instrument_id
                    vol_surface_dbos.append(vol_surface_dbo)
            # 将波动率曲面写入数据库
            VolSurfaceRepo.insert_vol_surfaces(db_session, vol_surface_dbos)
            logger.info('爬虫vol surface计算完成')
        except Exception as e:
            logger.error(e)


if __name__ == '__main__':
    VolSurfaceSpiderService.calc_all_instrument_quote_vol_surface(date(2019, 8, 8))
