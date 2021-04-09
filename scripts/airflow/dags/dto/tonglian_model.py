from enum import Enum


class ExchangeIdType(Enum):
    XSHG = 'XSHG'
    XSHE = 'XSHE'


class Instrument:
    def __init__(self, instrumentId, assetClass, exchangeId, instrumentType = None):
        self.instrumentId = instrumentId
        self.assetClass = assetClass
        self.exchangeId = exchangeId
        self.instrumentType = instrumentType
