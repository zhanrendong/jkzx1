from market_data.eod_market_data import update_eod_price
from regression.RegressionTestCase import RegressionTestCase
from regression.regression_tables import bct_quote


# 6. eod_price_update_run(bct)
class UpdateBCTQuoteTest(RegressionTestCase):
    def __init__(self, eod_date):
        self.eod_date = eod_date
        self.result_tables = [
            bct_quote
        ]

    def test_run(self):
        update_eod_price(self.eod_date)
