import unittest
import math
import utils


class BlackTestCase(unittest.TestCase):
    def setUp(self):
        """
        Initialise objects used by all the tests. For example, construct an instrument to be priced by the tests
        :return: None
        """
        self.method = 'qlBlackCalc'
        self.params = {
            'request': 'price',
            'spot': 100,
            'strike': 105,
            'vol': 0.2,
            'tau': 1.0,
            'r': 0.02,
            'q': 0.01,
            'type': 'call'
        }

    def tearDown(self):
        """
        Dispose any objects created by setUp(). Usually not needed because objects are managed by quant-service.
        May be necessary if one want's to manually remove the objects from the quant-service.
        :return:
        """
        self.params = {}

    def test_price(self):
        """
        test
        Returns:

        """
        self.params['request'] = 'price'
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(6.234595809, price, places=9)

    def test_parity(self):
        self.params['request'] = 'price'
        self.params['type'] = 'call'
        call_price = utils.call(self.method, self.params)
        self.params['type'] = 'put'
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * self.params['tau'])
        df_q = math.exp(-self.params['q'] * self.params['tau'])
        self.assertAlmostEqual(call_price - put_price - self.params['spot'] * df_q + self.params['strike'] * df_r,
                               0.0, places=13)


suite = unittest.TestLoader().loadTestsFromTestCase(BlackTestCase)
