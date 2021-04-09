from .base_dto import BaseDTO
from marshmallow import Schema, fields, post_load
from .base_schema import BaseSchema


class FutureContractInfoDTO(BaseDTO):
    def __init__(self, contractType=None, trade_date=None, primaryContractId=None, secondaryContractId=None):
        self.contractType = contractType
        self.tradeDate = trade_date
        self.primaryContractId = primaryContractId
        self.secondaryContractId = secondaryContractId

class FutureContractInfoSchema(BaseSchema):
    contractType = fields.Str()
    tradeDate = fields.Date()
    primaryContractId = fields.Str()
    secondaryContractId = fields.Str()

    @post_load
    def make_future_contract_info(self, dic):
        return FutureContractInfoDTO(**dic)