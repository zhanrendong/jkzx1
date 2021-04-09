from datetime import datetime
from terminal import Logging
import uuid
from dags.dbo import OptionCloseBreak, QuoteCloseBreak, QuoteCloseMilestone, OptionCloseMilestone, \
    TickSnapshotBreak, TickSnapshotMilestone
from dags.dto import BreakType, ReconFromType

logging = Logging.getLogger(__name__)


class BaseReconRepo(object):
    @classmethod
    def upsert_milestone(cls, db_session, instrument, start_date, end_date, milestone, table_dbo):
        logging.info("准备更新Milestone到数据库: : %s" % instrument.instrumentId)
        if milestone is None:
            db_session.add(table_dbo(instrumentId=instrument.instrumentId,
                                     startDate=start_date,
                                     endDate=end_date,
                                     updatedAt=datetime.now()))
            logging.info("写入Milestone：%s -> [%s, %s]" % (
                instrument.instrumentId, start_date.strftime('%Y%m%d'), end_date.strftime('%Y%m%d')))
            db_session.flush()
            db_session.commit()
        else:
            new_start_date = start_date if start_date < milestone.startDate else milestone.startDate
            new_end_date = end_date if end_date > milestone.endDate else milestone.endDate
            milestone.startDate = new_start_date
            milestone.endDate = new_end_date
            milestone.updatedAt = datetime.now()
            db_session.commit()
            logging.info("更新Milestone：%s -> [%s, %s]" % (
                instrument.instrumentId, new_start_date.strftime('%Y%m%d'), new_end_date.strftime('%Y%m%d')))

    @staticmethod
    def get_existing_trading_dates(db_session, instrument, table_dbo):
        existing_items = db_session.query(table_dbo.tradeDate).filter(
            table_dbo.instrumentId == instrument.instrumentId).all()
        existing_trading_dates = [item.tradeDate for item in existing_items]
        logging.debug("当前的已存在的交易日为：%s" % ','.join([date.strftime('%Y%m%d') for date in existing_trading_dates]))
        return existing_trading_dates

    @classmethod
    def get_milestone(cls, db_session, instrument, table_dbo):
        milestone = db_session.query(table_dbo).filter(
            table_dbo.instrumentId == instrument.instrumentId).one_or_none()
        if milestone is None:
            logging.info("标的物的milestone: %s -> 为空" % instrument.instrumentId)
        else:
            logging.info("标的物的milestone: %s -> [%s, %s]" % (instrument.instrumentId,
                                                            milestone.startDate.strftime('%Y%m%d'),
                                                            milestone.endDate.strftime('%Y%m%d')))
        return milestone


class TickSnapshotReconRepo(BaseReconRepo):
    @staticmethod
    def upsert_tick_snapshot_break(db_session, instrument, missing_trading_dates):
        # 如果有差值，清空当前Break表
        logging.info("准备更新Breaks到数据库: : %s" % instrument.instrumentId)
        db_session.query(TickSnapshotBreak).filter(
            TickSnapshotBreak.instrumentId == instrument.instrumentId).delete()
        # 重新写入Break表
        for trading_date in missing_trading_dates:
            item = TickSnapshotBreak(uuid=uuid.uuid1(),
                                     instrumentId=instrument.instrumentId,
                                     instrumentType=instrument.instrumentType,
                                     tradeDate=trading_date,
                                     breakType=BreakType.MISSING.name,
                                     updatedAt=datetime.now(),
                                     reconFrom=ReconFromType.TICK_SNAPSHOT_RECON.name)
            db_session.add(item)
        db_session.flush()
        db_session.commit()

    @classmethod
    def upsert_tick_snapshot_milestone(cls, db_session, instrument, start_date, end_date, milestone):
        cls.upsert_milestone(db_session, instrument, start_date, end_date, milestone, TickSnapshotMilestone)

    @classmethod
    def get_tick_snapshot_milestone(cls, db_session, instrument):
        return cls.get_milestone(db_session, instrument, TickSnapshotMilestone)


class OptionReconRepo(BaseReconRepo):
    @staticmethod
    def upsert_option_close_break(db_session, instrument, missing_trading_dates):
        # 如果有差值，清空当前Break表
        logging.info("准备更新Breaks到数据库: : %s" % instrument.instrumentId)
        db_session.query(OptionCloseBreak).filter(
            OptionCloseBreak.instrumentId == instrument.instrumentId).delete()
        # 重新写入Break表
        for trading_date in missing_trading_dates:
            item = OptionCloseBreak(uuid=uuid.uuid1(),
                                    instrumentId=instrument.instrumentId,
                                    tradeDate=trading_date,
                                    breakType=BreakType.MISSING.name,
                                    updatedAt=datetime.now())
            db_session.add(item)
        db_session.flush()
        db_session.commit()

    @classmethod
    def upsert_option_close_milestone(cls, db_session, instrument, start_date, end_date, milestone):
        cls.upsert_milestone(db_session, instrument, start_date, end_date, milestone, OptionCloseMilestone)

    @classmethod
    def get_option_close_milestone(cls, db_session, instrument):
        return cls.get_milestone(db_session, instrument, OptionCloseMilestone)


class QuoteCloseReconRepo(BaseReconRepo):
    @staticmethod
    # 如果有差值，清空当前Break表
    def upsert_quote_close_break(db_session, instrument, missing_trading_dates):
        logging.info("准备更新Breaks到数据库: : %s" % instrument.instrumentId)
        # 清空表
        db_session.query(QuoteCloseBreak).filter(
            QuoteCloseBreak.instrumentId == instrument.instrumentId).delete()
        # 重新写入Break表
        breaks = []
        for trading_date in missing_trading_dates:
            item = QuoteCloseBreak(uuid=uuid.uuid1(),
                                   instrumentId=instrument.instrumentId,
                                   instrumentType=instrument.instrumentType,
                                   tradeDate=trading_date,
                                   breakType=BreakType.MISSING.name,
                                   updatedAt=datetime.now(),
                                   reconFrom=ReconFromType.QUOTE_CLOSE_RECON.name)
            breaks.append(item)
        db_session.bulk_save_objects(breaks)
        db_session.flush()
        db_session.commit()

    @classmethod
    def upsert_quote_close_milestone(cls, db_session, instrument, start_date, end_date, milestone):
        cls.upsert_milestone(db_session, instrument, start_date, end_date, milestone, QuoteCloseMilestone)

    @classmethod
    def get_quote_close_milestone(cls, db_session, instrument):
        return cls.get_milestone(db_session, instrument, QuoteCloseMilestone)
