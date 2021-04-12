from .json_rpc_handler import JsonRpcHandler
from terminal.jsonrpc import method
from terminal.service import OptionStructureService, InstrumentService
from terminal.dto import OptionStructureSchema


class OptionStructureHandler(JsonRpcHandler):
    @method
    async def get_instrument_option_chain(self, underlier, trade_date, has_dividend=True):
        with self.make_session() as db_session:
            if InstrumentService.is_commodity_variety_type(db_session, underlier) is False:
                option_dto_list = OptionStructureService.get_instrument_option_chain(db_session, underlier,
                                                                                     trade_date, has_dividend)
            else:
                option_dto_list = OptionStructureService.get_option_chain_by_variety_type(db_session, underlier,
                                                                                          trade_date, has_dividend)
        option_dto_schema = OptionStructureSchema(many=True, exclude=[])
        return option_dto_schema.dump(option_dto_list)

