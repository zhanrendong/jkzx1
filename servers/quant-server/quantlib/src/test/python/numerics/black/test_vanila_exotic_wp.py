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

    # 1.vol=0
    # fix S,K,change tau
    # case 1:'valDate': '2016-08-08T00:00:00'
    def test_exotic_case1(self):
        self.params['vol'] = 0.
        self.params['valDate'] = '2016-08-08T00:00:00'
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # case 2:'valDate': '2017-01-01T00:00:00'
    def test_exotic_case2(self):
        self.params['vol'] = 0.
        self.params['valDate'] = '2017-01-01T00:00:00'
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 219. / 365.25)
        df_q = math.exp(-self.params['q'] * 219. / 365.25)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # fix K,tau,change S
    # case 3:S=1
    def test_exotic_case3(self):
        self.params['vol'] = 0.
        self.params['spot'] = 1.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # case 4:S=77
    def test_exotic_case4(self):
        self.params['vol'] = 0.
        self.params['spot'] = 77.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # fix S,tau,change K
    # case 5:K=1
    def test_exotic_case5(self):
        self.params['vol'] = 0.
        self.option_property['strike'] = 1.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # case 6:K=77
    def test_exotic_case6(self):
        self.params['vol'] = 0.
        self.option_property['strike'] = 77.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # 2.vol=1e-11
    # fix S,K,change tau
    # case 7:'valDate': '2016-08-08T00:00:00'
    def test_exotic_case7(self):
        self.params['vol'] = 1e-11
        self.params['valDate'] = '2016-08-08T00:00:00'
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # case 8:'valDate': '2017-01-01T00:00:00'
    def test_exotic_case8(self):
        self.params['vol'] = 1e-11
        self.params['valDate'] = '2017-01-01T00:00:00'
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 219. / 365.25)
        df_q = math.exp(-self.params['q'] * 219. / 365.25)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # fix K,tau,change S
    # case 9:S=1
    def test_exotic_case9(self):
        self.params['vol'] = 1e-11
        self.params['spot'] = 1.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # case 10:S=77
    def test_exotic_case10(self):
        self.params['vol'] = 1e-11
        self.params['spot'] = 77.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # fix S,tau,change K
    # case 11:K=1
    def test_exotic_case11(self):
        self.params['vol'] = 1e-11
        self.option_property['strike'] = 1.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # case 12:K=77
    def test_exotic_case12(self):
        self.params['vol'] = 1e-11
        self.option_property['strike'] = 77.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * 0.9993155373)
        df_q = math.exp(-self.params['q'] * 0.9993155373)
        self.assertAlmostEqual(call_price, max(0, self.params['spot'] * df_q - self.option_property['strike'] * df_r),
                               places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] * df_r - self.params['spot'] * df_q, 0),
                               places=10)

    # 3.tau=0
    # fix S,K,change q
    # case 13:q=0.0001
    def test_exotic_case13(self):
        self.params['valDate'] = '2017-08-08T00:00:00'
        self.params['q'] = 0.0001
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(call_price, max(self.params['spot'] - self.option_property['strike'], 0.), places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] - self.params['spot'], 0.), places=10)

    # case 14:q=0.2
    def test_exotic_case14(self):
        self.params['valDate'] = '2017-08-08T00:00:00'
        self.params['q'] = 0.2
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(call_price, max(self.params['spot'] - self.option_property['strike'], 0.), places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] - self.params['spot'], 0.), places=10)

    # fix K,q,change S
    # case 15:S=1
    def test_exotic_case15(self):
        self.params['valDate'] = '2017-08-08T00:00:00'
        self.params['spot'] = 1.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(call_price, max(self.params['spot'] - self.option_property['strike'], 0.), places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] - self.params['spot'], 0.), places=10)

    # case 16:S=77
    def test_exotic_case16(self):
        self.params['valDate'] = '2017-08-08T00:00:00'
        self.params['spot'] = 77.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(call_price, max(self.params['spot'] - self.option_property['strike'], 0.), places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] - self.params['spot'], 0.), places=10)

    # fix S,q,change K
    # case 17:K=1
    def test_exotic_case17(self):
        self.params['valDate'] = '2017-08-08T00:00:00'
        self.option_property['strike'] = 1.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(call_price, max(self.params['spot'] - self.option_property['strike'], 0.), places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] - self.params['spot'], 0.), places=10)

    # case 18:K=77
    def test_exotic_case18(self):
        self.params['valDate'] = '2017-08-08T00:00:00'
        self.option_property['strike'] = 77.
        self.option_property['type'] = 'call'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        call_price = utils.call(self.method, self.params)
        self.option_property['type'] = 'put'
        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        put_price = utils.call(self.method, self.params)
        self.assertAlmostEqual(call_price, max(self.params['spot'] - self.option_property['strike'], 0.), places=10)
        self.assertAlmostEqual(put_price, max(self.option_property['strike'] - self.params['spot'], 0.), places=10)
