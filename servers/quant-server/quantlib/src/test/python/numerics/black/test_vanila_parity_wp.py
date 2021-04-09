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

    # 1.K=1
    # case 1:S=0.1
    def test_parity_case1(self):
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 0.1
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 0.1
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 2:S=0.3
    def test_parity_case2(self):
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 0.3
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 0.3
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 3:S=0.5
    def test_parity_case3(self):
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 0.5
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 0.5
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 4:S=0.7
    def test_parity_case4(self):
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 0.7
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 0.7
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 5:S=0.9
    def test_parity_case5(self):
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 0.9
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 0.9
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 6:S=1.2
    def test_parity_case6(self):
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 1.2
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 1.2
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 7:S=1.4
    def test_parity_case7(self):
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 1.4
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 1.4
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 8:S=1.6
    def test_parity_case8(self):
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 1.6
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 1.6
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 9:S=1.8
    def test_parity_case9(self):
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 1.8
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 1.8
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 10:S=2.0
    def test_parity_case10(self):
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 2.0
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        self.params['spot'] = 2.0
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # 2.S=1
    # case 11:K=0.1
    def test_parity_case11(self):
        self.option_property['strike'] = 0.1
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['strike'] = 0.1
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 12:K=0.3
    def test_parity_case12(self):
        self.option_property['strike'] = 0.3
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['strike'] = 0.3
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 13:K= 0.5
    def test_parity_case13(self):
        self.option_property['strike'] = 0.5
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['strike'] = 0.5
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 14:K=0.7
    def test_parity_case14(self):
        self.option_property['strike'] = 0.7
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['strike'] = 0.7
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 15:K=0.9
    def test_parity_case15(self):
        self.option_property['strike'] = 0.9
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['strike'] = 0.9
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 16:K=1.2
    def test_parity_case16(self):
        self.option_property['strike'] = 1.2
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['strike'] = 1.2
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 17:K=1.4
    def test_parity_case17(self):
        self.option_property['strike'] = 1.4
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['strike'] = 1.4
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 18:K=1.6
    def test_parity_case18(self):
        self.option_property['strike'] = 1.6
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['strike'] = 1.6
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 19:K=1.8
    def test_parity_case19(self):
        self.option_property['strike'] = 1.8
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['strike'] = 1.8
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)

    # case 20:K=2.0
    def test_parity_case20(self):
        self.option_property['strike'] = 2.0
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['strike'] = 2.0
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(
            call_price - put_price - self.params['spot'] * df_q + self.option_property['strike'] * df_r,
            0.0, places=10)
