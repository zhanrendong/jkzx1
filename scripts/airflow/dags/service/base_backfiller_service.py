from dags.dbo import create_db_session
from concurrent.futures import ThreadPoolExecutor, as_completed
from terminal.utils import Logging
from abc import abstractmethod
from terminal.dao import TradingCalendarRepo, InstrumentRepo

logging = Logging.getLogger(__name__)


class BaseBackfillerService:
    @abstractmethod
    def backfill_one_instrument(params):
        pass

    def load_instruments(self, db_session, active_only):
        return InstrumentRepo.get_active_instruments(db_session, active_only)

    def backfill_all_instruments(self, start_date, end_date, active_only=True, force_update=False):
        db_session = create_db_session()
        # 获取标的物
        all_instruments = self.load_instruments(db_session, active_only)
        logging.info("当前需要Backfill的标的物总数为: %d:" % len(all_instruments))
        holidays = TradingCalendarRepo.get_holiday_list(db_session)
        db_session.close()
        # 并发进行 backfiller
        with ThreadPoolExecutor(max_workers=8) as executor:
            # 开始并行向远程服务器请求数据
            all_task = [executor.submit(self.backfill_one_instrument, (item, start_date, end_date, holidays,
                                                                  force_update)) for item in
                        all_instruments]
            # 处理Backfill结果
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
                raise Exception(
                    "处理失败的标的物为：%s" % ','.join([instrument.instrumentId for instrument in failed_instruments]))
