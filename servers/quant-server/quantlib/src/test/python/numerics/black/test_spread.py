import unittest
import utils
import mc


class SpreadTestCase(unittest.TestCase):
    def setUp(self):
        self.method = 'qlSpreadOptionPriceCalc'
        self.params = {
            's1': 100.0,
            's2': 105.0,
            'k': 2.0,
            'tau': 1.0,
            'r': 0.02,
            'q1': 0.01,
            'q2': 0.02,
            'vol1': 0.2,
            'vol2': 0.3,
            'rho': 0.5

        }

    def tearDown(self):
        self.params = {}

    # test price
    # compared with the price calculated using Monte Carlo method, allowed relative error 10% using 10,000 samples
    def test_price(self):
        price = utils.call(self.method, self.params)
        spot1 = self.params['s1']
        spot2 = self.params['s2']
        strike = self.params['k']
        tau = self.params['tau']
        r = self.params['r']
        q1 = self.params['q1']
        q2 = self.params['q2']
        vol1 = self.params['vol1']
        vol2 = self.params['vol2']
        rho = self.params['rho']
        mc_price = mc.spread_mc(spot1, spot2, strike, tau, r, q1, q2, vol1, vol2, rho, 10000)
        self.assertAlmostEqual(mc_price, price, delta=0.1*price)

    # test greeks. The Greeks (for now, delta, dual_delta, gamma and vega, theta, rho_r are supported)
    # delta .
    # delta for spread option is an array delta_i = dU/ds_i
    # delta1
    def test_delta1(self):
        delta = utils.call('qlSpreadOptionDeltaCalc', self.params)
        s1 = self.params['s1']
        step = s1 / 100.0
        self.params['s1'] = s1 + 0.5 * step
        price1 = utils.call(self.method, self.params)
        self.params['s1'] = s1 - 0.5 * step
        price2 = utils.call(self.method, self.params)
        delta1_fd = (price1 - price2) / step
        self.assertAlmostEqual(delta[0], delta1_fd, delta=1e-4)

    # delta2
    def test_delta2(self):
        delta = utils.call('qlSpreadOptionDeltaCalc', self.params)
        s2 = self.params['s2']
        step = s2 / 100.0
        self.params['s2'] = s2 + 0.5 * step
        price1 = utils.call(self.method, self.params)
        self.params['s2'] = s2 - 0.5 * step
        price2 = utils.call(self.method, self.params)
        delta2_fd = (price1 - price2) / step
        self.assertAlmostEqual(delta[1], delta2_fd, delta=1e-4)

    # dual_delta
    def test_dual_delta(self):
        dual_delta = utils.call('qlSpreadOptionDualDeltaCalc', self.params)
        k = self.params['k']
        h = k / 100.0
        self.params['k'] = k + 0.5 * h
        price1 = utils.call(self.method, self.params)
        self.params['k'] = k - 0.5 * h
        price2 = utils.call(self.method, self.params)
        dual_delta_fd = (price1 - price2) / h
        self.assertAlmostEqual(dual_delta, dual_delta_fd, delta=1e-4)

    # gamma
    # gamma_ij = d^2U/ds_i/ds_j
    # gamma11 = d^2U/ds_1/ds_1
    def test_gamma11(self):
        gamma = utils.call('qlSpreadOptionGammaCalc', self.params)
        price = utils.call(self.method, self.params)
        s1 = self.params['s1']
        h = s1 / 100.0
        self.params['s1'] = s1 + 0.5 * h
        price1 = utils.call(self.method, self.params)

        self.params['s1'] = s1 - 0.5 * h
        price2 = utils.call(self.method, self.params)

        gamma11_fd = (price1 + price2 - 2.0 * price) / (0.5 * h) / (0.5 * h)
        self.assertAlmostEqual(gamma[0][0], gamma11_fd, delta=1e-4)

    # gamma22 = d^2U/ds_2/ds_2
    def test_gamma22(self):
        gamma = utils.call('qlSpreadOptionGammaCalc', self.params)
        price = utils.call(self.method, self.params)
        s2 = self.params['s2']
        h = s2 / 100.0
        self.params['s2'] = s2 + 0.5 * h
        price1 = utils.call(self.method, self.params)

        self.params['s2'] = s2 - 0.5 * h
        price2 = utils.call(self.method, self.params)

        gamma22_fd = (price1 + price2 - 2.0 * price) / (0.5 * h) / (0.5 * h)
        self.assertAlmostEqual(gamma[1][1], gamma22_fd, delta=1e-4)

    # gamma12 = gamma21 = d^2U/ds_1/ds_2
    def test_gamma12(self):
        gamma = utils.call('qlSpreadOptionGammaCalc', self.params)
        s1 = self.params['s1']
        s2 = self.params['s2']
        h1 = s1 / 100.0
        h2 = s2 / 100.0
        self.params['s1'] = s1 + h1
        self.params['s2'] = s2 + h2
        price1 = utils.call(self.method, self.params)

        self.params['s1'] = s1 + h1
        self.params['s2'] = s2 - h2
        price2 = utils.call(self.method, self.params)

        self.params['s1'] = s1 - h1
        self.params['s2'] = s2 + h2
        price3 = utils.call(self.method, self.params)

        self.params['s1'] = s1 - h1
        self.params['s2'] = s2 - h2
        price4 = utils.call(self.method, self.params)
        gamma12_fd = (price1 - price2 - price3 + price4) / 4.0 / h1 / h2

        self.assertAlmostEqual(gamma[0][1], gamma12_fd, delta=1e-4)

    # vega
    # vega_i = dU/d vol_i
    # vega1 = dU/d vol_1
    def test_vega1(self):
        vega = utils.call('qlSpreadOptionVegaCalc', self.params)

        vol1 = self.params['vol1']
        h = vol1 / 100.0
        self.params['vol1'] = vol1 + 0.5 * h
        price1 = utils.call(self.method, self.params)

        self.params['vol1'] = vol1 - 0.5 * h
        price2 = utils.call(self.method, self.params)

        vega1_fd = (price1 - price2) / h
        self.assertAlmostEqual(vega[0], vega1_fd, places=15)

    # vega2 = dU/d vol_2
    def test_vega2(self):
        vega = utils.call('qlSpreadOptionVegaCalc', self.params)

        vol2 = self.params['vol2']
        h = vol2 / 100.0
        self.params['vol2'] = vol2 + 0.5 * h
        price1 = utils.call(self.method, self.params)

        self.params['vol2'] = vol2 - 0.5 * h
        price2 = utils.call(self.method, self.params)

        vega2_fd = (price1 - price2) / h
        self.assertAlmostEqual(vega[1], vega2_fd, places=15)

    # theta
    # theta = dU/d tau
    def test_theta(self):
        theta = utils.call('qlSpreadOptionThetaCalc', self.params)
        tau = self.params['tau']
        h = tau / 100.0
        self.params['tau'] = tau - 0.5 * h
        price1 = utils.call(self.method, self.params)

        self.params['tau'] = tau + 0.5 * h
        price2 = utils.call(self.method, self.params)

        theta_fd = (price2 - price1) / h
        self.assertAlmostEqual(theta, theta_fd, places=15)

    # rho_r
    # rho_r = dU/dr
    def test_rho_r(self):
        rho_r = utils.call('qlSpreadOptionRhoRCalc', self.params)
        r = self.params['r']
        h = r / 100.0
        self.params['r'] = r - 0.5 * h
        price1 = utils.call(self.method, self.params)

        self.params['r'] = r + 0.5 * h
        price2 = utils.call(self.method, self.params)

        rho_r_fd = (price2 - price1) / h
        self.assertAlmostEqual(rho_r, rho_r_fd, places=15)


suite = unittest.TestLoader().loadTestsFromTestCase(SpreadTestCase)
