from regression.RegressionTestCase import RegressionTestCase
from regression.regression_tables import bct_quote, bct_auth_resource, bct_party, bct_trade_index, bct_trade, \
    bct_position
from trade_import.trade_import_fuc import trade_data_import


# 7. trade_import_task(bct): book, auth, party, trades,
class ImportBCTTradeTest(RegressionTestCase):
    def __init__(self, eod_date):
        self.eod_date = eod_date
        self.result_tables = [
            bct_auth_resource,
            bct_party,
            bct_trade_index,
            bct_trade
        ]

    def test_run(self):
        trade_data_import(self.eod_date)
