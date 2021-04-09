import time
from datetime import datetime, date, timedelta
from dags.utils import *
from dags.dao import ROTCPositionRepo, ROTCTradedataRepo
from terminal.dao import OTCPositionSnapshotRepo
from terminal.dbo import OTCPositionSnapshot
from terminal.utils import DateTimeUtils
from terminal.utils import logging
from dags.dbo import create_db_session


class ROTCPositionService:

    @staticmethod
    def save_position_snapshot(db_session, report_date, exiting_trans_codes, position_snapshots, force_update):
        """
        保存数据
        :param db_session:
        :param report_date:
        :param exiting_trans_codes:
        :param position_snapshots:
        :param force_update:
        :return:
        """
        exiting_trans_code_dict = {}
        for trans_code in exiting_trans_codes:
            exiting_trans_code_dict[trans_code] = trans_code
        missing_position_snapshots = []
        for position_snapshot in position_snapshots:
            if exiting_trans_code_dict.get(position_snapshot.transCode) is None:
                missing_position_snapshots.append(position_snapshot)
        if force_update is True:
            # 强制更新
            # 删除已经存在的
            if len(position_snapshots) != 0:
                OTCPositionSnapshotRepo.delete_position_snapshot_by_report_date(db_session, report_date)
                logging.info('开始强制更新数据, 长度为%d' % (len(position_snapshots)))
                db_session.add_all(position_snapshots)
                db_session.commit()
                logging.info('保存数据成功')
            else:
                logging.info('没有需要强制更新的数据')
        else:
            if len(missing_position_snapshots) != 0:
                logging.info('开始保存数据, 长度为%d' % len(missing_position_snapshots))
                db_session.add_all(missing_position_snapshots)
                db_session.commit()
                logging.info('保存数据成功')
            else:
                logging.info('没有缺失的数据')

    @staticmethod
    def convert_data_to_position_snapshot(clean_position_dict):
        """
        格式化为otc_position_snapshot表model
        :param clean_position_dict:
        :return:
        """
        position_snapshots = []
        for trans_code, clean_position in clean_position_dict.items():
            # todo 需要删除continue
            if clean_position is None:
                continue
            report_date = DateTimeUtils.str2date(clean_position.get('report_date'))
            position_date = DateTimeUtils.str2date(clean_position.get('position_date'))
            item = OTCPositionSnapshot(transCode=clean_position.get('trans_code'),
                                       recordId=clean_position.get('record_id'),
                                       reportDate=report_date,
                                       positionDate=position_date,
                                       instrumentId=clean_position.get('underlyer'),
                                       mainBodyName=clean_position.get('main_body_name'),
                                       tradeNotional=clean_position.get('trade_notional'),
                                       impliedVol=clean_position.get('implied_vol'),
                                       interestRate=clean_position.get('interest_rate'),
                                       dividend=clean_position.get('dividend'),
                                       updatedAt=datetime.now())
            position_snapshots.append(item)
        return position_snapshots

    @staticmethod
    def organize_trade_data(position_data, trade_data):
        """
        :param position_data:
        :param trade_data:
        :return:
        """
        _DATE_FMT = '%Y-%m-%d'

        _DIRECTION_SELLER = 'SELLER'
        _DIRECTION_BUYER = 'BUYER'
        _UNIT_PERCENT = 'PERCENT'
        _UNIT_LOT = 'LOT'
        _UNIT_CNY = 'CNY'

        _OPTION_CALL = 'CALL'
        _OPTION_PUT = 'PUT'

        _SETTLE_CLOSE = 'CLOSE'
        _SETTLE_TWAP = 'TWAP'

        trade_id = position_data.TRANSCODE
        trade_origin_data = {
            'trade_id': trade_id,
            'book_name': position_data.MAINBODYNAME,
            'trade_date': datetime.strptime(position_data.TRANSCONFIRTIME, _DATE_FMT),
        }
        trade_origin_data['trader'] = trade_origin_data['book_name']
        counter_party_name = position_data.ANALOGUENAME
        if counter_party_name is None:
            raise RuntimeError('传输编号：' + trade_id + ',交易对手为null')
        # 期权结构逻辑
        product_type_data = position_data.OPTEXERCTMTYPE or trade_data.OPTEXERCTMTYPE
        if product_type_data in ['US']:
            product_type = 'VANILLA_AMERICAN'
        elif product_type_data in ['EU']:
            product_type = 'VANILLA_EUROPEAN'
        else:
            return None
        # 处理标的信息
        instrument_id = position_data.STANDASSCONT
        category = position_data.UNDERASSVARIT
        if '.' not in instrument_id:
            category = str(category).upper()
            if category in u_name_to_dec:
                dec = u_name_to_dec[category]
                exe = dec_to_exe[dec]
            elif category in u_type_to_dec:
                dec = u_type_to_dec[category]
                exe = dec_to_exe[dec]
            else:
                raise RuntimeError('交易品找不到对应的交易场所，交易品种：' + category)
            underlyer = str(instrument_id).upper() + '.' + exe
        else:
            underlyer = str(instrument_id).upper()
        # 处理看涨看跌逻辑
        option_type_data = position_data.OPTRIGHTTYPE or trade_data.OPTRIGHTTYPE
        if option_type_data in ['1', 'put', 'PUT', 'Put', 'P']:
            option_type = _OPTION_PUT
        elif option_type_data in ['2', 'call', 'CALL', 'Call', 'C']:
            option_type = _OPTION_CALL
        else:
            raise RuntimeError('传输编号：' + trade_id + ',权利类型错误：' + str(option_type_data))
        # 处理买卖方向逻辑
        direction_data = position_data.SIDE or trade_data.DIRECTREPPARTY
        if direction_data in ['B', 'BYER', '买']:
            direction = _DIRECTION_BUYER
        elif direction_data in ['S', 'SLLR', '卖']:
            direction = _DIRECTION_SELLER
        else:
            raise RuntimeError('传输编号：' + trade_id + ',买卖方向错误：' + str(direction_data))
        # 处理年化
        annualized_data = position_data.ANNUALIZED or trade_data.ANNUALIZED
        if annualized_data in ['Y', 'y']:
            annualized = True
        else:
            annualized = False
        days_in_year = position_data.EFFECTIVEDAY
        if days_in_year is None:
            days_in_year = 365
        # 参与率（暂时都为1）
        participation_rate = position_data.PARTICIPATERATE or trade_data.PARTICIPATIONRATE
        if participation_rate is None:
            participation_rate = 1
        #     raise RuntimeError('传输编号：' + trade_id + ',参与率为null')
        # 行权价
        strike = position_data.STRIKE or trade_data.EXECUTPRICE
        if strike is None:
            raise RuntimeError('传输编号：' + trade_id + ',行权价为null')
        # 期初价格
        init_spot = position_data.VALUATIONSPOT or trade_data.ASSENTRYPRICE
        if init_spot is None:
            raise RuntimeError('传输编号：' + trade_id + ',期初价格为null')
        # 名义本金
        notional = position_data.TRADENOTIONAL or trade_data.NOMINALAMOUNT
        if notional is None:
            raise RuntimeError('传输编号：' + trade_id + ',名义本金为null')
        # 期权费
        premium = trade_data.PREMIUMAMOUNT
        if premium is None:
            raise RuntimeError('传输编号：' + trade_id + ',期权费为null')
        # 生效日
        effective_date = trade_data.EFFECTDATE
        if effective_date is None:
            raise RuntimeError('传输编号：' + trade_id + ',起始日为null')
        effective_date = datetime.strptime(effective_date, _DATE_FMT)
        # 过期日
        expire_date = trade_data.EXPIREDATE
        if expire_date is None:
            raise RuntimeError('传输编号：' + trade_id + ',结束日为null')
        expire_date = datetime.strptime(expire_date, _DATE_FMT)
        # 结算日
        settle_date = trade_data.EXERCISEDATE
        if settle_date is None:
            settle_date = expire_date
        else:
            settle_date = datetime.strptime(settle_date, _DATE_FMT)
        # 结算方式
        settle_method = trade_data.SETTPRIMETHED
        if settle_method == '2':
            specified_price = _SETTLE_CLOSE
        elif settle_method == '3':
            specified_price = _SETTLE_TWAP
        else:
            specified_price = _SETTLE_CLOSE

        term = (expire_date - effective_date).days
        trade_origin_data['participation_rate'] = participation_rate
        trade_origin_data['counter_party'] = counter_party_name
        trade_origin_data['specified_price'] = specified_price
        trade_origin_data['effective_date'] = effective_date
        trade_origin_data['days_in_year'] = days_in_year
        trade_origin_data['product_type'] = product_type
        trade_origin_data['expire_date'] = expire_date
        trade_origin_data['settle_date'] = settle_date
        trade_origin_data['option_type'] = option_type
        trade_origin_data['annualized'] = annualized
        trade_origin_data['underlyer'] = underlyer
        trade_origin_data['direction'] = direction
        trade_origin_data['init_spot'] = init_spot
        trade_origin_data['notional'] = notional
        trade_origin_data['premium'] = premium
        trade_origin_data['strike'] = strike
        trade_origin_data['term'] = term

        trade_origin_data['front_premium'] = 0
        trade_origin_data['minimum_premium'] = 0
        trade_origin_data['implied_vol'] = float(
            position_data.IMPLIEDVOL) if position_data.IMPLIEDVOL else position_data.IMPLIEDVOL

        trade_origin_data['trans_code'] = position_data.TRANSCODE
        trade_origin_data['record_id'] = position_data.RECORDID
        trade_origin_data['report_date'] = position_data.REPORTDATE
        trade_origin_data['position_date'] = position_data.POSITIONDATE
        trade_origin_data['main_body_name'] = position_data.MAINBODYNAME
        trade_origin_data['trade_notional'] = float(
            position_data.TRADENOTIONAL) if position_data.TRADENOTIONAL else position_data.TRADENOTIONAL
        trade_origin_data['interest_rate'] = float(
            position_data.INTERESTRATE) if position_data.INTERESTRATE else position_data.INTERESTRATE
        trade_origin_data['dividend'] = float(
            position_data.DIVIDEND) if position_data.DIVIDEND else position_data.DIVIDEND

        return trade_origin_data

    @staticmethod
    def clean_one_day_implied_vol(db_session, report_date):
        """
        清洗一天的数据
        :param db_session:
        :param report_date:
        :return:
        """
        report_date = DateTimeUtils.date2str(report_date)
        trans_code_list = ROTCPositionRepo.get_trans_code_list_by_date(db_session, report_date)
        logging.info('正在获取持仓表数据长度为: %d' % (len(trans_code_list)))
        position_data_list = ROTCPositionRepo.get_otc_position_by_trans_codes(db_session, report_date, trans_code_list)

        trans_code_list = [position_data.TRANSCODE for position_data in position_data_list]
        logging.info('正在获取成交表数据,长度为: %d' % (len(trans_code_list)))
        trade_data_dict = ROTCTradedataRepo.get_trade_data_by_trans_codes(db_session, report_date, trans_code_list)

        clean_position_dict = {}
        for index, position_data in enumerate(position_data_list):
            trans_code = position_data.TRANSCODE
            trade_data = trade_data_dict.get(trans_code)
            if trade_data is not None:
                try:
                    logging.debug('正在组合数据, 当前进度为: %d of %d' % (index, len(position_data_list)))
                    clean_position = ROTCPositionService.organize_trade_data(position_data, trade_data)
                    implied_vol = clean_position.get('implied_vol')
                    if implied_vol is not None:
                        clean_position_dict[trans_code] = clean_position
                    else:
                        # TODO: 如果没有implied_vol，则直接根据价格反推隐含波动率，按以下规则进行
                        # 除了价格，利率 / 分红，还需要检查行权价，到期日和期权类型：
                        # 行权价可能需要注意是否为百分比，如果为百分比，需要继续检查是否有期初价格。
                        # 反推隐含波动率需要知道每份期权的价格，因此价格需要检查是否有对应的数量。没有数量，则需要名义本金，参与率，期限（如果年化），期初价格。
                        clean_position_dict[trans_code] = None
                except Exception as e:
                    logging.error('交易数据转化错误，trans_code: %s, 异常：%s' % (trans_code, str(e)))
        return clean_position_dict

    @staticmethod
    def update_implied_vol(start_date, end_date, force_update):
        """
        根据日期循环导入数到数据平台otc_position_snapshot表
        :param start_date:
        :param end_date:
        :param force_update:
        :return:
        """
        db_session = create_db_session()
        try:
            for index in range(0, (end_date - start_date).days + 1):
                report_date = (start_date + timedelta(index))
                logging.info('开始获取report_date: %s的数据' % report_date)
                clean_position_dict = ROTCPositionService.clean_one_day_implied_vol(db_session, report_date)
                position_snapshots = ROTCPositionService.convert_data_to_position_snapshot(clean_position_dict)
                exiting_trans_codes = OTCPositionSnapshotRepo.get_trans_codes_by_report_date(db_session, report_date)
                ROTCPositionService.save_position_snapshot(db_session, report_date, exiting_trans_codes,
                                                           position_snapshots, force_update)
        except Exception as e:
            logging.error('运行错误: %s' % e)
            db_session.close()
            raise Exception(e)


if __name__ == '__main__':
    start_date, end_date = date(2019, 10, 14), date(2019, 10, 14)
    ROTCPositionService.update_implied_vol(start_date, end_date, force_update=False)
