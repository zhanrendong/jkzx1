# -*- coding: utf-8 -*-
from datetime import datetime
from sqlalchemy import or_
from trade_import.db_utils import *


def get_trans_code_list(oracle_session):
    # 查询TRANSCODE集合 （以position表数据为基准）
    trans_code_list = oracle_session.query(
        ROTCPosition.TRANSCODE,  # 0 (传输编号,为主)
    ).group_by(
        ROTCPosition.TRANSCODE
    ).all()
    return trans_code_list


def get_active_trans_code_list(oracle_session, cur_date):
    # 查询d当前日期未到期、未行权、未终止的TRANSCODE集合 （以position表数据为基准）
    trans_code_list = oracle_session.query(
        ROTCPosition.TRANSCODE
    ).filter(
        ROTCPosition.POSITIONDATE == cur_date,
        ROTCPosition.EXPIREDATE >= cur_date
    ).group_by(
        ROTCPosition.TRANSCODE
    ).all()
    return trans_code_list


def get_position_by_trans_code(oracle_session, trans_code):
    trans_position_list = oracle_session.query(
        ROTCPosition.RECORDID,  #（唯一标识，伪唯一）
        ROTCPosition.REPORTID,  # （报告编号）
        ROTCPosition.TRANSCODE,  # （传输编号，用于和交易表连接）
        ROTCPosition.COMPANYID,  # （上报公司）
        ROTCPosition.REPORTDATE,  # （上报日期）
        ROTCPosition.MAINBODYNAME,  #  (报送主体名称)
        ROTCPosition.ANALOGUECODE,  # （交易对手编号）
        ROTCPosition.ANALOGUENAME,  # （交易对手名称）
        ROTCPosition.POSITIONDATE,  # （持仓日期）
        ROTCPosition.TRANSCONFIRTIME,  # （交易确认时间）
        ROTCPosition.OPTEXERCTMTYPE,  # （行权时间类型）
        ROTCPosition.OPTRIGHTTYPE,  # （期权权利类型）
        ROTCPosition.RTVALMEPAY,  # （产品结构）
        ROTCPosition.UNDERASSTYPE,  #  （标的资产类型）
        ROTCPosition.UNDERASSVARIT,  #  （标的资产品种）
        ROTCPosition.STANDASSCONT,  # （标的资产对应合约）
        ROTCPosition.STATUS,  #（状态）
        ROTCPosition.SIDE,  # 填报方向）
        ROTCPosition.PARTICIPATERATE,  # （参与率）
        ROTCPosition.ANNUALIZED,  #  （年化否）
        ROTCPosition.STRIKE,  # （行权价格）
        ROTCPosition.VALUATIONSPOT,  # （合约估值时标的价格）
        ROTCPosition.TRADENOTIONAL,  # （总名义金额）
        ROTCPosition.TRADEQAUNTITY,  # （总名义数量）
        ROTCPosition.CLOSEDNOTIONAL,  # （已平仓名义金额）
        ROTCPosition.CLOSEDQUANTITY,  # （已平仓名义数量）
        ROTCPosition.PRICESYMBOL,  # （价格符号）
        ROTCPosition.EFFECTIVEDAY,  #  （一年有效天数）
    ).filter(
        ROTCPosition.STATUS == '1',
        ROTCPosition.RTVALMEPAY == 'VA',
        ROTCPosition.TRANSCODE == trans_code,
    ).order_by(
        ROTCPosition.REPORTDATE.desc(),
        ROTCPosition.RECORDID.desc()
    ).all()
    if len(trans_position_list) == 0 or trans_position_list is None:
        return None
    # 取最新一条持仓数据作为持仓基础数据
    position_data = trans_position_list[0]
    return position_data


