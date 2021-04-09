import unittest
import utils
import mc


class QuantoTestCase(unittest.TestCase):
    def setUp(self):
        self.method = 'qlQuantoOptionCalc'
        self.params = {
            'request': 'price',
            'optionType': 'call',
            'spot': 100,
            'strike': 105,
            'rf': 0.03,
            'q': 0.02,
            'vols': 0.2,
            'r': 0.04,
            'Ep': 1.2,
            'vole': 0.3,
            'rho': 0.5,
            'tau': 1.0,
        }

    def tearDown(self):
        self.params = {}

    # compare with the price calculated using Monte Carlo method, the allowed relative error 10%
    def test_price(self):
        self.params['request'] = 'price'
        price = utils.call(self.method, self.params)
        option_type = self.params['optionType']
        spot = self.params['spot']
        strike = self.params['strike']
        rf = self.params['rf']
        q = self.params['q']
        vols = self.params['vols']
        r = self.params['r']
        Ep = self.params['Ep']
        vole = self.params['vole']
        rho = self.params['rho']
        tau = self.params['tau']
        n = 10000
        mc_price = mc.quanto_mc(option_type, spot, strike, rf, q, vols, r, Ep, vole, rho, tau, n)
        self.assertAlmostEqual(mc_price, price, delta=price * 0.1)

    # test Greeks
    # delta
    def test_delta(self):
        self.params['request'] = 'delta'
        delta = utils.call(self.method, self.params)

        self.params['request'] = 'price'
        spot = self.params['spot']
        h = spot / 100.0

        self.params['spot'] = spot - 0.5 * h
        price1 = utils.call(self.method, self.params)
        self.params['spot'] = spot + 0.5 * h
        price2 = utils.call(self.method, self.params)

        delta_fd = (price2 - price1) / h
        self.assertAlmostEqual(delta, delta_fd, delta=0.01)

    # gamma
    def test_gamma(self):
        self.params['request'] = 'gamma'
        gamma = utils.call(self.method, self.params)

        self.params['request'] = 'price'
        price = utils.call(self.method, self.params)
        spot = self.params['spot']
        h = spot / 100.0

        self.params['spot'] = spot - h
        price1 = utils.call(self.method, self.params)
        self.params['spot'] = spot + h
        price2 = utils.call(self.method, self.params)

        gamma_fd = (price2 + price1 - 2.0 * price) / h / h
        self.assertAlmostEqual(gamma, gamma_fd, delta=0.01)

    # dual_delta
    def test_dual_delta(self):
        self.params['request'] = 'dual_delta'
        dual_delta = utils.call(self.method, self.params)

        self.params['request'] = 'price'
        strike = self.params['strike']
        h = strike / 100

        self.params['strike'] = strike - 0.5 * h
        price1 = utils.call(self.method, self.params)
        self.params['strike'] = strike + 0.5 * h
        price2 = utils.call(self.method, self.params)
        dual_delta_fd = (price2 - price1) / h
        self.assertAlmostEqual(dual_delta, dual_delta_fd, delta=0.01)

    # dual_gamma
    def test_dual_gamma(self):
        self.params['request'] = 'dual_gamma'
        dual_gamma = utils.call(self.method, self.params)

        self.params['request'] = 'price'
        price = utils.call(self.method, self.params)
        strike = self.params['strike']
        h = strike / 100

        self.params['strike'] = strike - h
        price1 = utils.call(self.method, self.params)
        self.params['strike'] = strike + h
        price2 = utils.call(self.method, self.params)
        dual_gamma_fd = (price2 + price1 - 2 * price) / h / h
        self.assertAlmostEqual(dual_gamma, dual_gamma_fd, delta=0.01)

    # theta
    def test_theta(self):
        self.params['request'] = 'theta'
        theta = utils.call(self.method, self.params)

        self.params['request'] = 'price'
        tau = self.params['tau']
        h = tau / 100

        self.params['tau'] = tau - 0.5 * h
        price1 = utils.call(self.method, self.params)
        self.params['tau'] = tau + 0.5 * h
        price2 = utils.call(self.method, self.params)
        theta_fd = (price2-price1) / h
        self.assertAlmostEqual(theta, theta_fd, delta=0.01)

    # vega with respect to the volatility of the spot price
    def test_vega(self):
        self.params['request'] = 'vega'
        vega = utils.call(self.method, self.params)

        self.params['request'] = 'price'
        vol = self.params['vols']
        h = vol / 100

        self.params['vols'] = vol - 0.5 * h
        price1 = utils.call(self.method, self.params)
        self.params['vols'] = vol + 0.5 * h
        price2 = utils.call(self.method, self.params)

        vega_fd = (price2 - price1) / h
        self.assertAlmostEqual(vega, vega_fd, delta=0.01)

suite = unittest.TestLoader().loadTestsFromTestCase(QuantoTestCase)
