from dags.service.trading_calendar_service import TradingCalendarService
from regression.RegressionTestCase import RegressionTestCase


# 2. sync calendar between BCT & Terminal(terminal_data)
from regression.regression_tables import terminal_trading_calendar


class ImportTerminalCalendarTest(RegressionTestCase):
    def __init__(self):
        self.result_tables = [
            terminal_trading_calendar
        ]

    def test_run(self):
        TradingCalendarService.update_trading_calendar()
