import unittest
import utils
import instruments


class RangeTestCase(unittest.TestCase):
    def setUp(self):
        self.option_property = {
            'cumulative': 'call',
            'payoff': 100.0,
            'expiry': '2015-07-06T00:00:00',
            'delivery': '2015-07-06T00:00:00',
            'fixings': [0.0],
            'schedule': ['2015-01-06T00:00:00', '2015-01-12T00:00:00', '2015-01-19T00:00:00',
                         '2015-01-26T00:00:00', '2015-02-02T00:00:00', '2015-02-09T00:00:00',
                         '2015-02-16T00:00:00', '2015-02-23T00:00:00', '2015-03-02T00:00:00',
                         '2015-03-09T00:00:00', '2015-03-16T00:00:00', '2015-03-23T00:00:00',
                         '2015-03-30T00:00:00', '2015-04-06T00:00:00', '2015-04-13T00:00:00',
                         '2015-04-20T00:00:00', '2015-04-27T00:00:00', '2015-05-04T00:00:00',
                         '2015-05-11T00:00:00', '2015-05-18T00:00:00', '2015-05-25T00:00:00',
                         '2015-06-01T00:00:00', '2015-06-08T00:00:00', '2015-06-15T00:00:00',
                         '2015-06-22T00:00:00', '2015-06-29T00:00:00', '2015-07-06T00:00:00'],
            'min': 98.0,
            'max': 102,
        }
        self.method = 'qlOptionCalcBlack'
        self.params = {
            'instrument': 'option',
            'request': 'price',
            'spot': 100.,
            'valDate': '2015-01-05T00:00:00',
            'vol': 0.5,
            'r': 0.08,
            'q': 0.03,
        }

    # test price
    def test_price(self):
        self.params['instrument'] = instruments.range_accrual(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(price, 86.905659871371, places=9)

    # test Greeks
    # range accrual Greeks are calculated using Auto difference, here we use finite difference to test several of them
    # 1 delta, 2 gamma, 3, vega, 4, dual_delta
    # test delta
    def test_delta(self):
        # create the option
        self.params['instrument'] = instruments.range_accrual(self.option_property)
        # calculates the gamma
        self.params['request'] = 'delta'
        delta = utils.call(self.method, self.params)
        # calculates the price with spot = spot + 0.5h
        self.params['request'] = 'price'
        spot = self.params['spot']
        h = spot / 100.0
        self.params['spot'] = spot + 0.5 * h
        price1 = utils.call(self.method, self.params)
        self.params['spot'] = spot - 0.5 * h
        price2 = utils.call(self.method, self.params)
        delta_fd = (price1 - price2) / h
        self.assertAlmostEqual(delta, delta_fd, delta=h * h)

    # test gamma
    def test_gamma(self):
        # create the option
        self.params['instrument'] = instruments.range_accrual(self.option_property)
        # calculates the gamma
        self.params['request'] = 'gamma'
        gamma = utils.call(self.method, self.params)
        # calculates the price with spot = spot + 0.5h, spot, spot-0.5*h
        self.params['request'] = 'price'
        price = utils.call(self.method, self.params)
        spot = self.params['spot']
        h = spot / 100.0
        self.params['spot'] = spot + 0.5 * h
        price1 = utils.call(self.method, self.params)
        self.params['spot'] = spot - 0.5 * h
        price2 = utils.call(self.method, self.params)
        gamma_fd = (price1 + price2 - 2 * price) / (0.5 * h) / (0.5 * h)
        self.assertAlmostEqual(gamma, gamma_fd, delta=0.25 * h * h)

    # test vega
    def test_vega(self):
        # create the option
        self.params['instrument'] = instruments.range_accrual(self.option_property)
        # calculates the gamma
        self.params['request'] = 'vega'
        vega = utils.call(self.method, self.params)
        # calculates the price with vol = vol + 0.5h, vol - 0.5h
        self.params['request'] = 'price'
        vol = self.params['vol']
        h = vol / 100.0
        self.params['vol'] = vol + 0.5 * h
        price1 = utils.call(self.method, self.params)
        self.params['vol'] = vol - 0.5 * h
        price2 = utils.call(self.method, self.params)
        vega_fd = (price1 - price2) / h
        self.assertAlmostEqual(vega, vega_fd, delta=h)

suite = unittest.TestLoader().loadTestsFromTestCase(RangeTestCase)
