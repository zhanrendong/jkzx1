from market_data.eod_market_data import update_market_instruments
from regression.RegressionTestCase import RegressionTestCase

# 4. market_instruments_update_run(bct)
from regression.regression_tables import bct_instrument, bct_quote, bct_model


class UpdateBCTInstrumentTest(RegressionTestCase):
    def __init__(self):
        self.result_tables = [
            bct_instrument,
            bct_model
        ]

    def test_run(self):
        update_market_instruments(force_update=True)
