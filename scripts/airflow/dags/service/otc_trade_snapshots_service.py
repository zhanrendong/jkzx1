# -*- coding: utf-8 -*-

import datetime
from dags.dao import OracleROTCPositionRepo
from dags.dao import OracleROTCTradedataRepo
from dags.dbo.db_model import create_db_session as Session
from dags.dbo import ROTCPosition, ROTCTradeData
import os
from dags.dao import ROTCPositionRepo
from dags.dao import ROTCTradedataRepo
from terminal.utils import DateTimeUtils
from terminal.utils.logging_utils import Logging

os.environ['NLS_LANG'] = 'SIMPLIFIED CHINESE_CHINA.UTF8'
logger =Logging.getLogger(__name__)


class OTCTradeSnapshotsService:
    @staticmethod
    def transform_r_otc_position_model(requested_models):
        """
        将一种数据库对象转换为另一种数据库对象
        :param requested_models:
        :return:
        """
        models = []
        for requested_model in requested_models:
            item = ROTCPosition(RECORDID=str(requested_model.RECORDID),
                                REPORTID=requested_model.REPORTID,
                                REPORTDATE=requested_model.REPORTDATE,
                                COMPANYID=requested_model.COMPANYID,
                                MAINBODYCODE=requested_model.MAINBODYCODE,
                                SUBID=requested_model.SUBID,
                                MAINBODYNAME=requested_model.MAINBODYNAME,
                                CUSTOMERTYPE=requested_model.CUSTOMERTYPE,
                                NOCID=requested_model.NOCID,
                                FUTURESID=requested_model.FUTURESID,
                                ANALOGUECODE=requested_model.ANALOGUECODE,
                                ANALOGUENAME=requested_model.ANALOGUENAME,
                                ANALOGUECUSTYPE=requested_model.ANALOGUECUSTYPE,
                                ANALOGUENOCID=requested_model.ANALOGUENOCID,
                                TRANSCONFIRNUMBER=requested_model.TRANSCONFIRNUMBER,
                                TRANSCONFIRTIME=requested_model.TRANSCONFIRTIME,
                                POSITIONDATE=requested_model.POSITIONDATE,
                                ANALOGUEFUID=requested_model.ANALOGUEFUID,
                                TRANSCODE=requested_model.TRANSCODE,
                                UTIID=requested_model.UTIID,
                                ASSETTYPE=requested_model.ASSETTYPE,
                                TOOLTYPE=requested_model.TOOLTYPE,
                                OPTEXERCTMTYPE=requested_model.OPTEXERCTMTYPE,
                                OPTRIGHTTYPE=requested_model.OPTRIGHTTYPE,
                                RTVALMEPAY=requested_model.RTVALMEPAY,
                                UNDERASSTYPE=requested_model.UNDERASSTYPE,
                                UNDERASSVARIT=requested_model.UNDERASSVARIT,
                                STANDASSCONT=requested_model.STANDASSCONT,
                                CONTRACTVALUE=requested_model.CONTRACTVALUE,
                                VALMETHOD=requested_model.VALMETHOD,
                                BUYPOSMONYE=requested_model.BUYPOSMONYE,
                                SALEPOSMONYE=requested_model.SALEPOSMONYE,
                                MONEYACCOUNT=requested_model.MONEYACCOUNT,
                                BUYPOSAMOUNT=requested_model.BUYPOSAMOUNT,
                                SALEPOSAMOUNT=requested_model.SALEPOSAMOUNT,
                                QUANTITYUNIT=requested_model.QUANTITYUNIT,
                                TOTPOSMONYE=requested_model.TOTPOSMONYE,
                                NETPOSMONYE=requested_model.NETPOSMONYE,
                                TOPOSAMOUNT=requested_model.TOPOSAMOUNT,
                                NETPOSAMOUNT=requested_model.NETPOSAMOUNT,
                                STATUS=requested_model.STATUS,
                                LEGID=requested_model.LEGID,
                                SIDE=requested_model.SIDE,
                                PARTICIPATERATE=requested_model.PARTICIPATERATE,
                                ANNUALIZED=requested_model.ANNUALIZED,
                                STRIKE=requested_model.STRIKE,
                                VALUATIONSPOT=requested_model.VALUATIONSPOT,
                                TRADENOTIONAL=requested_model.TRADENOTIONAL,
                                CLOSEDNOTIONAL=requested_model.CLOSEDNOTIONAL,
                                PRICESYMBOL=requested_model.PRICESYMBOL,
                                EXCHANGERATE=requested_model.EXCHANGERATE,
                                TRADEQAUNTITY=requested_model.TRADEQAUNTITY,
                                CLOSEDQUANTITY=requested_model.CLOSEDQUANTITY,
                                IMPLIEDVOL=requested_model.IMPLIEDVOL,
                                EFFECTIVEDAY=requested_model.EFFECTIVEDAY,
                                DELTA=requested_model.DELTA,
                                GAMMA=requested_model.GAMMA,
                                VEGA=requested_model.VEGA,
                                THETA=requested_model.THETA,
                                RHO=requested_model.RHO,
                                DELTACASH=requested_model.DELTACASH,
                                INTERESTRATE=requested_model.INTERESTRATE,
                                DIVIDEND=requested_model.DIVIDEND,
                                UPDATEAT=datetime.datetime.now())

            models.append(item)

        return models

    @staticmethod
    def transform_r_otc_tradedata_model(requested_models):
        """
        将一种数据库对象转换为另一种数据库对象
        :param requested_models:
        :return:
        """
        models = []
        for requested_model in requested_models:
            item = ROTCTradeData(RECORDID=str(requested_model.RECORDID),
                                 REPORTID=requested_model.REPORTID,
                                 REPORTDATE=requested_model.REPORTDATE,
                                 COMPANYID=requested_model.COMPANYID,
                                 MAINBODYCODE=requested_model.MAINBODYCODE,
                                 SUBID=requested_model.SUBID,
                                 MAINBODYNAME=requested_model.MAINBODYNAME,
                                 NOCID=requested_model.NOCID,
                                 CUSTOMERTYPE=requested_model.CUSTOMERTYPE,
                                 FUTURESID=requested_model.FUTURESID,
                                 ANALOGUECODE=requested_model.ANALOGUECODE,
                                 ANALOGUENAME=requested_model.ANALOGUENAME,
                                 ANALOGUENOCID=requested_model.ANALOGUENOCID,
                                 ANALOGUECUSTYPE=requested_model.ANALOGUECUSTYPE,
                                 ANALOGUEFUID=requested_model.ANALOGUEFUID,
                                 MAINPROTTYPE=requested_model.MAINPROTTYPE,
                                 MAINPROTDATE=requested_model.MAINPROTDATE,
                                 ISCREDIT=requested_model.ISCREDIT,
                                 CREDITLINE=requested_model.CREDITLINE,
                                 INITMARGINREQ=requested_model.INITMARGINREQ,
                                 MAINTAINMARGIN=requested_model.MAINTAINMARGIN,
                                 OPERTYPE=requested_model.OPERTYPE,
                                 TRANSCODE=requested_model.TRANSCODE,
                                 UTIID=requested_model.UTIID,
                                 TRANSCONFIRNUMBER=requested_model.TRANSCONFIRNUMBER,
                                 TRANSCONFIRTIME=requested_model.TRANSCONFIRTIME,
                                 EFFECTDATE=requested_model.EFFECTDATE,
                                 EXPIREDATE=requested_model.EXPIREDATE,
                                 EXERCISEDATE=requested_model.EXERCISEDATE,
                                 EARLYTERMDATE=requested_model.EARLYTERMDATE,
                                 SUBJMATTERINFO=requested_model.SUBJMATTERINFO,
                                 DIRECTREPPARTY=requested_model.DIRECTREPPARTY,
                                 ASSETTYPE=requested_model.ASSETTYPE,
                                 TOOLTYPE=requested_model.TOOLTYPE,
                                 OPTEXERCTMTYPE=requested_model.OPTEXERCTMTYPE,
                                 OPTRIGHTTYPE=requested_model.OPTRIGHTTYPE,
                                 RTVALMEPAY=requested_model.RTVALMEPAY,
                                 UNDERASSTYPE=requested_model.UNDERASSTYPE,
                                 UNDERASSVARIT=requested_model.UNDERASSVARIT,
                                 STANDASSCONT=requested_model.STANDASSCONT,
                                 STANDASSTRADPLC=requested_model.STANDASSTRADPLC,
                                 GENERALNAMNUM=requested_model.GENERALNAMNUM,
                                 VALUUNIT=requested_model.VALUUNIT,
                                 EXECUTPRICE=requested_model.EXECUTPRICE,
                                 ASSENTRYPRICE=requested_model.ASSENTRYPRICE,
                                 PRICESYMBOL=requested_model.PRICESYMBOL,
                                 ACCOUNTMONEY=requested_model.ACCOUNTMONEY,
                                 NOMINALAMOUNT=requested_model.NOMINALAMOUNT,
                                 PREMIUMAMOUNT=requested_model.PREMIUMAMOUNT,
                                 CONTRACTVALUE=requested_model.CONTRACTVALUE,
                                 VALMETHOD=requested_model.VALMETHOD,
                                 SETTMETHOD=requested_model.SETTMETHOD,
                                 FINALSETTDAY=requested_model.FINALSETTDAY,
                                 SETTPRIMETHED=requested_model.SETTPRIMETHED,
                                 ONEPRICE=requested_model.ONEPRICE,
                                 COMMREFPRICE=requested_model.COMMREFPRICE,
                                 STATUS=requested_model.STATUS,
                                 LEGID=requested_model.LEGID,
                                 TRADEQAUNTITY=requested_model.TRADEQAUNTITY,
                                 PARTICIPATIONRATE=requested_model.PARTICIPATIONRATE,
                                 ANNUALIZED=requested_model.ANNUALIZED,
                                 EXCHANGERATE=requested_model.EXCHANGERATE,
                                 TRADENOTIONAL=requested_model.TRADENOTIONAL,
                                 UPDATEAT=datetime.datetime.now())

            models.append(item)

        return models

    @staticmethod
    def save_r_otc_position(db_session, r_otc_position_dict, existing_record_ids, start_date, end_date, force_update):
        """
        保存r_otc_position数据到数据平台
        :param db_session:
        :param r_otc_position_dict:
        :param existing_record_ids:
        :param start_date:
        :param end_date:
        :param force_update:
        :return:
        """
        # 强制更新
        if force_update is False:
            for record_id in existing_record_ids:
                if record_id in r_otc_position_dict:
                    del r_otc_position_dict[record_id]
            missing_r_otc_positions = list(r_otc_position_dict.values())
            # 保存数据
            if len(missing_r_otc_positions) != 0:
                logger.info('时间段为: %s至%s内,准备从保证金中心的r_otc_positioin表中导入%d条数据到数据平台' %
                            (start_date, end_date, len(missing_r_otc_positions)))
                db_session.add_all(missing_r_otc_positions)
                db_session.commit()
                logger.info('时间段为: %s至%s内,成功的从保证金中心的r_otc_positioin表中导入了%d条数据到数据平台' %
                            (start_date, end_date, len(missing_r_otc_positions)))
            else:
                logger.info('时间段为: %s至%s内,没有数据需要从r_otc_positioin表中导入到数据平台' % (start_date, end_date))
        else:
            r_otc_positions = list(r_otc_position_dict.values())
            if len(r_otc_positions) != 0:
                # 删除
                ROTCPositionRepo.delete_data_by_dates(db_session, start_date, end_date)
                # 保存数据
                logger.info('时间段为: %s至%s内,准备从保证金中心的r_otc_positioin表中导入%d条数据到数据平台' %
                            (start_date, end_date, len(r_otc_positions)))
                db_session.add_all(r_otc_positions)
                db_session.commit()
                logger.info('时间段为: %s至%s内,成功的从保证金中心的r_otc_positioin表中导入了%d条数据到数据平台' %
                            (start_date, end_date, len(r_otc_positions)))
            else:
                logger.info('时间段为: %s至%s内,保证金中心没有数据,无需强制更新')

    @staticmethod
    def save_r_otc_tradedata(db_session, r_otc_tradedata_dict, existing_record_ids, start_date, end_date, force_update):
        """
        保存r_otc_tradedata数据到数据平台
        :param db_session:
        :param r_otc_tradedata_dict:
        :param existing_record_ids:
        :param start_date:
        :param end_date:
        :param force_update:
        :return:
        """
        # 强制更新
        if force_update is False:
            for record_id in existing_record_ids:
                if record_id in r_otc_tradedata_dict:
                    del r_otc_tradedata_dict[record_id]
            missing_r_otc_tradedatas = list(r_otc_tradedata_dict.values())
            # 保存数据
            if len(missing_r_otc_tradedatas) != 0:
                logger.info('时间段为: %s至%s内,准备从保证金中心的r_otc_tradedata表中导入%d条数据到数据平台' %
                            (start_date, end_date, len(missing_r_otc_tradedatas)))
                db_session.add_all(missing_r_otc_tradedatas)
                db_session.commit()
                logger.info('时间段为: %s至%s内,成功的从保证金中心的r_otc_tradedata表中导入了%d条数据到数据平台' %
                            (start_date, end_date, len(missing_r_otc_tradedatas)))
            else:
                logger.info('时间段为: %s至%s内,没有数据需要从r_otc_tradedata表中导入到数据平台' % (start_date, end_date))
        else:
            r_otc_tradedatas = list(r_otc_tradedata_dict.values())
            if len(r_otc_tradedatas) != 0:
                # 删除
                ROTCTradedataRepo.delete_data_by_dates(db_session, start_date, end_date)
                # 保存数据
                logger.info('时间段为: %s至%s内,准备从保证金中心的r_otc_tradedata表中导入%d条数据到数据平台' %
                            (start_date, end_date, len(r_otc_tradedatas)))
                db_session.add_all(r_otc_tradedatas)
                db_session.commit()
                logger.info('时间段为: %s至%s内,成功的从保证金中心的r_otc_tradedata表中导入了%d条数据到数据平台' %
                            (start_date, end_date, len(r_otc_tradedatas)))
            else:
                logger.info('时间段为: %s至%s内,保证金中心没有数据,无需强制更新')

    @staticmethod
    def r_otc_objs_to_dict(r_otc_objs):
        r_otc_obj_dict = {}
        for r_otc_obj in r_otc_objs:
            r_otc_obj_dict[str(r_otc_obj.RECORDID)] = r_otc_obj
        return r_otc_obj_dict

    @staticmethod
    def import_otc_trade_snapshots(start_date, end_date, force_update):
        """
        根据开始结束日期导入oracle数据库中的保证金中心r_otc_position和r_otc_tradedate数据
        :param start_date:
        :param end_date:
        :param force_update: 强制更新
        :return:
        """
        db_session = Session()
        try:
            logger.info('开始导入: %s 至 %s的数据' % (start_date, end_date))
            # 时间段分割
            date_quantum_list = DateTimeUtils.date_quantum_partition(start_date, end_date, step=0)
            for dates in date_quantum_list:
                start_date, end_date = dates[0], dates[1]
                # r_otc_postion
                r_otc_positions = OracleROTCPositionRepo.get_otc_position_by_date(start_date, end_date)
                if len(r_otc_positions) != 0:
                    # 转换数据
                    r_otc_positions = OTCTradeSnapshotsService.transform_r_otc_position_model(r_otc_positions)
                    r_otc_position_dict = OTCTradeSnapshotsService.r_otc_objs_to_dict(r_otc_positions)
                    # 查询数据平台数据
                    existing_record_ids = ROTCPositionRepo.get_record_ids_by_date(db_session, start_date, end_date)
                    # 保存到数据平台
                    OTCTradeSnapshotsService.save_r_otc_position(db_session, r_otc_position_dict, existing_record_ids,
                                                                 start_date, end_date, force_update)

                # r_otc_tradedata
                r_otc_tradedatas = OracleROTCTradedataRepo.get_otc_tradedata_by_date(start_date, end_date)
                if len(r_otc_tradedatas) != 0:
                    # 转换数据
                    r_otc_tradedatas = OTCTradeSnapshotsService.transform_r_otc_tradedata_model(r_otc_tradedatas)
                    r_otc_tradedata_dict = OTCTradeSnapshotsService.r_otc_objs_to_dict(r_otc_tradedatas)
                    # 查询数据平台数据
                    existing_record_ids = ROTCTradedataRepo.get_record_ids_by_date(db_session, start_date, end_date)
                    # 保存到数据平台
                    OTCTradeSnapshotsService.save_r_otc_tradedata(db_session, r_otc_tradedata_dict, existing_record_ids,
                                                                  start_date, end_date, force_update)
        except Exception as e:
            logger.error('导入数据异常, 异常信息为: %s' % e)
            db_session.close()
            raise Exception(e)


if __name__ == '__main__':
    start_date, end_date = datetime.date(2019, 9, 25), datetime.date(2019, 9, 25)
    OTCTradeSnapshotsService.import_otc_trade_snapshots(start_date, end_date, force_update=False)
