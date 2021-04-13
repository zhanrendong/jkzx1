from eod_pd import basic_otc_company_type_run
from regression.RegressionTestCase import RegressionTestCase
from regression.regression_tables import bct_subcompany_names


# 13.basic_otc_company_type_run, used for categorizing clients
class CacheCompanyTest(RegressionTestCase):
    def __init__(self):
        self.result_tables = [
            bct_subcompany_names
        ]

    def test_run(self):
        basic_otc_company_type_run()
