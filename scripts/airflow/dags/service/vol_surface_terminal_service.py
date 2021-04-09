import time
from datetime import date
from dags.utils import server_utils
from terminal.utils import DateTimeUtils
from dags.dbo import create_redis_session, create_db_session
import traceback
from collections import Counter
import json
from terminal.dto import Constant, VolSurfaceSourceType
from terminal.dao import TradingCalendarRepo, VolSurfaceRepo
from terminal.utils import Logging


logging = Logging.getLogger(__name__)


def get_real_time_market_data_by_instrument_id(instrument_id):
    redis_session = create_redis_session()
    ret = redis_session.hget(Constant.REAL_TIME_MARKET_DATA, instrument_id)
    return json.loads(ret)


def get_otc_vol_surface_params(model_info, now_price, observed_date):
    """
    构建bct接口请求参数
    :param vol_surface:
    :param observed_date: 观察日
    :param percent_ls:
    :return:
    """

    model_info['useVolCalendar'] = True
    model_info['useCalendarForTenor'] = True
    model_info['calendars'] = ["DEFAULT_CALENDAR"]
    model_info['volCalendar'] = 'DEFAULT_VOL_CALENDAR'
    model_info['valuationDate'] = DateTimeUtils.date2str(observed_date)
    model_info['save'] = True
    # TODO 定义的结构中没有instance
    model_info['instance'] = model_info['underlyer']['instance']
    # now price
    model_info['underlyer']['quote'] = now_price
    return model_info


def update_one_instrument_to_terminal(vol_surface, header, now_price, observed_date):
    """
    :param vol_surface: 标的对象
    :param observed_date: 观察日
    :param header: 登陆头信息
    :return:
    """
    try:
        mark_vol_params = get_otc_vol_surface_params(vol_surface.modelInfo, now_price, observed_date)
        ret = server_utils.call_terminal_request('terminal-service', 'mdlVolSurfaceInterpolatedStrikeCreate',
                                                 mark_vol_params, header)
        if 'error' in ret:
            logging.info('写入标的物Vol Surface失败: %s; 失败的结果为: %s' % (vol_surface.instrumentId, ret))
            return False
        else:
            logging.info('写入标的物Vol Surface成功: %s' % vol_surface.instrumentId)
            return True
    except Exception as e:
        logging.error('写入标的物Vol Surface失败: %s, 异常错误为: %s' % (vol_surface.instrumentId, traceback.format_exc()))
    return False


def unique_instruments(instrument_list):
    """
    去除重复的标的(1.先取official,official有多个再取最新的; 2. 没有official再取other, other有多个再取最新的)
    :param instruments:
    :return:
    """

    instrument_ids = []
    for instrument in instrument_list:
        instrument_ids.append(instrument.instrumentId)

    remove_instrument_index = []
    for instrument_id, value in Counter(instrument_ids).items():
        # 找出重复的标的
        duplication_official_instruments = {}
        duplication_other_instruments = {}
        if value == 1:
            continue
        for index, j in enumerate(instrument_ids):
            if value == 0:
                break
            if j == instrument_id:
                value -= 1
                if instrument_list[index].source == VolSurfaceSourceType.OFFICIAL.name:
                    duplication_official_instruments[index] = instrument_list[index]
                if instrument_list[index].source == VolSurfaceSourceType.OTHER.name:
                    duplication_other_instruments[index] = instrument_list[index]
                if instrument_list[index].source == None:
                    duplication_other_instruments[index] = instrument_list[index]
        # official 有一个, 将other的删除
        if duplication_official_instruments and len(duplication_official_instruments.keys()) == 1:
            for duplication_other_instrument_index in duplication_other_instruments.keys():
                remove_instrument_index.append(duplication_other_instrument_index)
        # official 有两个或以上, 只要official最新的,将other和official不是最新的删除
        elif duplication_official_instruments and len(duplication_official_instruments.keys()) > 1:
            updated_at = []
            for duplication_official_instrument in duplication_official_instruments.values():
                updated_at.append(duplication_official_instrument.updatedAt)

            last_date = max(updated_at)
            for duplication_official_instrument_index, duplication_official_instrument in duplication_official_instruments.items():
                if duplication_official_instrument.updatedAt != last_date:
                    remove_instrument_index.append(duplication_official_instrument_index)

            for duplication_other_instrument_index in duplication_other_instruments.keys():
                remove_instrument_index.append(duplication_other_instrument_index)
        # 没有 official, other必有两个或以上
        elif not duplication_official_instruments:
            updated_at = []
            for duplication_other_instrument in duplication_other_instruments.values():
                updated_at.append(duplication_other_instrument.updatedAt)

            last_date = max(updated_at)
            for duplication_other_instrument_index, duplication_other_instrument in duplication_other_instruments.items():
                if duplication_other_instrument.updatedAt != last_date:
                    remove_instrument_index.append(duplication_other_instrument_index)

    # 不重复的标的
    unique_instruments = []
    for index, instrument in enumerate(instrument_list):
        if index in remove_instrument_index:
            continue
        unique_instruments.append(instrument)

    return unique_instruments


