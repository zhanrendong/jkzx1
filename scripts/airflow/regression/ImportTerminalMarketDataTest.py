from dags.service.otc_maket_data_service import OTCMarketDataService
from regression.RegressionTestCase import RegressionTestCase

# 3. eod_wind_market_data_update_task(terminal_data)
from regression.regression_tables import terminal_instrument, terminal_quote


class ImportTerminalMarketDataTest(RegressionTestCase):
    def __init__(self, start_date, end_date):
        self.start_date = start_date
        self.end_date = end_date
        self.result_tables = [
            terminal_instrument,
            terminal_quote
        ]

    def test_run(self):
        OTCMarketDataService.backfill_otc_market_data(self.start_date, self.end_date, force_update=True)
