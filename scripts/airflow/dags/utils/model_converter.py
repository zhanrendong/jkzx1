import terminal.dbo.db_model as std
import dags.dbo.tonglian_model as tonglian
from terminal.dto import AssetClassType
import re
from terminal.dto import InstrumentType, AssetClassType

normalize_exchange = {'XSHG': 'SH',
                      'XSHE': 'SZ',
                      'XSGE': 'SHF',
                      'XZCE': 'CZC',
                      'XDCE': 'DCE',
                      'CCFX': 'CFE',
                      'XSIE': 'INE',
                      }

tl_asset_class_dict = {'stock': {'assetClass': 'E'}, 'index': {'assetClass': 'IDX'},
                       'future': {'assetClass': None}, 'fund': {'assetClass': 'F'},
                       'option': {'assetClass': None}}

normalize_orgcode = {# 上海证券交易所
                     17764: 'SH',
                     # 深圳证券交易所
                     17765: 'SZ',
                     # 深圳证券信息有限公司
                     17766: 'SZ',
                     #中证指数有限公司
                     # 17768: 'SZ' or 'CSI'
                     }

exchange_asset_class_dict = {
    'XSHG': AssetClassType.EQUITY.name,
    'XSHE': AssetClassType.EQUITY.name,
    'CCFX': AssetClassType.EQUITY.name,
    'XZCE': AssetClassType.COMMODITY.name,
    'XDCE': AssetClassType.COMMODITY.name,
    'XSGE': AssetClassType.COMMODITY.name,
    'XSIE': AssetClassType.COMMODITY.name,
}

index_code_start = {
    '0' : 'SH',
    '3' : 'SZ'
}

zhongzheng_info={
    'org_id': 17768,
    'end_fix': 'ZICN'
}


def get_tl_asset_class(std_asset_class, std_instrument_type):
    if tl_asset_class_dict.get(std_instrument_type.lower()) is None:
        return None
    return tl_asset_class_dict.get(std_instrument_type.lower()).get('assetClass')


def convertToStd(tl_instrument):
    if tl_instrument.instrumentType == InstrumentType.OPTION.name:
        return tl_instrument.instrumentId.upper()
    else:
        return tl_instrument.instrumentId.upper() + "." + normalize_exchange[tl_instrument.exchangeId]


def convertToTonglian(stdInstrument):
    code_type = stdInstrument.instrumentType.lower()
    exchange_dict = {}
    for key, value in normalize_exchange.items():
        exchange_dict[value] = key
    if code_type in tl_asset_class_dict.keys():
        if len(stdInstrument.instrumentId.split('.')) == 2:
            code, exc = stdInstrument.instrumentId.split('.')
            assetClass = tl_asset_class_dict[code_type]['assetClass']
            exchangeId = exchange_dict[exc]
            instrumentId = code
            code_app = tonglian.Instrument(instrumentId, assetClass, exchangeId)
        # 处理标的物代码中没有"."的情况
        elif len(stdInstrument.instrumentId.split('.')) == 1:
            instrumentId = stdInstrument.instrumentId
            code_app = tonglian.Instrument(instrumentId, None, None)
        else:
            code_app = None
    else:
        return None
    return code_app


def get_future_contract_type(instrument_id):
    # 期货主力合约是Active的情况不跳过
    ticker = re.match(r'^[A-Za-z]*', instrument_id)
    if ticker is not None and ticker.group(0) is not '':
        primary_contract_id = ticker.group(0)
    else:
        primary_contract_id = instrument_id
    return primary_contract_id.upper()


def get_std_asset_class(exchange_id):
    # 获取交易所对应的资产类型
    return exchange_asset_class_dict.get(exchange_id)


def convert_idx_to_std(tl_instrument):
    return tl_instrument.instrumentId.upper() + "." + normalize_orgcode[tl_instrument.exchangeId]

