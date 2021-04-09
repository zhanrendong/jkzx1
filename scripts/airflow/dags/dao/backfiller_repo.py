from datetime import datetime
from sqlalchemy import and_
from terminal import Logging
from dags.dbo import OptionCloseBreak, QuoteCloseBreak, TickSnapshot, OptionClose, TickSnapshotBreak
from dags.dto import ReconFromType

logging = Logging.getLogger(__name__)


class BaseBackfillerRepo(object):
    @classmethod
    def get_recon_breaks(cls, db_session, start_date, end_date, table_dbo, instrument=None):
        if instrument is None:
            recon_breaks = db_session.query(table_dbo).filter(
                table_dbo.tradeDate >= start_date,
                table_dbo.tradeDate <= end_date,
            ).all()
            logging.info("获取到的[标的物: %s, startDate: %s, endDate: %s] Option Breaks为: %d:" %
                         (instrument, start_date.strftime('%Y%m%d'),
                          end_date.strftime('%Y%m%d'), len(recon_breaks)))
        else:
            recon_breaks = db_session.query(table_dbo).filter(
                table_dbo.tradeDate >= start_date,
                table_dbo.tradeDate <= end_date,
                table_dbo.instrumentId == instrument.instrumentId
            ).all()
            logging.info("获取到的[标的物: %s, startDate: %s, endDate: %s] Option Breaks为: %d:" %
                         (instrument, start_date.strftime('%Y%m%d'),
                          end_date.strftime('%Y%m%d'), len(recon_breaks)))
        recon_breaks_str = []
        for item in recon_breaks:
            recon_breaks_str.append(item.__str__())
        logging.info(",".join(recon_breaks_str))
        return recon_breaks


class QuoteCloseBackfillerRepo(BaseBackfillerRepo):
    @staticmethod
    def get_quote_close_recon_breaks(db_session, start_date, end_date, instrument=None,
                         recon_from_type=ReconFromType.QUOTE_CLOSE_RECON):
        if instrument is None:
            recon_breaks = db_session.query(QuoteCloseBreak).filter(
                and_(QuoteCloseBreak.tradeDate >= start_date,
                     QuoteCloseBreak.tradeDate <= end_date,
                     QuoteCloseBreak.reconFrom == recon_from_type.name)).all()
            logging.info("获取到的[标的物: %s, startDate: %s, endDate: %s] Breaks为: %d:" %
                         (instrument, start_date.strftime('%Y%m%d'),
                          end_date.strftime('%Y%m%d'), len(recon_breaks)))
        else:
            recon_breaks = db_session.query(QuoteCloseBreak).filter(
                and_(QuoteCloseBreak.instrumentId == instrument.instrumentId,
                     QuoteCloseBreak.tradeDate >= start_date,
                     QuoteCloseBreak.tradeDate <= end_date,
                     QuoteCloseBreak.reconFrom == recon_from_type.name)).all()
            logging.info("获取到的[标的物: %s, startDate: %s, endDate: %s] Breaks为: %d:" %
                         (instrument.instrumentId, start_date.strftime('%Y%m%d'),
                          end_date.strftime('%Y%m%d'), len(recon_breaks)))
        recon_breaks_str = []
        for item in recon_breaks:
            recon_breaks_str.append(item.__str__())
        logging.info(",".join(recon_breaks_str))
        return recon_breaks

    @staticmethod
    def copy_quote_close_properties(src_quote_close, dest_quote_close):
        src_quote_close.openPrice = dest_quote_close.openPrice
        src_quote_close.closePrice = dest_quote_close.closePrice
        src_quote_close.highPrice = dest_quote_close.highPrice
        src_quote_close.lowPrice = dest_quote_close.lowPrice
        src_quote_close.settlePrice = dest_quote_close.settlePrice
        src_quote_close.preClosePrice = dest_quote_close.preClosePrice
        src_quote_close.volume = dest_quote_close.volume
        src_quote_close.amount = dest_quote_close.amount
        src_quote_close.returnRate = dest_quote_close.returnRate
        src_quote_close.updatedAt = datetime.now()

    @staticmethod
    def upsert_one_quote_close(db_session, quote_close, existing_quote_close_dict, force_update):
        try:
            if quote_close is not None:
                if quote_close.tradeDate not in existing_quote_close_dict:
                    return quote_close

                else:
                    if force_update:
                        old_quote_close = existing_quote_close_dict[quote_close.trade_date]
                        QuoteCloseBackfillerRepo.copy_quote_close_properties(old_quote_close, quote_close)
                        db_session.flush()
                        db_session.commit()

                # TODO: 需要批量删除breaks
                # 删除该Break
                # print(quote_close.instrumentId,quote_close.tradeDate)
                # # 事务未提交，需要fetch同步线程
                db_session.query(QuoteCloseBreak).filter(
                    and_(QuoteCloseBreak.instrumentId == quote_close.instrumentId,
                         QuoteCloseBreak.tradeDate == quote_close.tradeDate)).delete(synchronize_session='fetch')
                db_session.flush()
                db_session.commit()
                return True
            else:
                return False
        except Exception as e:
            logging.error('更新失败，异常：%s', str(e))
            return False

    @staticmethod
    def upsert_instrument_close_list(db_session, quote_close_list, instrument):
        db_session.bulk_save_objects(quote_close_list)
        db_session.flush()
        db_session.commit()
        db_session.query(QuoteCloseBreak).filter(
            QuoteCloseBreak.instrumentId == instrument.instrumentId).delete(synchronize_session=False)
        db_session.flush()
        db_session.commit()
        return True


