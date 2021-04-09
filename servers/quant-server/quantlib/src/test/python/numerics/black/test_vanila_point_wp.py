import unittest
import math
import utils
import instruments
from scipy.stats import norm


class BlackTestCase(unittest.TestCase):
    def setUp(self):
        self.option_property = {
            'type': 'call',
            'strike': 1.,
            'expiry': '2017-08-08T00:00:00',
            'delivery': '2017-08-08T00:00:00'
        }
        self.method = 'qlOptionCalcBlack'
        self.params = {
            'request': 'price',
            'instrument': 'option',
            'valDate': '2016-08-08T00:00:00',
            'spot': 1.,
            'vol': 0.25,
            'r': 0.1,
            'q': 0.01
        }

    # 1.fix r,q,vol,tau and K,change S
    # case 1:S=0.00003
    def test_point_case1(self):
        self.params['spot'] = 0.00003
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        d1 = (math.log(self.params['spot'] / self.option_property['strike']) + 0.9993155373 *
              (self.params['r'] + (self.params['vol'] ** 2.) * 0.5)) / (self.params['vol'] * math.sqrt(0.9993155373))
        d2 = d1 - self.params['vol'] * math.sqrt(0.9993155373)
        self.assertAlmostEqual(
            self.params['spot'] * df_q * norm.cdf(d1) - self.option_property['strike'] * df_r * norm.cdf(d2),
            call_price, places=10)
        self.assertAlmostEqual(
            self.option_property['strike'] * df_r * norm.cdf(-d2) - self.params['spot'] * df_q * norm.cdf(-d1),
            put_price, places=10)

    # case 2:S=100
    def test_point_case2(self):
        self.params['spot'] = 100.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        d1 = (math.log(self.params['spot'] / self.option_property['strike']) + 0.9993155373 *
              (self.params['r'] + (self.params['vol'] ** 2.) * 0.5)) / (self.params['vol'] * math.sqrt(0.9993155373))
        d2 = d1 - self.params['vol'] * math.sqrt(0.9993155373)
        self.assertAlmostEqual(
            self.params['spot'] * df_q * norm.cdf(d1) - self.option_property['strike'] * df_r * norm.cdf(d2),
            call_price, places=10)
        self.assertAlmostEqual(
            self.option_property['strike'] * df_r * norm.cdf(-d2) - self.params['spot'] * df_q * norm.cdf(-d1),
            put_price, places=10)

    # 2.fix r,q,vol,tau and S,change K
    # case 3:K=0.00001
    def test_point_case3(self):
        self.option_property['strike'] = 0.00001
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        d1 = (math.log(self.params['spot'] / self.option_property['strike']) + 0.9993155373 *
              (self.params['r'] + (self.params['vol'] ** 2.) * 0.5)) / (self.params['vol'] * math.sqrt(0.9993155373))
        d2 = d1 - self.params['vol'] * math.sqrt(0.9993155373)
        self.assertAlmostEqual(
            self.params['spot'] * df_q * norm.cdf(d1) - self.option_property['strike'] * df_r * norm.cdf(d2),
            call_price, places=10)
        self.assertAlmostEqual(
            self.option_property['strike'] * df_r * norm.cdf(-d2) - self.params['spot'] * df_q * norm.cdf(-d1),
            put_price, places=10)

    # case 4:K=100
    def test_point_case4(self):
        self.option_property['strike'] = 100.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        d1 = (math.log(self.params['spot'] / self.option_property['strike']) + 0.9993155373 *
              (self.params['r'] + (self.params['vol'] ** 2.) * 0.5)) / (self.params['vol'] * math.sqrt(0.9993155373))
        d2 = d1 - self.params['vol'] * math.sqrt(0.9993155373)
        self.assertAlmostEqual(
            self.params['spot'] * df_q * norm.cdf(d1) - self.option_property['strike'] * df_r * norm.cdf(d2),
            call_price, places=10)
        self.assertAlmostEqual(
            self.option_property['strike'] * df_r * norm.cdf(-d2) - self.params['spot'] * df_q * norm.cdf(-d1),
            put_price, places=10)
