from terminal.service import WingModelService
from terminal.dto.vol_surface_dto import VolSurfaceSchema
from .json_rpc_handler import JsonRpcHandler
from terminal.jsonrpc import method
from terminal.utils import DateTimeUtils
from terminal.dao import OptionStructureRepo
from terminal.dto import FittingModelStrikeType, VolSurfaceStrikeType, CustomException, VolSurfaceInstanceType
from terminal.service import VolSurfaceService, ImpliedVolService


class FittingModelHandler(JsonRpcHandler):
    @method
    async def get_wing_model(self, contract_type, observed_date, start_strike=None,
                             end_strike=None, point_num=None, step=None,
                             strike_type=FittingModelStrikeType.valueOf('STRIKE'), days_in_year=365, calendar_name=None):
        with self.make_session() as db_session:
            observed_date = DateTimeUtils.str2date(observed_date)
            if OptionStructureRepo.check_commodity(db_session, contract_type):
                vol_surface_dto = WingModelService.calc_commodity_vol_surface(db_session, contract_type, observed_date, start_strike,
                                                                              end_strike, point_num, step, FittingModelStrikeType.valueOf(strike_type.upper()), days_in_year, calendar_name)
            else:
                vol_surface_dto = WingModelService.calc_fund_vol_surface(db_session, contract_type, observed_date, start_strike,
                                                                         end_strike, point_num, step, FittingModelStrikeType.valueOf(strike_type.upper()), days_in_year, calendar_name)
            vol_surface_schema = VolSurfaceSchema()
            return vol_surface_schema.dump(vol_surface_dto)

    @method
    async def save_instrument_vol_surface(self, **kwargs):
        with self.make_session() as db_session:
            # kwargs = self.convert_snake_case_keys(kwargs)
            vol_surface_schema = VolSurfaceSchema()
            vol_surface_dto = vol_surface_schema.load(kwargs)
            vol_surface_dto = VolSurfaceService.save_vol_surface(db_session, vol_surface_dto)
        return vol_surface_dto

    @method
    async def get_instrument_vol_surface(self, instrument_id, valuation_date,
                                         strike_type=VolSurfaceStrikeType.PERCENT.name,
                                         instance=VolSurfaceInstanceType.CLOSE.name,
                                         is_primary=False):
        with self.make_session() as db_session:
            vol_surface_dto, diagnostic = ImpliedVolService.get_instrument_vol_surface(db_session, instrument_id,
                                                                           DateTimeUtils.str2date(valuation_date),
                                                                           strike_type, instance, is_primary)
            if vol_surface_dto is None:
                raise CustomException(diagnostic.message)
        vol_surface_schema = VolSurfaceSchema(exclude=[])
        return vol_surface_schema.dump(vol_surface_dto)

    @method
    async def get_latest_vol_surface(self, instrument_id, valuation_date,
                                     strike_type=VolSurfaceStrikeType.PERCENT.name,
                                     instance=VolSurfaceInstanceType.CLOSE.name,
                                     is_primary=False):
        with self.make_session() as db_session:
            vol_surface_dto = ImpliedVolService.get_latest_vol_surface(db_session, instrument_id,
                                                                       DateTimeUtils.str2date(valuation_date),
                                                                       strike_type, instance, is_primary)
            if vol_surface_dto is None:
                raise CustomException('没有找到对应的波动率曲面')
        vol_surface_schema = VolSurfaceSchema(exclude=[])
        return vol_surface_schema.dump(vol_surface_dto)


