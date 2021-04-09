import unittest
import utils
import instruments


class DoubleKnockOutTestCase(unittest.TestCase):

    def setUp(self):
        self.option_property = {
            'type': 'call',
            'strike': 100.,
            'lower_barrier': 90.0,
            'lower_rebate': 5.0,
            'upper_barrier': 110.0,
            'upper_rebate': 10.,
            'barrier_start': '2016-08-08T00:00:00',
            'barrier_end': '2017-08-08T00:00:00',
            'expiry': '2017-08-08T00:00:00',
            'delivery': '2017-08-08T00:00:00',
            # attributes for knock out options
            'barrier': 0.,
            'rebate_amount': 0,
            'rebate_type': 'pay_when_hit',
            'direction': 'down_and_out'
        }
        self.method = 'qlOptionCalcBlack'
        self.params = {
            'request': 'price',
            'instrument': 'option',
            'valDate': '2016-08-08T00:00:00',
            'spot': 100.,
            'vol': 0.2,
            'r': 0.1,
            'q': 0.1
        }

    # 1, point test
    # 1.1 call price
    # case 1, spot = 90
    def test_call_price_case1(self):
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        self.params['spot'] = 90.
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(price, self.option_property['lower_rebate'], places=9)

    # case 2, spot = 100
    def test_call_price_case2(self):
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        self.params['spot'] = 100.
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(7.269179953568781, price, places=9)

    # case 3, spot = 110
    def test_call_price_case3(self):
        self.params['spot'] = 110.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(price, self.option_property['upper_rebate'], places=9)

    # 1.2 put price
    # case 1, spot = 90
    def test_put_price_case1(self):
        self.params['spot'] = 90.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(price, self.option_property['lower_rebate'], places=9)

    # case 2, spot = 100
    def test_put_price_case2(self):
        self.params['spot'] = 100.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(price, 7.273463970405533, places=9)

    # case 3, spot = 110
    def test_put_price_case3(self):
        self.params['spot'] = 110.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(price, self.option_property['upper_rebate'], places=9)

    # 2, limit test
    # 2.1 when the upper barrier goes to infinity, the option converges to the down and out option
    # case 1, call option, rebate paid when hit
    def test_upper_limit_case1(self):
        self.option_property['upper_barrier'] = 1000.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        double_knock_out_price = utils.call(self.method, self.params)

        self.option_property['barrier'] = self.option_property['lower_barrier']
        self.option_property['rebate_amount'] = self.option_property['lower_rebate']
        self.option_property['direction'] = 'down_and_out'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(double_knock_out_price,  knock_out_price, places=9)

    # case 2, call option, rebate paid at expiry
    def test_upper_limit_case2(self):
        self.option_property['upper_barrier'] = 1000.
        self.option_property['rebate_type'] = 'pay_at_expiry'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        double_knock_out_price = utils.call(self.method, self.params)

        self.option_property['barrier'] = self.option_property['lower_barrier']
        self.option_property['rebate_amount'] = self.option_property['lower_rebate']
        self.option_property['direction'] = 'down_and_out'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(double_knock_out_price, knock_out_price, places=9)

    # case 3, put option, rebate paid when hit
    def test_upper_limit_case3(self):
        self.option_property['upper_barrier'] = 1000.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        double_knock_out_price = utils.call(self.method, self.params)

        self.option_property['barrier'] = self.option_property['lower_barrier']
        self.option_property['rebate_amount'] = self.option_property['lower_rebate']
        self.option_property['direction'] = 'down_and_out'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(double_knock_out_price, knock_out_price,places=9)

    # case 4, put option, rebate paid at expiry
    def test_upper_limit_case4(self):
        self.option_property['upper_barrier'] = 1000.
        self.option_property['type'] = 'put'
        self.option_property['rebate_type'] = 'pay_at_expiry'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        double_knock_out_price = utils.call(self.method, self.params)

        self.option_property['barrier'] = self.option_property['lower_barrier']
        self.option_property['rebate_amount'] = self.option_property['lower_rebate']
        self.option_property['direction'] = 'down_and_out'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(double_knock_out_price, knock_out_price, places=9)

    # 2.2, when the lower barrier goes to zero, the option converges to the up and out option
    # case 1, call option, rebate paid when hit
    def test_lower_limit_case1(self):
        self.option_property['lower_barrier'] = 0.1
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        double_knock_out_price = utils.call(self.method, self.params)

        self.option_property['barrier'] = self.option_property['upper_barrier']
        self.option_property['rebate_amount'] = self.option_property['upper_rebate']
        self.option_property['direction'] = 'up_and_out'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(double_knock_out_price, knock_out_price, places=9)

    # case 2, call option, rebate paid at expiry
    def test_lower_limit_case2(self):
        self.option_property['lower_barrier'] = 0.1
        self.option_property['rebate_type'] = 'pay_at_expiry'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        double_knock_out_price = utils.call(self.method, self.params)

        self.option_property['barrier'] = self.option_property['upper_barrier']
        self.option_property['rebate_amount'] = self.option_property['upper_rebate']
        self.option_property['direction'] = 'up_and_out'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(double_knock_out_price, knock_out_price, places=9)

    # case 3, put option, rebate paid when hit
    def test_lower_limit_case3(self):
        self.option_property['lower_barrier'] = 0.1
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        double_knock_out_price = utils.call(self.method, self.params)

        self.option_property['barrier'] = self.option_property['upper_barrier']
        self.option_property['rebate_amount'] = self.option_property['upper_rebate']
        self.option_property['direction'] = 'up_and_out'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(double_knock_out_price, knock_out_price, places=9)

    # case 4, put option, rebate paid at expiry
    def test_lower_limit_case4(self):
        self.option_property['lower_barrier'] = 0.1
        self.option_property['type'] = 'put'
        self.option_property['rebate_type'] = 'pay_at_expiry'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        double_knock_out_price = utils.call(self.method, self.params)

        self.option_property['barrier'] = self.option_property['upper_barrier']
        self.option_property['rebate_amount'] = self.option_property['upper_rebate']
        self.option_property['direction'] = 'up_and_out'
        self.params['instrument'] = instruments.knock_out_continuous(self.option_property)
        knock_out_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(double_knock_out_price, knock_out_price, places=9)

    # 3, exotic test. test the calculator using exotic combinations of vol r, q
    # 3.1 vol = 0.
    # case 1: call option
    def test_ex1_call(self):
        self.params['vol'] = 0.0
        # self.option_property['upper_rebate'] = 0
        # self.option_property['lower_rebate'] = 0
        # self.params['valDate'] = '2017-08-07T00:00:00'
        # self.params['r'] = 0.1
        # self.option_property['rebate_type'] = 'pay_at_expiry'
        # self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(0.0, price, places=15)

    # case 2: put option
    def test_ex1_put(self):
        self.params['vol'] = 0.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(0, price, places=15)

    # 3.2 vol =1e-3
    # case 1: call option
    def test_ex2_call(self):
        self.params['vol'] = 1e-4
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(0.003608790436758738, price, places=15)

    # case 2: put option
    def test_ex2_put(self):
        self.params['vol'] = 1e-3
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(0.036087902879740126, price, places=15)

    # 3.3 vol = 10
    # case 1: call option
    def test_ex3_call(self):
        self.params['vol'] = 10.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(7.499925463157357, price, places=15)

    # case 2:put option
    def test_ex3_put(self):
        self.params['vol'] = 10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(7.499925463157357, price, places=15)

    # 3.4  r = -10
    # case 1: call option
    def test_ex4_call(self):
        self.params['r'] = -10
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(5.549751796764291, price, places=15)

    # case 2: put option
    def test_ex4_put(self):
        self.params['r'] = -10.0
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(5.549751796764291, price, places=15)

    # 3.5 r = 10
    # case 1: call
    def test_ex5_call(self):
        self.params['r'] = 10
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(9.082179025578567, price, places=15)

    # case 2: put
    def test_ex5_put(self):
        self.params['r'] = 10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(9.082179025578567, price, places=15)

    # 3.6 q = -10
    # case 1: call
    def test_ex6_call(self):
        self.params['q'] = -10.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(9.990549279751074, price, places=15)

    # case 2: put
    def test_ex6_put(self):
        self.params['q'] = -10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(9.990549279751074, price, places=15)

    # 3.7 q = 10
    # case 1: call
    def test_ex7_call(self):
        self.params['q'] = 10.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(4.994692417102769, price, places=15)

    # case 2: put
    def test_ex7_put(self):
        self.params['q'] = 10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(4.994692417102769, price, places=15)

    # 3.8 vol = 1e-6, r =10, q=0
    # case 1: call option
    def test_ex8_call(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 10.
        self.params['q'] = 0.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(9.09090909090909, price, places=15)

    # case 2: put option
    def test_ex8_put(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 10.
        self.params['q'] = 0.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(9.09090909090909, price, places=15)

    # 3.9 vol = 1e-6, r =10, q=10
    # case 1: call option
    def test_ex9_call(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 10.
        self.params['q'] = 10.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(0, price, places=15)

    # case 2: put option
    def test_ex9_put(self):
        self.params['vol'] = 1e-6
        self.params['r'] = 10.
        self.params['q'] = 10.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(2.0522817086275678e-08, price, places=15)

    # 3.10 vol = 1e-6, r =-10, q=0
    # case 1: call option
    def test_ex10_call(self):
        self.params['vol'] = 1e-6
        self.params['r'] = -10.
        self.params['q'] = 0.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(5.555555555555555, price, places=15)

    # case 2: put option
    def test_ex10_put(self):
        self.params['vol'] = 1e-6
        self.params['r'] = -10.
        self.params['q'] = 0.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(5.555555555555555, price, places=15)

    # 3.11 vol = 10, r =-10, q=0
    # case 1: call option
    def test_ex11_call(self):
        self.params['vol'] = 10
        self.params['r'] = -10.
        self.params['q'] = 0.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(7.482397755413479, price, places=15)

    # case 2: put option
    def test_ex11_put(self):
        self.params['vol'] = 10
        self.params['r'] = -10.
        self.params['q'] = 0.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(7.482397755413479, price, places=15)

    # 3.12 vol = 10, r =10, q=0
    # case 1: call option
    def test_ex12_call(self):
        self.params['vol'] = 10
        self.params['r'] = 10.
        self.params['q'] = 0.
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(7.517572769502204, price, places=15)

    # case 2: put option
    def test_ex12_put(self):
        self.params['vol'] = 10
        self.params['r'] = 10.
        self.params['q'] = 0.
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(7.517572769502204, price, places=15)

        # 3.13 vol = 10, r =10, q=10
        # case 1: call option

    def test_ex13_call(self):
        self.params['vol'] = 10
        self.params['r'] = 10
        self.params['q'] = 10
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(7.492552470854562, price, places=15)

        # case 2: put option

    def test_ex13_put(self):
        self.params['vol'] = 10
        self.params['r'] = 10
        self.params['q'] = 10
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.double_knock_out_continuous(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(7.492552470854562, price, places=15)

suit = unittest.TestLoader().loadTestsFromTestCase(DoubleKnockOutTestCase)
