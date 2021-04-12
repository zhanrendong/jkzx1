# -*- coding: utf-8 -*-
import sys
import xlrd

from dags.conf.settings import DataServerConfig, RedisConfig
from terminal import Logging
from terminal.dto import VolSurfaceDTO, enum_dto, VolSurfaceModelInfoDTO, Constant, VolGridItemDTO, \
    VolSurfaceUnderlyerDTO, VolItemDTO, VolSurfaceSchema, FittingModelDTO
from dags.utils.server_utils import call_request
from terminal.utils import DateTimeUtils

logging = Logging.getLogger(__name__)


class VolSurfaceExcelService:

    tenor_list = ['5D', '10D', '15D', '25D', '50D', '75D', '100D', '120D']
    percent_list = [70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120, 125, 130]

    @staticmethod
    def convert_to_vol_item_dto(table, index):
        """
        构造vol item dto 对象
        :param table:
        :param index:
        :return:
        """
        vol_item_dtos = []
        nol = table.ncols
        for j in range(1, nol):
            vol_item_dto = VolItemDTO()
            vol_item_dto.strike = None
            vol_item_dto.percent = table.cell_value(0, j)

            vol_item_dto.quote = float('%.6f' % table.cell_value(index, j))
            vol_item_dto.label = str(int(table.cell_value(0, j) * 100)) + "% SPOT"
            vol_item_dtos.append(vol_item_dto)

        return vol_item_dtos

    @staticmethod
    def convert_to_vol_grid_item_dto(table):
        """
        格式化为vol grid item dto对象
        :param table:
        :return:
        """
        nor = table.nrows
        vol_grid_item_dtos = []
        for index in range(1, nor):
            vol_grid_item_dto = VolGridItemDTO()
            vol_grid_item_dto.tenor = str(int(table.cell_value(index, 0))) + 'D'
            vol_grid_item_dto.expiry = None
            # List of VolItemDTO
            vol_grid_item_dto.vols = VolSurfaceExcelService.convert_to_vol_item_dto(table, index)
            vol_grid_item_dtos.append(vol_grid_item_dto)

        return vol_grid_item_dtos

    @staticmethod
    def convert_to_vol_surface_underlyer_dto(instrument_id, spot_price):
        """
        格式化为vol surface underlyer dto对象
        :param instrument_id:
        :param spot_price:
        :return:
        """
        vol_surface_underlyer_dto = VolSurfaceUnderlyerDTO()
        vol_surface_underlyer_dto.instrumentId = instrument_id
        vol_surface_underlyer_dto.instance = enum_dto.InstanceType.CLOSE.name
        vol_surface_underlyer_dto.field = None
        vol_surface_underlyer_dto.quote = spot_price

        return vol_surface_underlyer_dto

    @staticmethod
    def convert_to_vol_surface_model_info_dto(instrument_id, table, spot_price, days_in_year=245):
        """
        构造vol surface model info dto对象
        :param instrument_id:
        :param table:
        :param spot_price:
        :param days_in_year:
        :return:
        """

        vol_surface_model_info_dto = VolSurfaceModelInfoDTO()
        vol_surface_model_info_dto.daysInYear = days_in_year
        vol_surface_model_info_dto.modelName = Constant.TRADER_VOL
        vol_surface_model_info_dto.save = True

        # Object of VolSurfaceUnderlyerDTO
        vol_surface_model_info_dto.underlyer = VolSurfaceExcelService.\
            convert_to_vol_surface_underlyer_dto(instrument_id, spot_price)
        # List of VolGridItemDTO
        vol_surface_model_info_dto.instruments = VolSurfaceExcelService.convert_to_vol_grid_item_dto(table)

        return vol_surface_model_info_dto

    @staticmethod
    def dilute_vols(vols):
        """
        稀释vols
        :param vols:
        :return:
        """
        diluted_vols = []
        for vol in vols:
            percent = int(vol.label.split('%')[0])
            if percent not in VolSurfaceExcelService.percent_list:
                continue
            diluted_vols.append(vol)
        return diluted_vols

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
            tenor = instrument.tenor
            # 稀释tenor
            if tenor not in VolSurfaceExcelService.tenor_list:
                continue
            fitting_model_dto = FittingModelDTO()
            fitting_model_dto.underlyer = None
            fitting_model_dto.expiry = None
            fitting_model_dto.tenor = tenor
            fitting_model_dto.spotPrice = None
            fitting_model_dto.params = None
            # List of VolItemDTO
            diluted_vols = VolSurfaceExcelService.dilute_vols(instrument.vols)
            fitting_model_dto.scatter = diluted_vols
            fitting_model_dto.r = None
            fitting_model_dto.q = None
            fitting_model_dto.daysInYear = days_in_year
            fitting_models_dtos.append(fitting_model_dto)

        return fitting_models_dtos

    @staticmethod
    def convert_to_vol_surface_dto(instrument_id, valuation_date, table, spot_price):
        """
        构造vol surface dto对象
        :param instrument_id:
        :param valuation_date:
        :param table:
        :param spot_price:
        :return:
        """

        vol_surface_dto = VolSurfaceDTO()
        vol_surface_dto.instrumentId = instrument_id
        vol_surface_dto.valuationDate = valuation_date
        vol_surface_dto.strikeType = enum_dto.VolSurfaceStrikeType.PERCENT.name
        vol_surface_dto.instance = enum_dto.InstanceType.CLOSE.name
        vol_surface_dto.source = enum_dto.VolSurfaceSourceType.OFFICIAL.name

        # Object of VolSurfaceModelInfoDTO
        vol_surface_dto.modelInfo = VolSurfaceExcelService.convert_to_vol_surface_model_info_dto(instrument_id, table,
                                                                                                 spot_price)
        # List of FittingModelDTO
        vol_surface_dto.fittingModels = VolSurfaceExcelService.convert_to_fitting_Models_dto(vol_surface_dto)

        return vol_surface_dto

    @staticmethod
    def get_instrument_spot(instrument_id):
        """
        获取标的spot price
        :param instrument_id:
        :return:
        """
        host = DataServerConfig.host
        port = DataServerConfig.port
        service = 'data-service'
        # host = 'localhost'
        # port = 18000
        # service = None
        method = 'getRealTimeMarketData'
        params = {
            "instrumentIds": [instrument_id]
        }
        headers = {'Content-Type': 'application/json'}

        # 发送请求
        logging.info('开始发送请求获取标的:%s的spot price' % instrument_id)
        result = call_request(host, port, service, method, params, headers)
        if result == 'error':
            logging.error('发送请求获取spot price失败')
            return None
        instrument_dict = result['result'][0]

        return instrument_dict

    @staticmethod
    def send_post_request(vol_surface):
        host = DataServerConfig.host
        port = DataServerConfig.port
        service = 'data-service'
        # host = 'localhost'
        # port = 18000
        # service = None
        method = 'saveInstrumentVolSurface'
        params = vol_surface
        headers = {'Content-Type': 'application/json'}

        # 发送请求
        logging.info('开始发送请求保存数据到vol surface表')
        result = call_request(host, port, service, method, params, headers)
        if result == 'error':
            logging.error('发送请求保存数据到vol surface表失败')
        logging.info('发送请求保存数据到vol surface表成功')

    @staticmethod
    def import_vol_surface_from_excel_file():
        """
        解析excel文件保存到 vol surface表
        :return:
        """
        instrument_ids = sys.argv[1].split(",")
        dir_case = sys.argv[3]
        valuation_date = DateTimeUtils.str2date(sys.argv[2])
        data = xlrd.open_workbook(dir_case)

        for instrument_id in instrument_ids:
            try:
                # 判断是否获取到了spot price
                instrument_dict = VolSurfaceExcelService.get_instrument_spot(instrument_id)
                spot_price = instrument_dict.get('lastPrice')
                if spot_price is None:
                    logging.error('发送请求获取标的: %s的成功, 但是没有spot_price' % instrument_id)
                    continue
                logging.info('发送请求获取标的: %s的spot price成功' % instrument_id)
                logging.info('开始解析excel,标的为: %s' % instrument_id)
                table = data.sheet_by_name(instrument_id)
                vol_surface_dto = VolSurfaceExcelService.convert_to_vol_surface_dto(instrument_id, valuation_date,
                                                                                    table, spot_price)
                logging.info('解析excel成功,标的为: %s' % instrument_id)
                vol_surface_schema = VolSurfaceSchema()
                # 序列化对象
                vol_surface = vol_surface_schema.dump(vol_surface_dto)
                # 发送请求
                VolSurfaceExcelService.send_post_request(vol_surface)
            except Exception as e:
                logging.error('标的: %s,出现错误: %s' % (instrument_id, e))


if __name__ == '__main__':
    VolSurfaceExcelService.import_vol_surface_from_excel_file()
