import unittest
import utils
import mc
import numpy as np


class BasketTestCase(unittest.TestCase):
    def setUp(self):
        self.method = 'qlBasketPriceCalc'
        self.params = {
            'spot': [95., 105.],
            'weight': [0.5, 0.5],
            'rho': [[1., 0.5], [0.5, 1]],
            'strike': 100.,
            'vol': [0.2, 0.3],
            'tau': 1.0,
            'r': 0.02,
            'q': [0.01, 0.05],
            'type': 'put'
        }

    # compare with the price calculated using Monte Carlo method with 10,000 samples, the allowed relative error 10%
    def test_price(self):
        price = utils.call(self.method, self.params)
        spot_array = np.array([self.params['spot']])
        weight_array = np.array([self.params['weight']])
        rho_matrix = np.array(self.params['rho'])
        strike = self.params['strike']
        vol_array = np.array([self.params['vol']])
        tau = self.params['tau']
        r = self.params['r']
        q_array = np.array([self.params['q']])
        option_type = self.params['type']
        n = 10000
        mc_price = mc.basket_mc(option_type, spot_array, weight_array, rho_matrix, strike, vol_array, tau, r, q_array, n)
        self.assertAlmostEqual(mc_price, price, delta=0.1*price)

    # test the delta of the basket option
    # 1. delta1 = dU/ds_1
    def test_delta1(self):
        delta = utils.call('qlBasketDeltaCalc', self.params)
        spot = self.params['spot']
        h = spot[0] / 100.0
        self.params['spot'] = [spot[0] + 0.5 * h, spot[1]]
        price1 = utils.call(self.method, self.params)
        self.params['spot'] = [spot[0] - 0.5 * h, spot[1]]
        price2 = utils.call(self.method, self.params)
        delta_fd = (price1 - price2) / h
        self.assertAlmostEqual(delta[0], delta_fd, delta=h * h)

    # 2, delta2 = dU/ds_2
    def test_delta2(self):
        delta = utils.call('qlBasketDeltaCalc', self.params)
        spot = self.params['spot']
        h = spot[1] / 100.0
        self.params['spot'] = [spot[0], spot[1] + 0.5 * h]
        price1 = utils.call(self.method, self.params)
        self.params['spot'] = [spot[0], spot[1] - 0.5 * h]
        price2 = utils.call(self.method, self.params)
        delta_fd = (price1 - price2) / h
        self.assertAlmostEqual(delta_fd, delta[1], delta=h * h)

    # test vega of the basket option
    # vega1 dU/d vol_1
    def test_vega1(self):
        vega = utils.call('qlBasketVegaCalc', self.params)
        vol = self.params['vol']
        h = vol[0] / 100.0
        self.params['vol'] = [vol[0] + 0.5 * h, vol[1]]
        price1 = utils.call(self.method, self.params)
        self.params['vol'] = [vol[0] - 0.5 * h, vol[1]]
        price2 = utils.call(self.method, self.params)
        vega_fd = (price1 - price2) / h
        self.assertAlmostEqual(vega_fd, vega[0], delta=h * h)

    def test_vega2(self):
        vega = utils.call('qlBasketVegaCalc', self.params)
        vol = self.params['vol']
        h = vol[1] / 100.0
        self.params['vol'] = [vol[0], vol[1] + 0.5 * h]
        price1 = utils.call(self.method, self.params)
        self.params['vol'] = [vol[0], vol[1] - 0.5 * h]
        price2 = utils.call(self.method, self.params)
        vega_fd = (price1 - price2) / h
        self.assertAlmostEqual(vega_fd, vega[1], delta=h * h)

    # test theta of the basket option
    # theta = dU/d tau
    def test_theta(self):
        theta = utils.call('qlBasketThetaCalc', self.params)
        tau = self.params['tau']
        h = tau / 100.0
        self.params['tau'] = tau + 0.5 * h
        price1 = utils.call(self.method, self.params)
        self.params['tau'] = tau - 0.5 * h
        price2 = utils.call(self.method, self.params)
        theta_fd = (price1 - price2) / h
        self.assertAlmostEqual(theta, theta_fd, delta=h * h)

    # test dual_delta of the basket option
    # dual_delta = dU/dk
    def test_dual_delta(self):
        dual_delta = utils.call('qlBasketDualDeltaCalc', self.params)
        k = self.params['strike']
        h = k / 100.0
        self.params['strike'] = k + 0.5 * h
        price1 = utils.call(self.method, self.params)
        self.params['strike'] = k - 0.5 * h
        price2 = utils.call(self.method, self.params)
        dual_delta_fd = (price1 - price2) / h
        self.assertAlmostEqual(dual_delta, dual_delta_fd, delta=h * h)

    # test gamma of the basket option
    # gamma_i,j = dU/ds_ids_j
    # gamma11 = d^2U/ds_1/ds_1
    def test_gamma11(self):
        gamma = utils.call('qlBasketGammaCalc', self.params)
        price = utils.call(self.method, self.params)
        spot = self.params['spot']
        h = spot[0] / 100.0
        self.params['spot'] = [spot[0] + 0.5 * h, spot[1]]
        price1 = utils.call(self.method, self.params)
        self.params['spot'] = [spot[0] - 0.5 * h, spot[1]]
        price2 = utils.call(self.method, self.params)
        gamma11_fd = (price1 + price2 - 2.0 * price) / (0.5 * h) / (0.5 * h)
        self.assertAlmostEqual(gamma[0][0], gamma11_fd, delta=h * h)

    # gamma22 = d^2U/ds_2/ds_2
    def test_gamma22(self):
        gamma = utils.call('qlBasketGammaCalc', self.params)
        price = utils.call(self.method, self.params)
        spot = self.params['spot']
        h = spot[1] / 100.0
        self.params['spot'] = [spot[0], spot[1] + 0.5 * h]
        price1 = utils.call(self.method, self.params)
        self.params['spot'] = [spot[0], spot[1] - 0.5 * h]
        price2 = utils.call(self.method, self.params)
        gamma11_fd = (price1 + price2 - 2.0 * price) / (0.5 * h) / (0.5 * h)
        self.assertAlmostEqual(gamma[1][1], gamma11_fd, delta=h * h)

    # gamma12 = d^2U/ds_1/ds_2
    def test_gamma12(self):
        gamma = utils.call('qlBasketGammaCalc', self.params)
        price = utils.call(self.method, self.params)
        spot = self.params['spot']
        h0 = spot[0] / 100.0
        h1 = spot[1] /100
        self.params['spot'] = [spot[0] + h0, spot[1] + h1]
        price1 = utils.call(self.method, self.params)
        self.params['spot'] = [spot[0]+h0, spot[1] - h1]
        price2 = utils.call(self.method, self.params)
        self.params['spot'] = [spot[0] - h0, spot[1] + h1]
        price3 = utils.call(self.method, self.params)
        self.params['spot'] = [spot[0] - h0, spot[1] - h1]
        price4 = utils.call(self.method, self.params)
        gamma12 = (price1-price2-price3+price4)/4.0/h0/h1
        self.assertAlmostEqual(gamma[0][1], gamma12, delta=h0 * h1)

suite = unittest.TestLoader().loadTestsFromTestCase(BasketTestCase)
