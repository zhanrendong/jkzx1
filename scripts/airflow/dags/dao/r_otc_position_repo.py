from dags.dbo import ROTCPosition
from terminal.utils import DateTimeUtils
from terminal.utils import Logging

logger = Logging.getLogger(__name__)


class ROTCPositionRepo:

    @staticmethod
    def get_record_ids_by_date(db_session, start_date, end_date):
        """
        根据传入的时间,获取数据
        :param db_session:
        :param start_date:
        :param end_date:
        :return:
        """
        logger.info('开始获取数据平台r_otc_position表 %s 到 %s 的数据' % (start_date, end_date))
        record_ids = db_session.query(ROTCPosition.RECORDID).filter(
            ROTCPosition.REPORTDATE >= DateTimeUtils.date2str(start_date),
            ROTCPosition.REPORTDATE <= DateTimeUtils.date2str(end_date)).all()
        if len(record_ids) == 0 or record_ids is None:
            logger.info('数据平台r_otc_position表 %s 到 %s 之间没有数据' % (start_date, end_date))
            return []
        record_ids = [record_id.RECORDID for record_id in record_ids]
        logger.info('数据平台r_otc_position表 %s 到 %s 数据长度为: %d' %
                    (start_date, end_date, len(record_ids)))

        return record_ids

    @staticmethod
    def get_otc_position_by_trans_codes(db_session, report_date, trans_code_list):
        position_list = db_session.query(
            ROTCPosition.RECORDID,  # （唯一标识，伪唯一）
            ROTCPosition.REPORTID,  # （报告编号）
            ROTCPosition.TRANSCODE,  # （传输编号，用于和交易表连接）
            ROTCPosition.COMPANYID,  # （上报公司）
            ROTCPosition.REPORTDATE,  # （上报日期）
            ROTCPosition.MAINBODYNAME,  # (报送主体名称)
            ROTCPosition.ANALOGUECODE,  # （交易对手编号）
            ROTCPosition.ANALOGUENAME,  # （交易对手名称）
            ROTCPosition.POSITIONDATE,  # （持仓日期）
            ROTCPosition.TRANSCONFIRTIME,  # （交易确认时间）
            ROTCPosition.OPTEXERCTMTYPE,  # （行权时间类型）
            ROTCPosition.OPTRIGHTTYPE,  # （期权权利类型）
            ROTCPosition.RTVALMEPAY,  # （产品结构）
            ROTCPosition.UNDERASSTYPE,  # （标的资产类型）
            ROTCPosition.UNDERASSVARIT,  # （标的资产品种）
            ROTCPosition.STANDASSCONT,  # （标的资产对应合约）
            ROTCPosition.STATUS,  # （状态）
            ROTCPosition.SIDE,  # 填报方向）
            ROTCPosition.PARTICIPATERATE,  # （参与率）
            ROTCPosition.ANNUALIZED,  # （年化否）
            ROTCPosition.STRIKE,  # （行权价格）
            ROTCPosition.VALUATIONSPOT,  # （合约估值时标的价格）
            ROTCPosition.TRADENOTIONAL,  # （总名义金额）
            ROTCPosition.TRADEQAUNTITY,  # （总名义数量）
            ROTCPosition.CLOSEDNOTIONAL,  # （已平仓名义金额）
            ROTCPosition.CLOSEDQUANTITY,  # （已平仓名义数量）
            ROTCPosition.PRICESYMBOL,  # （价格符号）
            ROTCPosition.EFFECTIVEDAY,  # （一年有效天数）
            ROTCPosition.IMPLIEDVOL,  # （上报的银行波动率）
            ROTCPosition.INTERESTRATE,  # （无风险利率）
            ROTCPosition.DIVIDEND  # （贴现率）
        ).filter(
            # TODO 一下两种可能会用到
            # ROTCPosition.STATUS == '1',
            # ROTCPosition.RTVALMEPAY == 'VA',
            ROTCPosition.TRANSCODE.in_(trans_code_list),
            ROTCPosition.REPORTDATE <= report_date
        ).all()
        if len(position_list) == 0 or position_list is None:
            return []
        # 取最新一条持仓数据作为持仓基础数据
        trans_code_dict = {}
        for position in position_list:
            if position.TRANSCODE not in trans_code_dict:
                trans_code_dict[position.TRANSCODE] = []
            trans_code_dict[position.TRANSCODE].append(position)
        position_data_list = []
        for trans_code, position_list in trans_code_dict.items():
            report_date_dict = {}
            for position in position_list:
                if position.REPORTDATE not in report_date_dict:
                    report_date_dict[position.REPORTDATE] = {}
                report_date_dict[position.REPORTDATE][position.RECORDID] = position
            max_report_date = max(report_date_dict)
            max_record_id = max(report_date_dict[max_report_date])
            filtered_posiotion = report_date_dict[max_report_date][max_record_id]
            position_data_list.append(filtered_posiotion)
        return position_data_list

    @staticmethod
    def get_trans_code_list_by_date(db_session, report_date):
        # 查询TRANSCODE集合 （以position表数据为基准）
        trans_code_list = db_session.query(
            ROTCPosition.TRANSCODE  # 0 (传输编号,为主)
        ).filter(
            ROTCPosition.REPORTDATE == report_date
        ).group_by(
            ROTCPosition.TRANSCODE
        ).all()

        return [obj.TRANSCODE for obj in trans_code_list]

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
        db_session.query(ROTCPosition). \
            filter(ROTCPosition.REPORTDATE >= DateTimeUtils.date2str(start_date),
                   ROTCPosition.REPORTDATE <= DateTimeUtils.date2str(end_date)). \
            delete(synchronize_session=False)
        db_session.commit()
        logger.info('成功删除: %s 至 %s时间段内的标的数据' % (start_date, end_date))
