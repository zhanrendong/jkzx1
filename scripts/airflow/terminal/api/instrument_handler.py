from .json_rpc_handler import JsonRpcHandler
from terminal.jsonrpc import method
from terminal.service import InstrumentService
from terminal.utils import DateTimeUtils
from datetime import date


class InstrumentHandler(JsonRpcHandler):
    @method
    async def get_instrument_id_list(self, trade_date=DateTimeUtils.date2str(date.today()), filtering=True):
        with self.make_session() as db_session:
            instrument_id_list = InstrumentService.\
                get_instrument_id_list(db_session, DateTimeUtils.str2date(trade_date), filtering)
        return instrument_id_list



