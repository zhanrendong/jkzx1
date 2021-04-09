from datetime import datetime, timedelta
import datetime as dt
import math
from datetime import datetime, timedelta

DATE_PATTERN_DESC = {
    '%Y-%m-%d': 'YYYY-MM-DD'
}


class DateTimeUtils:

    @staticmethod
    def str2time(str, pattern='%H:%M:%S'):
        if str is None:
            return None
        return datetime.strptime(str, pattern).time()

    @staticmethod
    def time2str(time_obj, pattern='%H:%M:%S'):
        return time_obj.strftime(pattern)

    @staticmethod
    def str2date(str, pattern='%Y-%m-%d'):
        if str is None:
            return None
        return datetime.strptime(str, pattern).date()

    @staticmethod
    def date2str(date, pattern='%Y-%m-%d'):
        return date.strftime(pattern)

    @staticmethod
    def str2datetime(str, pattern='%Y-%m-%d %H:%M:%S'):
        return datetime.strptime(str, pattern)

    @staticmethod
    def datetime2str(time, pattern='%Y-%m-%d %H:%M:%S'):
        return time.strftime(str, pattern)

    @staticmethod
    def date_quantum_partition(start_date, end_date, step=3):
        """
        将时间段按步长分割
        :param start_date:
        :param end_date:
        :param step: 步长
        :return:
        """
        step = int(step)
        date_list = []
        dates = (end_date - start_date).days
        if step >= dates:
            date_list.append([start_date, end_date])
            return date_list
        if step == 0:
            for i in range(0, dates + 1):
                day = start_date + timedelta(days=i)
                date_list.append([day, day])
            return date_list

        transition_value = start_date
        for i in range(1, math.floor(dates / step) + 10):
            if transition_value + dt.timedelta(step) >= end_date:
                date_list.append([transition_value, end_date])
                break
            start = transition_value
            transition_value = transition_value + dt.timedelta(step)
            date_list.append([start, transition_value])
            transition_value = transition_value + dt.timedelta(1)

        return date_list

    @staticmethod
    def get_effective_days_num(start_date, end_date, holidays):
        count = 0
        trade_date = start_date
        while trade_date <= end_date:
            if DateTimeUtils.is_trading_day(trade_date, holidays):
                count += 1
            trade_date += timedelta(days=1)
        return count

    @staticmethod
    def is_trading_day(current_date, holidays, special_dates=[]):
        if current_date.weekday() != 5 and current_date.weekday() != 6 \
                and current_date not in holidays and current_date not in special_dates:
            return True
        return False

    @staticmethod
    def get_trading_day(current_date, holidays, special_dates, step, direction=1):
        # 根据step获取交易日，step必须大于0，direction=1往前走，direction=-1往后走
        while step > 0:
            current_date = current_date + timedelta(days=direction)
            if DateTimeUtils.is_trading_day(current_date, holidays, special_dates):
                step = step - 1
        return current_date

    @staticmethod
    def offset_trade_date(start_date, end_date, holidays, special_dates=[]):
        start_trade_date = DateTimeUtils.get_trading_day(start_date, holidays, special_dates, 1, direction=-1)
        end_trade_date = DateTimeUtils.get_trading_day(end_date, holidays, special_dates, 1, direction=-1)
        return start_trade_date, end_trade_date

    @staticmethod
    def validate(date_text, pattern):
        try:
            datetime.strptime(date_text, pattern)
        except ValueError:
            raise ValueError('日期格式不正确, 格式应为 {}'
                             .format(DATE_PATTERN_DESC.get(pattern) or pattern))


if __name__ == '__main__':
    # time = DateTimeUtils.str2datetime('20191020 12:01:01', '%Y%m%d %H:%M:%S')
    # print(time)
    print(DateTimeUtils.validate('2019-09-23', '%Y-%m-%d'))
