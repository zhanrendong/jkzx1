from .base_dto import BaseDTO
from .base_schema import BaseSchema
from marshmallow import Schema, fields, post_load
from enum import Enum


class FutureContractOrder(Enum):
    PRIMARY = 0
    SECONDARY = 1


class CommodityFutureContractDTO(BaseDTO):
    def __init__(self, variety_type=None ,primary_contract_id=None, secondary_contract_id=None, trade_date=None):
        self.varietyType = variety_type
        self.primaryContractId = primary_contract_id
        self.secondaryContractId = secondary_contract_id
        self.tradeDate = trade_date


class CommodityFutureContractSchema(BaseSchema):
    varietyType = fields.Str()
    primaryContractId = fields.Str()
    secondaryContractId = fields.Str()
    tradeDate = fields.Date()

    @post_load
    def make_future_contract(self, dic):
        return CommodityFutureContractDTO(**dic)
