from sqlalchemy import distinct

from dags.dbo import OtcOptionQuote
from terminal import Logging

logging = Logging.getLogger(__name__)


class OTCOptionQuoteRepo:

    @staticmethod
    def get_instrument_ids_by_observe_date(db_session, observed_date):
        """
        获取所有instrument_id
        :param observed_date:
        :param db_session:
        :return:
        """
        logging.info('开始从otc_option_quote表获取日期为: %s的标的id' % observed_date)
        instruments = db_session.\
            query(distinct(OtcOptionQuote.underlier)).\
            filter(OtcOptionQuote.observeDate == observed_date).\
            all()
        if len(instruments) == 0 or instruments is None:
            logging.info('从otc_option_quote表没有获取到日期为: %s的数据' % observed_date)
            return []

        instrument_ids = [instrument[0] for instrument in instruments]
        logging.info('observed_date为: %s时,在otc_option_quote表中加载到的标的长度为: %d' %
                     (observed_date, len(instrument_ids)))

        return instrument_ids

    @staticmethod
    def get_quotes_by_observed_date_and_instrument_id(db_session, observed_date, instrument_id):
        """
        获取单个标的的数据
        :param db_session:
        :param observed_date:
        :param instrument_id:
        :return:
        """
        logging.info('开始从otc_option_quote表获取日期为: %s,标的为: %s的数据' % (observed_date, instrument_id))
        quotes = db_session.query(OtcOptionQuote).filter(
            OtcOptionQuote.underlier == instrument_id, OtcOptionQuote.observeDate == observed_date).all()

        if len(quotes) == 0 or quotes is None:
            logging.info('从otc_option_quote表没有获取到日期为: %s,标的为: %s的数据' % (observed_date, instrument_id))
            return []

        logging.info('observed_date为: %s时,在otc_option_quote表中加载到的标的为: %s' %
                     (observed_date, instrument_id))

        return quotes
