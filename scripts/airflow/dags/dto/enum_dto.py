from enum import Enum


class BreakType(Enum):
    MISSING = 0
    EXTRA = 1


class ReconFromType(Enum):
    TICK_SNAPSHOT_RECON = 0
    QUOTE_CLOSE_RECON = 1
    FUTURE_RECON = 2
    CLOSE_RECKON = 3
    RETURN_RATE_RECKON = 4


class DataSourceType(Enum):
    TONGLIAN = 0


class ResponseType(Enum):
    JSON = 'json'
    CSV = 'csv'
