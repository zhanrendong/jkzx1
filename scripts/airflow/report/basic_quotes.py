# -*- coding: utf-8 -*-

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
        id_to_quotes = {q['instrumentId']: q for q in quotes}
        if instrument_ids is not None and len(quotes) < len(instrument_ids):
            missing_ids = []
            for i in instrument_ids:
                if i not in id_to_quotes:
                    missing_ids.append(i)
                    id_to_quotes[i] = {
                        'instrumentId': i,
                        'last': None,
                        'bid': None,
                        'ask': None,
                        'close': None,
                        'settle': None,
                        'multiplier': None
                    }
            # raise RuntimeError('Missing quotes: {ids}'.format(ids=missing_ids))
        return id_to_quotes
    else:
        raise RuntimeError('Failed to fetch underlying asset quotes.')
