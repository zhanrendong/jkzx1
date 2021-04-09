class WingModelConfig:
    bounds_dict = {
    '510050.SH': ([-0.1, 0, 0, 0, -1e5, 0, -1e6, -1e6], [0, 0.1, 1e5, 1e5, 1e5, 4, 1e6, 1e6]),
    'C': ([-0.025, 0, 0, 0, -1e6, 0, -1e6, -1e6], [0, 0.025, 1e10, 1e10, 1e6, 4, 1e6, 1e6]),
    'CF': ([-1, 1e-3, 0, 0, -1e5, 0, -1e6, -1e6], [-0.5, 1, 1e3, 1e10, 1e5, 4, 1e6, 1e6]),
    'SR': ([-0.1, 0, 0, 0, -1e5, 0, -1e6, -1e6], [0, 0.1, 1e5, 1e5, 1e5, 4, 1e6, 1e6]),
    'M': ([-0.1, 0, 0, 0, -1e5, 0, -1e6, -1e6], [0, 0.1, 1e5, 1e5, 1e5, 4, 1e6, 1e6]),
    'CU': ([-0.1, 0, 0, 0, -1e5, 0, -1e6, -1e6], [0, 0.1, 1e5, 1e5, 1e5, 4, 1e6, 1e6]),
    'RU': ([-0.1, 0, 0, 0, -1e5, 0, -1e6, -1e6], [0, 0.1, 1e5, 1e5, 1e5, 4, 1e6, 1e6])
    }

    data_source = {
        '510050.SH': 'QuoteClose',
        'C': 'QuoteClose',
        'CF': 'QuoteClose',
        'SR': 'QuoteClose',
        'M': 'QuoteClose',
        'CU': 'QuoteClose',
        'RU': 'QuoteClose'
    }


class JWTConfig:
    TOKEN_KEY = "Authorization"
    TOKEN_PREFIX = "Bearer "
    SIGN_SECRET = 'dkJ34Bdadf098adf'
    SIGN_ALGO = 'HS256'


