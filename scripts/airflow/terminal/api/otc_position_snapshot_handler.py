from .json_rpc_handler import JsonRpcHandler
from terminal.jsonrpc import method
from terminal.service import OTCPositionSnapshotService
from terminal.dto import ImpliedVolReportSchema, CustomException
from terminal.utils import DateTimeUtils
from datetime import date


class OTCPositionSnapshotHandler(JsonRpcHandler):
    @method
    async def get_implied_vol_report(self, instrument_id, report_date):
        with self.make_session() as db_session:
            report_date_obj = DateTimeUtils.str2date(report_date)
            if report_date_obj > date.today():
                raise CustomException('所选日期不能超过今天')
            vol_report_dto_list = OTCPositionSnapshotService.get_implied_vol_report(
                db_session, instrument_id, report_date_obj)
        implied_vol_report_schema = ImpliedVolReportSchema(many=True, exclude=[])
        return implied_vol_report_schema.dump(vol_report_dto_list)

