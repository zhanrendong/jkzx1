import unittest
import utils
import instruments


class UpInTestCase(unittest.TestCase):
    def setUp(self):
        self.option_property = {
            'type': 'call',
            'strike': 80.,
            'expiry': '2017-08-08T00:00:00',
            'delivery': '2017-08-08T00:00:00',
            'barrier': 90.,
            'direction': 'up_and_in',
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
    # case 1, spot = 80
    def test_call_price_case1(self):
        self.params['spot'] = 80.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 2, spot = 85
    def test_call_price_case2(self):
        self.params['spot'] = 85.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 3, spot = 90
    def test_call_price_case3(self):
        self.params['spot'] = 90.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 4, spot = 160
    def test_call_price_case4(self):
        self.params['spot'] = 160.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # 1.2 put price
    # case 1, spot = 80
    def test_put_price_case1(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        self.params['spot'] = 80.
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 2, spot = 85
    def test_put_price_case2(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        self.params['spot'] = 85.
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 3, spot = 90
    def test_put_price_case3(self):
        self.option_property['type'] = 'put'
        self.params['spot'] = 90.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

suite = unittest.TestLoader().loadTestsFromTestCase(UpInTestCase)
