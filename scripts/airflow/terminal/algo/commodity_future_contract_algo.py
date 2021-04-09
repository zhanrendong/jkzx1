from terminal.utils import DateTimeUtils
from terminal.utils import Logging
from terminal.dbo import FutureContractInfo

logging = Logging.getLogger(__name__)


class FutureContractInfoAlgo(object):
    @staticmethod
    def calc_one_future_contract_info(instruments_list, quote_close_list, contract_type, previous_trade_date):
        # pri, sec = get_query_future_in_sql(db_session, contract_type, trade_date)
        close_dict = {}
        if quote_close_list:
            quote_close_list.sort(key=lambda x: x.tradeDate, reverse=True)
            for close in quote_close_list:
                close_dict[str(close.tradeDate)] = close_dict.get(str(close.tradeDate), []) + [close]
        # 找到在指定交易日中有效的标的物
        valid_instruments = [x for x in instruments_list if (
                x.listedDate is not None and
                x.delistedDate is not None and
                x.delistedDate > previous_trade_date and
                x.listedDate <= previous_trade_date)]
        if valid_instruments is None:
            logging.error("合约类型%s在指定时间内未找到对应标的物" % contract_type)
            pri, sec = None, None
            return pri, sec
        else:
            instruments = []
            # 对有效标的物 取close表里找对应数据，没找到报错
            for instrument in valid_instruments:
                try:
                    if close_dict:
                        instrument_in_close = [x for x in close_dict.get(str(previous_trade_date),[])
                                               if x.instrumentId == instrument.instrumentId]
                    else:
                        instrument_in_close = []
                    if len(instrument_in_close) == 0:
                        logging.error("标的物%s在%s的close信息缺失！" % (instrument.instrumentId, str(previous_trade_date)))
                        # TODO: 如果收盘价信息缺失, 本来不应该算出主力, 应该直接返回 None, None
                        #  为了能画出vol surface曲面, 暂时continue, 如收盘价信息补上, 需要重新计算主力, 与vol surface
                        # pri, sec = None, None
                        # return pri, sec
                        continue
                    else:
                        if instrument_in_close and instrument_in_close[0].volume is not None:
                            instruments.append(instrument_in_close[0])
                        else:
                            logging.error('标的物没有成交量, %s, %s' % (instrument_in_close[0].instrumentId,
                                                                instrument_in_close[0].tradeDate))
                except Exception as e:
                    logging.error("查Instrument表时发生错误:%s" % e)

            if len(instruments) >= 2:
                # sort in instruments by volume
                instruments.sort(key=lambda x: x.volume, reverse=True)
                logging.info('合约%s共找到%d个Instruments' %
                             (contract_type, len(instruments)))
                # return first one and second one
                pri, sec = instruments[0].instrumentId, instruments[1].instrumentId

            elif len(instruments) == 1:
                logging.info('合约%s共找到%d个Instruments' % (contract_type, len(instruments)))
                pri, sec = instruments[0].instrumentId, None

            else:
                logging.info('合约%s共找到0个Instruments' % contract_type)
                pri, sec = None, None
        if pri:
            return pri, sec
        else:
            return None, None
