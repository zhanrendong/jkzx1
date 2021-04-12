import csv
import math
import os
import uuid
import csvdiff
import sqlalchemy
import json
from config.bct_config import pg_url
from terminal import Logging
import pandas as pd
import chardet
import numpy as np

logging = Logging.getLogger(__name__)


class RegressionResultTable:
    table_name: str
    key_columns: list
    value_columns: list

    def __init__(self, db_name, name, keys, values, roundings={}):
        url = pg_url + '/' + db_name
        engine = sqlalchemy.create_engine(url)
        self.db = engine.connect()
        self.table_name = name
        self.key_columns = keys
        self.value_columns = values
        self.roundings = roundings

    def reg_result_sql(self):
        columns = self.key_columns + self.value_columns
        sql = 'select ' + ','.join(columns) + ' from ' + self.table_name
        logging.info('reg result SQL:%s' % sql)
        return sql

    def reg_result_dataframe(self):
        sql = self.reg_result_sql()
        df = pd.read_sql(sql, self.db)
        return df.round(self.roundings)


def write_to_csv(name, data):
    file_name = name + '.csv'
    keys = data[0].keys()
    logging.info('write data to csv file %s' % file_name)
    with open(file_name, 'w', newline='') as output_file:
        dict_writer = csv.DictWriter(output_file, keys)
        dict_writer.writeheader()
        dict_writer.writerows(data)
        output_file.close()


def convert_dict(data):
    def is_json(myjson):
        if isinstance(myjson, str):
            try:
                json.loads(myjson)
            except ValueError:
                return False
            return True
        else:
            return False

    if isinstance(data, list):
        new_data = []
        for d in data:
            new_data.append(convert_dict(d))
        return new_data
    elif isinstance(data, dict):
        new_data = {}
        for k in data:
            v = data[k]
            if isinstance(v, float) and math.isnan(v):
                new_data[k] = 0.0
            elif v is None:
                new_data[k] = 0.0
            else:
                new_data[k] = convert_dict(data[k])
        return new_data
    elif is_json(data):
        dict_data = json.loads(data)
        return convert_dict(dict_data)
    else:
        return data


def db_to_record(tbl):
    reg_df = tbl.reg_result_dataframe()
    record = convert_dict(reg_df.to_dict('records'))
    tmp_name = str(uuid.uuid4())
    write_to_csv(tmp_name, record)
    tmp_file_name = tmp_name + '.csv'
    rows = file_to_record(tmp_file_name)
    os.remove(tmp_file_name)
    return tbl.table_name, rows


def file_to_record(path):
    # get file's encoding
    tmpfile = open(path, mode='rb')
    data = tmpfile.read()
    encoding = chardet.detect(data)["encoding"]
    logging.info('file:%s encoding is %s' %(tmpfile.name,encoding))

    with open(path, encoding=encoding) as f:
        rows = [{k: v for k, v in row.items()}
                for row in csv.DictReader(f, skipinitialspace=True)]
        return rows


def bas_to_record(test_name, table_name):
    cur_path = os.path.dirname(os.path.realpath(__file__))
    path = os.path.join(cur_path, 'bas', test_name, table_name + '.csv')
    rows = file_to_record(path)
    return table_name, rows


def assert_results(diff, name):
    added = diff['added']
    removed = diff['removed']
    changed = diff['changed']
    if len(added) == 0 and len(removed) == 0 and len(changed) == 0:
        logging.info('bas and reg table %s matched' % name)
    else:
        logging.error(json.dumps(diff, sort_keys=True, indent=4))
        raise RuntimeError('bas and reg table %s does not matched' % name)


class RegressionTestCase:
    # list[RegressionResultTable]
    result_tables: list

    def reg_result(self):
        return dict(map(lambda tbl: db_to_record(tbl), self.result_tables))

    def dump_result(self):
        records = self.reg_result()
        for name in records:
            data = records[name]
            write_to_csv(name, data)

    def bas_result(self):
        test_name = self.__class__.__name__
        return dict(map(lambda tbl: bas_to_record(test_name, tbl.table_name), self.result_tables))

    def test_run(self):
        pass

    def table_keys(self):
        return dict(map(lambda tbl: (tbl.table_name,
                                     list(map(lambda k: k.replace('"', ''), tbl.key_columns))),
                        self.result_tables))

    def run(self, dump: bool):
        self.test_run()
        if dump:
            self.dump_result()
        else:
            bas = self.bas_result()
            reg = self.reg_result()
            keys_map = self.table_keys()
            for bas_name in bas:
                bas_values = bas[bas_name]
                keys = keys_map[bas_name]
                reg_values = reg[bas_name]
                diff = csvdiff.diff_records(bas_values, reg_values, keys)
                assert_results(diff, bas_name)