class OptionCloseBackfillerRepo(BaseBackfillerRepo):
    @staticmethod
    def get_option_close_recon_breaks(db_session, start_date, end_date, instrument=None):
        if instrument is None:
            recon_breaks = db_session.query(OptionCloseBreak).filter(
                OptionCloseBreak.tradeDate >= start_date,
                OptionCloseBreak.tradeDate <= end_date,
            ).all()
            logging.info("获取到的[标的物: %s, startDate: %s, endDate: %s] Option Breaks为: %d:" %
                         (instrument, start_date.strftime('%Y%m%d'),
                          end_date.strftime('%Y%m%d'), len(recon_breaks)))
        else:
            recon_breaks = db_session.query(OptionCloseBreak).filter(
                OptionCloseBreak.tradeDate >= start_date,
                OptionCloseBreak.tradeDate <= end_date,
                OptionCloseBreak.instrumentId == instrument.instrumentId
            ).all()
            logging.info("获取到的[标的物: %s, startDate: %s, endDate: %s] Option Breaks为: %d:" %
                         (instrument, start_date.strftime('%Y%m%d'),
                          end_date.strftime('%Y%m%d'), len(recon_breaks)))
        recon_breaks_str = []
        for item in recon_breaks:
            recon_breaks_str.append(item.__str__())
        logging.info(",".join(recon_breaks_str))
        return recon_breaks

    @staticmethod
    def get_tick_data(db_session, instrument, option_close_break):
        option_close = db_session.query(TickSnapshot).filter(
            TickSnapshot.tradeDate == option_close_break.tradeDate,
            TickSnapshot.instrumentId == instrument.instrumentId
        ).one_or_none()
        response_data = option_close.responseData.get('data')
        if len(response_data) == 1 or len(response_data) == 2:
            return response_data
        data_half_hour = [i for i in response_data if int(i['dataTime'].replace(':', '')) in range(145500, 150000)]
        if len(data_half_hour):
            return data_half_hour
        else:
            return response_data[0:1]

    @classmethod
    def get_option_recon_breaks(cls, db_session, start_date, end_date, instrument=None):
        return OptionCloseBackfillerRepo.get_recon_breaks(db_session, start_date, end_date, OptionCloseBreak, instrument)

    @staticmethod
    def upsert_one_option_close(db_session, option_close, force_update):
        try:
            if option_close is None:
                logging.error("传入的close对象为None")
                return False
            logging.info(
                "开始处理标的物数据：[%s, %s]" % (option_close.instrumentId, option_close.tradeDate.strftime('%Y%m%d')))
            if force_update:
                db_session.query(OptionClose).filter(
                    and_(OptionClose.instrumentId == option_close.instrumentId,
                         OptionClose.tradeDate == option_close.tradeDate)).delete()
                db_session.flush()
                db_session.commit()
            # 查看当前数据库是否已经有该条数据
            db_option_close = db_session.query(OptionClose).filter(
                and_(OptionClose.instrumentId == option_close.instrumentId,
                     OptionClose.tradeDate == option_close.tradeDate)).one_or_none()
            logging.info("数据是否已经存在：%s" % (db_option_close is not None))
            if db_option_close is None:
                db_session.add(option_close)
            # 删除该Break
            db_session.query(OptionCloseBreak).filter(
                and_(OptionCloseBreak.instrumentId == option_close.instrumentId,
                     OptionCloseBreak.tradeDate == option_close.tradeDate)).delete()
            db_session.flush()
            db_session.commit()
            logging.info(
                "处理标的物数据结束：[%s, %s]" % (option_close.instrumentId, option_close.tradeDate.strftime('%Y%m%d')))
            return True
        except Exception as e:
            # 回滚数据
            logging.error("处理标的物数据失败 %s" % e)
            db_session.rollback()
            return False


