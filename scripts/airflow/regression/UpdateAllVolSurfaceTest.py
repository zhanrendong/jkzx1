import time

from dags.service.vol_surface_service import VolSurfaceService
from regression.RegressionTestCase import RegressionTestCase
from regression.regression_tables import vol_surface


# # 12. calc implied vol
# VolSurfaceService.update_all_vol_surface(eod_end_date.date(), eod_end_date.date(), 4)
class UpdateAllVolSurfaceTest(RegressionTestCase):
    def __init__(self, eod_start_date, eod_end_date):
        self.eod_start_date = eod_start_date,
        self.eod_end_date = eod_end_date
        self.result_tables = [
            vol_surface
        ]

    def test_run(self):
        VolSurfaceService.update_all_vol_surface(self.eod_start_date, self.eod_end_date, 4)
