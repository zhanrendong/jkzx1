from utils import utils
from pandas.io.json import json_normalize
import pandas as pd


def get_parties(domain, headers):
    party_data = utils.call_request(domain, 'reference-data-service', 'refGetAllParties', {}, headers)
    if 'result' in party_data:
        return pd.DataFrame(party_data['result'])