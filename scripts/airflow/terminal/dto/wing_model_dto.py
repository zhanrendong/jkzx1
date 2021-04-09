from .base_dto import BaseDTO
from enum import Enum
from marshmallow import Schema, fields, post_load


class WingModelStrikeType(Enum):
    STRIKE = 0
    LOGMONEYNESS = 1
    PERCENT = 2

    @staticmethod
    def valueOf(name):
        return WingModelStrikeType.__members__.get(name)

class WingModelDTO(BaseDTO):
    def __init__(self, underlyer=None, expiry=None, tenor=None, spotPrice=None,
                 params=None, scatter=None, r=None, q=None, daysInYear=None):
        self.underlyer=underlyer
        self.expiry=expiry
        self.tenor=tenor
        self.spotPrice=spotPrice
        self.params=params
        self.scatter=scatter
        self.r = r
        self.q = q
        self.daysInYear = daysInYear


class WingModelSchema(Schema):
    underlyer = fields.Str()
    expiry = fields.Date()
    tenor = fields.Int()
    spotPrice = fields.Float()
    params = fields.Dict()
    scatter = fields.List(fields.List(fields.Float()))
    r = fields.Float()
    q = fields.Float()
    daysInYear = fields.Int()

    @post_load
    def make_wing_model(self, dic):
        return WingModelDTO(**dic)