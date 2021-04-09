import os
from configparser import ConfigParser
from .settings import JWTConfig

cur_path=os.path.dirname(os.path.realpath(__file__))
default_parser = ConfigParser(interpolation=None)
default_parser.read(os.path.join(cur_path, 'default_config.ini'))


def get_parser_from_file(filename):
    cur_path = os.path.dirname(os.path.realpath(__file__))
    path = os.path.join(cur_path, filename)
    parser = ConfigParser(interpolation=None)
    parser.read(path)
    parser = get_parser_from_dict(overwrite(default_parser, parser))
    parser = get_db_uri(parser)
    return parser


def get_parser_from_dict(dic):
    parser = ConfigParser(interpolation=None)
    parser.read_dict(dic)
    return parser


def overwrite(default, parser):
    ret = {}
    for key in default.keys():
        ret[key]=default[key]
    for key in parser.keys():
        ret[key].update(parser[key])
    return ret


def get_db_uri(parser):
    parser['datasource']['sqlalchemy_db_uri'] = "%s://%s:%s@%s:%s/%s" % (parser['datasource']['platform'],
                                                           parser['datasource']['username'],
                                                           parser['datasource']['password'],
                                                           parser['datasource']['host'],
                                                           parser['datasource']['port'],
                                                           parser['datasource']['db_name']
                                                           )
    return parser


config = {
    'default': get_parser_from_file('default_config.ini'),
    'dev': get_parser_from_file('dev_config.ini'),
    'testing': get_parser_from_file('testing_config.ini'),
    'prod': get_parser_from_file('prod_config.ini'),
    'beta': get_parser_from_file('beta_config.ini')
}
