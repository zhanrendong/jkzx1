from .base_dto import BaseDTO
from marshmallow import Schema, fields, post_load
from enum import Enum
from .base_schema import BaseSchema


class OptionType(Enum):
    CALL = 0
    PUT = 1

    @staticmethod
    def valueOf(name):
        return OptionType.__members__.get(name)


class OptionProductType(Enum):
    VANILLA_EUROPEAN = '香草欧式'
    VANILLA_AMERICAN = '香草美式'
    AUTOCALL = 'AutoCall'
    OTHER = '其他'

    @staticmethod
    def valueOf(name):
        return OptionProductType.__members__.get(name)


class OptionStructureDTO(BaseDTO):
    def __init__(self, instrumentId=None, underlier=None, expirationDate=None, strike=None, price=None,
                 optionType=None, fullName=None):
        self.instrumentId = instrumentId
        self.underlier = underlier
        self.expirationDate = expirationDate
        self.strike = strike
        self.price = price
        self.optionType = optionType
        self.fullName = fullName


class OptionStructureSchema(BaseSchema):
    instrumentId = fields.Str()
    underlier = fields.Str()
    expirationDate = fields.Date()
    strike = fields.Float()
    price = fields.Float()
    optionType = fields.Str()
    fullName = fields.Str()

    @post_load
    def make_option_structure(self, dic):
        return OptionStructureDTO(**dic)
