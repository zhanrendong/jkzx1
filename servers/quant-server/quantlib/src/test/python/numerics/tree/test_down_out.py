import unittest
import utils
import instruments


class DownOutTestCase(unittest.TestCase):
    def setUp(self):
        self.option_property = {
            'type': 'call',
            'strike': 100.0,
            'expiry': '2017-08-08T00:00:00',
            'delivery': '2017-08-08T00:00:00',
            'barrier': 90.,
            'direction': 'down_and_out',
            'barrier_start': '2016-08-08T00:00:00',
            'barrier_end': '2017-08-08T00:00:00',
            'rebate_amount': 10,
            'rebate_type': 'pay_when_hit'
        }
        self.method = 'qlOptionCalcBinomialTree'
        self.params = {
            'request': 'price',
            'instrument': 'option',
            'valDate': '2016-08-08T00:00:00',
            'spot': 95.0,
            'vol': 0.2,
            'r': 0.1,
            'q': 0.2,
            'N': 1000
        }

    # 1, point test
    # 1.1 call price
    # case 1, spot = 90, knocked out, option price = rebate
    def test_call_price_case1(self):
        self.params['spot'] = 90.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        tree_price = utils.call(self.method, self.params)
        rebate = self.option_property['rebate_amount']
        self.assertAlmostEqual(tree_price, rebate, places=15)

    # case 2, spot = 95
    def test_call_price_case2(self):
        self.params['spot'] = 95.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        tree_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-3
        self.assertAlmostEqual(tree_price, black_price, delta=eps)

    # case 3, spot = 100
    def test_call_price_case3(self):
        self.params['spot'] = 100.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        tree_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-3
        self.assertAlmostEqual(tree_price, black_price, delta=eps)

    # 1.2 put price
    # case 1, spot = 90, knocked out, option price = rebate
    def test_put_price_case1(self):
        self.option_property['type'] = 'put'
        self.params['spot'] = 90.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        tree_price = utils.call(self.method, self.params)
        rebate = self.option_property['rebate_amount']
        self.assertAlmostEqual(tree_price, rebate, places=15)

    # case 2, spot = 95,
    def test_put_price_case2(self):
        self.option_property['type'] = 'put'
        self.params['spot'] = 95.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        tree_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-3
        self.assertAlmostEqual(tree_price, black_price, delta=eps)

    # case 3, spot = 100,
    def test_put_price_case3(self):
        self.option_property['type'] = 'put'
        self.params['spot'] = 100.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        tree_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-3
        self.assertAlmostEqual(tree_price, black_price, delta=eps)

    # 2 limit test
    # 2.1 for down and out options, as the barrier goes to zero, the option price converges to the vanilla European
    # option
    # case 1 call option
    def test_down_limit_call(self):
        self.option_property['barrier'] = 0.01
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        tree_price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        vanilla_price = utils.call(self.method, self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(tree_price, vanilla_price, delta=eps)

    # case 2 put option
    def test_down_limit_put(self):
        self.option_property['barrier'] = 0.01
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        tree_price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        vanilla_price = utils.call(self.method, self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(tree_price, vanilla_price, delta=eps)

    # 3 parity test
    # a knock out option + a knock in option is equivalent to a vanilla European option(rebate =0)
    def test_parity(self):
        self.option_property['rebate_amount'] = 0
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)

        self.option_property['direction'] = 'down_and_in'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        knock_in_price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        vanilla_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(knock_out_price+knock_in_price, vanilla_price, delta=0)

    # 4 exotic test
    # 4.1 vol = 0
    # case 1 call option
    def test_ex1_call(self):
        self.params['vol'] = 0.0
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        tree_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(tree_price, black_price, delta=eps)

    # case 2 put option
    def test_ex1_put(self):
        self.params['vol'] = 0.0
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        tree_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(tree_price, black_price, delta=eps)

    # 4.3 vol = 1e-3, r= 10, q = 0
    def test_ex3_call(self):
        self.params['vol'] = 1e-3
        self.params['r'] = 10.0
        self.params['q'] = 0.0
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        self.params['alpha'] = 0.5
        tree_price = utils.call(self.method, self.params)
        black_price = utils.call('qlOptionCalcBlack', self.params)
        eps = self.params['spot'] * 1e-5
        self.assertAlmostEqual(black_price, tree_price, delta=eps)


suite = unittest.TestLoader().loadTestsFromTestCase(DownOutTestCase)
