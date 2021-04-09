from dags.service.r_otc_position_service import ROTCPositionService
from market_data.eod_market_data import update_eod_price
from regression.RegressionTestCase import RegressionTestCase
from regression.regression_tables import bct_quote, terminal_otc_pos_snapshot


# 9. update_implied_vol of OTCPositionSnapshot(terminal_data)
class UpdateImpliedVolTest(RegressionTestCase):
    def __init__(self, start_date, end_date):
        self.end_date = end_date
        self.start_date = start_date
        self.result_tables = [
            terminal_otc_pos_snapshot
        ]

    def test_run(self):
        ROTCPositionService.update_implied_vol(self.start_date, self.end_date, force_update=True)
