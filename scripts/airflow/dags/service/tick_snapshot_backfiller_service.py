import dags.dbo.db_model as std
import dags.utils.model_converter as converter_utils
from dags.utils.dataapi_utils import Client
import json
from datetime import datetime, date
from dags.service.base_backfiller_service import BaseBackfillerService
from terminal.utils import Logging
from terminal.dao import InstrumentRepo, TradingCalendarRepo
from terminal.dbo import Instrument
from terminal.dto import InstrumentType, AssetClassType, DataSourceType
from dags.dao import TickSnapshotBackfillerRepo
from dags.dbo import TickSnapshot
from dags.dto import ReconFromType, ResponseType, BreakType

logging = Logging.getLogger(__name__)


class TickSnapshotBackfillerService(BaseBackfillerService):
    @staticmethod
    def get_recon_breaks(db_session, start_date, end_date, instrument=None,
                         recon_from_type=ReconFromType.TICK_SNAPSHOT_RECON):
        return TickSnapshotBackfillerRepo.get_tick_snapshot_recon_breaks(db_session, start_date, end_date, instrument,
                                                                         recon_from_type)

    @staticmethod
    def request_one_tick_snapshot(instrument, tick_snapshot_break, response_type):
        try:
            client = Client()
            # TODO 需要放到配置文件或者从数据库读取
            logging.info(
                '开始获取标的物数据：[%s, %s]' % (
                    tick_snapshot_break.instrumentId, tick_snapshot_break.tradeDate.strftime('%Y%m%d')))
            # TODO 需要从配置文件读取
            version = 'v1'
            tonglian_instrument = converter_utils.convertToTonglian(Instrument(
                instrumentId=tick_snapshot_break.instrumentId, instrumentType=tick_snapshot_break.instrumentType))
            if (instrument.instrumentType == InstrumentType.STOCK.name or
                    instrument.instrumentType == InstrumentType.FUND.name or
                    instrument.instrumentType == InstrumentType.INDEX.name):
                url = '/api/market/getSHSZTicksHistOneDay.%s?ticker=%s&exchangeCD=%s&tradeDate=%s' \
                      % (response_type.value,
                         tonglian_instrument.instrumentId,
                         tonglian_instrument.exchangeId,
                         tick_snapshot_break.tradeDate.strftime('%Y%m%d'))
            elif instrument.instrumentType == InstrumentType.FUTURE.name \
                    and instrument.assetClass == AssetClassType.COMMODITY.name:
                url = '/api/market/getFutureTicksHistOneDay.%s?instrumentID=%s&date=%s' \
                      % (response_type.value,
                         tonglian_instrument.instrumentId,
                         tick_snapshot_break.tradeDate.strftime('%Y%m%d'))
            elif instrument.instrumentType == InstrumentType.OPTION.name \
                    and instrument.assetClass == AssetClassType.EQUITY.name:
                url = '/api/market/getOptionTicksHistOneDay.%s?optionId=%s&date=%s' \
                      % (response_type.value,
                         tonglian_instrument.instrumentId,
                         tick_snapshot_break.tradeDate.strftime('%Y%m%d'))
            elif instrument.instrumentType == InstrumentType.OPTION.name \
                    and instrument.assetClass == AssetClassType.COMMODITY.name:
                url = '/api/market/getFutureOptTicksHistOneDay.%s?instrumentID=%s&date=%s' \
                      % (response_type.value,
                         tonglian_instrument.instrumentId,
                         tick_snapshot_break.tradeDate.strftime('%Y%m%d'))
            else:
                logging.info(
                    "不支持处理标的物类型：%s -> %s" % (tick_snapshot_break.instrumentId, tick_snapshot_break.instrumentType))
                return None

            # 请求数据并进行处理
            code, result = client.getData(url)
            logging.debug(result)

            if code != 200:
                logging.error(
                    "获取标的物数据异常：[%s, %s], 远程服务器请求返回错误代码: %d" % (
                        tick_snapshot_break.instrumentId, tick_snapshot_break.tradeDate.strftime('%Y%m%d'), code))
                logging.error(result)
                return None

            if response_type == ResponseType.JSON:
                ret = json.loads(result)
                if ret['retCode'] != 1:
                    logging.error(
                        "获取标的物数据异常：[%s, %s], 返回的结果: %s" % (
                            tick_snapshot_break.instrumentId, tick_snapshot_break.tradeDate.strftime('%Y%m%d'), ret))
                    return None
            else:
                ret = result.decode('gbk')
            std_tick_snapshot = TickSnapshot(instrumentId=tick_snapshot_break.instrumentId,
                                             tradeDate=tick_snapshot_break.tradeDate,
                                             instrumentType=tick_snapshot_break.instrumentType,
                                             responseData=ret, url=url, version=version,
                                             responseType=response_type.name,
                                             data_source=DataSourceType.TONGLIAN.name,
                                             updatedAt=datetime.now())
            logging.info('%s成功获得tick snapshot信息' % tick_snapshot_break.instrumentId)
            return std_tick_snapshot
        except Exception as e:
            logging.error("远程获取标的物数据失败 %s" % e)
            return None

    @staticmethod
    def upsert_one_tick_snapshot(db_session, tick_snapshot, force_update):
        return TickSnapshotBackfillerRepo.upsert_one_tick_snapshot(db_session, tick_snapshot, force_update)

    def backfill_one_tick_snapshot(self, db_session, instrument, tick_snapshot_break, force_update, response_type):
        try:
            tick_snapshot = self.request_one_tick_snapshot(instrument, tick_snapshot_break, response_type)
            upsert_flag = self.upsert_one_tick_snapshot(db_session, tick_snapshot, force_update)
            return upsert_flag
        except Exception as e:
            logging.error(
                '处理单条标的物数据失败：[%s, %s]' % (
                    tick_snapshot_break.instrumentId, tick_snapshot_break.tradeDate.strftime('%Y%m%d')))
            return False

    def backfill_multiple_tick_snapshot(self, db_session, instrument, tick_snapshot_breaks, holidays, special_dates,
                                        force_update=False,
                                        response_type=ResponseType.JSON):
        logging.info("开始tick snapshot backfilling")
        # 计算成功和失败的条数
        missing_success_count = 0
        missing_failed_count = 0
        try:
            # TODO 将Break按类型分组， MISSING or EXTRA
            missing_breaks = []
            for item in tick_snapshot_breaks:
                if item.breakType == BreakType.MISSING.name:
                    missing_breaks.append(item)

            for item in missing_breaks:
                ret = self.backfill_one_tick_snapshot(db_session, instrument, item, force_update, response_type)
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

    def backfill_one_instrument(self, params):
        instrument, start_date, end_date, holidays, force_update = params
        logging.info("开始Backfill标的物: %s, startDate: %s, endDate: %s" % (instrument.instrumentId,
                                                                        start_date.strftime('%Y%m%d'),
                                                                        end_date.strftime('%Y%m%d')))
        db_session = std.create_db_session()
        try:
            special_dates = TradingCalendarRepo.load_special_dates(db_session, instrument)
            tick_snapshot_breaks = self.get_recon_breaks(db_session, start_date, end_date, instrument,
                                                         ReconFromType.TICK_SNAPSHOT_RECON)
            ret = self.backfill_multiple_tick_snapshot(db_session, instrument, tick_snapshot_breaks, holidays,
                                                       special_dates)
            return instrument, ret
        except Exception as e:
            return instrument, False
        finally:
            db_session.close()

    def load_instruments(self, db_session, active_only):
        return InstrumentRepo.load_instruments_in_option_conf(db_session)


if __name__ == '__main__':
    TickSnapshotBackfillerService().backfill_all_instruments(start_date=date(2019, 8, 26), end_date=date(2019, 8, 26),
                                                             active_only=True,
                                                             force_update=False)
