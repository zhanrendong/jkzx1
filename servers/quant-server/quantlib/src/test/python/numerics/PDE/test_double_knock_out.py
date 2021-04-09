import unittest
import utils
import instruments


class DoubleKnockOutTestCase(unittest.TestCase):
    def setUp(self):
        self.option_property = {
            'type': 'call',
            'strike': 100.,
            'lower_barrier': 90.,
            'lower_rebate': 5.0,
            'upper_barrier': 110.,
            'upper_rebate': 10.,
            'barrier_start': '2016-08-08T00:00:00',
            'barrier_end': '2017-08-08T00:00:00',
            'expiry': '2017-08-08T00:00:00',
            'delivery': '2017-08-08T00:00:00',
            'rebate_type': 'pay_when_hit'

        }
        self.method = 'qlOptionCalcPDE'
        self.params = {
            'request': 'price',
            'instrument': 'option',
            'valDate': '2016-08-08T00:00:00',
            'spot': 100.,
            'vol': 0.2,
            'r': 0.1,
            'q': 0.1,
            'eps': 1e-6,
            'alpha': 0.5
        }

    # 1, point test
    # 1.1 call price
    # case 1, spot = 90, touches the lower barrier, option price = lower rebate
    def test_call_price_case1(self):
        self.params['spot'] = 90.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        rebate = self.option_property['lower_rebate']
        pde_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(pde_price, rebate, places=15)

    # case 2, spot = 100
    def test_call_price_case2(self):
        self.params['spot'] = 100.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 3, spot = 110, touches the upper barrier, option price = upper rebate
    def test_call_price_case3(self):
        self.params['spot'] = 110.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        rebate = self.option_property['upper_rebate']
        self.assertAlmostEqual(pde_price, rebate, places=15)

    # 1.2 put price
    # case 1, spot = 90, touches the lower barrier, option price = lower rebate
    def test_put_price_case1(self):
        self.params['spot'] = 90.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        rebate = self.option_property['lower_rebate']
        self.assertAlmostEqual(pde_price, rebate, places=15)

    # case 2, spot = 100
    def test_put_price_case2(self):
        self.params['spot'] = 100.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(pde_price, black_price, delta=eps)

    # case 3, spot = 110, touches the upper barrier, option price = upper rebate
    def test_put_price_case3(self):
        self.params['spot'] = 110.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        pde_price = utils.call(self.method, self.params)
        rebate = self.option_property['upper_rebate']
        self.assertAlmostEqual(pde_price, rebate, places=15)

suite = unittest.TestLoader().loadTestsFromTestCase(DoubleKnockOutTestCase)