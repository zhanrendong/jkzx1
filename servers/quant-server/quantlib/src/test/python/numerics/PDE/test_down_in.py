import unittest
import utils
import instruments
from math import exp


class DownInTestCase(unittest.TestCase):
    def setUp(self):
        self.option_property = {
            'type': 'call',
            'strike': 80.,
            'expiry': '2017-08-08T00:00:00',
            'delivery': '2017-08-08T00:00:00',
            'barrier': 90.,
            'direction': 'down_and_in',
            'barrier_start': '2016-08-08T00:00:00',
            'barrier_end': '2017-08-08T00:00:00',
            'rebate_amount': 20.,
            'rebate_type': 'pay_at_expiry'
        }
        self.method = 'qlOptionCalcPDE'
        self.params = {
            'request': 'price',
            'instrument': 'option',
            'valDate': '2016-08-08T00:00:00',
            'spot': 95,
            'vol': 0.25,
            'r': 0.1,
            'q': 0.,
            'eps': 1e-6,
            'alpha': 0.5
        }

    # 1, point test
    # 1.1 call price
    # case 1, spot = 90
    def test_call_price_case1(self):
        self.params['spot'] = 92.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 2, spot = 95
    def test_call_price_case2(self):
        self.params['spot'] = 95.
        self.option_property['direction'] = 'down_and_out'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        black_out_price = utils.call('qlOptionCalcBlack', self.params)
        pde_out_price = utils.call('qlOptionCalcPDE', self.params)

        self.option_property['direction'] = 'down_and_in'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        black_in_price = utils.call('qlOptionCalcBlack', self.params)
        pde_in_price = utils.call('qlOptionCalcPDE', self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        black_euro_price = utils.call('qlOptionCalcBlack', self.params)
        pde_euro_price = utils.call('qlOptionCalcPDE', self.params)

        rebate = self.option_property['rebate_amount']
        r = self.params['r']

        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_euro_price-pde_out_price+rebate*exp(-r*0.9993), pde_in_price, delta=eps)

    # case 3, spot = 160
    def test_call_price_case3(self):
        self.params['spot'] = 160.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # 1.2 put price
    # case 1, spot = 90
    def test_put_price_case1(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        self.params['spot'] = 90.
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 2, spot = 95
    def test_put_price_case2(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        self.params['spot'] = 95.
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 3, spot = 160
    def test_put_price_case3(self):
        self.option_property['type'] = 'put'
        self.params['spot'] = 160.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

suite = unittest.TestLoader().loadTestsFromTestCase(DownInTestCase)
