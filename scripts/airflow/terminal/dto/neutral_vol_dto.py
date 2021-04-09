from .base_dto import BaseDTO
from marshmallow import Schema, fields, post_load
from .base_schema import BaseSchema


class NeutralVolDTO(BaseDTO):
    def __init__(self, instrumentId=None, minVol=None, maxVol=None, neutralVol=None):
        self.instrumentId = instrumentId
        self.minVol = minVol
        self.maxVol = maxVol
        self.neutralVol = neutralVol


class NeutralVolSchema(BaseSchema):
    instrumentId = fields.Str()
    minVol = fields.Float()
    maxVol = fields.Float()
    neutralVol = fields.Float()

    @post_load
    def make_implied_vol_report(self, dic):
        return NeutralVolDTO(**dic)
