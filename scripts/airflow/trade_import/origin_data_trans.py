# -*- coding: utf-8 -*-
from datetime import datetime, date
from trade_import.db_utils import *

_DATE_FMT = '%Y-%m-%d'

_DIRECTION_SELLER = 'SELLER'
_DIRECTION_BUYER = 'BUYER'
_UNIT_PERCENT = 'PERCENT'
_UNIT_LOT = 'LOT'
_UNIT_CNY = 'CNY'

_OPTION_CALL = 'CALL'
_OPTION_PUT = 'PUT'

_SETTLE_CLOSE = 'CLOSE'
_SETTLE_TWAP = 'TWAP'


def organize_trade_data(position_data, code_and_exchange_relation_dict):
    trade_id = position_data.TRANSCODE
    trade_origin_data = {
        'trade_id': trade_id,
        'book_name': position_data.MAINBODYNAME,
        'trade_date': datetime.strptime(position_data.TRANSCONFIRTIME, _DATE_FMT),
    }
    trade_origin_data['trader'] = trade_origin_data['book_name']
    counter_party_name = position_data.ANALOGUENAME
    if counter_party_name is None:
        raise RuntimeError('传输编号：' + trade_id + ',交易对手为null' )
    # 期权结构逻辑
    product_type_data = position_data.OPTEXERCTMTYPE
    if product_type_data in ['US']:
        product_type = 'VANILLA_AMERICAN'
    elif product_type_data in ['EU']:
        product_type = 'VANILLA_EUROPEAN'
    else:
        return None
    # 处理标的信息
    instrument_id = position_data.STANDASSCONT
    category = position_data.UNDERASSVARIT
    if '.' not in instrument_id:
        code_exchange = code_and_exchange_relation_dict.get(instrument_id.upper())
        category = str(category).upper()
        if code_exchange is not None:
            exe = code_exchange if dec_to_exe.get(code_exchange) is None else dec_to_exe.get(code_exchange)
        else:
            raise RuntimeError('交易标的：{} 找不到对应的交易场所，交易品种：{}'.format(instrument_id, category))
        underlyer = str(instrument_id).upper() + '.' + exe
    else:
        underlyer = str(instrument_id).upper()
    # 处理看涨看跌逻辑
    option_type_data = position_data.OPTRIGHTTYPE
    if option_type_data in ['1', 'put', 'PUT', 'Put', 'P']:
        option_type = _OPTION_PUT
    elif option_type_data in ['2', 'call', 'CALL', 'Call', 'C']:
        option_type = _OPTION_CALL
    else:
        raise RuntimeError('传输编号：' + trade_id + ',权利类型错误：' + str(option_type_data))
    # 处理买卖方向逻辑
    direction_data = position_data.SIDE
    if direction_data in ['B', 'BYER', '买']:
        direction = _DIRECTION_BUYER
    elif direction_data in ['S', 'SLLR', '卖']:
        direction = _DIRECTION_SELLER
    else:
        raise RuntimeError('传输编号：' + trade_id + ',买卖方向错误：' + str(direction_data))
    # 处理年化
    annualized_data = position_data.ANNUALIZED
    if annualized_data in ['Y', 'y']:
        annualized = True
    else:
        annualized = False
    days_in_year = position_data.EFFECTIVEDAY
    if days_in_year is None:
        days_in_year = 365
    # 参与率（暂时都为1）
    participation_rate = position_data.PARTICIPATERATE
    if participation_rate is None:
        participation_rate = 1
    #     raise RuntimeError('传输编号：' + trade_id + ',参与率为null')
    # 行权价
    strike = position_data.STRIKE
    if strike is None:
        raise RuntimeError('传输编号：' + trade_id + ',行权价为null')
    # 期初价格
    init_spot = position_data.VALUATIONSPOT
    if init_spot is None:
        raise RuntimeError('传输编号：' + trade_id + ',期初价格为null')
    # 名义本金
    notional = position_data.TRADENOTIONAL
    if notional is None:
        raise RuntimeError('传输编号：' + trade_id + ',名义本金为null')
    # 期权费
    premium = 0
    # 生效日
    effective_date = position_data.EFFECTDATE
    if effective_date is None:
        raise RuntimeError('传输编号：' + trade_id + ',起始日为null')
    effective_date = datetime.strptime(effective_date, _DATE_FMT)
    # 过期日
    expire_date = position_data.EXPIREDATE
    if expire_date is None:
        raise RuntimeError('传输编号：' + trade_id + ',结束日为null')
    expire_date = datetime.strptime(expire_date, _DATE_FMT)
    # 结算日
    settle_date = expire_date
    # 结算方式
    specified_price = _SETTLE_CLOSE
    # 年化系数
    ann_val_ratio = position_data.ANNVALRATIO
    if not annualized:
        ann_val_ratio = None

    term = (expire_date - effective_date).days
    trade_origin_data['participation_rate'] = participation_rate
    trade_origin_data['counter_party'] = counter_party_name
    trade_origin_data['specified_price'] = specified_price
    trade_origin_data['effective_date'] = effective_date
    trade_origin_data['days_in_year'] = days_in_year
    trade_origin_data['product_type'] = product_type
    trade_origin_data['expire_date'] = expire_date
    trade_origin_data['settle_date'] = settle_date
    trade_origin_data['option_type'] = option_type
    trade_origin_data['annualized'] = annualized
    trade_origin_data['underlyer'] = underlyer
    trade_origin_data['direction'] = direction
    trade_origin_data['init_spot'] = init_spot
    trade_origin_data['notional'] = notional
    trade_origin_data['premium'] = premium
    trade_origin_data['strike'] = strike
    trade_origin_data['term'] = term
    trade_origin_data['ann_val_ratio'] = ann_val_ratio

    trade_origin_data['front_premium'] = 0
    trade_origin_data['minimum_premium'] = 0

    return trade_origin_data







