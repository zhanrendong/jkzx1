import os
from configparser import ConfigParser
from gmssl.sm4 import CryptSM4, SM4_DECRYPT, SM4_ENCRYPT
import base64

cur_path = os.path.dirname(os.path.realpath(__file__))
default_parser = ConfigParser(interpolation=None)
default_parser.read(os.path.join(cur_path, 'default_config.ini'))
KEY = 'KaYup#asD1%79iYu'
ENCODING_UTF8 = 'utf-8'


def get_parser_from_file(filename):
    cur_path = os.path.dirname(os.path.realpath(__file__))
    path = os.path.join(cur_path, filename)
    parser = ConfigParser(interpolation=None)
    parser.read(path)
    parser = get_parser_from_dict(overwrite(default_parser, parser))
    return parser


def get_parser_from_dict(dic):
    parser = ConfigParser(interpolation=None)
    parser.read_dict(dic)
    return parser


# !!!!!bug
def overwrite(default, parser):
    ret = {}
    for key in default.keys():
        ret[key] = default[key]
    for key in parser.keys():
        ret[key].update(parser[key])
    return ret


def decrypt_data_cbc(encrypted_text):
    encrypted_bytes = bytes.fromhex(base64.b64decode(encrypted_text).decode(encoding=ENCODING_UTF8))
    crypt_sm4 = CryptSM4()
    crypt_sm4.set_key(KEY.encode(encoding=ENCODING_UTF8), SM4_DECRYPT)
    decrypt_bytes = crypt_sm4.crypt_ecb(encrypted_bytes)
    return decrypt_bytes.decode(ENCODING_UTF8)


def encrypt_data_cbc(original_text):
    crypt_sm4 = CryptSM4()
    crypt_sm4.set_key(KEY.encode(encoding=ENCODING_UTF8), SM4_ENCRYPT)
    encrypt_data = crypt_sm4.crypt_ecb(original_text.encode(encoding=ENCODING_UTF8))
    return base64.b64encode(encrypt_data).decode(encoding=ENCODING_UTF8)


config = {
    'dev': get_parser_from_file('dev_config.ini'),
    'testing': get_parser_from_file('testing_config.ini'),
    'prod': get_parser_from_file('prod_config.ini'),
    'default': get_parser_from_file('default_config.ini'),
    'beta': get_parser_from_file('beta_config.ini'),
    'regression': get_parser_from_file('regression_config.ini')
}

config_name = os.environ.get('TERMINAL_ENV') or 'default'
app_config = config[config_name]


class OracleDBConfig:
    oracle_trade_username = decrypt_data_cbc(app_config['oracledatasource']['oracle_trade_username'])
    oracle_trade_password = decrypt_data_cbc(app_config['oracledatasource']['oracle_trade_password'])
    trade_snapshot_db_connection = app_config['oracledatasource']['trade_snapshot_db_connection'] \
        .replace('${USERNAME}', oracle_trade_username).replace('${PASSWORD}', oracle_trade_password)
    trade_snapshot_schema = app_config['oracledatasource']['trade_snapshot_schema']
    report_to_oracle_schema = app_config['oracledatasource']['report_to_oracle_schema']
    oracle_wind_username = decrypt_data_cbc(app_config['oracledatasource']['oracle_wind_username'])
    oracle_wind_password = decrypt_data_cbc(app_config['oracledatasource']['oracle_wind_password'])
    market_data_db_connection = app_config['oracledatasource']['market_data_db_connection'] \
        .replace('${USERNAME}', oracle_wind_username).replace('${PASSWORD}', oracle_wind_password)
    market_data_schema = app_config['oracledatasource']['market_data_schema']


class EODDateConfig:
    eod_start_date = app_config['eod-date']['eod_start_date']
    eod_cutoff_time = app_config['eod-date']['eod_cutoff_time']


class RedisConfig:
    redis_host = app_config['redis']['host']
    redis_port = app_config['redis']['port']


class DBConfig:
    db_connection = "%s://%s:%s@%s:%s/%s" % (app_config['datasource']['platform'],
                                             decrypt_data_cbc(app_config['datasource']['username']),
                                             decrypt_data_cbc(app_config['datasource']['password']),
                                             app_config['datasource']['host'],
                                             app_config['datasource']['port'],
                                             app_config['datasource']['db_name'])
    default_schema = app_config['datasource']['default_schema']
    show_sql = (app_config['datasource']['show_sql'] == str(True))


class TerminalServerConfig:
    username = app_config['terminal-server']['username']
    password = app_config['terminal-server']['password']
    host = app_config['terminal-server']['host']
    port = app_config['terminal-server']['port']
    client_id = app_config['terminal-server']['client_id']
    grant_type = app_config['terminal-server']['grant_type']


class BCTServerConfig:
    username = encrypt_data_cbc(decrypt_data_cbc(app_config['bct-server']['username']))
    password = encrypt_data_cbc(decrypt_data_cbc(app_config['bct-server']['password']))
    host = app_config['bct-server']['host']
    port = app_config['bct-server']['port']
    client_id = app_config['bct-server']['client_id']
    grant_type = app_config['bct-server']['grant_type']
    special_captcha = app_config['bct-server']['special_captcha']


class DataServerConfig:
    username = app_config['data-server']['username']
    password = app_config['data-server']['password']
    host = app_config['data-server']['host']
    port = app_config['data-server']['port']
    client_id = app_config['data-server']['client_id']
    grant_type = app_config['data-server']['grant_type']


class HiveConfig:
    hive_host = app_config['hivedatasource']['hive_host']
    hive_port = app_config['hivedatasource']['hive_port']
    hive_username = decrypt_data_cbc(app_config['hivedatasource']['hive_username'])
    hive_password = decrypt_data_cbc(app_config['hivedatasource']['hive_password'])
    hive_database = app_config['hivedatasource']['hive_database']
    hive_auth = app_config['hivedatasource']['hive_auth']
    # hive_connection = hive://hive:hive@10.1.5.41:10000/default?auth=CUSTOM
    platform = app_config['hivedatasource']['platform']
    hive_connection = "%s://%s:%s@%s:%s/%s?%s" % (platform, hive_username, hive_password, hive_host,
                                                       hive_port, hive_database, hive_auth)
    hive_report_schema = app_config['hivedatasource']['hive_report_schema']
    hive_trade_schema = app_config['hivedatasource']['hive_trade_schema']
    hive_tradedate_schema = app_config['hivedatasource']['hive_tradedate_schema']


class BCTSpecialConfig:
    instrument_types = app_config['realized-vol-filter']['instrument_types'].split(' ')
    instrument_ids = app_config['realized-vol-filter']['instrument_ids'].split(' ')
    contract_types = app_config['vol-surface-filter']['contract_types'].split(' ')
    suf_names = app_config['market-data-filter']['suf_names'].split(' ')