class TickSnapshotBackfillerRepo(BaseBackfillerRepo):
    @staticmethod
    def get_tick_snapshot_recon_breaks(db_session, start_date, end_date, instrument=None,
                         recon_from_type=ReconFromType.TICK_SNAPSHOT_RECON):
        if instrument is None:
            recon_breaks = db_session.query(TickSnapshotBreak).filter(
                and_(TickSnapshotBreak.tradeDate >= start_date,
                     TickSnapshotBreak.tradeDate <= end_date,
                     TickSnapshotBreak.reconFrom == recon_from_type.name)).all()
            logging.info("获取到的[标的物: %s, startDate: %s, endDate: %s] Tick Snapshot Breaks为: %d:" %
                         (instrument, start_date.strftime('%Y%m%d'),
                          end_date.strftime('%Y%m%d'), len(recon_breaks)))
        else:
            recon_breaks = db_session.query(TickSnapshotBreak).filter(
                and_(TickSnapshotBreak.instrumentId == instrument.instrumentId,
                     TickSnapshotBreak.tradeDate >= start_date,
                     TickSnapshotBreak.tradeDate <= end_date,
                     TickSnapshotBreak.reconFrom == recon_from_type.name)).all()
            logging.info("获取到的[标的物: %s, startDate: %s, endDate: %s] Tick Snapshot Breaks为: %d:" %
                         (instrument.instrumentId, start_date.strftime('%Y%m%d'),
                          end_date.strftime('%Y%m%d'), len(recon_breaks)))
        recon_breaks_str = []
        for item in recon_breaks:
            recon_breaks_str.append(item.__str__())
        logging.info(",".join(recon_breaks_str))
        return recon_breaks

    @staticmethod
    def upsert_one_tick_snapshot(db_session, tick_snapshot, force_update):
        try:
            if tick_snapshot is None:
                logging.error("远程调用获取不到标的物数据")
                return False
            logging.info(
                "开始处理标的物数据：[%s, %s]" % (tick_snapshot.instrumentId, tick_snapshot.tradeDate.strftime('%Y%m%d')))
            if force_update:
                db_session.query(TickSnapshot).filter(
                    and_(TickSnapshot.instrumentId == tick_snapshot.instrumentId,
                         TickSnapshot.tradeDate == tick_snapshot.tradeDate)).delete()
                db_session.flush()
                db_session.commit()
            # 查看当前数据库是否已经有该条数据
            db_tick_snapshot = db_session.query(TickSnapshot).filter(
                and_(TickSnapshot.instrumentId == tick_snapshot.instrumentId,
                     TickSnapshot.tradeDate == tick_snapshot.tradeDate)).one_or_none()
            logging.info("数据是否已经存在：%s" % (db_tick_snapshot is not None))
            if db_tick_snapshot is None:
                db_session.add(tick_snapshot)
            # 删除该Break
            db_session.query(TickSnapshotBreak).filter(
                and_(TickSnapshotBreak.instrumentId == tick_snapshot.instrumentId,
                     TickSnapshotBreak.tradeDate == tick_snapshot.tradeDate)).delete()
            db_session.flush()
            db_session.commit()
            logging.info(
                "处理标的物数据结束：[%s, %s]" % (tick_snapshot.instrumentId, tick_snapshot.tradeDate.strftime('%Y%m%d')))
            return True
        except Exception as e:
            # 回滚数据
            logging.error("处理标的物数据失败 %s" % e)
            db_session.rollback()
            return False