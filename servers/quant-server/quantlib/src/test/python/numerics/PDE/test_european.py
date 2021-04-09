import unittest
import utils
import instruments


class EuropeanTestCase(unittest.TestCase):
    def setUp(self):
        self.option_property = {
            'type': 'call',
            'strike': 100,
            'expiry': '2017-08-08T00:00:00',
            'delivery': '2017-08-08T00:00:00'

        }
        self.method = 'qlOptionCalcPDE'
        self.params = {
            'instrument': 'option',
            'valDate': '2016-08-08T00:00:00',
            'spot': 100,
            'r': 0.02,
            'vol': 0.2,
            'q': 0.01,
            'request': 'price',
            'eps': 1e-6,
            'alpha': 0.5
        }

    # 1, point test
    # 1.1 call price
    # case 1, spot = 50
    def test_call_price_case1(self):
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 50.
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 2, spot = 100
    def test_call_price_case2(self):
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 200.
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 2, spot = 200
    def test_call_price_case3(self):
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 200.
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # 1.2 put price
    # case 1, spot = 50
    def test_put_price_case1(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 50.
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 2, spot = 100
    def test_put_price_case2(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 100.
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-4
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 3, spot = 200
    def test_put_price_case3(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 200.
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # 2, exotic test
    # 2.1 vol = 0
    def test_ex_case1(self):
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['vol'] = 0.0
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-4
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

suite = unittest.TestLoader().loadTestsFromTestCase(EuropeanTestCase)
