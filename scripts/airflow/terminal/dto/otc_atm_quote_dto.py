from .base_dto import BaseDTO
from marshmallow import Schema, fields, post_load


class OtcAtmQuoteDTO(BaseDTO):
    def __init__(self, uuid=None, variety_type=None, underlyer=None, valuation_date=None, option_type=None,
                 expire_date=None, legal_entity_name=None, source=None, ask_vol=None, bid_vol=None, vol_edge=None, ptm=None, updated_at=None):
        self.uuid = uuid
        self.varietyType = variety_type
        self.underlyer = underlyer
        self.valuationDate = valuation_date
        self.optionType = option_type
        self.expireDate = expire_date
        self.legalEntityName = legal_entity_name
        self.source = source
        self.askVol = ask_vol
        self.bidVol = bid_vol
        self.volEdge = vol_edge
        self.ptm = ptm
        self.updatedAt = updated_at


class OtcAtmQuoteSchema(Schema):
    uuid = fields.Str()
    varietyType = fields.Str()
    underlyer = fields.Str()
    valuationDate = fields.Date()
    optionType = fields.Str()
    expireDate = fields.Date()
    legal_entity_name = fields.Str()
    source = fields.Str()
    askVol = fields.Float()
    bidVol = fields.Float()
    volEdge = fields.Float()
    ptm = fields.Float()
    updatedAt = fields.DateTime()

    @post_load
    def make_quote_close(self, dic):
        return OtcAtmQuoteDTO(**dic)
