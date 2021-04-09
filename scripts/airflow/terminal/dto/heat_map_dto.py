from .base_dto import BaseDTO
from marshmallow import fields, post_load
from .base_schema import BaseSchema


class HeatMapDTO(BaseDTO):
    def __init__(self, name=None, contract=None, realizedVol=None, realizedVolPercentile=None, impliedVol=None,
                 impliedVolPercentile=None):
        self.name = name
        self.contract = contract
        self.realizedVol = realizedVol
        self.realizedVolPercentile = realizedVolPercentile
        self.impliedVol = impliedVol
        self.impliedVolPercentile = impliedVolPercentile


class HeatMapSchema(BaseSchema):
    name = fields.Str()
    contract = fields.Str()
    realizedVol = fields.Float()
    realizedVolPercentile = fields.Float()
    impliedVol = fields.Float()
    impliedVolPercentile = fields.Float()

    @post_load
    def make_heat_vol(self, dic):
        return HeatMapDTO(**dic)
