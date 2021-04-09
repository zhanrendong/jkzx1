import unittest
import utils
import instruments


class InBarrierTestCase(unittest.TestCase):

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
        self.method = 'qlOptionCalcBlack'
        self.params = {
            'request': 'price',
            'instrument': 'option',
            'valDate': '2016-08-08T00:00:00',
            'spot': 95,
            'vol': 0.25,
            'r': 0.1,
            'q': 0.
        }

    # 1, point test
    # 1.1 call price
    # case 1: spot = 90, already knocked in
    def test_call_price_case1(self):
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        self.params['spot'] = 90.
        price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        vanilla_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(vanilla_price, price, places=9)

    # case 2: spot = 95
    def test_call_price_case2(self):
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        self.params['spot'] = 95.
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(18.10672675741688, price, places=9)

    # 1.2 put price
    # case 1: spot = 90, already knocked in
    def test_put_price_case1(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        self.params['spot'] = 90.
        price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        vanilla_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(vanilla_price, price, places=9)

    # case 2: spot = 95
    def test_put_price_case2(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        self.params['spot'] = 95.
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(5.499066511352499, price, places=9)

    # 2, limit test:
    # 2.1
    # for down-and-in options, as barrier goes close to zero, the option can never be knocked in , the option price = 0
    # without rebate
    # case 1: call option
    def test_barrier_lower_limit_case1(self):
        self.option_property['barrier'] = 0.0
        self.option_property['rebate_amount'] = 0.0
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        knock_in_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(knock_in_price, 0.0, places=9)

    # case 2: put option
    def test_barrier_lower_limit_case2(self):
        self.option_property['barrier'] = 0.0
        self.option_property['rebate_amount'] = 0.0
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        knock_in_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(knock_in_price, 0.0, places=9)

    # 2.2
    # for up-and-in options, as barrier goes close to infinity, the option can never be knocked in, the option price = 0
    # option price
    # case 1: call option
    def test_barrier_upper_limit_case1(self):
        self.option_property['barrier'] = 1000.
        self.option_property['direction'] = 'up_and_in'
        self.option_property['rebate_amount'] = 0.0
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        knock_in_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(knock_in_price, 0.0, places=9)

    # case 2: put option
    def test_barrier_upper_limit_case2(self):
        self.option_property['barrier'] = 1000.
        self.option_property['direction'] = 'up_and_in'
        self.option_property['rebate_amount'] = 0.0
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        knock_in_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(knock_in_price, 0.0, places=9)

    # 3
    # in and out parity test
    # for in and out options without rebate, in + out = vanilla European options
    def test_in_out_parity(self):
        self.option_property['rebate_amount'] = 0.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        in_price = utils.call(self.method, self.params)

        self.option_property['direction'] = 'down_and_out'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        out_price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        vanilla_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(out_price+in_price-vanilla_price, 0.0, places=9)

    # 4 Exotic test
    #  test the pricing formulas using exotic combinations of tau, r, q, vol
    # 4.1 vol = 0  and compare with vol = 1e-6
    # case 1: call option
    def test_ex1_call(self):
        # self.option_property['barrier'] = 90.
        self.params['r'] = -0.1
        self.params['vol'] = 0
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price1 = utils.call(self.method, self.params)
        self.params['vol'] = 1e-6
        price2 = utils.call(self.method, self.params)

        self.assertAlmostEqual(price1, price2, places=15)

    # case 2: put option
    def test_ex1_put(self):
        self.params['vol'] = 0
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price1 = utils.call(self.method, self.params)
        self.params['vol'] = 1e-6
        price2 = utils.call(self.method, self.params)

        self.assertAlmostEqual(price1, price2, places=15)

    # 4.2 T = 0  , at give conditions, option is not knocked in, price = rebate
    # case 1: call option
    def test_ex2_call(self):
        self.params['valDate'] = '2017-08-08T00:00:00'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price1 = utils.call(self.method, self.params)
        price2 = self.option_property['rebate_amount']

        self.assertAlmostEqual(price1, price2, places=15)

    # case 2: put option
    def test_ex2_put(self):
        self.params['valDate'] = '2017-08-08T00:00:00'
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price1 = utils.call(self.method, self.params)
        price2 = self.option_property['rebate_amount']

        self.assertAlmostEqual(price1, price2, places=15)

    # 4.3 r = -10
    # case 1: call option
    def test_ex3_call(self):
        self.params['r'] = -10.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(0, price, places=15)

    # case 2: put option
    def test_ex3_put(self):
        self.params['r'] = -10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(1750002.4108721288, price, places=15)

    # 4.4 r = 10
    # case 1: call option
    def test_ex4_call(self):
        self.params['r'] = 10.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(0.0009169907572615757, price, places=15)

    # case 2: put option
    def test_ex4_put(self):
        self.params['r'] = 10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(0.0009142347953555541, price, places=15)

    # 4.5 q = -10
    # case 1: call option
    def test_ex5_call(self):
        self.params['q'] = -10.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(18.148698212347103, price, places=15)

    # case 2: put option
    def test_ex5_put(self):
        self.params['q'] = -10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(18.097986565965865, price, places=15)

    # 4.6 q = 10
    # case 1: call option
    def test_ex6_call(self):
        self.params['q'] = 10.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(0, price, places=15)

    # case 2: put option
    def test_ex6_put(self):
        self.params['q'] = 10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(72.38760561669933, price, places=15)

    # 4.7 vol=1e-6, r=10, q=0, in this case, the option is not knocked in during its lifetime, the option price
    # (call/put) actually equals to the rebate discounted at the valuation date.
    # case 1: call option
    def test_ex7_call(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 10.
        self.params['q'] = 0.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(0.0009142348249076429, price, places=15)

    # case 2: put option
    def test_ex7_put(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 10.
        self.params['q'] = 0.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(0.0009142348249076429, price, places=15)

    # 4.8 vol = 1e-6, r=-10, q =0
    # case 1: call
    def test_ex8_call(self):
        self.params['vol'] = 1e-6
        self.params['r'] = -10.
        self.params['q'] = 0.
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(0, price, places=15)

    # case 2: put
    def test_ex8_put(self):
        self.params['vol'] = 1e-6
        self.params['r'] = -10.
        self.params['q'] = 0.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(1750002.4108721288, price, places=15)

    # 4.9 vol =1e-6, r=0, q=0
    # case 1: call
    def test_ex9_call(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 0
        self.params['q'] = 0
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(20, price, places=15)

    # case 2: put
    def test_ex9_put(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 0
        self.params['q'] = 0
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(20, price, places=15)

suite = unittest.TestLoader().loadTestsFromTestCase(InBarrierTestCase)
