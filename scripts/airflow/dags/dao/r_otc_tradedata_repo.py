from dags.dbo import ROTCTradeData
from terminal.utils import DateTimeUtils
from terminal.utils import Logging

logger = Logging.getLogger(__name__)


class ROTCTradedataRepo:

    @staticmethod
    def get_record_ids_by_date(db_session, start_date, end_date):
        """
        根据传入的时间,获取时间列表
        :param db_session:
        :param start_date:
        :param end_date:
        :return:
        """
        logger.info('开始获取数据平台r_otc_tradedata表 %s 到 %s 的数据' % (start_date, end_date))
        record_ids = db_session.query(ROTCTradeData.RECORDID).filter(
            ROTCTradeData.REPORTDATE >= DateTimeUtils.date2str(start_date),
            ROTCTradeData.REPORTDATE <= DateTimeUtils.date2str(end_date)).all()
        if len(record_ids) == 0 or record_ids is None:
            logger.info('数据平台r_otc_tradedata表 %s 到 %s 之间没有数据' % (start_date, end_date))
            return []
        record_ids = [record_id.RECORDID for record_id in record_ids]
        logger.info('数据平台r_otc_tradedata表 %s 到 %s 数据长度为: %d, record id为: %s' %
                    (start_date, end_date, len(record_ids), record_ids))

        return record_ids

    @staticmethod
    def get_trade_data_by_trans_codes(db_session, report_date, trans_code_list):
        trade_list = db_session.query(
            ROTCTradeData.RECORDID,  # (唯一标识,伪唯一)
            ROTCTradeData.REPORTID,  # (报告编号)
            ROTCTradeData.TRANSCODE,  # (传输编号,用于和交易表连接)
            ROTCTradeData.REPORTDATE,  # (报告日期)
            ROTCTradeData.EFFECTDATE,  # （生效日）
            ROTCTradeData.EXPIREDATE,  # （过期日）
            ROTCTradeData.EXERCISEDATE,  # （结算日）
            ROTCTradeData.DIRECTREPPARTY,  # （买卖方向）
            ROTCTradeData.OPTRIGHTTYPE,  # （期权权利类型）
            ROTCTradeData.RTVALMEPAY,  # （产品类型）
            ROTCTradeData.OPTEXERCTMTYPE,  # (期权执行时间类型)
            ROTCTradeData.EXECUTPRICE,  # （执行价格）
            ROTCTradeData.ASSENTRYPRICE,  # （标的资产进场价格）
            ROTCTradeData.NOMINALAMOUNT,  # （总名义金额）
            ROTCTradeData.PREMIUMAMOUNT,  # （期权费）
            ROTCTradeData.SETTMETHOD,  # （结算方式）
            ROTCTradeData.OPERTYPE,  # (操作方式)
            ROTCTradeData.SETTPRIMETHED,  # (结算价确认方式)
            ROTCTradeData.PARTICIPATIONRATE,
            ROTCTradeData.ANNUALIZED,
        ).filter(
            # TODO 一下两种可能会用到
            # ROTCTradeData.STATUS == '1',
            # ROTCTradeData.RTVALMEPAY == 'VA',
            ROTCTradeData.TRANSCODE.in_(trans_code_list),
            ROTCTradeData.REPORTDATE <= report_date
            # ROTCTradeData.OPTRIGHTTYPE == option_type,
            # or_(ROTCTradeData.OPERTYPE == 'NT', ROTCTradeData.OPERTYPE == '新交易')
        ).all()
        if len(trade_list) == 0 or trade_list is None:
            return {}
        # 取最新一条持仓数据作为持仓基础数据
        trans_code_dict = {}
        for trade in trade_list:
            if trade.TRANSCODE not in trans_code_dict:
                trans_code_dict[trade.TRANSCODE] = []
            trans_code_dict[trade.TRANSCODE].append(trade)
        trad_data_dict = {}
        for trans_code, trade_list in trans_code_dict.items():
            report_date_dict = {}
            for trade in trade_list:
                if trade.REPORTDATE not in report_date_dict:
                    report_date_dict[trade.REPORTDATE] = {}
                report_date_dict[trade.REPORTDATE][trade.RECORDID] = trade
            max_report_date = max(report_date_dict)
            max_record_id = max(report_date_dict[max_report_date])
            filtered_posiotion = report_date_dict[max_report_date][max_record_id]
            trad_data_dict[trans_code] = filtered_posiotion
        return trad_data_dict

    @staticmethod
    def delete_data_by_dates(db_session, start_date, end_date):
        """
        根据日期和标的删除数据
        :param db_session:
        :param start_date:
        :param end_date:
        :return:
        """
        logger.info('准备删除: %s 至 %s时间段内的标数据' % (start_date, end_date))
        db_session.query(ROTCTradeData). \
            filter(ROTCTradeData.REPORTDATE >= DateTimeUtils.date2str(start_date),
                   ROTCTradeData.REPORTDATE <= DateTimeUtils.date2str(end_date)). \
            delete(synchronize_session=False)
        db_session.commit()
        logger.info('成功删除: %s 至 %s时间段内的标的数据' % (start_date, end_date))
