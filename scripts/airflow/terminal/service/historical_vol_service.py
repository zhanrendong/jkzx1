from terminal.service import TradingDayService
from .quote_close_service import QuoteCloseService
from terminal.dto import RealizedVolDTO, VolConeDTO, NeutralVolDTO, CustomException, DiagnosticDTO, DiagnosticType, \
    VolSurfaceInstanceType, VolSurfaceStrikeType
import logging
from terminal.algo import HistoricalVolAlgo
from terminal.dao import TradingCalendarRepo, VolSurfaceRepo
from datetime import date
from terminal.utils import DateTimeUtils


class HistoricalVolService:
    @staticmethod
    def calc_one_rolling_vol_by_quotes(quote_close_dto_dict, window):
        return_rate_dict = HistoricalVolService.to_return_rate_dict(quote_close_dto_dict)
        rolling_vol_dict = HistoricalVolAlgo.calc_one_rolling_vol(return_rate_dict, window)
        # 转化为DTO
        realized_vol_dto_list = []
        for trade_date in rolling_vol_dict:
            dto = RealizedVolDTO()
            dto.tradeDate = trade_date
            dto.vol = rolling_vol_dict.get(trade_date)
            dto.window = window
            realized_vol_dto_list.append(dto)
        return realized_vol_dto_list

    @staticmethod
    def calc_multiple_window_percentiles_by_quotes(start_date, end_date, quote_close_dto_dict, windows, percentiles):
        # 计算不同窗口的结果
        vol_cone_dict = {}
        return_rate_dict = HistoricalVolService.to_return_rate_dict(quote_close_dto_dict)
        for window in windows:
            vol_cone_dict[window] = HistoricalVolAlgo.calc_one_window_percentiles(start_date, end_date,
                                                                                  return_rate_dict, window, percentiles)
        # TODO: 需要返回数据库数据开始日期和结束日期
        vol_cone_dto_list = []
        # 转化为DTO
        for window in vol_cone_dict:
            vol_cone_dto = VolConeDTO()
            vol_cone_dto.window = window
            realized_vol_dict = vol_cone_dict.get(window)
            # 如果计算得到的结果为空，则直接跳过
            if realized_vol_dict is not None:
                for percentile in realized_vol_dict:
                    realized_vol_dto = RealizedVolDTO()
                    realized_vol_dto.percentile = percentile
                    realized_vol_dto.vol = realized_vol_dict.get(percentile)
                    vol_cone_dto.vols.append(realized_vol_dto)
            vol_cone_dto_list.append(vol_cone_dto)
        return vol_cone_dto_list

    @staticmethod
    def calc_instrument_realized_vol(db_session, instrument_id, trade_date, windows, is_primary=False):
        if trade_date > date.today():
            raise CustomException('所选日期不能超过今天')
        # TODO: 如果不是交易日，当前默认使用上一个交易日
        holidays = TradingCalendarRepo.get_holiday_list(db_session)
        start_date = DateTimeUtils.get_trading_day(trade_date, holidays, special_dates=[], step=max(windows),
                                                       direction=-1)
        quote_close_dto_dict, diagnostics = QuoteCloseService.get_instrument_quote_close_dict_by_period(
            db_session, instrument_id, start_date, trade_date, is_primary)
        realized_vol_dto_list = []
        for window in windows:
            return_rates = []
            # 判断是否为节假日, 非节假日start_date为: trade_date往前走window-1天, 节假日start_date为: trade_date往前走window天
            if DateTimeUtils.is_trading_day(trade_date, holidays):
                start_date = DateTimeUtils.get_trading_day(trade_date, holidays, special_dates=[], step=window - 1,
                                                           direction=-1)
            else:
                start_date = DateTimeUtils.get_trading_day(trade_date, holidays, special_dates=[], step=window,
                                                           direction=-1)
            # 只处理在窗口范围日期内的数据
            for key in quote_close_dto_dict:
                if start_date <= key <= trade_date:
                    quote_close = quote_close_dto_dict.get(key)
                    if quote_close is not None and quote_close.returnRate is not None:
                        return_rates.append(quote_close.returnRate)
            if len(return_rates) < window:
                logging.debug('窗口大小大于回报率个数，窗口大小：%d，回报率个数：%d' % (window, len(return_rates)))
            realized_vol = HistoricalVolAlgo.calc_one_realized_vol(return_rates)
            if realized_vol is None:
                # 如果计算得到的结果为None，直接跳过处理
                logging.debug('计算得到的值为空，窗口：%d' % window)
                continue
            dto = RealizedVolDTO()
            dto.window = window
            dto.vol = realized_vol
            realized_vol_dto_list.append(dto)
        return realized_vol_dto_list, diagnostics

    @staticmethod
    def calc_instrument_rolling_vol(db_session, instrument_id, start_date, end_date, window, is_primary=False):
        holidays = TradingDayService.get_holiday_list(db_session)
        # 判断是否时节假日
        if DateTimeUtils.is_trading_day(end_date, holidays):
            start_date = TradingDayService.go_trading_day(db_session, start_date, window - 1, direction=-1)
        else:
            start_date = TradingDayService.go_trading_day(db_session, start_date, window, direction=-1)
        quote_close_dto_dict, diagnostics = QuoteCloseService.get_instrument_quote_close_dict_by_period(
            db_session, instrument_id, start_date, end_date, is_primary)
        realized_vol_dto_list = HistoricalVolService.calc_one_rolling_vol_by_quotes(quote_close_dto_dict, window)
        return realized_vol_dto_list, diagnostics

    @staticmethod
    def calc_instrument_vol_cone(db_session, instrument_id, start_date, end_date, windows, percentiles,
                                 is_primary=False):
        origin_start_date = start_date
        holidays = TradingDayService.get_holiday_list(db_session)
        # 判断是否时节假日
        if DateTimeUtils.is_trading_day(end_date, holidays):
            start_date = TradingDayService.go_trading_day(db_session, start_date, max(windows) - 1, direction=-1)
        else:
            start_date = TradingDayService.go_trading_day(db_session, start_date, max(windows), direction=-1)
        quote_close_dto_dict, diagnostics = QuoteCloseService.\
            get_instrument_quote_close_dict_by_period(db_session, instrument_id, start_date, end_date, is_primary)
        vol_cone_dto_list = HistoricalVolService.\
            calc_multiple_window_percentiles_by_quotes(origin_start_date, end_date, quote_close_dto_dict, windows,
                                                       percentiles)
        return vol_cone_dto_list, diagnostics

    @staticmethod
    def to_return_rate_dict(quote_close_dto_dict):
        return_rate_dict = {}
        for key in quote_close_dto_dict:
            quote_close_dto = quote_close_dto_dict.get(key)
            if quote_close_dto is not None:
                return_rate_dict[key] = quote_close_dto.returnRate
            else:
                return_rate_dict[key] = None
        return return_rate_dict

    @staticmethod
    def calc_historical_and_neutral_vol_list(db_session, instrument_ids, start_date, end_date, window, is_primary):
        neutral_vol_list = []
        diagnostic_list = []
        for instrument_id in instrument_ids:
            try:
                realized_vol_dto_list, diagnostics = HistoricalVolService.calc_instrument_rolling_vol(
                    db_session, instrument_id, start_date, end_date, window, is_primary)
                vols = [realized_vol.vol for realized_vol in realized_vol_dto_list]
                # 需要从波动率曲面中取公允波动率
                if len(vols) > 0:
                    neutral_vol = HistoricalVolService.get_neutral_vol(db_session, instrument_id, end_date, window)
                    neutral_vol_list.append(NeutralVolDTO(instrument_id, min(vols), max(vols), neutral_vol))
                diagnostic_list.extend(diagnostics)
            except CustomException as e:
                message = '标的物%s无法获取历史波动率和公允波动率' % instrument_id
                diagnostic_list.append(DiagnosticDTO(DiagnosticType.ERROR, message, str(e)))
        return neutral_vol_list, diagnostic_list

    @staticmethod
    def get_neutral_vol(db_session, instrument_id, valuation_date, window):
        strike_type = VolSurfaceStrikeType.PERCENT.name
        instance = VolSurfaceInstanceType.CLOSE.name
        vol_surface_dbo = VolSurfaceRepo.\
            get_instrument_vol_surface(db_session, instrument_id, valuation_date, strike_type, instance)
        if vol_surface_dbo is None:
            logging.error('标的: %s在%s时没有找到vol surface' % (instrument_id, valuation_date))
            raise CustomException('标的: %s在%s时没有找到波动率曲面' % (instrument_id, valuation_date))
        instruments = vol_surface_dbo.modelInfo['instruments']
        # 获取公允波动率
        neutral_vol = None
        for instrument in instruments:
            if instrument['tenor'] == str(window) + 'D':
                for vol in instrument['vols']:
                    if vol['percent'] == 1:
                        neutral_vol = vol['quote']
                        break
                break
        if neutral_vol is None:
            logging.error('标的: %s在%s时找到波动率曲面, 但是无法计算公允波动率' % (instrument_id, valuation_date))
            raise CustomException('标的: %s在%s时找到波动率曲面, 但是无法计算公允波动率' % (instrument_id, valuation_date))
        return neutral_vol
