from .base_dto import BaseDTO
from terminal.dto import RealizedVolSchema
from marshmallow import Schema, fields, post_load
from .base_schema import BaseSchema


class VolConeDTO(BaseDTO):
    def __init__(self):
        self.window = None
        # List of RealizedVolDTO
        self.vols = []


class VolConeSchema(BaseSchema):
    window = fields.Int()
    vols = fields.Nested(RealizedVolSchema, many=True)

    @post_load
    def make_vol_cone(self, dic):
        return VolConeDTO(**dic)