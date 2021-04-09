# -*- coding: utf-8 -*-

import datetime
import os
from dags.conf.settings import BCTSpecialConfig
from dags.dbo.db_model import create_db_session as Session
from dags.dao import OracleGoldSpotEODPricesRepo, OracleCommodityFuturesEODPricesRepo, OracleAShareEODPricesRepo
from dags.dao import OracleFuturesDescriptionRepo
from dags.dao import OracleAIndexDescriptionRepo
from dags.dao import OracleIndexFuturesEODPricesRepo
from dags.dao import OracleAIndexEODPricesRepo
from dags.dao import OracleAShareDescriptionRepo
from dags.dao import OracleGoldSpotDescriptionRepo
from terminal.dao import InstrumentRepo, QuoteCloseRepo
from terminal.dto import DataSourceType, InstrumentType, AssetClassType, OptionTradedType
from terminal import Logging, QuoteClose, Instrument
from terminal.utils import DateTimeUtils

os.environ['NLS_LANG'] = 'SIMPLIFIED CHINESE_CHINA.UTF8'
logger = Logging.getLogger(__name__)


class OTCMarketDataService:

    @staticmethod
    def convert_index_futures_to_quote_close(index_futures_eod_prices):
        """
        格式化万德指数期货每日收盘价为数据平台格式
        :param index_futures_eod_prices:
        :return:
        """
        quote_closes = []
        for index_futures_eod_price in index_futures_eod_prices:

            wind_code = index_futures_eod_price.S_INFO_WINDCODE.upper()
            close_price = index_futures_eod_price.S_DQ_CLOSE
            change = index_futures_eod_price.S_DQ_CHANGE
            pre_settle = index_futures_eod_price.S_DQ_PRESETTLE
            settle = index_futures_eod_price.S_DQ_SETTLE
            trade_date = DateTimeUtils.str2date(index_futures_eod_price.TRADE_DT, '%Y%m%d')

            try:
                if close_price is not None and change is not None:
                    pre_close_price = float(close_price) - float(change)
                else:
                    pre_close_price = float(pre_settle)
                if pre_close_price is None or pre_close_price == 0.0:
                    logger.error('时间为: %s,在index_futures_eod_price数据中没有前收盘价, %s' % (trade_date, wind_code))
                    continue
                if close_price is not None:
                    close_price = float(close_price)
                elif settle is not None:
                    close_price = float(settle)
                elif change is not None and pre_settle is not None:
                    close_price = float(pre_settle) + float(change)
                else:
                    logger.error('时间为: %s,在index_futures_eod_price数据中没有收盘价, %s' % (trade_date, wind_code))
                    continue
                return_rate = (close_price - pre_close_price) / pre_close_price

                item = QuoteClose(instrumentId=wind_code,
                                  tradeDate=trade_date,
                                  openPrice=index_futures_eod_price.S_DQ_OPEN,
                                  closePrice=close_price,
                                  highPrice=index_futures_eod_price.S_DQ_HIGH,
                                  lowPrice=index_futures_eod_price.S_DQ_LOW,
                                  settlePrice=index_futures_eod_price.S_DQ_SETTLE,
                                  volume=index_futures_eod_price.S_DQ_VOLUME,
                                  amount=index_futures_eod_price.S_DQ_AMOUNT,
                                  preClosePrice=pre_close_price,
                                  returnRate=return_rate,
                                  dataSource=DataSourceType.WIND.name,
                                  quoteTime=index_futures_eod_price.OPDATE,
                                  updatedAt=datetime.datetime.now())
                quote_closes.append(item)
            except Exception as e:
                logger.error('wind_code: %s 转化为quote_close出现异常，%s' % (wind_code, e))

        return quote_closes

    @staticmethod
    def convert_commodity_futures_to_quote_close(commodity_futures_eod_prices):
        """
        格式化万德大宗期货每日收盘价为数据平台格式
        :param commodity_futures_eod_prices:
        :return:
        """
        quote_closes = []
        for commodity_futures_eod_price in commodity_futures_eod_prices:

            wind_code = commodity_futures_eod_price.S_INFO_WINDCODE.upper()
            close_price = commodity_futures_eod_price.S_DQ_CLOSE
            change = commodity_futures_eod_price.S_DQ_CHANGE
            pre_settle = commodity_futures_eod_price.S_DQ_PRESETTLE
            settle = commodity_futures_eod_price.S_DQ_SETTLE
            trade_date = DateTimeUtils.str2date(commodity_futures_eod_price.TRADE_DT, '%Y%m%d')

            try:
                if close_price is not None and change is not None:
                    pre_close_price = float(close_price) - float(change)
                else:
                    pre_close_price = float(pre_settle)
                if pre_close_price is None or pre_close_price == 0.0:
                    logger.error('时间为: %s,在commodity_futures_eod_price数据中没有前收盘价, %s' % (trade_date, wind_code))
                    continue
                if close_price is not None:
                    close_price = float(close_price)
                elif settle is not None:
                    close_price = float(settle)
                elif change is not None and pre_settle is not None:
                    close_price = float(pre_settle) + float(change)
                else:
                    logger.error('时间为: %s,在commodity_futures_eod_price数据中没有收盘价, %s' % (trade_date, wind_code))
                    continue
                return_rate = (close_price - pre_close_price) / pre_close_price

                item = QuoteClose(instrumentId=wind_code,
                                  tradeDate=trade_date,
                                  openPrice=commodity_futures_eod_price.S_DQ_OPEN,
                                  closePrice=close_price,
                                  highPrice=commodity_futures_eod_price.S_DQ_HIGH,
                                  lowPrice=commodity_futures_eod_price.S_DQ_LOW,
                                  settlePrice=commodity_futures_eod_price.S_DQ_SETTLE,
                                  volume=commodity_futures_eod_price.S_DQ_VOLUME,
                                  amount=commodity_futures_eod_price.S_DQ_AMOUNT,
                                  preClosePrice=pre_close_price,
                                  dataSource=DataSourceType.WIND.name,
                                  returnRate=return_rate,
                                  quoteTime=commodity_futures_eod_price.OPDATE,
                                  updatedAt=datetime.datetime.now())
                quote_closes.append(item)
            except Exception as e:
                logger.error('wind_code: %s 转化为quote_close出现异常，%s' % (wind_code, e))

        return quote_closes

    @staticmethod
    def convert_gold_spot_to_quote_close(gold_spot_eod_prices):
        """
        格式化万德黄金现货每日收盘价为数据平台格式
        :param gold_spot_eod_prices:
        :return:
        """
        quote_closes = []
        for gold_spot_eod_price in gold_spot_eod_prices:

            wind_code = gold_spot_eod_price.S_INFO_WINDCODE.upper()
            trade_date = DateTimeUtils.str2date(gold_spot_eod_price.TRADE_DT, '%Y%m%d')

            try:
                if gold_spot_eod_price.S_DQ_CLOSE is None:
                    logger.error('时间为: %s,在c_gold_spot_eod_price数据中没有收盘价, %s' % (trade_date, wind_code))
                    continue
                if gold_spot_eod_price.S_PCT_CHG is None:
                    logger.error('时间为: %s,在c_gold_spot_eod_price数据中没有回报率, %s' % (trade_date, wind_code))
                    continue
                pre_close_price = float(gold_spot_eod_price.S_DQ_CLOSE) / (float(gold_spot_eod_price.S_PCT_CHG) + 1)

                item = QuoteClose(instrumentId=wind_code,
                                  tradeDate=trade_date,
                                  openPrice=gold_spot_eod_price.S_DQ_OPEN,
                                  closePrice=gold_spot_eod_price.S_DQ_CLOSE,
                                  highPrice=gold_spot_eod_price.S_DQ_HIGH,
                                  lowPrice=gold_spot_eod_price.S_DQ_LOW,
                                  settlePrice=gold_spot_eod_price.S_DQ_SETTLE,
                                  volume=gold_spot_eod_price.S_DQ_VOLUME,
                                  amount=gold_spot_eod_price.S_DQ_AMOUNT,
                                  preClosePrice=pre_close_price,
                                  dataSource=DataSourceType.WIND.name,
                                  returnRate=gold_spot_eod_price.S_PCT_CHG / 100,
                                  quoteTime=gold_spot_eod_price.OPDATE,
                                  updatedAt=datetime.datetime.now())
                quote_closes.append(item)
            except Exception as e:
                logger.error('wind_code: %s 转化为quote_close出现异常，%s' % (wind_code, e))

        return quote_closes

    @staticmethod
    def convert_a_share_to_quote_close(a_share_eod_prices):
        """
        格式化万德A股股票每日收盘价为数据平台格式
        :param a_share_eod_prices:
        :return:
        """
        quote_closes = []
        for a_share_eod_price in a_share_eod_prices:

            wind_code = a_share_eod_price.S_INFO_WINDCODE.upper()
            trade_date = DateTimeUtils.str2date(a_share_eod_price.TRADE_DT, '%Y%m%d')

            try:
                if a_share_eod_price.S_DQ_PRECLOSE is None:
                    logger.error('时间为: %s,在a_share_eod_price数据中没有前收盘价, %s' % (trade_date, wind_code))
                    continue
                if a_share_eod_price.S_DQ_CLOSE is None:
                    logger.error('时间为: %s,在a_share_eod_price数据中没有收盘价, %s' % (trade_date, wind_code))
                    continue
                if a_share_eod_price.S_DQ_PCTCHANGE is None:
                    logger.error('时间为: %s,在a_share_eod_price数据中没有回报率, %s' % (trade_date, wind_code))
                    continue

                item = QuoteClose(instrumentId=wind_code,
                                  tradeDate=trade_date,
                                  openPrice=a_share_eod_price.S_DQ_OPEN,
                                  closePrice=a_share_eod_price.S_DQ_CLOSE,
                                  highPrice=a_share_eod_price.S_DQ_HIGH,
                                  lowPrice=a_share_eod_price.S_DQ_LOW,
                                  settlePrice=None,
                                  volume=a_share_eod_price.S_DQ_VOLUME,
                                  amount=a_share_eod_price.S_DQ_AMOUNT,
                                  preClosePrice=a_share_eod_price.S_DQ_PRECLOSE,
                                  returnRate=a_share_eod_price.S_DQ_PCTCHANGE / 100,
                                  dataSource=DataSourceType.WIND.name,
                                  quoteTime=a_share_eod_price.OPDATE,
                                  updatedAt=datetime.datetime.now())
                quote_closes.append(item)
            except Exception as e:
                logger.error('wind_code: %s 转化为quote_close出现异常，%s' % (wind_code, e))

        return quote_closes

    @staticmethod
    def convert_a_index_to_quote_close(a_index_eod_prices):
        """
        格式化万德A股指数每日收盘价为数据平台格式
        :param a_index_eod_prices:
        :return:
        """
        quote_closes = []
        for a_index_eod_price in a_index_eod_prices:

            wind_code = a_index_eod_price.S_INFO_WINDCODE.upper()
            trade_date = DateTimeUtils.str2date(a_index_eod_price.TRADE_DT, '%Y%m%d')

            try:
                if a_index_eod_price.S_DQ_PRECLOSE is None:
                    logger.error('时间为: %s,在a_index_eod_price数据中没有前收盘价,%s' % (trade_date, wind_code))
                    continue
                if a_index_eod_price.S_DQ_CLOSE is None:
                    logger.error('时间为: %s,在a_index_eod_price数据中没有收盘价,%s' % (trade_date, wind_code))
                    continue
                if a_index_eod_price.S_DQ_PCTCHANGE is None:
                    logger.error('时间为: %s,在a_index_eod_price数据中没有回报率,%s' % (trade_date, wind_code))
                    continue

                item = QuoteClose(instrumentId=wind_code,
                                  tradeDate=trade_date,
                                  openPrice=a_index_eod_price.S_DQ_OPEN,
                                  closePrice=a_index_eod_price.S_DQ_CLOSE,
                                  highPrice=a_index_eod_price.S_DQ_HIGH,
                                  lowPrice=a_index_eod_price.S_DQ_LOW,
                                  settlePrice=None,
                                  volume=a_index_eod_price.S_DQ_VOLUME,
                                  amount=a_index_eod_price.S_DQ_AMOUNT,
                                  preClosePrice=a_index_eod_price.S_DQ_PRECLOSE,
                                  returnRate=a_index_eod_price.S_DQ_PCTCHANGE / 100,
                                  dataSource=DataSourceType.WIND.name,
                                  quoteTime=a_index_eod_price.OPDATE,
                                  updatedAt=datetime.datetime.now())
                quote_closes.append(item)
            except Exception as e:
                logger.error('wind_code: %s 转化为quote_close出现异常，%s' % (wind_code, e))

        return quote_closes

    @staticmethod
    def convert_a_index_model_to_instrument(wind_codes):
        """
        格式化a_index wind数据model
        :param wind_codes:
        :return:
        """
        instruments = []
        for wind_code in wind_codes:

            asset_class = AssetClassType.EQUITY.name
            instrument_type = InstrumentType.INDEX.name
            listed_date = DateTimeUtils.str2date(wind_code.S_INFO_LISTDATE, '%Y%m%d')
            delisted_date = DateTimeUtils.str2date(wind_code.EXPIRE_DATE, '%Y%m%d')

            item = Instrument(instrumentId=wind_code.S_INFO_WINDCODE.upper(),
                              instrumentType=instrument_type,
                              updatedAt=datetime.datetime.now(),
                              listedDate=listed_date,
                              delistedDate=delisted_date,
                              assetClass=asset_class,
                              dataSource=DataSourceType.WIND.name,
                              status='ACTIVE',
                              shortName=wind_code.S_INFO_NAME,
                              optionTradedType=OptionTradedType.OTC.name,
                              contractType=None)
            instruments.append(item)

        return instruments

    @staticmethod
    def convert_a_share_model_to_instrument(wind_codes):
        """
        格式化a_share wind数据model
        :param wind_codes:
        :return:
        """
        instruments = []
        for wind_code in wind_codes:

            asset_class = AssetClassType.EQUITY.name
            instrument_type = InstrumentType.STOCK.name
            listed_date = DateTimeUtils.str2date(wind_code.S_INFO_LISTDATE, '%Y%m%d')
            delisted_date = DateTimeUtils.str2date(wind_code.S_INFO_DELISTDATE, '%Y%m%d')

            item = Instrument(instrumentId=wind_code.S_INFO_WINDCODE.upper(),
                              instrumentType=instrument_type,
                              updatedAt=datetime.datetime.now(),
                              listedDate=listed_date,
                              delistedDate=delisted_date,
                              assetClass=asset_class,
                              dataSource=DataSourceType.WIND.name,
                              status='ACTIVE',
                              shortName=wind_code.S_INFO_NAME,
                              optionTradedType=OptionTradedType.OTC.name,
                              contractType=None)
            instruments.append(item)

        return instruments

    @staticmethod
    def convert_gold_spot_model_to_instrument(wind_codes):
        """
        格式化gold_spot wind数据model
        :param wind_codes:
        :return:
        """
        instruments = []
        for wind_code in wind_codes:

            asset_class = AssetClassType.COMMODITY.name
            instrument_type = InstrumentType.SPOT.name

            item = Instrument(instrumentId=wind_code.S_INFO_WINDCODE.upper(),
                              instrumentType=instrument_type,
                              updatedAt=datetime.datetime.now(),
                              listedDate=None,
                              delistedDate=None,
                              assetClass=asset_class,
                              dataSource=DataSourceType.WIND.name,
                              status='ACTIVE',
                              shortName=wind_code.S_INFO_NAME,
                              optionTradedType=OptionTradedType.OTC.name,
                              contractType=None)
            instruments.append(item)

        return instruments

    @staticmethod
    def convert_commodity_futures_model_to_instrument(wind_codes):
        """
        格式化commodity_futures wind数据model
        :param wind_codes:
        :return:
        """
        instruments = []
        for wind_code in wind_codes:

            asset_class = AssetClassType.COMMODITY.name
            instrument_type = InstrumentType.FUTURE.name
            listed_date = DateTimeUtils.str2date(wind_code.S_INFO_LISTDATE, '%Y%m%d')
            delisted_date = DateTimeUtils.str2date(wind_code.S_INFO_DELISTDATE, '%Y%m%d')

            item = Instrument(instrumentId=wind_code.S_INFO_WINDCODE.upper(),
                              instrumentType=instrument_type,
                              updatedAt=datetime.datetime.now(),
                              listedDate=listed_date,
                              delistedDate=delisted_date,
                              assetClass=asset_class,
                              dataSource=DataSourceType.WIND.name,
                              status='ACTIVE',
                              shortName=wind_code.S_INFO_NAME,
                              optionTradedType=OptionTradedType.OTC.name,
                              contractType=wind_code.FS_INFO_SCCODE)
            instruments.append(item)

        return instruments

    @staticmethod
    def convert_index_futures_model_to_instrument(wind_codes):
        """
        格式化index_futures wind数据model
        :param wind_codes:
        :return:
        """
        instruments = []
        for wind_code in wind_codes:
            asset_class = AssetClassType.EQUITY.name
            instrument_type = InstrumentType.INDEX_FUTURE.name
            listed_date = DateTimeUtils.str2date(wind_code.S_INFO_LISTDATE, '%Y%m%d')
            delisted_date = DateTimeUtils.str2date(wind_code.S_INFO_DELISTDATE, '%Y%m%d')

            item = Instrument(instrumentId=wind_code.S_INFO_WINDCODE.upper(),
                              instrumentType=instrument_type,
                              updatedAt=datetime.datetime.now(),
                              listedDate=listed_date,
                              delistedDate=delisted_date,
                              assetClass=asset_class,
                              dataSource=DataSourceType.WIND.name,
                              status='ACTIVE',
                              shortName=wind_code.S_INFO_NAME,
                              optionTradedType=OptionTradedType.OTC.name,
                              contractType=wind_code.FS_INFO_SCCODE)
            instruments.append(item)

        return instruments

    @staticmethod
    def get_active_instrument(instruments, quote_closes):
        """
        找出有行情的标的
        :param instruments: 万德标的
        :param quote_closes: 万德每日收盘价
        :return:
        """
        active_instruments, inactive_instruments = [], []
        instrument_ids = [quote_close.instrumentId for quote_close in quote_closes]
        for instrument in instruments:
            if instrument.instrumentId in instrument_ids:
                active_instruments.append(instrument)
            else:
                inactive_instruments.append(instrument)

        active_instrument_ids = [_.instrumentId for _ in active_instruments]
        inactive_instrument_ids = [_.instrumentId for _ in inactive_instruments]
        logger.info('有日行情的活跃标的长度为%s,没有日行情的不活跃标的长度为%s' %
                    (len(active_instrument_ids), len(inactive_instrument_ids)))
        return active_instruments, inactive_instruments

    @staticmethod
    def get_missing_instrument(instruments, all_instrument_ids):
        """
        找出数据平台缺失的标的
        :param instruments:
        :param all_instrument_ids:
        :return:
        """
        missing_instruments, existing_instruments = [], []
        for instrument in instruments:
            if instrument.instrumentId not in all_instrument_ids:
                missing_instruments.append(instrument)
            else:
                existing_instruments.append(instrument)
        missing_instrument_ids = [_.instrumentId for _ in missing_instruments]
        existing_instrument_ids = [_.instrumentId for _ in existing_instruments]
        logger.info('缺失的标的长度为%s,存在的标的长度为%s' %
                    (len(missing_instrument_ids), len(existing_instrument_ids)))

        return missing_instruments, existing_instruments

    @staticmethod
    def get_active_quote_close(instruments, quote_closes):
        """
        找出有行情的标的收盘价
        :param instruments: 万德标的
        :param quote_closes: 万德每日收盘价
        :return:
        """
        active_quote_closes, inactive_quote_closes = [], []
        instrument_ids = [instrument.instrumentId for instrument in instruments]
        for quote_close in quote_closes:
            if quote_close.instrumentId in instrument_ids:
                active_quote_closes.append(quote_close)
            else:
                inactive_quote_closes.append(quote_close)

        missing_quote_closes_ids = [_.instrumentId for _ in active_quote_closes]
        extra_quote_closes_ids = [_.instrumentId for _ in inactive_quote_closes]
        logger.info('活跃标的有日行情的长度为%s,不活跃标的没有日行情的长度为%s' %
                    (len(missing_quote_closes_ids), len(extra_quote_closes_ids)))
        return active_quote_closes, inactive_quote_closes

    @staticmethod
    def get_missing_quote_close(quote_closes, all_instrument_ids):
        """
        找出数据平台缺失的收盘价
        :param quote_closes:
        :param all_instrument_ids:
        :return:
        """
        missing_quote_closes, existing_quote_closes = [], []
        for quote_close in quote_closes:
            if quote_close.instrumentId not in all_instrument_ids:
                missing_quote_closes.append(quote_close)
            else:
                existing_quote_closes.append(quote_close)
        missing_quote_close_ids = [_.instrumentId for _ in missing_quote_closes]
        existing_quote_close_ids = [_.instrumentId for _ in existing_quote_closes]
        logger.info('缺失的标的收盘价长度为%s,多余的标的收盘价长度为%s' %
                    (len(missing_quote_close_ids), len(existing_quote_close_ids)))

        return missing_quote_closes, existing_quote_closes

    @staticmethod
    def save_quote_close(db_session, force_update, trade_date, active_quote_closes, all_instrument_ids):
        """
        保存收盘价到数据平台
        :param db_session:
        :param force_update:
        :param trade_date:
        :param active_quote_closes:
        :param all_instrument_ids:
        :return:
        """
        # 找出数据平台缺失的收盘价
        missing_quote_closes, extra_quote_closes = OTCMarketDataService.get_missing_quote_close(active_quote_closes,
                                                                                                all_instrument_ids)
        # 强制更新
        if force_update is False:
            # 保存收盘价到数据平台
            if len(missing_quote_closes) != 0:
                filtered_quote_closes = []
                for quote_close in missing_quote_closes:
                    try:
                        if quote_close.instrumentId.split('.')[1].upper() not in BCTSpecialConfig.suf_names:
                            filtered_quote_closes.append(quote_close)
                    except Exception as e:
                        logger.error('标的: %s格式不对, 标的中不含(.)符号, 错误为: %s' % (quote_close.instrumentId, e))
                logger.info('开始向quote_close表写入时间为: %s的数据, 共有 %d条新标的数据,标的为: %s' %
                            (trade_date, len(filtered_quote_closes),
                             [quote_close.instrumentId for quote_close in filtered_quote_closes]))
                db_session.add_all(filtered_quote_closes)
                db_session.commit()
                logger.info('成功将日期为: %s的数据导入到数据平台quote_close表' % trade_date)
            else:
                logger.info('在日期为: %s时没有数据可导入到数据平台quote_close表' % trade_date)
        else:
            if len(active_quote_closes) != 0:
                filtered_active_quote_closes = []
                for active_quote_close in active_quote_closes:
                    try:
                        if active_quote_close.instrumentId.split('.')[1].upper() not in BCTSpecialConfig.suf_names:
                            filtered_active_quote_closes.append(active_quote_close)
                    except Exception as e:
                        logger.error('标的: %s格式不对, 标的中不含(.)符号, 错误为: %s' % (active_quote_close.instrumentId, e))
                # 删除当天万德标的的收盘价
                instrument_ids = [quote_close.instrumentId for quote_close in filtered_active_quote_closes]
                QuoteCloseRepo.delete_all_instrument_quote_close_by_date(db_session, trade_date, instrument_ids)
                logger.info('开始向quote_close表写时间为: %s的数据, 共有 %d条新标的数据' % (trade_date, len(instrument_ids)))
                db_session.add_all(filtered_active_quote_closes)
                db_session.commit()
                logger.info('成功将日期为: %s的数据导入到数据平台quote_close表' % trade_date)
            else:
                logger.info('时间为: %s没有标的收盘价需要强制更新' % trade_date)

    @staticmethod
    def save_instrument(db_session, force_update, trade_date, active_instruments, all_instrument_ids):
        """
        保存数据到instrument表
        :param db_session:
        :param force_update:
        :param trade_date:
        :param active_instruments:
        :param all_instrument_ids:
        :return:
        """
        # 找出数据平台缺失的标的
        missing_instruments, existing_instrument = OTCMarketDataService.get_missing_instrument(active_instruments,
                                                                                               all_instrument_ids)
        # 是否强制更新
        if force_update is False:
            if len(missing_instruments) != 0:
                filtered_missing_instruments = []
                for missing_instrument in missing_instruments:
                    try:
                        if missing_instrument.instrumentId.split('.')[1].upper() not in BCTSpecialConfig.suf_names:
                            filtered_missing_instruments.append(missing_instrument)
                    except Exception as e:
                        logger.error('标的: %s格式不对, 标的中不含(.)符号, 错误为: %s' % (missing_instrument.instrumentId, e))
                logger.info('开始向instrument表写入trade_date为: %s的数据, 共有 %d 条新标的数据,标的为: %s' %
                            (trade_date, len(filtered_missing_instruments),
                             [instrument.instrumentId for instrument in filtered_missing_instruments]))
                # 保存标的到instrument表
                db_session.add_all(filtered_missing_instruments)
                db_session.commit()
                logger.info('成功将万德数据导入到数据平台instrument表')
            else:
                logger.info('trade_date为: %s时,没有新的数据需要写入到数据平台instrument表' % trade_date)
        else:
            if len(active_instruments) != 0:
                filtered_active_instruments = []
                for active_instrument in active_instruments:
                    try:
                        if active_instrument.instrumentId.split('.')[1].upper() not in BCTSpecialConfig.suf_names:
                            filtered_active_instruments.append(active_instrument)
                    except Exception as e:
                        logger.error('标的: %s格式不对, 标的中不含(.)符号, 错误为: %s' % (active_instrument.instrumentId, e))
                # 删除标的信息
                instrument_ids = [instrument.instrumentId for instrument in filtered_active_instruments]
                InstrumentRepo.delecte_instrumet_by_instrument_ids(db_session, instrument_ids)
                logger.info('开始重新向instrument表写标的, 共有 %d条新标的' % (len(instrument_ids)))
                # 重新写入标的信息
                db_session.add_all(filtered_active_instruments)
                db_session.commit()
                logger.info('成功的强制更新了数据平台instrument表')
            else:
                logger.info('时间为: %s, 没有标的需要强制更新' % trade_date)

    @staticmethod
    def filter_description(descriptions, eod_prices):
        """
        找description数据分类
        :param descriptions:
        :param eod_prices:
        :return:
        """
        instrument_ids = [_.S_INFO_WINDCODE for _ in eod_prices]
        filtered_descriptions = []
        for description in descriptions:
            if description.S_INFO_WINDCODE in instrument_ids:
                filtered_descriptions.append(description)

        return filtered_descriptions

    @staticmethod
    def save_data(db_session, instrument, wind_instrument):
        """
        保存数据
        :param db_session:
        :param instrument:
        :param wind_instrument:
        :return:
        """
        if wind_instrument.delistedDate is None:
            logger.debug('wind description表中标的: %s没有退市日期,不需要更新' % wind_instrument.instrumentId)
        else:
            if instrument.delistedDate != wind_instrument.delistedDate:
                logger.debug(
                    '开始更新标的: %s的退市日期, 退市日期为: %s' % (wind_instrument.instrumentId, wind_instrument.delistedDate))
                instrument.delistedDate = wind_instrument.delistedDate
                db_session.commit()
                logger.debug('更新标的: %s退市日期: %s成功' % (instrument.instrumentId, instrument.delistedDate))
            else:
                logger.debug('wind description表中标的: %s有退市日期,不需要更新' % wind_instrument.instrumentId)

    @staticmethod
    def update_instrument(db_session, fs_info_type):
        """
        更新instrument表退市日期
        :param db_session:
        :param fs_info_type:
        :return:
        """
        # 获取数据平台instrument表全量wind标的
        instruments = InstrumentRepo.get_instrument_by_source(db_session, DataSourceType.WIND.name)
        if len(instruments) != 0:

            wind_instruments = []
            # A股指数
            a_index_descriptions = OracleAIndexDescriptionRepo.get_all_a_index_description()
            a_index_instruments = OTCMarketDataService.convert_a_index_model_to_instrument(a_index_descriptions)
            # A股股票
            a_share_descriptions = OracleAShareDescriptionRepo.get_all_a_share_description()
            a_share_instruments = OTCMarketDataService.convert_a_share_model_to_instrument(a_share_descriptions)
            # 黄金现货
            gold_spot_descriptions = OracleGoldSpotDescriptionRepo.get_all_gold_spot_description()
            gold_spot_instruments = OTCMarketDataService. \
                convert_gold_spot_model_to_instrument(gold_spot_descriptions)
            # 期货
            futures_descriptions = OracleFuturesDescriptionRepo.get_futures_description_by_fs_info_type(fs_info_type)
            # 没有分类，futures_instruments数据的asset_clase 与 instrument_type字段数据不能使用
            futures_instruments = OTCMarketDataService. \
                convert_index_futures_model_to_instrument(futures_descriptions)
            wind_instruments.extend(a_index_instruments)
            wind_instruments.extend(a_share_instruments)
            wind_instruments.extend(gold_spot_instruments)
            wind_instruments.extend(futures_instruments)

            wind_instrument_ids = {}
            for wind_instrument in wind_instruments:
                wind_instrument_ids[wind_instrument.instrumentId] = wind_instrument
            extra_instrument_ids = []
            for index, instrument in enumerate(instruments):
                try:
                    # logger.info('当前进度为: %s, 当前进度为: %s of %d' % (index, index, len(instruments)))
                    if instrument.instrumentId in wind_instrument_ids:
                        wind_instrument = wind_instrument_ids[instrument.instrumentId]
                        OTCMarketDataService.save_data(db_session, instrument, wind_instrument)
                    else:
                        extra_instrument_ids.append(instrument.instrumentId)
                except Exception as e:
                    logger.error('更新标的: %s失败,错误为: %s' % (instrument, e))
            if len(extra_instrument_ids) != 0:
                logger.error('数据平台instrument表有多余的标的,这些标的在wind description表中是不存在的, 标的为: %s' %
                             extra_instrument_ids)
        else:
            logger.info('数据平台没有wind标的基本信息，无需更新')

    @staticmethod
    def get_wind_quote_closes(trade_date):
        """
        获取wind eod_price表数据并格式化
        :param trade_date:
        :return:
        """
        wind_quote_closes = []
        # A股指数收盘价
        a_index_eod_prices = OracleAIndexEODPricesRepo.get_a_index_eod_prices_by_date(trade_date)
        a_index_quote_closes = OTCMarketDataService.convert_a_index_to_quote_close(a_index_eod_prices)
        # A股股票收盘价
        a_share_eod_prices = OracleAShareEODPricesRepo.get_a_share_eod_prices_by_date(trade_date)
        a_share_quote_closes = OTCMarketDataService.convert_a_share_to_quote_close(a_share_eod_prices)
        # 黄金现货收盘价
        gold_spot_eod_prices = OracleGoldSpotEODPricesRepo.get_gold_spot_eod_prices_by_date(trade_date)
        gold_spot_quote_closes = OTCMarketDataService.convert_gold_spot_to_quote_close(gold_spot_eod_prices)
        # 商品期货收盘价
        commodity_futures_eod_prices = OracleCommodityFuturesEODPricesRepo. \
            get_commodity_futures_eod_prices_by_date(trade_date)
        commodity_futures_quote_closes = OTCMarketDataService. \
            convert_commodity_futures_to_quote_close(commodity_futures_eod_prices)
        # 指数期货收盘价
        index_futures_eod_prices = OracleIndexFuturesEODPricesRepo. \
            get_index_futures_eod_prices_by_date(trade_date)
        index_futures_quote_closes = OTCMarketDataService. \
            convert_index_futures_to_quote_close(index_futures_eod_prices)

        wind_quote_closes.extend(a_index_quote_closes)
        wind_quote_closes.extend(a_share_quote_closes)
        wind_quote_closes.extend(gold_spot_quote_closes)
        wind_quote_closes.extend(commodity_futures_quote_closes)
        wind_quote_closes.extend(index_futures_quote_closes)

        return wind_quote_closes

    @staticmethod
    def get_wind_instruments(fs_info_type, trade_date):
        """
        获取wind description表数据并格式化
        :param fs_info_type:
        :param trade_date:
        :return:
        """
        wind_instruments = []
        # A股指数
        a_index_descriptions = OracleAIndexDescriptionRepo.get_all_a_index_description()
        a_index_instruments = OTCMarketDataService.convert_a_index_model_to_instrument(a_index_descriptions)
        # A股股票
        a_share_descriptions = OracleAShareDescriptionRepo.get_all_a_share_description()
        a_share_instruments = OTCMarketDataService.convert_a_share_model_to_instrument(a_share_descriptions)
        # 黄金现货
        gold_spot_descriptions = OracleGoldSpotDescriptionRepo.get_all_gold_spot_description()
        gold_spot_instruments = OTCMarketDataService. \
            convert_gold_spot_model_to_instrument(gold_spot_descriptions)
        # 期货
        futures_descriptions = OracleFuturesDescriptionRepo. \
            get_futures_description_by_fs_info_type(fs_info_type)
        # 商品期货
        commodity_futures_eod_prices = OracleCommodityFuturesEODPricesRepo. \
            get_commodity_futures_eod_prices_by_date(trade_date)
        commodity_futures_descriptions = OTCMarketDataService. \
            filter_description(futures_descriptions, commodity_futures_eod_prices)
        commodity_futures_instruments = OTCMarketDataService. \
            convert_commodity_futures_model_to_instrument(commodity_futures_descriptions)
        # 指数期货
        index_futures_eod_prices = OracleIndexFuturesEODPricesRepo. \
            get_index_futures_eod_prices_by_date(trade_date)
        index_futures_descriptions = OTCMarketDataService. \
            filter_description(futures_descriptions, index_futures_eod_prices)
        index_futures_instruments = OTCMarketDataService. \
            convert_index_futures_model_to_instrument(index_futures_descriptions)

        wind_instruments.extend(a_index_instruments)
        wind_instruments.extend(a_share_instruments)
        wind_instruments.extend(gold_spot_instruments)
        wind_instruments.extend(commodity_futures_instruments)
        wind_instruments.extend(index_futures_instruments)

        return wind_instruments

    @staticmethod
    def backfill_otc_market_data(start_date, end_date, force_update=False):
        """
        根据传入的时间段,将万德市场行情数据导入到数据平台'
        :param start_date: 开始日期
        :param end_date: 结束日期
        :param force_update: 强制更新
        :return:
        """
        db_session = Session()
        try:
            # 过滤条件
            filters = {
                'fs_info_type': [1]  # 合约类型
            }
            fs_info_type = filters['fs_info_type']
            for index in range(0, (end_date - start_date).days + 1):
                trade_date = (start_date + datetime.timedelta(index))
                try:
                    """
                    获取数据平台数据
                    """
                    # 获取数据平台instrument表全量数据
                    all_instruments = InstrumentRepo.get_all_instrument(db_session)
                    all_instrument_ids = [instrument.instrumentId for instrument in all_instruments]
                    # 获取数据平台quote_close表当天全量数据
                    all_quote_closes = QuoteCloseRepo.get_all_instrument_quote_close_by_date(db_session, trade_date)
                    all_quote_close_ids = [quote_close.instrumentId for quote_close in all_quote_closes]
                    # 获取wind eod_prices数据
                    wind_quote_closes = OTCMarketDataService.get_wind_quote_closes(trade_date)
                    # 获取wind description数据
                    wind_instruments = OTCMarketDataService.get_wind_instruments(fs_info_type, trade_date)
                    """
                    保存wind descripitons 和 eod_prices 数据到数据平台
                    """
                    # 有日行情的标的
                    active_instruments, inactive_instruments = OTCMarketDataService. \
                        get_active_instrument(wind_instruments, wind_quote_closes)
                    # 保存标的
                    OTCMarketDataService. \
                        save_instrument(db_session, force_update, trade_date, active_instruments, all_instrument_ids)
                    # 标的存在日行情
                    active_quote_closes, inactive_quote_closes = OTCMarketDataService. \
                        get_active_quote_close(wind_instruments, wind_quote_closes)
                    # 保存日行情
                    OTCMarketDataService. \
                        save_quote_close(db_session, force_update, trade_date, active_quote_closes, all_quote_close_ids)
                except Exception as e:
                    logger.error('在trade_date: %s,处理数据失败: %s' % (trade_date, e))
            # 更新退市日期到instrument表
            OTCMarketDataService.update_instrument(db_session, fs_info_type)
        except Exception as e:
            logger.error('运行出错,错误信息为: %s' % e)
        finally:
            db_session.close()


if __name__ == '__main__':
    start_date, end_date = datetime.date(2019, 9, 10), datetime.date(2019, 9, 10)
    OTCMarketDataService.backfill_otc_market_data(start_date, end_date, False)
