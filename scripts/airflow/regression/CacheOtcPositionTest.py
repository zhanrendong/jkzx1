from eod_pd import basic_position_pd_run
from regression.RegressionTestCase import RegressionTestCase
from regression.regression_tables import bct_otc_positions, bct_otc_position_map


# 16. fetch otc position
class CacheOtcPositionTest(RegressionTestCase):
    def __init__(self, eod_date):
        self.eod_date = eod_date
        self.result_tables = [
            bct_otc_positions,
            bct_otc_position_map
        ]

    def test_run(self):
        basic_position_pd_run(self.eod_date)
