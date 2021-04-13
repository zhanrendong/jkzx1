from datetime import timedelta

from dags.service.future_contract_info_service import FutureContractInfoService
from regression.RegressionTestCase import RegressionTestCase
from regression.regression_tables import future_contract_info


# # 10. eod_otc_future_contract_update_task(terminal_data), 需要更多的RB/IF标的和行情
# FutureContractInfoService.update_all_future_contract_info(eod_start_date - timedelta(days=300), eod_end_date,
#                                                           force_update=True)
class UpdateEodOtcFutureContractTest(RegressionTestCase):
    def __init__(self, eod_start_date, eod_end_date):
        self.eod_start_date = eod_start_date
        self.eod_end_date = eod_end_date
        self.result_tables = [
            future_contract_info
        ]

    def test_run(self):
        FutureContractInfoService.update_all_future_contract_info(self.eod_start_date - timedelta(days=300)
                                                                  , self.eod_end_date, force_update=True)
