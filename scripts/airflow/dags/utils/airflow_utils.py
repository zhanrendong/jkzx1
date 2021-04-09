from airflow.models import Variable
from terminal.utils import DateTimeUtils


class Constants:
    EOD_START_DATE = 'EOD_START_DATE'
    EOD_END_DATE = 'EOD_END_DATE'
    EOD_MANUAL_DATE = 'AIRFLOW_TASK_EOD_VALUATION_DATE'
    FAIR_VOL_MARKING_PROCESS_NUM = 'FAIR_VOL_MARKING_PROCESS_NUM'


class AirflowVariableUtils:
    @staticmethod
    def set_eod_start_date(date):
        return Variable.set(Constants.EOD_START_DATE, DateTimeUtils.date2str(date))

    @staticmethod
    def get_eod_start_date():
        return DateTimeUtils.str2date(Variable.get(Constants.EOD_START_DATE))

    @staticmethod
    def set_eod_end_date(date):
        return Variable.set(Constants.EOD_END_DATE, DateTimeUtils.date2str(date))

    @staticmethod
    def get_eod_end_date():
        return DateTimeUtils.str2date(Variable.get(Constants.EOD_END_DATE))

    @staticmethod
    def get_eod_manual_date():
        return DateTimeUtils.str2date(Variable.get(Constants.EOD_MANUAL_DATE))

    @staticmethod
    def set_fair_vol_marking_process_num(n):
        return Variable.set(Constants.FAIR_VOL_MARKING_PROCESS_NUM, n)

    @staticmethod
    def get_fair_vol_marking_process_num():
        return int(Variable.get(Constants.FAIR_VOL_MARKING_PROCESS_NUM, default_var=4))
