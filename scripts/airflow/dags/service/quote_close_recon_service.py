import dags.dbo.db_model as std
import json
from dags.utils.dataapi_utils import Client
import dags.utils.model_converter as converter_utils
from datetime import date
from dags.service.base_recon_service import BaseReconService
from terminal.utils import DateTimeUtils, Logging
from terminal.dto import InstrumentType, AssetClassType
from terminal.dbo import Instrument, QuoteClose
from dags.dao import QuoteCloseReconRepo, BaseReconRepo
from terminal.dbo import QuoteClose

logging = Logging.getLogger(__name__)


class QuoteCloseReconService(BaseReconService):
    def upsert_missing_trading_dates(self, db_session, instrument, missing_trading_dates):
        QuoteCloseReconRepo.upsert_quote_close_break(db_session, instrument, missing_trading_dates)

    def upsert_milestone(self, db_session, instrument, start_date, end_date, milestone):
        QuoteCloseReconRepo.upsert_quote_close_milestone(db_session, instrument, start_date, end_date, milestone)

    def get_milestone(self, db_session, instrument):
        return QuoteCloseReconRepo.get_quote_close_milestone(db_session, instrument)

    def request_quote_close_dates(self, instrument, start_date, end_date):
        if start_date > end_date:
            logging.warning("%s 起始日期大于结束日期" % instrument.instrumentId)
            return []
        client = Client()
        # TODO 需要放到配置文件或者从数据库读取
        logging.info(
            '开始获取标的物数据：[%s]' % (
                instrument.instrumentId))
        version = 'v1'
        tonglian_instrument = converter_utils.convertToTonglian(Instrument(
            instrumentId=instrument.instrumentId, instrumentType=instrument.instrumentType))
        if instrument.instrumentType == InstrumentType.STOCK.name:
            url = '/api/market/getMktEqud.json?field=&beginDate=%s&endDate=%s&ticker=%s' \
                  % (start_date.strftime('%Y%m%d'),
                     end_date.strftime('%Y%m%d'),
                     tonglian_instrument.instrumentId)
        elif instrument.instrumentType == InstrumentType.FUND.name:
            url = '/api/market/getMktFundd.json?field=&beginDate=%s&endDate=%s&ticker=%s' \
                  % (start_date.strftime('%Y%m%d'),
                     end_date.strftime('%Y%m%d'),
                     tonglian_instrument.instrumentId)
        elif instrument.instrumentType == InstrumentType.INDEX.name:
            url = '/api/market/getMktIdxd.json?field=&beginDate=%s&endDate=%s&ticker=%s' \
                  % (start_date.strftime('%Y%m%d'),
                     end_date.strftime('%Y%m%d'),
                     tonglian_instrument.instrumentId)
        # 接口兼容EQUITY / COMMODITY
        elif instrument.instrumentType == InstrumentType.FUTURE.name:
            url = '/api/market/getMktFutd.json?field=&beginDate=%s&endDate=%s&ticker=%s' \
                  % (start_date.strftime('%Y%m%d'),
                     end_date.strftime('%Y%m%d'),
                     tonglian_instrument.instrumentId.lower())
        elif instrument.instrumentType == InstrumentType.OPTION.name and \
                instrument.assetClass == AssetClassType.EQUITY.name:
            url = '/api/market/getMktOptd.json?field=&beginDate=%s&endDate=%s&optID=%s' \
                  % (start_date.strftime('%Y%m%d'),
                     end_date.strftime('%Y%m%d'),
                     tonglian_instrument.instrumentId.lower())
        elif instrument.instrumentType == InstrumentType.OPTION.name and \
                instrument.assetClass == AssetClassType.COMMODITY.name:
            url = '/api/market/getMktOptd.json?field=&beginDate=%s&endDate=%s&ticker=%s' \
                  % (start_date.strftime('%Y%m%d'),
                     end_date.strftime('%Y%m%d'),
                     tonglian_instrument.instrumentId.lower())
        # TODO 还有几个接口没有被使用 : getMktSpotd getMktBondd getMktComIdxd
        else:
            logging.info(
                "不支持处理标的物类型：%s -> %s" % (instrument.instrumentId, instrument.instrumentType))
            return []

        # 请求数据并进行处理
        code, result = client.getData(url)
        logging.debug(result)

        if code != 200:
            logging.error(
                "获取标的物数据异常：[%s], 远程服务器请求返回错误代码: %d" % (
                    instrument.instrumentId, code))
            logging.error(result)
            return []

        ret = json.loads(result)
        if ret['retCode'] != 1:
            logging.error(
                "获取标的物数据异常：[%s], 返回的结果: %s" % (
                    instrument.instrumentId, ret))
            return []

        close_datas = ret['data']
        close_date = []
        for item in close_datas:
            close_date.append(item['tradeDate'])
        return close_date

    # missing = all - existing
    def get_existing_trading_dates(self, db_session, instrument):
        return BaseReconRepo.get_existing_trading_dates(db_session, instrument, QuoteClose)

    # overwrite from base_recon
    def recon_one_instrument(self, params):
        instrument, start_date, end_date, holidays, dry_run, ignore_milestone = params
        db_session = std.create_db_session()
        try:
            logging.info("开始Recon标的物: %s" % instrument.instrumentId)
            # 获取标的物Milestone
            milestone = self.get_milestone(db_session, instrument)
            # 只处理标的物上市日期和退市日期之间的数据
            if instrument.listedDate is not None and start_date < instrument.listedDate:
                start_date = instrument.listedDate
            if instrument.delistedDate is not None and end_date > instrument.delistedDate:
                end_date = instrument.delistedDate
            # 需要Recon的日期由API给出
            quote_close_dates = self.request_quote_close_dates(instrument, start_date, end_date)
            if not quote_close_dates:
                unchecked_trading_dates = []
            else:
                unchecked_trading_dates = [DateTimeUtils.str2date(x) for x in quote_close_dates]
            # 对缺数数据进行处理
            existing_trading_dates = self.get_existing_trading_dates(db_session, instrument)
            # 计算出缺失数据
            missing_trading_dates = self.get_missing_trading_dates(instrument, unchecked_trading_dates,
                                                                   existing_trading_dates)
            if not dry_run:
                self.upsert_missing_trading_dates(db_session, instrument, missing_trading_dates)
            # TODO 需要对多余的数据进行处理
            # 计算出多余数据
            extra_trading_dates = []
            logging.info("Recon找到多余数据：%s -> %d 个Break" % (instrument.instrumentId, len(extra_trading_dates)))
            # 如果没有差值，则比较开始日期和Milestone，更新Milestone
            has_missing_dates = len(missing_trading_dates) != 0
            has_extra_dates = len(extra_trading_dates) != 0
            if (not has_missing_dates) and (not has_extra_dates) and (not dry_run):
                if start_date <= end_date:
                    self.upsert_milestone(db_session, instrument, start_date, end_date, milestone)
                else:
                    logging.info("标的物开始日期大于结束日期，不更新Milestone：%s [%s %s]" % (
                        instrument.instrumentId, start_date.strftime('%Y%m%d'), end_date.strftime('%Y%m%d')))

            return instrument, (not has_missing_dates) and (not has_extra_dates)
        except Exception as e:
            logging.error("Recon失败，标的物：%s, 异常：%s" % (instrument.instrumentId, e))
            return instrument, False
        finally:
            db_session.close()


if __name__ == '__main__':
    QuoteCloseReconService().recon_all_instrument(start_date=date(2019, 8, 26), end_date=date(2019, 9, 10),
                                                  fail_fast=False,
                                                  dry_run=False, active_only=True, ignore_milestone=True)
