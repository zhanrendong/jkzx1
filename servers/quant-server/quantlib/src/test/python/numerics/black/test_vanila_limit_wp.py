import unittest
import math
import utils
import instruments


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

    # S=1e-11,C->0
    def test_limit_case1(self):
        self.params['spot'] = 1e-11
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(call_price, 0., places=10)

    # K=1e-11,C->S*exp(-q*tau)
    def test_limit_case2(self):
        self.option_property['strike'] = 1e-11
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(call_price, self.params['spot'] * math.exp(-0.9993155373 * self.params['q']), places=10)

    # S=1e-11,P->K*exp(-r*tau)
    def test_limit_case3(self):
        self.params['spot'] = 1e-11
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(put_price, self.option_property['strike'] * math.exp(-0.9993155373 * self.params['r']),
                               places=10)

    # K=1e-11,P->0
    def test_limit_case4(self):
        self.option_property['strike'] = 1e-11
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(put_price, 0., places=10)
