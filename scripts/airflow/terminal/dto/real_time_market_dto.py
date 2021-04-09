from .base_dto import BaseDTO
from marshmallow import fields, post_load
from .base_schema import BaseSchema


class QuoteIntradayDTO(BaseDTO):
    def __init__(self, instrumentId=None, assetClass=None, instrumentType=None, askPrice1=None, bidPrice1=None,
                 shortName=None, lastPrice=None, prevClosePrice=None, timestamp=None, updatedAt=None):
        self.instrumentId = instrumentId
        self.assetClass = assetClass
        self.instrumentType = instrumentType
        self.askPrice1 = askPrice1
        self.bidPrice1 = bidPrice1
        self.shortName = shortName
        self.lastPrice = lastPrice
        self.prevClosePrice = prevClosePrice
        self.timestamp = timestamp
        self.updatedAt = updatedAt


class QuoteIntradaySchema(BaseSchema):
    instrumentId = fields.Str()
    assetClass = fields.Str()
    instrumentType = fields.Str()
    askPrice1 = fields.Float()
    bidPrice1 = fields.Float()
    shortName = fields.Str()
    lastPrice = fields.Float()
    prevClosePrice = fields.Float()
    timestamp = fields.DateTime()
    updatedAt = fields.DateTime()

    @post_load
    def make_real_time(self, dic):
        return QuoteIntradayDTO(**dic)
