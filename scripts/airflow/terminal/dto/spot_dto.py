from .base_dto import BaseDTO
from marshmallow import Schema, fields, post_load
from .base_schema import BaseSchema


class SpotDTO(BaseDTO):
    def __init__(self, spot=None, effectiveDays=None, underlyer=None):
        self.spot = spot
        self.effectiveDays = effectiveDays
        self.underlyer = underlyer


class SpotSchema(BaseSchema):
    spot = fields.Float()
    effectiveDays = fields.Int()
    underlyer = fields.Str()

    @post_load
    def make_spot(self, dic):
        return SpotDTO(**dic)
