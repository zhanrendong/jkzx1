#!/usr/bin/python
# -*- coding: utf-8 -*-

import pandas as pd
from pandas.io.json import json_normalize
import numpy as np


def gen_rename_dict(fields):
    rename = {}
    for field in fields:
        if field.startswith('asset.'):
            rename[field] = field.replace('asset.', '')
    return rename


def remove_ignore_fields(fields):
    fields_to_del = []
    for field in fields:
        if field.startswith('fixing') or field.startswith('observationDates') or field.startswith(
                'knockInObservationDates'):
            fields_to_del.append(field)
    for field_to_del in fields_to_del:
        fields.remove(field_to_del)


def dump_to_csv(file, data, sort_col):
    data.replace('None', np.nan, inplace=True)
    if sort_col:
        data.sort_values(sort_col, inplace=True)
    if data.shape[0] > 1:
        data = data.apply(lambda col: col if str(col.dtype) == 'object' else np.float64(col), axis=0)
    data.to_csv(file, na_rep='', index=0, encoding="utf_8_sig", float_format='%.8f')
    print(file + ' is generated')


def dump_basic_position(prod_data, qa_data, prod_file, qa_file):
    prod = json_normalize(prod_data.values())
    qa = qa_data.drop(['initialSpot', 'underlyerMultiplier', 'comment'], axis=1)
    qa_columns = list(qa.columns.values)
    qa.rename(columns=gen_rename_dict(qa_columns), inplace=True)
    columns_range = list(prod.columns.values)
    remove_ignore_fields(columns_range)
    index_range = qa['positionId'].isin(prod['positionId'])

    dump_to_csv(prod_file, prod[columns_range], "positionId")
    dump_to_csv(qa_file, qa[columns_range][index_range], "positionId")


def dump_position_index(prod_data, qa_data, prod_file, qa_file):
    prod = json_normalize(prod_data.values())
    qa = qa_data.reset_index()
    qa_columns = list(qa.columns.values)
    qa.rename(columns=gen_rename_dict(qa_columns), inplace=True)
    columns_range = list(prod.columns.values)
    index_range = qa['positionId'].isin(prod['positionId'])

    dump_to_csv(prod_file, prod[columns_range], "positionId")
    dump_to_csv(qa_file, qa[columns_range][index_range], "positionId")


def dump_basic_underlyer_position(prod_data, qa_data, prod_file, qa_file):
    temp = []
    for l in prod_data.values():
        for d in l.values():
            temp.append(d)
    key = ['bookId', 'instrumentId']
    prod = pd.DataFrame(temp).set_index(key)
    qa = qa_data.set_index(key)
    columns_range = list(prod.columns.values)
    index_range = qa.index.isin(prod.index)
    dump_to_csv(prod_file, prod[columns_range].reset_index(), key)
    dump_to_csv(qa_file, qa[columns_range][index_range].reset_index(), key)


def dump_basic_cash_flow(prod_data, qa_data, prod_file, qa_file):
    prod = json_normalize(prod_data.values())
    columns_range = list(prod.columns.values)
    index_range = qa_data['positionId'].isin(prod['positionId'])
    dump_to_csv(prod_file, prod[columns_range], "positionId")
    dump_to_csv(qa_file, qa_data[columns_range][index_range], "positionId")


def dump_basic_portfolio_trades(prod_data, qa_data, prod_file, qa_file):
    datas = []
    for k, v in prod_data.items():
        for t in v:
            datas.append({'portfolioName': k, 'tradeId': t})
    prod = pd.DataFrame(datas)
    columns_range = list(prod.columns.values)
    dump_to_csv(prod_file, prod[columns_range], 'tradeId')
    dump_to_csv(qa_file, qa_data[columns_range], 'tradeId')


def dump_basic_risk(prod_data, qa_data, prod_file, qa_file):
    prod = json_normalize(prod_data.values())
    qa = qa_data.reset_index()
    columns_range = list(prod.columns.values)
    columns_range.remove('isSuccess')
    index_range = qa['positionId'].isin(prod['positionId'])
    dump_to_csv(prod_file, prod[columns_range], 'positionId')
    dump_to_csv(qa_file, qa[index_range][columns_range], 'positionId')


def dump_report(prod_data, qa_data, prod_file, qa_file, key=[]):
    prod = pd.DataFrame(prod_data)
    qa = pd.DataFrame(qa_data)
    if key:
        qa.set_index(key, inplace=True)
        prod.set_index(key, inplace=True)
    index_range = qa.index.isin(prod.index)
    if key:
        prod.reset_index(inplace=True)
        qa.reset_index(inplace=True)
    dump_to_csv(prod_file, prod, key)
    dump_to_csv(qa_file, qa[index_range], key)
