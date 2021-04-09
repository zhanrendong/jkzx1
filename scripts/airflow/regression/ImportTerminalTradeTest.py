from dags.service.otc_trade_snapshots_service import OTCTradeSnapshotsService
from regression.RegressionTestCase import RegressionTestCase
from regression.regression_tables import terminal_otc_position, terminal_otc_trade
from trade_import.trade_import_fuc import trade_data_import


# 8. eod_otc_trade_snapshot_update_task(terminal_data)
class ImportTerminalTradeTest(RegressionTestCase):
    def __init__(self, start_date, end_date):
        self.start_date = start_date
        self.end_date = end_date
        self.result_tables = [
            terminal_otc_position,
            terminal_otc_trade
        ]

    def test_run(self):
        OTCTradeSnapshotsService.import_otc_trade_snapshots(self.start_date, self.end_date, force_update=True)
