import uuid
from datetime import date, timedelta, datetime
from dags.conf.settings import BCTSpecialConfig
from dags.dao import RealizedVolRepo
from dags.dbo import create_db_session, RealizedVol
from terminal.dao import InstrumentRepo
from terminal.utils import logging
from terminal.service import HistoricalVolService


class RealizedVolService:
    windows = [1, 5, 22, 242]

    @staticmethod
    def save_realized_vol(db_session, valuation_date, realized_vol_dict, existed_realized_vol_dict, force_update):
        missing_realized_vol_list, existed_realized_vol_list, uuids = [], [], []
        for instrument_id, windows_dict in realized_vol_dict.items():
            for windows, realized_vol in windows_dict.items():
                if instrument_id in existed_realized_vol_dict and windows in existed_realized_vol_dict[instrument_id]:
                    existed_realized_vol_list.append(realized_vol)
                    uuids.append(existed_realized_vol_dict[instrument_id][windows].uuid)
                else:
                    missing_realized_vol_list.append(realized_vol)
        if len(missing_realized_vol_list) != 0:
            # 保存历史波动率
            RealizedVolRepo.save_realized_vol_list(db_session, missing_realized_vol_list)
        else:
            logging.info('交易日为: %s时, 没有历史波动率需要保存' % valuation_date)

        # 强制更新
        if force_update is True:
            if len(existed_realized_vol_list) != 0:
                # 删除历史波动率
                RealizedVolRepo.delete_realized_vol_by_uuids(db_session, uuids)
                # 保存历史波动率
                RealizedVolRepo.save_realized_vol_list(db_session, existed_realized_vol_list)
            else:
                logging.info('交易日为: %s时, 没有历史波动率需要强制更新' % valuation_date)

    @staticmethod
    def update_day_instrument_realized_vol(db_session, instrument_id_dict, instruments, valuation_date):
        """
        保存一天所有标的的历史波动率
        :param db_session:
        :param instrument_id_dict:
        :param instruments:
        :param valuation_date:
        :return:
        """
        logging.info('开始计算交易日: %s的历史波动率' % valuation_date)
        failed_instrument_id_dict = {}
        realized_vol_dict = {}
        for index, instrument in enumerate(instruments):
            instrument_id = instrument.instrumentId
            try:
                if instrument_id_dict[instrument_id] in BCTSpecialConfig.instrument_types:
                    # 计算realized vol 时, 过滤掉基金, 期权
                    logging.debug('instrument_id: %s 不支持realized_vol计算' % instrument_id)
                    continue
                logging.debug('开始计算: %s的历史波动率, 进度为: %d of %d' % (instrument_id, index, len(instruments)))
                realized_vol_dto_list, diagnostics = HistoricalVolService. \
                    calc_instrument_realized_vol(db_session, instrument_id, valuation_date,
                                                 windows=RealizedVolService.windows, is_primary=True)
                if len(realized_vol_dto_list) == 0:
                    failed_instrument_id_dict[instrument_id] = instrument_id_dict[instrument_id]
                for realized_vol_dto in realized_vol_dto_list:
                    realized_vol = RealizedVol(
                        uuid=str(uuid.uuid1()),
                        instrumentId=instrument_id,
                        valuationDate=valuation_date,
                        vol=realized_vol_dto.vol,
                        exfsid=instrument.contractType,
                        windows=realized_vol_dto.window,
                        updatedAt=datetime.now()
                    )
                    if realized_vol.instrumentId not in realized_vol_dict:
                        realized_vol_dict[realized_vol.instrumentId] = {}
                    realized_vol_dict[realized_vol.instrumentId][realized_vol.windows] = realized_vol
            except Exception as e:
                logging.error('计算: %s的历史波动率失败, 错误: %s' % (instrument_id, e))
                failed_instrument_id_dict[instrument_id] = instrument_id_dict[instrument_id]
        logging.info('完成计算交易日: %s的历史波动率，成功%s条，失败%s条' % (valuation_date, len(realized_vol_dict),
                                                        len(failed_instrument_id_dict)))
        return realized_vol_dict, failed_instrument_id_dict

    @staticmethod
    def update_days_instrument_realized_vol(start_date, end_date, force_update=False):
        """
        保存不同交易日的标的历史波动率
        :param start_date:
        :param end_date:
        :param force_update:
        :return:
        """
        db_session = create_db_session
        while start_date <= end_date:
            try:
                valuation_date = start_date
                # 获取全量instrument表数据
                instrument_id_dict = {}
                for instrument in InstrumentRepo.get_all_instrument(db_session):
                    instrument_id_dict[instrument.instrumentId] = instrument.instrumentType
                # 获取instrument id list
                instruments = InstrumentRepo.get_instruments_by_trade_date(db_session, valuation_date)
                # 获取valuation_date的标的历史波动率
                realized_vol_dict, failed_instrument_id_dict = RealizedVolService. \
                    update_day_instrument_realized_vol(db_session, instrument_id_dict, instruments, valuation_date)
                # 获取valuation_date已经存在的标的历史波动率
                existed_realized_vol_dict = RealizedVolRepo. \
                    get_realized_vol_dict_by_valuation_date(db_session, valuation_date)
                # 保存历史波动率
                RealizedVolService. \
                    save_realized_vol(db_session, valuation_date, realized_vol_dict, existed_realized_vol_dict,
                                      force_update)
                start_date = start_date + timedelta(days=1)
                if len(failed_instrument_id_dict) != 0:
                    failed_length = len(failed_instrument_id_dict)
                    total_length = len(instruments)
                    logging.error('交易日为: %s, 计算历史波动率失败为 %d of %d, 标的id为: %s' %
                                  (valuation_date, failed_length, total_length, failed_instrument_id_dict))
            except Exception as e:
                db_session.close()
                raise e
        db_session.close()


if __name__ == '__main__':
    start_date, end_date = date(2019, 10, 25), date(2019, 10, 25)
    RealizedVolService.update_days_instrument_realized_vol(start_date, end_date, force_update=True)
