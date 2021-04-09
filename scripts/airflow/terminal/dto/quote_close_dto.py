from .base_dto import BaseDTO
from marshmallow import Schema, fields, post_load
from .base_schema import BaseSchema


class QuoteCloseDTO(BaseDTO):
    def __init__(self, instrumentId=None, tradeDate=None, closePrice=None, preClosePrice=None, returnRate=None):
        self.instrumentId = instrumentId
        self.tradeDate = tradeDate
        self.closePrice = closePrice
        self.preClosePrice = preClosePrice
        self.returnRate = returnRate

class QuoteCloseSchema(BaseSchema):
    instrumentId = fields.Str()
    tradeDate = fields.Date()
    closePrice = fields.Float()
    preClosePrice = fields.Float()
    returnRate = fields.Float()

    @post_load
    def make_quote_close(self, dic):
        return QuoteCloseDTO(**dic)