from .base_dto import BaseDTO
from enum import Enum
from marshmallow import Schema, fields, post_load
from .base_schema import BaseSchema


class VolItemDTO(BaseDTO):
    def __init__(self, strike=None, percent=None, quote=None, label=None):
        self.strike = strike
        self.percent = percent
        self.quote = quote
        self.label = label


class VolItemSchema(BaseSchema):
    strike = fields.Float(missing=None)
    percent = fields.Float(missing=None)
    quote = fields.Float(missing=None)
    label = fields.Str(missing=None)

    @post_load
    def make_vol_item(self, dic):
        return VolItemDTO(**dic)


class VolGridItemDTO(BaseDTO):
    def __init__(self, tenor=None, expiry=None, vols=None):
        self.tenor = tenor
        self.expiry = expiry
        # List of VolItemDTO
        self.vols = vols


class VolGridItemSchema(BaseSchema):
    tenor = fields.Str(missing=None)
    expiry = fields.Int(missing=None)
    # List of VolItemDTO
    vols = fields.Nested(VolItemSchema, many=True, missing=None)

    @post_load
    def make_vol_grid_item(self, dic):
        return VolGridItemDTO(**dic)


class VolSurfaceUnderlyerDTO(BaseDTO):
    def __init__(self, instrumentId=None, instance=None,
                 field=None, quote=None):
        self.instrumentId = instrumentId
        self.instance = instance
        self.field = field
        self.quote = quote


class VolSurfaceUnderlyerSchema(BaseSchema):
    instrumentId = fields.Str(missing=None)
    instance = fields.Str(missing=None)
    field = fields.Str(missing=None)
    quote = fields.Float(missing=None)

    @post_load
    def make_underlyer_item(self, dic):
        return VolSurfaceUnderlyerDTO(**dic)


class VolSurfaceModelInfoDTO(BaseDTO):
    def __init__(self, daysInYear=None, instruments=None,
                 modelName=None, save=None, underlyer=None):
        self.daysInYear = daysInYear
        self.modelName = modelName
        self.save = save
        # Object of VolSurfaceUnderlyerDTO
        self.underlyer = underlyer
        # List of VolGridItemDTO
        self.instruments = instruments


class VolSurfaceModelInfoSchema(BaseSchema):
    daysInYear = fields.Int(missing=None)
    modelName = fields.Str(missing=None)
    save = fields.Bool(missing=None)
    # Object of VolSurfaceUnderlyerDTO
    underlyer = fields.Nested(VolSurfaceUnderlyerSchema, missing=None)
    # List of VolGridItemDTO
    instruments = fields.Nested(VolGridItemSchema, many=True, missing=None)

    @post_load
    def make_vol_surface_model_item(self, dic):
        return VolSurfaceModelInfoDTO(**dic)


class FittingModelStrikeType(Enum):
    STRIKE = 0
    LOGMONEYNESS = 1
    PERCENT = 2

    @staticmethod
    def valueOf(name):
        return FittingModelStrikeType.__members__.get(name)


class FittingModelDTO(BaseDTO):
    def __init__(self, underlyer=None, expiry=None, tenor=None, spotPrice=None,
                 params=None, scatter=None, r=None, q=None, daysInYear=None):
        self.underlyer = underlyer
        self.expiry = expiry
        self.tenor = tenor
        self.spotPrice = spotPrice
        self.params = params
        # List of VolItemDTO
        self.scatter = scatter
        self.r = r
        self.q = q
        self.daysInYear = daysInYear


class FittingModelSchema(BaseSchema):
    underlyer = fields.Str(missing=None)
    expiry = fields.Date(missing=None)
    tenor = fields.Str(missing=None)
    spotPrice = fields.Float(missing=None)
    params = fields.Dict(missing=None)
    scatter = fields.Nested(VolItemSchema, many=True)
    r = fields.Float(missing=None)
    q = fields.Float(missing=None)
    daysInYear = fields.Int(missing=None)

    @post_load
    def make_fitting_model(self, dic):
        return FittingModelDTO(**dic)



class VolSurfaceDTO(BaseDTO):
    def __init__(self, instrumentId=None, valuationDate=None,
                 strikeType=None, tag=None, instance=None,
                 source=None, updatedAt=None, modelInfo=None, fittingModels=None):
        self.instrumentId = instrumentId
        self.valuationDate = valuationDate
        self.strikeType = strikeType
        self.instance = instance
        self.source = source
        self.tag = tag
        # Object of VolSurfaceModelInfoDTO
        self.modelInfo = modelInfo
        self.updatedAt = updatedAt
        # List of FittingModelDTO
        self.fittingModels = fittingModels


class VolSurfaceSchema(BaseSchema):
    instrumentId = fields.Str(missing=None)
    valuationDate = fields.Date(missing=None)
    strikeType = fields.Str(missing=None)
    instance = fields.Str(missing=None)
    source = fields.Str(missing=None)
    tag = fields.Str(missing=None)
    # Object of VolSurfaceModelInfoDTO
    modelInfo = fields.Nested(VolSurfaceModelInfoSchema, missing=None)
    updatedAt = fields.DateTime(missing=None)
    fittingModels = fields.Nested(FittingModelSchema, many=True)

    @post_load
    def make_vol_surface_item(self, dic):
        return VolSurfaceDTO(**dic)


