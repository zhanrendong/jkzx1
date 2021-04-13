from eod_pd import basic_instrument_contract_type_run
from regression.RegressionTestCase import RegressionTestCase
from regression.regression_tables import bct_instrument_type


# 15. fetch instrument and contract type mapping
class CacheInstrumentTypeTest(RegressionTestCase):
    def __init__(self):
        self.result_tables = [
            bct_instrument_type
        ]

    def test_run(self):
        basic_instrument_contract_type_run()
