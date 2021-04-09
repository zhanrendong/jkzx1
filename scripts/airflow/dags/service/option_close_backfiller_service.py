from dags.service.base_backfiller_service import BaseBackfillerService
from terminal.utils import Logging
from datetime import date, datetime
from dags.dbo import create_db_session
from terminal.dao import TradingCalendarRepo, InstrumentRepo
from dags.dbo import OptionClose
from dags.dto import BreakType
from dags.dao import OptionCloseBackfillerRepo

logging = Logging.getLogger(__name__)


class OptionCloseBackfillerService(BaseBackfillerService):
    def backfill_one_instrument(self, params):
        instrument, start_date, end_date, holidays, force_update = params
        logging.info("开始Backfill标的物: %s, startDate: %s, endDate: %s" % (instrument.instrumentId,
                                                                        start_date.strftime('%Y%m%d'),
                                                                        end_date.strftime('%Y%m%d')))
        db_session = create_db_session()
        try:
            special_dates = TradingCalendarRepo.load_special_dates(db_session, instrument)
            option_close_breaks = self.get_recon_breaks(db_session, start_date, end_date, instrument)
            ret = self.backfill_multiple_option_close(db_session, instrument, option_close_breaks, holidays,
                                                      special_dates)
            return instrument, ret
        except Exception as e:
            return instrument, False
        finally:
            db_session.close()

    def backfill_multiple_option_close(self, db_session, instrument, option_close_breaks, holidays, special_dates,
                                       force_update=False):
        logging.info("开始option backfilling")
        # 计算成功和失败的条数
        missing_success_count = 0
        missing_failed_count = 0
        try:
            # TODO 将Break按类型分组， MISSING or EXTRA
            missing_breaks = []
            for item in option_close_breaks:
                if item.breakType == BreakType.MISSING.name:
                    missing_breaks.append(item)

            for item in missing_breaks:
                ret = self.backfill_one_option_close(db_session, instrument, item, force_update)
                if ret is True:
                    missing_success_count = missing_success_count + 1
                else:
                    missing_failed_count = missing_failed_count + 1
                    logging.info("当前处理成功：%d of %d，失败：%d of %d Break" % (missing_success_count, len(missing_breaks),
                                                                        missing_failed_count, len(missing_breaks)))

            if missing_failed_count != 0:
                errMsg = "Backfill结束，处理成功：%d of %d，失败：%d of %d Break" % (missing_success_count, len(missing_breaks),
                                                                         missing_failed_count, len(missing_breaks))
                logging.error(errMsg)
                return False
            return True
        except Exception as e:
            logging.error("处理失败 %s" % e)
            return False

    def backfill_one_option_close(self, db_session, instrument, option_close_break, force_update):
        try:
            close_price = self.calc_one_option_close(db_session, instrument, option_close_break)
            option_close = OptionClose()
            option_close.tradeDate = option_close_break.tradeDate
            option_close.instrumentId = instrument.instrumentId
            option_close.closePrice = close_price
            option_close.updatedAt = datetime.now()
            upsert_flag = self.upsert_one_option_close(db_session, option_close, force_update)
            return upsert_flag
        except Exception as e:
            logging.error(
                '处理单条标的物数据失败：[%s, %s]' % (
                    option_close_break.instrumentId, option_close_break.tradeDate.strftime('%Y%m%d')))
            return False

    @staticmethod
    def get_tick_data(db_session, instrument, option_close_break):
        return OptionCloseBackfillerRepo.get_tick_data(db_session, instrument, option_close_break)

    @staticmethod
    def calc_one_option_close(db_session, instrument, option_close_break):
        data = OptionCloseBackfillerService.get_tick_data(db_session, instrument, option_close_break)
        close_sum = 0
        trade_num = 0
        if len(data) == 1 or len(data) == 2:
            # 停牌情况
            try:
                close_price = data[0]['prevClosePrice']
            except Exception as e:
                close_price = data[0]['preClosePrice']
            return close_price
        try:
            for item in data:
                ask = item.get('askPrice1', 0)
                bid = item.get('bidPrice1', 0)
                if ask != 0 and bid != 0:
                    close_sum += (ask + bid) / 2
                    trade_num += 1
                elif ask == 0 and bid != 0:
                    close_sum += bid
                    trade_num += 1
                elif bid == 0 and ask != 0:
                    close_sum += ask
                    trade_num += 1
                else:
                    logging.info("%s在%s下ask,bid都为零" % (instrument.instrumentId, option_close_break.tradeDate))
            if trade_num != 0:
                close_price = close_sum / trade_num
            else:
                logging.info("按照官方提供closePrice处理")
                close_price = data[-1]['closePrice']
        except Exception as e:
            logging.info("%s在%s下无数据, 按昨日收盘价处理" % (instrument.instrumentId, option_close_break.tradeDate))
            logging.error(e)
            close_price = data[0]['prevClosePrice']
        return close_price

    @staticmethod
    def get_recon_breaks(db_session, start_date, end_date, instrument=None):
        return OptionCloseBackfillerRepo.get_option_close_recon_breaks(db_session, start_date, end_date, instrument)

    @staticmethod
    def upsert_one_option_close(db_session, option_close, force_update):
        return OptionCloseBackfillerRepo.upsert_one_option_close(db_session, option_close, force_update)

    def load_instruments(self, db_session, active_only):
        return InstrumentRepo.load_instruments_in_option_conf(db_session, active_only)


if __name__ == '__main__':
    OptionCloseBackfillerService().backfill_all_instruments(start_date=date(2019, 8, 26), end_date=date(2019, 8, 26),
                                                            active_only=True,
                                                            force_update=False)
