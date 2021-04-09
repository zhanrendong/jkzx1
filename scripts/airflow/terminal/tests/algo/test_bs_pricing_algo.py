import unittest
from terminal.algo import BlackScholes, BS1993
from terminal.dto import OptionType


class TestBSPricingAlgo(unittest.TestCase):
    THRESHOLD = 0.000001

    def test_calc_european_call_price(self):
        result = BlackScholes.calc_call_price(10, 12, 0.1, 1, 0.1, 0.05)
        self.assertLessEqual(abs(result - 0.043994028), TestBSPricingAlgo.THRESHOLD)

    def test_calc_european_put_price(self):
        result = BlackScholes.calc_put_price(10, 9, 0.1, 1, 0.1, 0.05)
        self.assertLessEqual(abs(result - 0.0227805967), TestBSPricingAlgo.THRESHOLD)


class TestBS1993(unittest.TestCase):
    def test_calc_american_call_price(self):
        result = BS1993.calc_call_price(10, 12, 0.3, 1, 0.1, 0.05)
        self.assertLessEqual(abs(result - 0.6567308), TestBSPricingAlgo.THRESHOLD)

    def test_calc_american_put_price(self):
        result = BS1993.calc_put_price(10, 9, 0.3, 1, 0.1, 0.05)
        self.assertLessEqual(abs(result - 0.5287977), TestBSPricingAlgo.THRESHOLD)


if __name__ == '__main__':
    unittest.main()
