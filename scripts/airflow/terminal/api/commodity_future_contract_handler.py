from .json_rpc_handler import JsonRpcHandler
from terminal.jsonrpc import method
from terminal.service import CommodityFutureContractService
from terminal.dto import FutureContractInfoSchema
from terminal.utils import DateTimeUtils


class CommodityFutureContractHandler(JsonRpcHandler):
    @method
    async def get_commodity_future_contract_order(self, variety_type, start_date, end_date):
        with self.make_session() as db_session:
            fc_info_list = CommodityFutureContractService.get_future_contract_order_dto(
                db_session, variety_type, [],
                DateTimeUtils.str2date(start_date), DateTimeUtils.str2date(end_date))
            fc_info_schema = FutureContractInfoSchema(many=True)
            return fc_info_schema.dump(fc_info_list)