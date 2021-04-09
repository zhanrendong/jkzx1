# -*- coding: utf-8 -*-
from utils import utils
import pandas as pd

LISTED_POSITION_COLUMNS = ['instrumentId', 'underlyerInstrumentMultiplier']


def get_underlyer_positions(domain, headers, prcing_env):
    """Return exchange listed spot/future trade statistics."""
    param = {'similarBookName': ''}
    books = utils.call_request(domain, 'trade-service', 'trdBookListBySimilarBookName', param, headers)['result']
    param = {"pricingEnvironmentId": prcing_env, 'books': books}
    positions = utils.call_request(domain, 'pricing-service', 'prcPriceOnExchange', param, headers)
    if 'result' in positions:
        if len(positions['result']) == 0:
            # return pd.DataFrame(columns=LISTED_POSITION_COLUMNS), pd.DataFrame(columns=LISTED_POSITION_COLUMNS)
            return pd.DataFrame(), pd.DataFrame()
        positions_df = pd.DataFrame(positions['result'])
        instruments = list(positions_df.instrumentId.dropna().unique())
        param = {'instrumentIds': instruments}
        expiration_dates = utils.call_request(domain, 'market-data-service', 'mktInstrumentsListPaged', param, headers)
        expiration_date_df = pd.DataFrame(expiration_dates['result']['page'])[['instrumentId', 'expirationDate']]
        positions_df = positions_df.merge(expiration_date_df, on='instrumentId', how='left')
        underlyer_positions_criteria = positions_df.instrumentId == positions_df.underlyerInstrumentId
        underlyer_positions = positions_df[underlyer_positions_criteria]
        listed_option_positions = positions_df[~underlyer_positions_criteria]
        return underlyer_positions, listed_option_positions
    else:
        raise RuntimeError('Failed to fetch spot/future trade statistics.')
