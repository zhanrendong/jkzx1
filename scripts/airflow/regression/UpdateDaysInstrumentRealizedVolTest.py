from regression.RegressionTestCase import RegressionTestCase
from dags.service.realized_vol_service import RealizedVolService
from regression.regression_tables import terminal_realized_vol


# # 11. calc realized(historical) vol
# RealizedVolService.update_days_instrument_realized_vol(eod_end_date.date(), eod_end_date.date(), force_update=True)
class UpdateDaysInstrumentRealizedVolTest(RegressionTestCase):

    def __init__(self, start_date, end_date):
        self.start_date = start_date
        self.end_date = end_date
        self.result_tables = [terminal_realized_vol]

    def test_run(self):
        RealizedVolService.update_days_instrument_realized_vol(self.start_date, self.end_date, force_update=True)
