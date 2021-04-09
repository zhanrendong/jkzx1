import unittest
import utils
import instruments


class AsianTestCase(unittest.TestCase):
    def setUp(self):
        self.option_property = {
            'type': 'call',
            'strike': 100.,
            'expiry': '2015-07-06T00:00:00',
            'delivery': '2015-07-06T00:00:00',
            'schedule': ['2015-01-05T00:00:00', '2015-01-12T00:00:00', '2015-01-19T00:00:00',
                         '2015-01-26T00:00:00', '2015-02-02T00:00:00', '2015-02-09T00:00:00',
                         '2015-02-16T00:00:00', '2015-02-23T00:00:00', '2015-03-02T00:00:00',
                         '2015-03-09T00:00:00', '2015-03-16T00:00:00', '2015-03-23T00:00:00',
                         '2015-03-30T00:00:00', '2015-04-06T00:00:00', '2015-04-13T00:00:00',
                         '2015-04-20T00:00:00', '2015-04-27T00:00:00', '2015-05-04T00:00:00',
                         '2015-05-11T00:00:00', '2015-05-18T00:00:00', '2015-05-25T00:00:00',
                         '2015-06-01T00:00:00', '2015-06-08T00:00:00', '2015-06-15T00:00:00',
                         '2015-06-22T00:00:00', '2015-06-29T00:00:00', '2015-07-06T00:00:00'],
            'weights': [1.0 for i in range(27)],
            'fixings': [0.]
        }
        self.method = 'qlOptionCalcBlack'
        self.params = {
            'request': 'price',
            'spot': 100.,
            'instrument': 'option',
            'valDate': '2015-01-05T00:00:00',
            'vol': 0.5,
            'r': 0.08,
            'q': 0.03
        }

    def test_price(self):
        self.params['instrument'] = instruments.average_rate_arithmetic(self.option_property)
        price = utils.call(self.method, self.params)
        self.assertAlmostEqual(price, 8.463606549210105, places=9)

suite = unittest.TestLoader().loadTestsFromTestCase(AsianTestCase)
