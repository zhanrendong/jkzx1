from json import JSONDecoder

import sqlalchemy
from config.bct_config import pg_url
import pandas as pd

from eod_pd import redis_ip
from terminal import Logging
from utils import utils

logging = Logging.getLogger(__name__)


class RegressionResult:
    table_name: str
    key_columns: list
    value_columns: list

    def reg_result_dataframe(self):
        pass


class RegressionDBResult(RegressionResult):
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


class RegressionRedisResult(RegressionResult):
    def __init__(self, name, keys, values, roundings={}):
        self.redis = utils.get_redis_conn(redis_ip)
        self.table_name = name
        self.key_columns = keys
        self.value_columns = values
        self.roundings = roundings

    def reg_result_dataframe(self):
        reg_result_encoded = self.redis.get(self.table_name)
        df = self.to_dataframe(reg_result_encoded)
        all_columns = self.key_columns + self.value_columns
        df = df[all_columns].round(self.roundings)
        return df


class RegressionRedisListResult(RegressionRedisResult):
    LIST_KEY = 'key'
    LIST_VALUE = 'value'

    def __init__(self, name, keys, values, roundings={}):
        super().__init__(name, keys, values, roundings)

    def to_dataframe(self, reg_result_encoded):
        reg_result = JSONDecoder().decode(bytes.decode(reg_result_encoded))
        reg_result_dict = map(
            lambda v: {RegressionRedisListResult.LIST_KEY: v, RegressionRedisListResult.LIST_VALUE: v},
            reg_result)
        return pd.DataFrame(reg_result_dict)


class RegressionRedisDictResult(RegressionRedisResult):
    DICT_KEY = 'key'
    DICT_VALUE = 'value'

    def __init__(self, name, keys, values, roundings={}):
        super().__init__(name, keys, values, roundings)

    def to_dataframe(self, reg_result_encoded):
        reg_result = JSONDecoder().decode(bytes.decode(reg_result_encoded))
        reg_result_dict = map(
            lambda key: {RegressionRedisDictResult.DICT_KEY: key,
                         RegressionRedisDictResult.DICT_VALUE: reg_result[key]},
            reg_result)
        return pd.DataFrame(reg_result_dict)


class RegressionRedisDictListResult(RegressionRedisResult):
    def __init__(self, name, keys, values, roundings={}):
        super().__init__(name, keys, values, roundings)

    def to_dataframe(self, reg_result_encoded):
        reg_result = pd.read_msgpack(reg_result_encoded)
        return reg_result
