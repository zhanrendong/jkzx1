from enum import Enum


class AssetClassType(Enum):
    # 跟BCT内部分类保持一致
    EQUITY = 0
    COMMODITY = 1
    RATES = 2,
    FX = 3,
    CREDIT = 4,
    OTHER = 5


class InstrumentType(Enum):
    SPOT = 0
    FUTURE = 1
    STOCK = 2
    OPTION = 3
    INDEX = 4
    FUND = 5
    INDEX_FUTURE = 6


class TradingCalendarType(Enum):
    CHINA = 0


class InstrumentStatusType(Enum):
    ACTIVE = 0
    INACTIVE = 1


class DataSourceType(Enum):
    TONGLIAN = 0
    WIND = 1


class OptionTradedType(Enum):
    EXCHANGE_TRADED = 0
    OTC = 1


class InstanceType(Enum):
    INTRADAY = 0
    CLOSE = 1


class VolSurfaceSourceType(Enum):
    OFFICIAL = 0
    OTHER = 1


class VolSurfaceStrikeType(Enum):
    STRIKE = 0
    PERCENT = 1


class VolSurfaceInstanceType(Enum):
    CLOSE = 0
    INTRADAY = 1


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