def get_positions_by_trans_codes(oracle_session, trans_codes):
    trans_position_list = oracle_session.query(
        ROTCPosition.RECORDID,  #（唯一标识，伪唯一）
        ROTCPosition.REPORTID,  # （报告编号）
        ROTCPosition.TRANSCODE,  # （传输编号，用于和交易表连接）
        ROTCPosition.COMPANYID,  # （上报公司）
        ROTCPosition.REPORTDATE,  # （上报日期）
        ROTCPosition.MAINBODYNAME,  #  (报送主体名称)
        ROTCPosition.ANALOGUECODE,  # （交易对手编号）
        ROTCPosition.ANALOGUENAME,  # （交易对手名称）
        ROTCPosition.POSITIONDATE,  # （持仓日期）
        ROTCPosition.TRANSCONFIRTIME,  # （交易确认时间）
        ROTCPosition.OPTEXERCTMTYPE,  # （行权时间类型）
        ROTCPosition.OPTRIGHTTYPE,  # （期权权利类型）
        ROTCPosition.RTVALMEPAY,  # （产品结构）
        ROTCPosition.UNDERASSTYPE,  #  （标的资产类型）
        ROTCPosition.UNDERASSVARIT,  #  （标的资产品种）
        ROTCPosition.STANDASSCONT,  # （标的资产对应合约）
        ROTCPosition.STATUS,  #（状态）
        ROTCPosition.SIDE,  # 填报方向）
        ROTCPosition.PARTICIPATERATE,  # （参与率）
        ROTCPosition.ANNUALIZED,  #  （年化否）
        ROTCPosition.STRIKE,  # （行权价格）
        ROTCPosition.VALUATIONSPOT,  # （合约估值时标的价格）
        ROTCPosition.TRADENOTIONAL,  # （总名义金额）
        ROTCPosition.TRADEQAUNTITY,  # （总名义数量）
        ROTCPosition.CLOSEDNOTIONAL,  # （已平仓名义金额）
        ROTCPosition.CLOSEDQUANTITY,  # （已平仓名义数量）
        ROTCPosition.PRICESYMBOL,  # （价格符号）
        ROTCPosition.EFFECTIVEDAY,  # （一年有效天数）
        ROTCPosition.EFFECTDATE,    # 生效日
        ROTCPosition.EXPIREDATE,    # 到期日
        ROTCPosition.ANNVALRATIO    # 年化系数
    ).filter(
        ROTCPosition.STATUS == '1',
        ROTCPosition.RTVALMEPAY == 'VA',
        ROTCPosition.TRANSCODE.in_(trans_codes),
    ).order_by(
        ROTCPosition.REPORTDATE.desc(),
        ROTCPosition.RECORDID.desc()
    ).all()
    if trans_position_list is None or len(trans_position_list) == 0:
        return {}
    # 取最新一条持仓数据作为持仓基础数据
    position_data = {}
    for position in trans_position_list:
        if position_data.get(position.TRANSCODE) is None:
            position_data[position.TRANSCODE] = position
    return position_data


def get_trade_data_by_position_data(oracle_session, position_data):
    trans_code = position_data.TRANSCODE
    report_date = position_data.REPORTDATE
    option_type = position_data.OPTRIGHTTYPE
    product_type = position_data.RTVALMEPAY
    trade_data_list = oracle_session.query(
        ROTCTradeData.RECORDID,  # (唯一标识,伪唯一)
        ROTCTradeData.REPORTID,  # (报告编号)
        ROTCTradeData.TRANSCODE,  # (传输编号,用于和交易表连接)
        ROTCTradeData.REPORTDATE,  # (报告日期)
        ROTCTradeData.EFFECTDATE,  # （生效日）
        ROTCTradeData.EXPIREDATE,  # （过期日）
        ROTCTradeData.EXERCISEDATE,  #  （结算日）
        ROTCTradeData.DIRECTREPPARTY,  # （买卖方向）
        ROTCTradeData.OPTRIGHTTYPE,  # （期权权利类型）
        ROTCTradeData.RTVALMEPAY,  # （产品类型）
        ROTCTradeData.OPTEXERCTMTYPE,  #(期权执行时间类型)
        ROTCTradeData.EXECUTPRICE,  # （执行价格）
        ROTCTradeData.ASSENTRYPRICE,  # （标的资产进场价格）
        ROTCTradeData.NOMINALAMOUNT,  # （总名义金额）
        ROTCTradeData.PREMIUMAMOUNT,  #  （期权费）
        ROTCTradeData.SETTMETHOD,  #  （结算方式）
        ROTCTradeData.OPERTYPE,  #  (操作方式)
        ROTCTradeData.SETTPRIMETHED,  #   (结算价确认方式)
        ROTCTradeData.PARTICIPATIONRATE,
        ROTCTradeData.ANNUALIZED,
    ).filter(
        ROTCTradeData.STATUS == '1',
        ROTCTradeData.RTVALMEPAY == 'VA',
        ROTCTradeData.TRANSCODE == trans_code,
        # ROTCTradeData.OPTRIGHTTYPE == option_type,
        # or_(ROTCTradeData.OPERTYPE == 'NT', ROTCTradeData.OPERTYPE == '新交易')
    ).order_by(
        ROTCTradeData.REPORTDATE,
        ROTCTradeData.RECORDID
    ).all()

    if len(trade_data_list) == 0 or trade_data_list is None:
        return None
    trade_data = trade_data_list[0]
    return trade_data


