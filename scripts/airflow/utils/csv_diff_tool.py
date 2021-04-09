# -*- coding: utf-8 -*-
import pandas as pd
from utils import utils
import numpy as np

MISSING_BAS = 'missing_bas'
MISSING_QA = 'missing_qa'
BAS = 'bas'
QA = 'qa'


def has_diff(qa_data, bas_data, tolerance):
    if type(qa_data) == str or type(bas_data) == str:
        return qa_data != bas_data
    else:
        qa_num = 0 if qa_data is None or utils.is_nan(qa_data) else np.float(qa_data)
        bas_num = 0 if bas_data is None or utils.is_nan(bas_data) else np.float(bas_data)
    diff = abs(bas_data - qa_num)
    return diff > tolerance and (abs(diff / bas_num) > tolerance if bas_num != 0 else True)


def new_index(index, diff_key, suffix):
    return str(index if len(diff_key) < 2 else '-'.join(index)) + '-' + suffix


def diff_csv(qa_file, bas_file, diff_result, key=[], tolerance=0.0001, ignore=[]):
    return diff(pd.read_csv(qa_file), pd.read_csv(bas_file), diff_result, key=key, tolerance=tolerance, ignore=ignore)


def diff(qa, bas, diff_result, key=[], tolerance=0.0001, ignore=[]):
    clean_diff = True
    if key:
        qa.set_index(key, inplace=True)
        bas.set_index(key, inplace=True)
    fields = list(bas.columns.values)

    # sort columns
    qa = qa[fields]
    bas = bas[fields]

    qa_index = set(qa.index.values)
    bas_index = set(bas.index.values)
    missing_qa_index = bas_index.difference(qa_index)
    missing_bas_index = qa_index.difference(bas_index)
    indexes = bas_index.intersection(qa_index)
    result = pd.DataFrame(columns=fields)

    for index in indexes:
        qa_row = qa.loc[index]
        bas_row = bas.loc[index]
        for field in fields:
            if field not in ignore:
                qa_data = qa_row[field]
                bas_data = bas_row[field]
                if has_diff(qa_data, bas_data, tolerance):
                    clean_diff = False
                    result.loc[new_index(index, key, BAS), field] = bas_data
                    result.loc[new_index(index, key, QA), field] = qa_data

    for index in missing_qa_index:
        clean_diff = False
        for field in fields:
            result.loc[new_index(index, key, MISSING_QA), field] = bas.loc[index, field]

    for index in missing_bas_index:
        clean_diff = False
        for field in fields:
            result.loc[new_index(index, key, MISSING_BAS), field] = qa.loc[index, field]

    if not clean_diff:
        result.to_csv(diff_result, na_rep='', encoding="utf_8_sig",
                      index_label='key' if len(key) == 0 else '-'.join(key))
        print('diff observed, please check diff result file: ' + diff_result + ' and provide sign off')
        # raise Exception('diff observed, please check diff result file: ' + diff_result + ' and provide sign off')