def update_all_instrument_vol_surface_to_terminal(observed_date):
    db_session = create_db_session()
    holidays = TradingCalendarRepo.get_holiday_list(db_session)
    special_dates = []
    if not DateTimeUtils.is_trading_day(observed_date, holidays, special_dates):
        observed_date = DateTimeUtils.get_trading_day(observed_date, holidays, special_dates, 1, -1)

    # 登陆
    logging.info('update all instrument vol surface to bct观察日为: %s' % DateTimeUtils.date2str(observed_date))
    header = server_utils.login_terminal()

    # 从终端加载需要的标的物
    terminal_instrument_ids = [instrument['instrumentId'] for instrument in server_utils.get_instrument_list(header)]
    logging.info('从终端加载到的标的物为：%s, 长度为: %d' % (','.join(terminal_instrument_ids), len(terminal_instrument_ids)))

    # 获取数据库中标的物
    vol_surface_list = VolSurfaceRepo.get_vol_surface_by_observed_date(db_session, observed_date)

    if not vol_surface_list:
        logging.info('observed_date为: %s没有可以导入到终端的vol surface' % observed_date)

    # 将instruments去重
    unique_vol_surface_list = unique_instruments(vol_surface_list)
    logging.info('在trading_date为: %s时,vol surface 表获取到的标的数量为: %d' % (observed_date,
                                                                     len(unique_vol_surface_list)))

    # 写入vol surface 到 bct
    failed_instrument_ids = []
    # hacky here, 由于bct接收的tenor是自然日，需要将vol surface中工作日转为自然日
    for index, unique_vol_surface in enumerate(unique_vol_surface_list):
        # try:
            instrument_id = unique_vol_surface.instrumentId
            a = unique_vol_surface.modelInfo
            model_info = unique_vol_surface.modelInfo
            if model_info is None:
                continue
            # 获取到当前的vol surface的实时行情spot价格
            market_data = get_real_time_market_data_by_instrument_id(instrument_id)
            if market_data is not None and market_data.get('lastPrice') is not None:
                now_price = market_data.get('lastPrice')
            else:
                logging.error('标的物%s当前行情为空' % instrument_id)
                continue
            if unique_vol_surface.instrumentId not in terminal_instrument_ids:
                logging.info('当前标的: %s的vol surface不在终端需要的列表里' % unique_vol_surface.instrumentId)
                continue

            logging.info('当前准备写入到bct的标的为: %s, 进度为: %d of %d' %
                         (unique_vol_surface.instrumentId, index + 1, len(unique_vol_surface_list)))

            result = update_one_instrument_to_terminal(unique_vol_surface, header, now_price, observed_date)
            if not result:
                failed_instrument_ids.append(instrument_id)
            time.sleep(0.3)
        # except Exception as e:
        #     logging.error('更新标的物%s波动率曲面失败， 异常:%s' % (instrument_id, str(e)))
    logging.error('处理失败的标的物为：%d %s' % (len(failed_instrument_ids), ','.join(failed_instrument_ids)))
    db_session.close()


if __name__ == '__main__':
    update_all_instrument_vol_surface_to_terminal(date(2019, 7, 30))
