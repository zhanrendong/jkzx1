# -*- coding: utf-8 -*-
from utils.model_utils import get_terminal_session, Instrument


def get_instrument_contract_dict():
    contract_dict = {}
    session = None
    try:
        session = get_terminal_session()
        data_list = session.query(Instrument).all()
        if data_list is not None and len(data_list) > 0:
            for item in data_list:
                instrument_id = item.instrument_id
                contract_type = item.contract_type
                if instrument_id is not None:
                    contract_dict[instrument_id.upper()] = contract_type if contract_type is not None else instrument_id
        return contract_dict
    finally:
        if session is not None:
            session.close()
