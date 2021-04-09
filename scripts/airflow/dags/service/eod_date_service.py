from dags.utils.airflow_utils import AirflowVariableUtils
from datetime import datetime
from datetime import date
from dags.dbo import create_db_session
from terminal.service import TradingDayService
from dags.conf import EODDateConfig
from terminal.utils import DateTimeUtils


class EODDateService:
    @staticmethod
    def update_eod_date(start_date_str=EODDateConfig.eod_start_date,
                        end_date_str=DateTimeUtils.date2str(date.today()),
                        eod_cutoff_time=EODDateConfig.eod_cutoff_time):
        end_date = DateTimeUtils.str2date(end_date_str)
        start_date = DateTimeUtils.str2date(start_date_str)
        # 获取节假日信息
        db_session = create_db_session()
        # TODO: 需要从配置文件读取
        if not TradingDayService.is_trading_day(db_session, end_date):
            # 如果当天不是交易日则直接计算上一个交易日的值
            end_date = TradingDayService.go_trading_day(db_session, end_date, 1, -1)
        else:
            # 如果是交易日，小于cutoff time还是返回上一个交易日
            if datetime.now().time() < DateTimeUtils.str2time(eod_cutoff_time):
                end_date = TradingDayService.go_trading_day(db_session, end_date, 1, -1)
            else:
                end_date = end_date
        # 设置变量到airflow variable
        AirflowVariableUtils.set_eod_start_date(start_date)
        AirflowVariableUtils.set_eod_end_date(end_date)


if __name__ == '__main__':
    EODDateService.update_eod_date()
