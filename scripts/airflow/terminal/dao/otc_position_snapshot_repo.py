from terminal.dbo import OTCPositionSnapshot
from sqlalchemy import and_
from terminal.utils import logging


class OTCPositionSnapshotRepo:
    @staticmethod
    def get_otc_position_snapshot_list(db_session, instrument_id, position_date):
        otc_position_dbo_list = db_session.query(OTCPositionSnapshot).filter(
            and_(
                OTCPositionSnapshot.instrumentId == instrument_id,
                OTCPositionSnapshot.positionDate == position_date
            )
        ).all()
        return otc_position_dbo_list

    @staticmethod
    def delete_position_snapshot_by_report_date(db_session, report_date):
        """
        通过日期删除数据
        :param db_session:
        :param report_date:
        :return:
        """
        logging.info('开始删除otc_position_snapshot表的数据,日期为 %s' % report_date)
        db_session.query(OTCPositionSnapshot).filter(OTCPositionSnapshot.reportDate == report_date).delete()
        db_session.commit()
        logging.info('删除数据成功')

    @staticmethod
    def get_trans_codes_by_report_date(db_session, report_date):
        """
        根据日期获取当天所有数据
        :param db_session:
        :param report_date:
        :return:
        """
        logging.info('开始获取日期为: %s的trans_codes' % report_date)
        trans_codes = db_session.query(OTCPositionSnapshot.transCode).\
            filter(OTCPositionSnapshot.reportDate == report_date).all()
        if trans_codes is None or len(trans_codes) == 0:
            logging.info('日期为: %s,没有获取到trans_codes' % report_date)
            return []
        trans_codes = [_[0] for _ in trans_codes]
        return trans_codes

