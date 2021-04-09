import unittest
import math
import utils


class DigitalTestCase(unittest.TestCase):
    def setUp(self):
        self.method = 'qlBlackDigitalCalc'
        self.params = {
            'request': 'price',
            'spot': 100,
            'strike': 105,
            'vol': 0.2,
            'tau': 1.0,
            'r': 0.02,
            'q': 0.01,
            'type': 'call',
            'cashOrAsset': 'cash'
        }

    def test_parity(self):
        self.params['request'] = 'price'
        self.params['type'] = 'call'
        call_price = utils.call(self.method, self.params)
        self.params['type'] = 'put'
        put_price = utils.call(self.method, self.params)
        df_r = math.exp(-self.params['r'] * self.params['tau'])
        self.assertAlmostEqual(call_price + put_price - df_r,
                               0.0, places=13)


suite = unittest.TestLoader().loadTestsFromTestCase(DigitalTestCase)
