from calendar_import.calendar_import import calendar_import
from regression.RegressionTestCase import RegressionTestCase
from regression.regression_tables import bct_trading_holiday, bct_vol_special_date


def import_all_calendars(year_begin):
    for y in range(0, 5):
        year = year_begin + y
        calendar_import(str(year))


# 1. import trading & vol calendars from S_TRADEDATE into BCT (bct)
class ImportBCTCalendarTest(RegressionTestCase):
    def __init__(self):
        self.year_begin = 2018
        self.result_tables = [
            bct_trading_holiday,
            bct_vol_special_date
        ]

    def test_run(self):
        import_all_calendars(self.year_begin)
