from .base_dto import BaseDTO
from marshmallow import Schema, fields, post_load
from .base_schema import BaseSchema


class RealizedVolDTO(BaseDTO):
    def __init__(self, instrumentId=None, tradeDate=None, window=None, percentile=None, vol=None):
        self.instrumentId = instrumentId
        self.tradeDate = tradeDate
        self.window = window
        self.percentile = percentile
        self.vol = vol


class RealizedVolSchema(BaseSchema):
    instrumentId = fields.Str()
    tradeDate = fields.Date()
    window = fields.Int()
    percentile = fields.Float()
    vol = fields.Float()

    @post_load
    def make_realized_vol(self, dic):
        return RealizedVolDTO(**dic)
