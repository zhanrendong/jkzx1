from .json_rpc_handler import JsonRpcHandler
from terminal.jsonrpc import method
from terminal.service import QuoteCloseService, CommodityFutureContractService
from terminal.dto import QuoteCloseSchema, CommodityFutureContractSchema


class QuoteCloseHandler(JsonRpcHandler):
    @method
    async def get_instrument_quote_close_list(self, instrument_id=None, start_date=None, end_date=None, is_primary=False):
        with self.make_session() as db_session:
            quote_close_dto_list = QuoteCloseService.get_instrument_quote_close_list_by_period(db_session,
                                                                                               instrument_id,
                                                                                               start_date,
                                                                                               end_date, is_primary)
        quote_close_schema = QuoteCloseSchema(many=True, exclude=[])
        return quote_close_schema.dump(quote_close_dto_list)

    @method
    async def get_instrument_quote_close_batch(self, instrument_ids=[], trade_dates=[]):
        # 如果标的物代码不为空，则忽略instrument_ids
        with self.make_session() as db_session:
            quote_close_dto_list = QuoteCloseService.get_instrument_quote_close_list_by_days(db_session,
                                                                                             instrument_ids,
                                                                                             trade_dates)
        quote_close_schema = QuoteCloseSchema(many=True, exclude=[])
        return quote_close_schema.dump(quote_close_dto_list)

    @method
    async def get_commodity_future_contract_list(self, variety_type=None, start_date=None, end_date=None):
        with self.make_session() as db_session:
            contract_list = CommodityFutureContractService.get_commodity_future_contract_list(db_session, variety_type, start_date, end_date)
        future_contract_schema = CommodityFutureContractSchema(many=True, exclude=[])
        return future_contract_schema.dump(contract_list)



