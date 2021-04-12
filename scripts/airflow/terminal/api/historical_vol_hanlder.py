from .json_rpc_handler import JsonRpcHandler
from terminal.jsonrpc import method
from terminal.service import HistoricalVolService, TradingDayService
from terminal.utils import DateTimeUtils
from terminal.dto import CustomException, RealizedVolSchema, VolConeSchema, DiagnosticResponse, NeutralVolSchema
from datetime import date


class HistoricalVolHandler(JsonRpcHandler):
    @method
    async def get_instrument_realized_vol(self, instrument_id, trade_date,
                                          windows=[1, 3, 5, 10, 22, 44, 66, 132],
                                          is_primary=False):
        with self.make_session() as db_session:
            trade_date_obj = DateTimeUtils.str2date(trade_date)
            if trade_date_obj > date.today():
                raise CustomException('所选日期不能超过今天')
            realized_vol_dto_list, diagnostics = HistoricalVolService.calc_instrument_realized_vol(
                db_session, instrument_id, DateTimeUtils.str2date(trade_date), windows, is_primary)
        realized_vol_schema = RealizedVolSchema(many=True, exclude=['tradeDate', 'instrumentId', 'percentile'])
        return DiagnosticResponse(realized_vol_schema.dump(realized_vol_dto_list), diagnostics)

    @method
    async def get_instrument_rolling_vol(self, instrument_id, start_date, end_date, window, is_primary=False):
        with self.make_session() as db_session:
            start_date_obj = DateTimeUtils.str2date(start_date)
            end_date_obj = DateTimeUtils.str2date(end_date)
            realized_vol_dto_list, diagnostics = HistoricalVolService.calc_instrument_rolling_vol(
                db_session, instrument_id, start_date_obj, end_date_obj, window, is_primary)
        realized_vol_schema = RealizedVolSchema(many=True, exclude=['instrumentId', 'percentile'])
        return DiagnosticResponse(realized_vol_schema.dump(realized_vol_dto_list), diagnostics)

    @method
    async def get_instrument_vol_cone(self, instrument_id, start_date, end_date,
                                      windows=[1, 3, 5, 10, 22, 44, 66, 132],
                                      percentiles=[0, 0.1, 0.25, 0.50, 0.75, 0.90, 1], is_primary=False):
        with self.make_session() as db_session:
            start_date_obj = DateTimeUtils.str2date(start_date)
            end_date_obj = DateTimeUtils.str2date(end_date)
            vol_cone_dto_list, diagnostics = HistoricalVolService.calc_instrument_vol_cone(
                db_session, instrument_id, start_date_obj, end_date_obj,
                windows, [percentile * 100 for percentile in percentiles], is_primary)
        vol_cone_schema = VolConeSchema(many=True, exclude=['vols.instrumentId', 'vols.tradeDate', 'vols.window'])
        return DiagnosticResponse(vol_cone_schema.dump(vol_cone_dto_list), diagnostics)

    @method
    async def get_historical_and_neutral_vol_list(self, instrument_ids, start_date, end_date, window, is_primary=False):
        with self.make_session() as db_session:
            start_date_obj = DateTimeUtils.str2date(start_date)
            end_date_obj = DateTimeUtils.str2date(end_date)
            neutral_vol_list, diagnostics = HistoricalVolService.calc_historical_and_neutral_vol_list(
                db_session, instrument_ids, start_date_obj, end_date_obj, window, is_primary)
        neutral_vol_schema = NeutralVolSchema(many=True)
        return DiagnosticResponse(neutral_vol_schema.dump(neutral_vol_list), diagnostics)

    @staticmethod
    def check_date_params(db_session, start_date, end_date, window):
        if start_date > date.today() or end_date > date.today():
            raise CustomException('所选日期不能超过今天')
        if TradingDayService.get_effective_days_num(db_session, start_date, end_date) < window:
            raise CustomException('所选日期范围[%s,%s]交易日个数小于窗口大小，无法进行计算' % (
                DateTimeUtils.date2str(start_date), DateTimeUtils.date2str(end_date)))

