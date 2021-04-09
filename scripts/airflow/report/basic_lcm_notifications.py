# -*- coding: utf-8 -*-
from utils import utils
from datetime import datetime


def trade_notification_all(ip, headers):
    params = {}
    trades = utils.call_request(ip, "trade-service", "trdTradeLivedList", params, headers)
    if "error" != trades:
        trade_ids = []
        for trade in trades['result']:
            if trade['tradeStatus'] != 'LIVE':
                continue
            trade_id = trade['tradeId']
            notification_params = {
                'tradeId': trade_id,
                'validTime': str("2019-10-10T10:10:10")
            }
            try:
                utils.call_request(ip, "trade-service", "tradeLCMEventReGenerate", notification_params, headers)
                trade_ids.append(trade_id)
            except Exception:
                print("[" + trade_id + "],生成生命周期事件时发生错误")
        print("生命周期交易生成总条目数:" + str(len(trade_ids)) + ",交易编号列表:" + str(trade_ids))