def get_trade_datas_by_trans_codes(oracle_session, trans_codes):
    trade_data_list = oracle_session.query(
        ROTCTradeData.RECORDID,  # (唯一标识,伪唯一)
        ROTCTradeData.REPORTID,  # (报告编号)
        ROTCTradeData.TRANSCODE,  # (传输编号,用于和交易表连接)
        ROTCTradeData.REPORTDATE,  # (报告日期)
        ROTCTradeData.EFFECTDATE,  # （生效日）
        ROTCTradeData.EXPIREDATE,  # （过期日）
        ROTCTradeData.EXERCISEDATE,  #  （结算日）
        ROTCTradeData.DIRECTREPPARTY,  # （买卖方向）
        ROTCTradeData.OPTRIGHTTYPE,  # （期权权利类型）
        ROTCTradeData.RTVALMEPAY,  # （产品类型）
        ROTCTradeData.OPTEXERCTMTYPE,  #(期权执行时间类型)
        ROTCTradeData.EXECUTPRICE,  # （执行价格）
        ROTCTradeData.ASSENTRYPRICE,  # （标的资产进场价格）
        ROTCTradeData.NOMINALAMOUNT,  # （总名义金额）
        ROTCTradeData.PREMIUMAMOUNT,  #  （期权费）
        ROTCTradeData.SETTMETHOD,  #  （结算方式）
        ROTCTradeData.OPERTYPE,  #  (操作方式)
        ROTCTradeData.SETTPRIMETHED,  #   (结算价确认方式)
        ROTCTradeData.PARTICIPATIONRATE,
        ROTCTradeData.ANNUALIZED,
    ).filter(
        ROTCTradeData.STATUS == '1',
        ROTCTradeData.RTVALMEPAY == 'VA',
        ROTCTradeData.TRANSCODE.in_(trans_codes),
        # ROTCTradeData.OPTRIGHTTYPE == option_type,
        # or_(ROTCTradeData.OPERTYPE == 'NT', ROTCTradeData.OPERTYPE == '新交易')
    ).order_by(
        ROTCTradeData.REPORTDATE,
        ROTCTradeData.RECORDID
    ).all()

    if trade_data_list is None or len(trade_data_list) == 0:
        return {}
    trade_data = {}
    for trade in trade_data_list:
        if trade_data.get(trade.TRANSCODE) is None:
            trade_data[trade.TRANSCODE] = trade
    return trade_data


