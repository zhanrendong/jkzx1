from terminal.dbo.db_model import *
from tornado_sqlalchemy import make_session_factory
from terminal.conf import config
from apply_defaults import apply_config


def init_db(config_name='default'):
    app_config = config.get(config_name)
    @apply_config(config=app_config, section='datasource')
    def create_db_closures(
        sqlalchemy_db_uri=None,
        default_schema=None,
        show_sql=None
    ):
        # 读取配置文件
        echo = (show_sql == str(True))
        session_factory = make_session_factory(sqlalchemy_db_uri,
                                               connect_args={'options': '-csearch_path={}'.format(default_schema)},
                                               echo=echo)
        # 初始化数据库
        engine = sqla.create_engine(
            sqlalchemy_db_uri,
            connect_args={'options': '-csearch_path={}'.format(default_schema)},
            echo=echo)
        BaseModel.metadata.bind = engine
        BaseModel.metadata.create_all()
        return session_factory
    return create_db_closures


def init_dispatch(config_name='default'):
    app_config = config.get(config_name)
    session_name = 'dispatch'
    basic_logging = app_config.get(session_name, 'basic_logging')
    convert_camel_case = app_config.get(session_name, 'convert_camel_case')
    debug = app_config.get(session_name, 'debug')
    trim_log_values = app_config.get(session_name, 'trim_log_values')
    return basic_logging, convert_camel_case, debug, trim_log_values


def init_tornado(config_name='default'):
    app_config = config.get(config_name)
    return int(app_config.get('tornado-server', 'port'))


def init_logging(config_name='default'):
    app_config = config.get(config_name)
    session_name = 'logging'
    level = app_config.get(session_name, 'level')
    format = app_config.get(session_name, 'format')
    datefmt = app_config.get(session_name, 'datefmt')
    return level, format, datefmt


def init_redis(config_name='default'):
    app_config = config.get(config_name)
    session_name = 'redis'
    host = app_config.get(session_name, 'host')
    port = app_config.get(session_name, 'port')
    return host, port


def init_server(config_name='default'):
    app_config = config.get(config_name)
    session_name = 'bct-server'
    host = app_config.get(session_name, 'host')
    username = app_config.get(session_name, 'username')
    password = app_config.get(session_name, 'password')
    special_captcha = app_config.get(session_name, 'special_captcha')
    return host, username, password, special_captcha
