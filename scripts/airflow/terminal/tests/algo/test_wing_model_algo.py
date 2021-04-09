import unittest
import numpy as np
from terminal.dto import FittingModelStrikeType
from terminal.algo import WingModelAlgo, WingModelDataAlgo


class TestWingModelAlgo(unittest.TestCase):
    THRESHOLD = 1e-6

    def test_calc_wing_model(self):
        expected_dic = {'dc': -0.05, 'uc': 0.05, 'dsm': 50000.0, 'usm': 50000.0, 'pc': -0.150163, 'vc': 3.598334, 'sc': 1.461675, 'cc': 0.0}
        array = np.array([[2, 2.5, 3, 3.5, 4], [0.3, 0.25, 0.2, 0.26, 0.31]])
        test_result = WingModelAlgo.calc_wing_model(array, 3.0, (
        [-0.1, 0, 0, 0, -1e5, 0, -1e6, -1e6], [0, 0.1, 1e5, 1e5, 1e5, 4, 1e6, 1e6]))
        for key in test_result.keys():
            self.assertLessEqual(abs(test_result[key] - expected_dic[key]), TestWingModelAlgo.THRESHOLD)

    def test_fit_one_wing_model(self):
        expected_list = [14.3829869, 21.36214991, 32.83951127, 51.68274534, 82.53324196]
        test_result = WingModelAlgo.fit_one_wing_model(np.array([2, 2.5, 3, 3.5, 4]),
                                                       {'dc': -0.05, 'uc': 0.05, 'dsm': 50000.0, 'usm': 50000.0, 'pc': -0.150163, 'vc': 3.598334, 'sc': 1.461675, 'cc': 0.0},
                                                       FittingModelStrikeType.PERCENT, [], 0)
        for index in range(len(test_result)):
            self.assertLessEqual(abs(test_result[index] - expected_list[index]), TestWingModelAlgo.THRESHOLD)

    if __name__ == '__main__':
        unittest.main()