# 数据统计
def get_origin_data():
    oracle_session = get_session()
    try:
        no_match_list = []
        one_match_list = []
        many_match_list = []

        no_match_count = 0
        one_match_count = 0
        many_match_count = 0
        # 查询TRANSCODE集合 （以position表数据为基准）
        trans_code_list = oracle_session.query(
            ROTCPosition.TRANSCODE,  # 0 (传输编号,为主)
        ).group_by(
            ROTCPosition.TRANSCODE
        ).all()
        for trans_code_item in trans_code_list:
            # 查询最近一条持仓数据（TRANSCODE为主键,先按报告日期[REPORTDATE]倒序，再按记录编号[RECORDID]倒序）
            trans_code = trans_code_item[0]
            trans_position_list = oracle_session.query(
                ROTCPosition.RECORDID,      # 0 （唯一标识，伪唯一）
                ROTCPosition.REPORTID,      # 1 （报告编号）
                ROTCPosition.TRANSCODE,     # 2 （传输编号，用于和交易表连接）
                ROTCPosition.COMPANYID,     # 3 （上报公司）
                ROTCPosition.REPORTDATE,    # 4 （上报日期）
                ROTCPosition.MAINBODYNAME,  # 5  (报送主体名称)
                ROTCPosition.ANALOGUECODE,  # 6 （交易对手编号）
                ROTCPosition.ANALOGUENAME,  # 7 （交易对手名称）
                ROTCPosition.POSITIONDATE,  # 8 （持仓日期）
                ROTCPosition.TRANSCONFIRTIME,   # 9 （交易确认时间）
                ROTCPosition.OPTEXERCTMTYPE,    # 10 （行权时间类型）
                ROTCPosition.OPTRIGHTTYPE,      # 11 （期权权利类型）
                ROTCPosition.RTVALMEPAY,     # 12 （产品结构）
                ROTCPosition.UNDERASSTYPE,   # 13 （标的资产类型）
                ROTCPosition.UNDERASSVARIT,  # 14 （标的资产品种）
                ROTCPosition.STANDASSCONT,  # 15 （标的资产对应合约）
                ROTCPosition.STATUS,        # 16 （状态）
                ROTCPosition.SIDE,          # 17 （填报方向）
                ROTCPosition.PARTICIPATERATE,  # 18 （参与率）
                ROTCPosition.ANNUALIZED,       # 19 （年化否）
                ROTCPosition.STRIKE,           # 20 （行权价格）
                ROTCPosition.VALUATIONSPOT,    # 21 （合约估值时标的价格）
                ROTCPosition.TRADENOTIONAL,    # 22 （总名义金额）
                ROTCPosition.TRADEQAUNTITY,    # 23 （总名义数量）
                ROTCPosition.CLOSEDNOTIONAL,   # 24 （已平仓名义金额）
                ROTCPosition.CLOSEDQUANTITY,   # 25 （已平仓名义数量）
                ROTCPosition.PRICESYMBOL,      # 26 （价格符号）
                ROTCPosition.EFFECTIVEDAY,     # 27 （一年有效天数）
            ).filter(
                ROTCPosition.TRANSCODE == trans_code,
                ROTCPosition.STATUS == '1'
            ).order_by(
                ROTCPosition.REPORTDATE.desc(),
                ROTCPosition.RECORDID.desc()
            ).all()
            # 取最新一条持仓数据作为持仓基础数据
            position_data = trans_position_list[0]

            report_date = position_data[4]
            option_type = position_data[11]
            product_type = position_data[12]
            trade_data_list = oracle_session.query(
                ROTCTradeData.RECORDID,      # 0 (唯一标识,伪唯一)
                ROTCTradeData.REPORTID,      # 1 (报告编号)
                ROTCTradeData.TRANSCODE,     # 2 (传输编号,用于和交易表连接)
                ROTCTradeData.REPORTDATE,    # 3 (报告日期)
                ROTCTradeData.EFFECTDATE,    # 4 （生效日）
                ROTCTradeData.EXPIREDATE,    # 5 （过期日）
                ROTCTradeData.EXERCISEDATE,  # 6 （结算日）
                ROTCTradeData.DIRECTREPPARTY,  # 7 （买卖方向）
                ROTCTradeData.OPTRIGHTTYPE,    # 8 （期权权利类型）
                ROTCTradeData.RTVALMEPAY,      # 9 （产品类型）
                ROTCTradeData.EXECUTPRICE,     # 10 （执行价格）
                ROTCTradeData.ASSENTRYPRICE,   # 11 （标的资产进场价格）
                ROTCTradeData.NOMINALAMOUNT,   # 12 （总名义金额）
                ROTCTradeData.PREMIUMAMOUNT,   # 13 （期权费）
                ROTCTradeData.SETTMETHOD,      # 14 （结算方式）
                ROTCTradeData.OPERTYPE,        # 15  (操作方式)
                ROTCTradeData.SETTPRIMETHED,   # 16  (结算价确认方式)
                ROTCTradeData.PARTICIPATIONRATE,
            ).filter(
                ROTCTradeData.TRANSCODE == trans_code,
                ROTCTradeData.RTVALMEPAY == product_type,
                ROTCTradeData.OPTRIGHTTYPE == option_type,
                or_(ROTCTradeData.OPERTYPE == 'NT', ROTCTradeData.OPERTYPE == '新交易')
            ).order_by(
                ROTCTradeData.REPORTDATE.desc(),
                ROTCTradeData.RECORDID.desc()
            ).all()
            # 从成交表中获取数据（TRANSCODE为主键,先按报告日期[REPORTDATE]倒序，再按记录编号[RECORDID]倒序）
            trade_data = None
            if len(trade_data_list) == 0 or trade_data_list is None:
                no_match_count = no_match_count + 1
                no_match_list.append(trans_code)
                continue
            if len(trade_data_list) == 1:
                one_match_count = one_match_count + 1
                one_match_list.append(trans_code)
                trade_data = trade_data_list[0]
            else:
                many_match_count = many_match_count + 1
                many_match_list.append(trans_code)
                trade_data = trade_data_list[0]

        print("零匹配数据：" + str(no_match_count))
        print("唯一匹配数据：" + str(one_match_count))
        print("多重匹配数据：" + str(many_match_count))
    except Exception as error:
        print(error)
        oracle_session.rollback()
    finally:
        oracle_session.close()


