from dags.dbo import RealizedVol
from terminal.utils import logging


class RealizedVolRepo:

    @staticmethod
    def get_realized_vol_dict_by_valuation_date(db_session, valuation_date):
        logging.info('开始获取交易日: %s的历史波动率' % valuation_date)
        realized_vol_list = db_session.query(RealizedVol).filter(RealizedVol.valuationDate == valuation_date).all()
        if len(realized_vol_list) == 0:
            logging.info('交易日为: %s时, 没有获取到标的的历史波动率' % valuation_date)
            return {}
        realized_vol_dict = {}
        for realized_vol in realized_vol_list:
            if realized_vol.instrumentId not in realized_vol_dict:
                realized_vol_dict[realized_vol.instrumentId] = {}
            realized_vol_dict[realized_vol.instrumentId][realized_vol.windows] = realized_vol
        logging.info(
            '交易日: %s获取到了: %d 条历史波动率' % (valuation_date, len(realized_vol_dict)))
        return realized_vol_dict

    @staticmethod
    def save_realized_vol_list(db_session, realized_vol_list):
        logging.info('开始保存历史波动率, 长度为: %d' % len(realized_vol_list))
        db_session.add_all(realized_vol_list)
        db_session.commit()
        logging.info('成功保存历史波动率')

    @staticmethod
    def delete_realized_vol_by_uuids(db_session, uuids):
        logging.info('开始删除历史波动率, 长度为: %d' % len(uuids))
        db_session.query(RealizedVol).\
            filter(RealizedVol.uuid.in_(uuids)).delete(synchronize_session=False)
        db_session.commit()
        logging.info('删除历史波动率成功')
