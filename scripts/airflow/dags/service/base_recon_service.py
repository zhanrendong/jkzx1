from dags.dbo import create_db_session
from datetime import timedelta
from terminal.utils import Logging
from concurrent.futures import ThreadPoolExecutor, as_completed
from abc import abstractmethod
from terminal.dao import TradingCalendarRepo, InstrumentRepo

logging = Logging.getLogger(__name__)


class BaseReconService:
    @abstractmethod
    def get_milestone(self, db_session, instrument):
        pass

    @abstractmethod
    def upsert_milestone(self, db_session, instrument, start_date, end_date, milestone):
        pass

    @abstractmethod
    def upsert_missing_trading_dates(self, db_session, instrument, missing_trading_dates):
        pass

    @staticmethod
    def get_missing_trading_dates(instrument, unchecked_trading_dates, existing_trading_dates):
        # TODO 可以优化效率
        missing_trading_dates = []
        for trading_date in unchecked_trading_dates:
            if trading_date in existing_trading_dates:
                continue
            missing_trading_dates.append(trading_date)
        logging.info("Recon缺失的数据天数为：%s -> %d 个Break" % (instrument.instrumentId, len(missing_trading_dates)))
        logging.info("Recon找到的Break为：%s -> %s" %
                     (instrument.instrumentId, ','.join([date.strftime('%Y%m%d') for date in missing_trading_dates])))
        return missing_trading_dates

    @staticmethod
    def get_unchecked_trading_dates(start_date, end_date, milestone, holidays, special_dates, ignore_milestone):
        results = []
        while start_date <= end_date:
            if (start_date in holidays) \
                    or (start_date in special_dates) \
                    or (start_date.weekday() == 5 or start_date.weekday() == 6) \
                    or (
                    not ignore_milestone and milestone is not None and milestone.startDate <= start_date <= milestone.endDate):
                start_date += timedelta(days=1)
                continue
            results.append(start_date)
            start_date += timedelta(days=1)
        logging.debug("当前的未检测的交易日为：%s" % ','.join([date.strftime('%Y%m%d') for date in results]))
        return results

    @abstractmethod
    def get_existing_trading_dates(self, db_session, instrument):
        pass

    def recon_one_instrument(self, params):
        # 返回True或Fasle, 当前标的物是Clean则返回True
        instrument, start_date, end_date, holidays, dry_run, ignore_milestone = params
        db_session = create_db_session()
        try:
            logging.info("开始Recon标的物: %s" % instrument.instrumentId)
            # 获取标的物Milestone
            milestone = self.get_milestone(db_session, instrument)
            # 只处理标的物上市日期和退市日期之间的数据
            if instrument.listedDate is not None and start_date < instrument.listedDate:
                start_date = instrument.listedDate
            if instrument.delistedDate is not None and end_date > instrument.delistedDate:
                end_date = instrument.delistedDate
            # 计算需要Recon的日期，跳过节假日、周末和特殊日期
            special_dates = TradingCalendarRepo.load_special_dates(db_session, instrument)
            unchecked_trading_dates = self.get_unchecked_trading_dates(
                start_date, end_date, milestone, holidays, special_dates, ignore_milestone)
            # 获取标的物已经拥有的交易日
            existing_trading_dates = self.get_existing_trading_dates(db_session, instrument)
            # 计算出缺失数据
            missing_trading_dates = self.get_missing_trading_dates(instrument, unchecked_trading_dates,
                                                                   existing_trading_dates)
            # 对缺数数据进行处理
            if not dry_run:
                self.upsert_missing_trading_dates(db_session, instrument, missing_trading_dates)
            # TODO 需要对多余的数据进行处理
            # 计算出多余数据
            extra_trading_dates = []
            logging.info("Recon找到多余数据：%s -> %d 个Break" % (instrument.instrumentId, len(extra_trading_dates)))
            # 如果没有差值，则比较开始日期和Milestone，更新Milestone
            has_missing_dates = len(missing_trading_dates) != 0
            has_extra_dates = len(extra_trading_dates) != 0
            if (not has_missing_dates) and (not has_extra_dates) and (not dry_run):
                if start_date <= end_date:
                    self.upsert_milestone(db_session, instrument, start_date, end_date, milestone)
                else:
                    logging.info("标的物开始日期大于结束日期，不更新Milestone：%s [%s %s]" % (
                        instrument.instrumentId, start_date.strftime('%Y%m%d'), end_date.strftime('%Y%m%d')))

            return instrument, (not has_missing_dates) and (not has_extra_dates)
        except Exception as e:
            logging.error("Recon失败，标的物：%s, 异常：%s" % (instrument.instrumentId, e))
            return instrument, False
        finally:
            db_session.close()

    def load_instruments(self, db_session, active_only):
        return InstrumentRepo.get_active_instruments(db_session, active_only)

    def recon_all_instrument(self, start_date, end_date, fail_fast=False, dry_run=True, active_only=True,
                             ignore_milestone=False):
        logging.info("开始recon, %s %s" % (start_date.strftime('%Y%m%d'), end_date.strftime('%Y%m%d')))
        # 获取所有需要Recon的标的物，只处理状态为Active的标的物
        db_session = create_db_session()
        # 获取标的物
        all_instruments = self.load_instruments(db_session, active_only)
        logging.info("当前需要Recon的标的物总数为: %d:" % len(all_instruments))
        holidays = TradingCalendarRepo.get_holiday_list(db_session)
        db_session.close()
        # 并发进行recon
        with ThreadPoolExecutor(max_workers=8) as executor:
            # 开始并行向远程服务器请求数据
            all_task = [executor.submit(self.recon_one_instrument, (item, start_date, end_date, holidays, dry_run,
                                                                    ignore_milestone)) for item in all_instruments]
            # 处理Recon结果
            success_instruments = []
            failed_instruments = []
            for future in as_completed(all_task):
                instrument, status = future.result()
                if status is True:
                    success_instruments.append(instrument)
                else:
                    failed_instruments.append(instrument)
                logging.info("标的物处理结束: %s，处理结果为：%s" % (instrument.instrumentId, status))
                logging.info("当前处理成功的标的物：%d of %d，失败的标的物：%d of %d" %
                             (len(success_instruments), len(all_instruments), len(failed_instruments),
                              len(all_instruments)))
            if len(failed_instruments) != 0:
                err_msg = "处理失败的标的物为：%s" % ','.join([instrument.instrumentId for instrument in failed_instruments])
                logging.info(err_msg)
                if fail_fast:
                    raise Exception(err_msg)
            else:
                logging.info("完成Recon成功")
