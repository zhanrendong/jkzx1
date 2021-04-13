from eod_pd import basic_cash_flow_pd_run, basic_cash_flow_today_pd_run
from regression.RegressionTestCase import RegressionTestCase


class UpdateCashflowTest(RegressionTestCase):
    def __init__(self):
        self.result_tables = []

    def test_run(self):
        basic_cash_flow_pd_run()
        basic_cash_flow_today_pd_run()
