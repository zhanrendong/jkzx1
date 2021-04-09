from market_data.eod_market_data import update_market_instruments, synchronize_bct_and_terminal_market_data
from regression.RegressionTestCase import RegressionTestCase
from regression.regression_tables import bct_instrument, bct_quote, bct_model, terminal_instrument


#  5. market_instruments_synchronize_run(terminal_data)
class SyncTerminalInstrumentTest(RegressionTestCase):
    def __init__(self):
        self.result_tables = [
            terminal_instrument
        ]

    def test_run(self):
        synchronize_bct_and_terminal_market_data()
