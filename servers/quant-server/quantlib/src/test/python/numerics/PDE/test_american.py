import unittest
import utils
import instruments


class AmericanTestCase(unittest.TestCase):
    def setUp(self):
        self.option_property = {
            'type': 'call',
            'strike': 100,
            'expiry': '2017-08-08T00:00:00',
            'delivery': '2017-08-08T00:00:00'

        }
        self.method = 'qlOptionCalcPDE'
        self.params = {
            'instrument': 'option',
            'valDate': '2016-08-08T00:00:00',
            'spot': 100,
            'r': 0.02,
            'vol': 0.2,
            'q': 0.01,
            'request': 'price',
            'eps': 1e-6,
            'alpha': 0.5
        }

    # when q = 0, American call = European call
    def test_call_price_case1(self):
        self.params['q'] = 0
        self.params['instrument'] = instruments.vanilla_american(self.option_property)
        american_price = utils.call(self.method, self.params)

        self.params['instrument'] = instruments.vanilla_european(self.option_property)
        european_price = utils.call(self.method, self.params)

        self.assertAlmostEqual(american_price, european_price, places=15)

suite = unittest.TestLoader().loadTestsFromTestCase(AmericanTestCase)
