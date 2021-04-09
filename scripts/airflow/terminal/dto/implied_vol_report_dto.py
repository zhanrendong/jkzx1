from .base_dto import BaseDTO
from marshmallow import Schema, fields, post_load
from .base_schema import BaseSchema

class ImpliedVolReportDTO(BaseDTO):
    def __init__(self, instrumentId=None, reportDate=None, legalEntityName=None, notionalAmount=None,
                 minVol=None, maxVol=None, meanVol=None, medianVol=None, oneQuaterVol=None, threeQuaterVol=None):
        self.instrumentId = instrumentId
        self.reportDate = reportDate
        self.legalEntityName = legalEntityName
        self.notionalAmount = notionalAmount
        self.minVol = minVol
        self.maxVol = maxVol
        self.meanVol = meanVol
        self.medianVol = medianVol
        self.oneQuaterVol = oneQuaterVol
        self.threeQuaterVol = threeQuaterVol


class ImpliedVolReportSchema(BaseSchema):
    instrumentId = fields.Str()
    reportDate = fields.Date()
    legalEntityName = fields.Str()
    notionalAmount = fields.Float()
    minVol = fields.Float()
    maxVol = fields.Float()
    meanVol = fields.Float()
    medianVol = fields.Float()
    oneQuaterVol = fields.Float()
    threeQuaterVol = fields.Float()

    @post_load
    def make_implied_vol_report(self, dic):
        return ImpliedVolReportDTO(**dic)
