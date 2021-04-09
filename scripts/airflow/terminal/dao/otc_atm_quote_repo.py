from sqlalchemy import and_
from terminal.dbo import OtcAtmQuote
from terminal.utils import Logging
import uuid
logger = Logging.getLogger(__name__)


class OtcAtmQuoteRepo:
    @staticmethod
    def get_instrument_atm_quote_list_by_period(db_session, underlyer, start_date, end_date):
        quote_dbo_list = db_session.query(OtcAtmQuote).filter(
            and_(
                OtcAtmQuote.underlyer == underlyer,
                OtcAtmQuote.valuationDate >= start_date,
                OtcAtmQuote.valuationDate <= end_date
            )
        ).all()
        return quote_dbo_list

    @staticmethod
    def get_commodity_future_atm_quote_list_by_period(db_session, variety_type, start_date, end_date):
        quote_dbo_list = db_session.query(OtcAtmQuote).filter(
            and_(
                OtcAtmQuote.varietyType == variety_type,
                OtcAtmQuote.valuationDate >= start_date,
                OtcAtmQuote.valuationDate <= end_date
            )
        ).all()
        return quote_dbo_list

    @staticmethod
    def save_otc_atm_quote_list(db_session, atm_quote_list):
        if len(atm_quote_list) == 0:
            return atm_quote_list
        uuid_list = []
        for quote in atm_quote_list:
            if quote.uuid is None:
                # 没有uuid则直接赋值
                quote.uuid = uuid.uuid4()
            else:
                # 获取已经存入的uuid
                uuid_list.append(quote.uuid)
        # 删除掉已经存在的记录
        db_session.query(OtcAtmQuote).filter(
                OtcAtmQuote.uuid.in_(uuid_list)).delete(synchronize_session=False)
        # 插入新的记录
        db_session.add_all(atm_quote_list)
        db_session.commit()
        return atm_quote_list

