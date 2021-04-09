from .json_rpc_handler import JsonRpcHandler
from terminal.jsonrpc import method
from terminal.service import ImpliedVolService
import datetime
from terminal.dto import VolSurfaceInstanceType, VolSurfaceStrikeType, OptionStructureSchema, VolSurfaceSchema, \
    OptionType, OptionProductType
from terminal.dto import CustomException
from terminal.utils import DateTimeUtils


class ImpliedVolHandler(JsonRpcHandler):
    @method
    async def calc_implied_q(self, call_options, put_options, spot, r, expiration_date,
                             valuation_date=DateTimeUtils.date2str(datetime.date.today())):
        call_dto_list = OptionStructureSchema(many=True).load(call_options).data
        put_dto_list = OptionStructureSchema(many=True).load(put_options).data
        return ImpliedVolService.calc_implied_q(call_dto_list, put_dto_list, float(spot), float(r),
                                                DateTimeUtils.str2date(expiration_date),
                                                DateTimeUtils.str2date(valuation_date))

    @method
    async def calc_implied_vol(self, option_price, spot_price, strike_price, tau, r, q, option_type, product_type):
        if OptionType.valueOf(option_type) is None:
            raise CustomException('不支持期权类型：%s' % option_type)
        if OptionProductType.valueOf(product_type) is None:
            raise CustomException('不支持期权产品：%s' % product_type)
        return ImpliedVolService.calc_implied_vol(option_price, spot_price, strike_price, tau, r, q,
                                                  OptionType.valueOf(option_type), OptionProductType.valueOf(product_type))


