# -*- coding: utf-8 -*-
from pandas.io.json import json_normalize

from utils import utils

_datetime_fmt = '%Y-%m-%dT%H:%M:%S'


def get_underlyer_quotes(instrument_ids, valuation_time, domain, headers):
    """Return market value of underlying assets."""
    params = {
        'instrumentIds': instrument_ids,
        'valuationDate': valuation_time.strftime(_datetime_fmt),
        'timezone': None,
        'page': None,
        'pageSize': None
    }
    quote_data = utils.call_request(domain, 'market-data-service', 'mktQuotesListPaged', params, headers)
    if 'result' in quote_data:
        quotes = quote_data['result']['page']
        quotes_df = json_normalize(quotes)
        quotes_df.set_index('instrumentId', inplace=True)
        return quotes_df
    else:
        raise RuntimeError('Failed to fetch underlying asset quotes.')
