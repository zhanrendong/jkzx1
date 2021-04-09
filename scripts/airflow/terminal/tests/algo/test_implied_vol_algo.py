import unittest
from terminal.dto import OptionType, OptionProductType
from terminal.algo import ImpliedVolAlgo, BlackScholes


class TestImpliedVolAlgo(unittest.TestCase):
    THRESHOLD = 0.000001

    def test_calc_implied_vol_with_european_call(self):
        expected_vol = 0.1
        price = BlackScholes.calc_call_price(10, 12, expected_vol, 1, 0.1, 0.05)
        result = ImpliedVolAlgo.calc_implied_vol(price, 10, 12, 1, 0.1, 0.05,
                                                 OptionType.CALL, OptionProductType.VANILLA_EUROPEAN)
        self.assertLessEqual(abs(result - expected_vol), TestImpliedVolAlgo.THRESHOLD)

    def test_calc_implied_vol_with_european_put(self):
        expected_vol = 0.1
        price = BlackScholes.calc_put_price(10, 9, expected_vol, 1, 0.1, 0.05)
        result = ImpliedVolAlgo.calc_implied_vol(price, 10, 9, 1, 0.1, 0.05,
                                                 OptionType.PUT, OptionProductType.VANILLA_EUROPEAN)
        self.assertLessEqual(abs(result - expected_vol), TestImpliedVolAlgo.THRESHOLD)

    def test_calc_implied_vol_with_american_call(self):
        # TODO: 增加测试
        pass

    def test_calc_implied_vol_with_american_put(self):
        # TODO: 增加测试
        pass

    if __name__ == '__main__':
        unittest.main()
