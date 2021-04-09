# -*- coding: utf-8 -*-
from dags.utils import Client
from dags.dbo import create_db_session
import json
import dags.dbo.tonglian_model as tonglian
from dags.utils import model_converter
from terminal.utils.datetime_utils import DateTimeUtils
from dags.utils.model_converter import normalize_orgcode, index_code_start, zhongzheng_info
from terminal.utils import Logging
from datetime import datetime
from terminal.dto import InstrumentType, AssetClassType, InstrumentStatusType, DataSourceType, OptionTradedType
from terminal.dbo import OptionStructure, FutureStructure, IndexStructure, Instrument
from terminal.dao import InstrumentRepo, OptionStructureRepo, FutureStructureRepo

logging = Logging.getLogger(__name__)


class InstrumentListUpdateService(object):
    @staticmethod
    def request_remote_data(url):
        # 调API获取数据
        client = Client()
        # 请求数据
        code, result = client.getData(url)
        # 解析为JSON数据
        ret = json.loads(result)
        # 返回代码不为1，则抛出异常
        if ret['retCode'] != 1:
            raise Exception('获取远程数据异常，url：%s，返回结果：%s' % (url, ret))
        return ret

    @staticmethod
    def request_option_structure():
        # 请求数据
        url = '/api/options/getOpt.json?field='
        ret = InstrumentListUpdateService.request_remote_data(url)
        structure_dict = {}
        # TODO: 需要在统一的一个地方维护认购和认沽
        option_type = {'CO': 'CALL', 'PO': 'PUT'}
        for data in ret['data']:
            instrument_id = data.get('optID', data.get('tickerSymbol')).upper()
            # 大宗取主力作为其标的物
            underlier = model_converter.convertToStd(
                tonglian.Instrument(instrumentId=data['varTicker'], exchangeId=data['varExchangeCD'], assetClass=None))
            trade_code = data.get('tickerSymbol').upper()
            # 转化为underlier
            expire_date = DateTimeUtils.str2date(data['expDate'])
            asset_class = model_converter.get_std_asset_class(data['varExchangeCD'])
            if asset_class == AssetClassType.COMMODITY.name:
                contract_type = model_converter.get_future_contract_type(underlier)
            else:
                contract_type = None
            structure = OptionStructure(
                instrumentId=instrument_id, shortName=data['varShortName'],
                listedDate=DateTimeUtils.str2date(data['listDate']), delistedDate=expire_date,
                assetClass=asset_class, instrumentType=InstrumentType.OPTION.name,
                tradeCode=trade_code, fullName=data['secShortName'], underlier=underlier,
                contractType=contract_type,
                optionType=option_type[data['contractType']],
                exercisePrice=data['strikePrice'], contractUnit=data['contMultNum'],
                expireDate=expire_date,
                exerciseDate=DateTimeUtils.str2date(data['expDate']),
                settlementDate=DateTimeUtils.str2date(data['expDate'])
            )
            structure_dict[instrument_id] = structure
        logging.info('获取到总共%d个Option，分别为：%s' % (len(structure_dict.keys()), ','.join(structure_dict.keys())))
        return structure_dict

    @staticmethod
    def request_future_structure():
        # 请求数据
        url = '/api/future/getFutu.json?field='
        ret = InstrumentListUpdateService.request_remote_data(url)
        future_structure_dict = {}
        for data in ret['data']:
            exchange_id = data['exchangeCD']
            instrument_id = model_converter.convertToStd(
                tonglian.Instrument(instrumentId=data['ticker'], exchangeId=exchange_id, assetClass=None))
            contract_type = model_converter.get_future_contract_type(instrument_id)
            listed_date = DateTimeUtils.str2date(data['listDate'])
            asset_class = model_converter.get_std_asset_class(exchange_id)
            structure = FutureStructure(
                instrumentId=instrument_id, shortName=data['secShortName'],
                listedDate=listed_date, delistedDate=DateTimeUtils.str2date(data['lastTradeDate']),
                assetClass=asset_class, instrumentType=InstrumentType.FUTURE.name,
                firstDeliDate=DateTimeUtils.str2date(data.get('firstDeliDate')),
                lastDeliDate=DateTimeUtils.str2date(data.get('lastDeliDate')),
                contMultNum=data['contMultNum'], contMultUnit=data['contMultUnit'],
                contractType=contract_type
            )
            # TODO 数据缺陷：未上市的标的不存在以下两项
            if structure.firstDeliDate is None or structure.lastDeliDate is None:
                pass
            else:
                future_structure_dict[instrument_id] = structure
        logging.info(
            '获取到总共%d个Future，分别为：%s' % (len(future_structure_dict.keys()), ','.join(future_structure_dict.keys())))
        return future_structure_dict

    @staticmethod
    def request_stock_structure():
        # 请求数据
        url = '/api/equity/getEqu.json?field='
        ret = InstrumentListUpdateService.request_remote_data(url)
        structure_dict = {}
        for data in ret['data']:
            instrument_id = model_converter.convertToStd(
                tonglian.Instrument(instrumentId=data['ticker'], exchangeId=data['exchangeCD'], assetClass=None))
            listed_date = data.get('listDate')
            if listed_date is None:
                logging.warning('该标的物没有上市日期：%s' % instrument_id)
                continue
            structure = FutureStructure(
                instrumentId=instrument_id, shortName=data['secShortName'],
                listedDate=DateTimeUtils.str2date(listed_date),
                delistedDate=DateTimeUtils.str2date(data.get('delistDate')),
                assetClass=AssetClassType.EQUITY.name, instrumentType=InstrumentType.STOCK.name)
            structure_dict[instrument_id] = structure
        logging.info('获取到总共%d个Stock，分别为：%s' % (len(structure_dict.keys()), ','.join(structure_dict.keys())))
        return structure_dict

    @staticmethod
    def request_fund_structure():
        # 请求数据
        url = '/api/fund/getFund.json?field='
        ret = InstrumentListUpdateService.request_remote_data(url)
        structure_dict = {}
        for data in ret['data']:
            exchange_id = data.get('exchangeCd')
            if exchange_id is None:
                logging.warning('该标的物没有交易所信息：%s' % data['secID'])
                continue
            instrument_id = model_converter.convertToStd(
                tonglian.Instrument(instrumentId=data['ticker'], exchangeId=data['exchangeCd'], assetClass=None))
            listed_date = data.get('listDate')
            if listed_date is None:
                logging.warning('该标的物没有上市日期：%s' % instrument_id)
                continue
            structure = FutureStructure(
                instrumentId=instrument_id, shortName=data['secShortName'],
                listedDate=DateTimeUtils.str2date(listed_date),
                delistedDate=DateTimeUtils.str2date(data.get('delistDate')),
                assetClass=AssetClassType.EQUITY.name, instrumentType=InstrumentType.FUND.name)
            structure_dict[instrument_id] = structure
        logging.info('获取到总共%d个Fund，分别为：%s' % (len(structure_dict.keys()), ','.join(structure_dict.keys())))
        return structure_dict

    @staticmethod
    # 实现获取指数的基本信息
    def request_index_structure():
        # ticker_blacklist = ['000188']
        idx_valid_list = normalize_orgcode.keys()
        logging.info('开始获取指数')
        # 通联数据api http://apidoc.datayes.com/app/APIDetail/20
        url = '/api/idx/getIdx.json?field='
        ret = InstrumentListUpdateService.request_remote_data(url)
        structure_dict = {}
        for data in ret['data']:
            exchange_id = data.get('pubOrgCD')
            ticker = data['ticker']
            ticker_start = str(ticker)[0]
            if exchange_id in idx_valid_list and ticker_start in index_code_start:
                instrument_id = model_converter.convert_idx_to_std(
                    tonglian.Instrument(instrumentId=data['ticker'], exchangeId=exchange_id, assetClass=None))
                listed_date = data.get('publishDate')
                if listed_date is None:
                    logging.warning('该标的物没有上市日期：%s' % instrument_id)
                    continue
                structure = IndexStructure(
                    instrumentId=instrument_id, shortName=data['secShortName'],
                    listedDate=DateTimeUtils.str2date(listed_date),
                    delistedDate=DateTimeUtils.str2date(data.get('endDate')),
                    assetClass=AssetClassType.EQUITY.name, instrumentType=InstrumentType.INDEX.name)
                structure_dict[instrument_id] = structure
            # NOTE! 中证交易所 特判处理
            elif exchange_id == zhongzheng_info['org_id'] and \
                    data.get('secID').endswith(zhongzheng_info['end_fix']) and \
                    ticker_start in index_code_start:
                instrument_id = data['ticker'].upper() + "." + index_code_start[ticker_start]
                listed_date = data.get('publishDate')
                if listed_date is None:
                    logging.warning('该标的物没有上市日期：%s' % instrument_id)
                    continue
                structure = IndexStructure(
                    instrumentId=instrument_id, shortName=data['secShortName'],
                    listedDate=DateTimeUtils.str2date(listed_date),
                    delistedDate=DateTimeUtils.str2date(data.get('endDate')),
                    assetClass=AssetClassType.EQUITY.name, instrumentType=InstrumentType.INDEX.name)
                structure_dict[instrument_id] = structure
            else:
                sec_id = data.get('secID')
                logging.warning('该指数证券市场在万德中无法匹配:%s' % sec_id)
        logging.info('获取到总共%d个Index，分别为：%s' % (len(structure_dict.keys()), ','.join(structure_dict.keys())))
        return structure_dict

    @staticmethod
    def get_exchange_traded_instrument_ids(option_structure_dict):
        instrument_ids = []
        for item in option_structure_dict.values():
            if item.instrumentId not in instrument_ids:
                instrument_ids.append(item.instrumentId)
            if item.underlier not in instrument_ids:
                instrument_ids.append(item.underlier)
        logging.info('获取到的场内标的物为：%s' % ','.join(instrument_ids))
        return instrument_ids

    @staticmethod
    def recon_instrument_list(existing_instrument_dict, current_market_data_dict, fail_fast=False):
        active_instrument_ids = set()
        for key in existing_instrument_dict.keys():
            value = existing_instrument_dict[key]
            if value.status == InstrumentStatusType.ACTIVE.name:
                active_instrument_ids.add(key)
        missing_instrument_ids = []
        extra_instrument_ids = []
        # 缺失的instrument ids
        for id in current_market_data_dict.keys():
            # Future如果不在总的标的物列表中，则加入到MISSING列表
            if current_market_data_dict[id].instrumentType == InstrumentType.FUTURE.name \
                    and id not in existing_instrument_dict:
                missing_instrument_ids.append(id)
            if current_market_data_dict[id].instrumentType != InstrumentType.FUTURE.name \
                    and id not in active_instrument_ids:
                missing_instrument_ids.append(id)
        # 多余的instrument ids
        for id in active_instrument_ids:
            if id not in current_market_data_dict:
                extra_instrument_ids.append(id)
        logging.info('发现%d个缺失的标的物：%s' % (len(missing_instrument_ids), ','.join(missing_instrument_ids)))
        logging.info('发现%d个多余的标的物：%s' % (len(extra_instrument_ids), ','.join(extra_instrument_ids)))
        if len(missing_instrument_ids) and len(extra_instrument_ids) and fail_fast:
            raise Exception('Recon结果不为空')
        return missing_instrument_ids, extra_instrument_ids

    @staticmethod
    def get_upsert_instrument_list(db_session, instrument_structure_dict, exchange_traded_instrument_ids):
        existing_instruments_dict = InstrumentRepo.load_instruments_dict(db_session, active_only=False)
        logging.info('开始更新标的物列表')
        instrument_list = []
        for instrument_id in instrument_structure_dict.keys():
            structure = instrument_structure_dict[instrument_id]
            if instrument_id not in existing_instruments_dict:
                instrument = Instrument(instrumentId=structure.instrumentId, instrumentType=structure.instrumentType,
                                        shortName=structure.shortName, listedDate=structure.listedDate,
                                        delistedDate=structure.delistedDate, assetClass=structure.assetClass,
                                        dataSource=DataSourceType.TONGLIAN.name, updatedAt=datetime.now())
                # 该标的物对应的期权是场内还是场外交易
                instrument.optionTradedType = OptionTradedType.OTC.name
                if instrument.instrumentId in exchange_traded_instrument_ids:
                    instrument.optionTradedType = OptionTradedType.EXCHANGE_TRADED.name
                # 更新期货的合约类型
                if instrument.instrumentType == InstrumentType.FUTURE.name:
                    instrument.contractType = structure.contractType
                    instrument.status = InstrumentStatusType.ACTIVE.name
                if instrument.instrumentType == InstrumentType.OPTION.name:
                    instrument.status = InstrumentStatusType.ACTIVE.name
                instrument_list.append(instrument)
                logging.info('准备新增标的物到数据库：%s' % instrument_id)
            else:
                instrument = existing_instruments_dict[instrument_id]
                if instrument.delistedDate is None and structure.delistedDate is not None:
                    instrument.delistedDate = structure.delistedDate
                    instrument.updatedAt = datetime.now()
                    logging.info('准备更新标的物退市日期：%s -> %s' %
                                 (instrument_id, DateTimeUtils.date2str(instrument.delistedDate)))
                if instrument.instrumentId in exchange_traded_instrument_ids \
                        and instrument.optionTradedType != OptionTradedType.EXCHANGE_TRADED.name:
                    instrument.optionTradedType = OptionTradedType.EXCHANGE_TRADED.name
                    instrument.updatedAt = datetime.now()
                    logging.info('准备更新标的物对应期权交易方式：%s -> %s' % (instrument_id, instrument.optionTradedType))
        return instrument_list

    @staticmethod
    def update_instrument_list(dry_run=False):
        # 请求所有的标的物结构
        option_structure_dict = InstrumentListUpdateService.request_option_structure()
        future_structure_dict = InstrumentListUpdateService.request_future_structure()
        stock_structure_dict = InstrumentListUpdateService.request_stock_structure()
        fund_structure_dict = InstrumentListUpdateService.request_fund_structure()
        index_structure_dict = InstrumentListUpdateService.request_index_structure()

        # 获取到所有的标的物结构
        all_instrument_structure_dict = {}
        all_instrument_structure_dict.update(option_structure_dict)
        all_instrument_structure_dict.update(future_structure_dict)
        all_instrument_structure_dict.update(stock_structure_dict)
        all_instrument_structure_dict.update(fund_structure_dict)
        all_instrument_structure_dict.update(index_structure_dict)
        db_session = create_db_session()
        try:
            if not dry_run:
                # 更新期权结构列表
                existing_option_structure_dict = OptionStructureRepo.load_option_structure_dict(db_session)
                print(existing_option_structure_dict)
                FutureStructureRepo.update_structure_list(db_session, option_structure_dict,
                                                          existing_option_structure_dict)
                # 更新期货结构列表
                existing_future_structure_dict = FutureStructureRepo.load_future_structure_dict(db_session)
                FutureStructureRepo.update_structure_list(db_session, future_structure_dict,
                                                          existing_future_structure_dict)
                # 获取拥有场内期权的标的物
                exchange_traded_instrument_ids = InstrumentListUpdateService.get_exchange_traded_instrument_ids(
                    option_structure_dict)
                # 更新标的物列表
                instrument_list = InstrumentListUpdateService.get_upsert_instrument_list(db_session,
                                                                                         all_instrument_structure_dict,
                                                                                         exchange_traded_instrument_ids)
                InstrumentRepo.upsert_instrument_list(db_session, instrument_list)
                InstrumentListUpdateService.update_instrument_status(db_session)

        finally:
            db_session.close()

    @staticmethod
    def update_instrument_status(db_session):
        InstrumentRepo.update_instrument_status(db_session)


if __name__ == '__main__':
    InstrumentListUpdateService.update_instrument_list(dry_run=False)
    # update_instrument_status(date(2018, 5, 1), dry_run=False)
