import unittest
import utils
import instruments


class OutBarrierTestCase(unittest.TestCase):

    def setUp(self):
        """
        option_property is a dict that contains all the needed attributes to construct an option
        These attributes do not depend on the option itself, so the option_property can be used across different option
        creators.

        params is a dict of the parameters to calculate the designated instrument's price.
        the 'instrument' element should always be assigned before calculating the price.
        Returns:

        """
        self.option_property = {
            'type': 'call',
            'strike': 100.,
            'expiry': '2017-08-08T00:00:00',
            'delivery': '2017-08-08T00:00:00',
            'barrier': 90.,
            'direction': 'down_and_out',
            'barrier_start': '2016-08-08T00:00:00',
            'barrier_end': '2017-08-08T00:00:00',
            'rebate_amount': 20.,
            'rebate_type': 'pay_when_hit'
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
    # case 1: spot = 90
    def test_call_price_case1(self):
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        self.params['spot'] = 90.
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(20., price, places=9)

    # case 2: spot = 95
    def test_call_price_case2(self):
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        self.params['spot'] = 95.
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(21.284883096, price, places=9)

    # case 3: spot = 100
    def test_call_price_case3(self):
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        self.params['spot'] = 100.
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(22.866574567, price, places=9)

    # point test:
    # 1.2 put price
    # case 1: spot = 90
    def test_put_price_case1(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        self.params['spot'] = 90.
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(20., price, places=9)

    # case 2: spot = 95
    def test_put_price_case2(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        self.params['spot'] = 95.
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(15.333308745, price, places=9)

    # case 3: spot = 100
    def test_put_price_case3(self):
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        self.params['spot'] = 100.
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(11.621517755, price, places=9)

    # 2, limit test
    # 2.1 for down-and-out options, as barrier goes close to zero, the option price converges to the vanilla European
    # option price
    # case 1: call option
    def test_barrier_lower_limit_case1(self):
        self.option_property['barrier'] = 0.01
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        vanilla_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(knock_out_price, vanilla_price, places=9)

    # case 2: put option
    def test_barrier_lower_limit_case2(self):
        self.option_property['barrier'] = 0.01
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        vanilla_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(knock_out_price, vanilla_price, places=9)

    # 2.2 for up-and-out options, as barrier goes close to infinity, the option price converges to the vanilla European
    # option price
    # case 1: call option
    def test_barrier_upper_limit_case1(self):
        self.option_property['barrier'] = 1000.
        self.option_property['direction'] = 'up_and_out'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        vanilla_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(knock_out_price, vanilla_price, places=9)

    # case 2: put option
    def test_barrier_upper_limit_case2(self):
        self.option_property['barrier'] = 1000.
        self.option_property['direction'] = 'up_and_out'
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        vanilla_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(knock_out_price, vanilla_price, places=9)

    # 3, in and out parity test
    # for in and out options without rebate, in + out = vanilla European options
    def test_in_out_parity(self):
        self.option_property['rebate_amount'] = 0.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        out_price = utils.call(self.method, self.params)

        self.option_property['direction'] = 'down_and_in'
        self.params['instrument'] = instruments.knock_in_continuous(self.option_property)
        in_price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        vanilla_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(out_price+in_price-vanilla_price, 0.0, places=9)

    # 4 Exotic test
    #  test the pricing formulas using exotic combinations of tau, r, q, vol
    # 4.1 vol = 0  and compare with vol = 1e-6
    # case 1: call option
    def test_ex1_call(self):
        # self.option_property['barrier'] = 90.
        self.params['vol'] = 0
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price1 = utils.call(self.method, self.params)
        self.params['vol'] = 1e-6
        price2 = utils.call(self.method, self.params)

        self.assertAlmostEqual(price1, price2, places=15)

    # case 2: put option
    def test_ex1_put(self):
        self.params['vol'] = 0
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price1 = utils.call(self.method, self.params)
        self.params['vol'] = 1e-6
        price2 = utils.call(self.method, self.params)

        self.assertAlmostEqual(price1, price2, places=15)

    # 4.2 T = 0  , at give conditions, option is not knocked out, call= max(spot-strike,0)
    # case 1: call option
    def test_ex2_call(self):
        self.params['valDate'] = '2017-08-08T00:00:00'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(0, price, places=15)

    # case 2: put option
    def test_ex2_put(self):
        self.params['valDate'] = '2017-08-08T00:00:00'
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(5.0, price, places=15)

    # 4.3 r = -10
    # case 1: call option
    def test_ex3_call(self):
        self.params['r'] = -10.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(21.11111111111111, price, places=15)

    # case 2: put option
    def test_ex3_put(self):
        self.params['r'] = -10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(21.11111111111111, price, places=15)

    # 4.4 r = 10
    # case 1: call option
    def test_ex4_call(self):
        self.params['r'] = 10.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(94.99542668240535, price, places=15)

    # case 2: put option
    def test_ex4_put(self):
        self.params['r'] = 10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(6.124622476393796e-07, price, places=15)

    # 4.5 q = -10
    # case 1: call option
    def test_ex5_call(self):
        self.params['q'] = -10.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(2078150.134764752, price, places=15)

    # case 2: put option
    def test_ex5_put(self):
        self.params['q'] = -10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(5.434851880526098e-07, price, places=15)

    # 4.6 q = 10
    # case 1: call option
    def test_ex6_call(self):
        self.params['q'] = 10.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(19.98911500679125, price, places=15)

    # case 2: put option
    def test_ex6_put(self):
        self.params['q'] = 10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(19.98911500679125, price, places=15)

    # 4.7 vol=1e-6, r=10, q=0, in this case, the option is not knocked in during its lifetime, the option price
    # (call/put) actually equals to the rebate discounted at the valuation date.
    # case 1: call option
    def test_ex7_call(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 10.
        self.params['q'] = 0.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(94.99542882587546, price, places=15)

    # case 2: put option
    def test_ex7_put(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 10.
        self.params['q'] = 0.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(0, price, places=15)

    # 4.8 vol = 1e-6, r=-10, q =0
    # case 1: call
    def test_ex8_call(self):
        self.params['vol'] = 1e-6
        self.params['r'] = -10.
        self.params['q'] = 0.
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(21.11111111111111, price, places=15)

    # case 2: put
    def test_ex8_put(self):
        self.params['vol'] = 1e-6
        self.params['r'] = -10.
        self.params['q'] = 0.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(21.11111111111111, price, places=15)

    # 4.9 vol =1e-6, r=0, q=0
    # case 1: call
    def test_ex9_call(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 0
        self.params['q'] = 0
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(0, price, places=15)

    # case 2: put
    def test_ex9_put(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 0
        self.params['q'] = 0
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)

        self.assertAlmostEqual(5, price, places=15)


suite = unittest.TestLoader().loadTestsFromTestCase(OutBarrierTestCase)
