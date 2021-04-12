from _datetime import datetime
from terminal.utils import RedisUtils
from terminal.dto.heat_map_dto import HeatMapSchema
from .json_rpc_handler import JsonRpcHandler
from terminal.jsonrpc import method
from terminal.service import OtcAtmQuoteService, TradingDayService
from terminal.dto import OtcAtmQuoteDTO, OtcAtmQuoteSchema, RealizedVolSchema, DiagnosticResponse
from terminal.utils import DateTimeUtils


class OtcAtmQuoteHandler(JsonRpcHandler):
    @method
    async def get_instrument_atm_vol_list(self, underlyder, start_date, end_date, is_primary=False):
        with self.make_session() as db_session:
            atm_vol_list, diagnostics = OtcAtmQuoteService.calc_instrument_atm_vol_list(
                db_session, underlyder, DateTimeUtils.str2date(start_date), DateTimeUtils.str2date(end_date), is_primary)
        realized_vol_schema = RealizedVolSchema(many=True, exclude=['instrumentId', 'window', 'percentile'])
        return DiagnosticResponse(realized_vol_schema.dump(atm_vol_list), diagnostics)

    @method
    async def get_instrument_atm_quote_list(self, underlyer, start_date, end_date, is_primary=False):
        with self.make_session() as db_session:
            atm_quote_dto_list = OtcAtmQuoteService.get_instrument_atm_quote_list_by_period(
                db_session, underlyer, start_date, end_date, is_primary)
        otc_atm_quote_schema = OtcAtmQuoteSchema(many=True, exclude=[])
        return otc_atm_quote_schema.dump(atm_quote_dto_list)

    @method
    async def save_otc_atm_quote_list(self):
        # 如果标的物代码不为空，则忽略instrument_ids
        # TODO: 需要完善
        with self.make_session() as db_session:
            atm_quote_list = []
            dto = OtcAtmQuoteDTO()
            dto.uuid = 'f405e2f8-cb14-11e9-80de-00163e08c03e'
            dto.underlyer = 'RS911.CZC'
            dto.valuationDate = DateTimeUtils.str2date('2019-09-03')
            dto.askVol = 0.1
            dto.bidVol = 0.2
            atm_quote_list.append(dto)
            atm_quote_dto_list = OtcAtmQuoteService.save_otc_atm_quote_list(db_session, atm_quote_list)
        otc_atm_quote_schema = OtcAtmQuoteSchema(many=True, exclude=[])
        return otc_atm_quote_schema.dump(atm_quote_dto_list)

    @method
    async def get_heat_map(self, trade_date=None):
        with self.make_session() as db_session:
            if trade_date is None:
                trade_date = datetime.now().date()
            # 求上一个交易日
            holidays = TradingDayService.get_holiday_list(db_session)
            trade_date = DateTimeUtils.get_trading_day(trade_date, holidays, special_dates=[], step=2, direction=-1)
            heat_map_dto_list, diagnostics = OtcAtmQuoteService.get_heat_map(db_session, trade_date)
        heat_map_schema = HeatMapSchema(many=True, exclude=[])
        return DiagnosticResponse(heat_map_schema.dump(heat_map_dto_list), diagnostics)




