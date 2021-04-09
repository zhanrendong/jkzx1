from terminal.dbo import QuoteClose
from sqlalchemy import and_
from terminal.utils import Logging

logger = Logging.getLogger(__name__)


class QuoteCloseRepo:
    @staticmethod
    def get_instrument_quote_close_list_by_period(db_session, instrument_id, start_date, end_date):
        quote_close_dbo_list = db_session.query(QuoteClose).filter(
            and_(
                QuoteClose.instrumentId == instrument_id,
                QuoteClose.tradeDate >= start_date,
                QuoteClose.tradeDate <= end_date
            )
        ).all()
        return quote_close_dbo_list

    @staticmethod
    def get_instrument_quote_close_list_by_days(db_session, instrument_ids, trade_dates):
        quote_close_dbo_list = db_session.query(QuoteClose).filter(
            and_(
                QuoteClose.instrumentId.in_(instrument_ids),
                QuoteClose.tradeDate.in_(trade_dates))
        ).all()
        return quote_close_dbo_list

    @staticmethod
    def get_all_instrument_quote_close_by_date(db_session, date):
        """
        获取当天所有标的收盘价
        :param db_session:
        :param date:
        :return:
        """
        logger.info('开始从instrument_quote_close表获取日期为: %s的数据' % date)

        quote_close_dbo_list = db_session.query(QuoteClose).filter(QuoteClose.tradeDate == date).all()

        if len(quote_close_dbo_list) == 0 or quote_close_dbo_list is None:
            logger.info('从instrument_quote_close表没有获取到日期为: %s的数据' % date)
            return []

        logger.info('时间为: % s,在instrument_quote_close表查询到了%d条数据' %
                    (date, len(quote_close_dbo_list)))
        return quote_close_dbo_list

    @staticmethod
    def delete_all_instrument_quote_close_by_date(db_session, date, instrument_ids):
        """
        根据日期删除标的
        :param db_session:
        :param date:
        :param instrument_ids:
        :return:
        """
        logger.info('开始从instrument_quote_close表删除日期为: %s,标的数据，共计%d条' % (date, len(instrument_ids)))

        db_session.query(QuoteClose).filter(QuoteClose.instrumentId.in_(instrument_ids),
                                            QuoteClose.tradeDate == date).delete(synchronize_session=False)
        db_session.commit()
        logger.info('删除成功')

    @staticmethod
    def get_instrument_quote_closes(db_session, instrument):
        quote_closes = db_session.query(QuoteClose).filter(
            QuoteClose.instrumentId == instrument.instrumentId).all()
        return quote_closes

    @staticmethod
    def get_existing_trading_dates(db_session, instrument):
        existing_items = db_session.query(QuoteClose.tradeDate).filter(
            QuoteClose.instrumentId == instrument.instrumentId).all()
        existing_trading_dates = [item.tradeDate for item in existing_items]
        logger.info("当前的已存在的交易日为：%s" % ','.join([date.strftime('%Y%m%d') for date in existing_trading_dates]))
        return existing_trading_dates

    @staticmethod
    def get_instrument_quote_close_list_by_range(db_session, instrument_ids, listed_date, delisted_date):
        return db_session.query(QuoteClose).filter(
            QuoteClose.instrumentId.in_(instrument_ids),
            QuoteClose.tradeDate >= listed_date, QuoteClose.tradeDate <= delisted_date).all()

    @staticmethod
    def get_instrument_quote_close_by_date(db_session, instrument_id, trade_date):
        """
        根据标的id与交易日获取标的收盘价信息
        :param db_session:
        :param trade_date:
        :param instrument_id:
        :return:
        """
        logger.info('开始获取日期为: %s,标的为: %s的收盘价' % (trade_date, instrument_id))
        quote_close = db_session.query(QuoteClose).filter(QuoteClose.instrumentId == instrument_id,
                                                          QuoteClose.tradeDate == trade_date).one_or_none()
        logger.info('获取到了标的: %s的收盘价' % instrument_id)
        return quote_close
