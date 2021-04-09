import logging


class Logging:
    level = eval('logging.INFO')
    format = '%(asctime)s %(filename)s[line:%(lineno)d] %(levelname)s %(message)s'
    datefmt = '%a,%d %b %Y %H:%M:%S'
    name = __name__

    @staticmethod
    def set_logging_params(params):
        level, format, datefmt = params
        if level is not None:
            Logging.level = level
        if format is not None:
            Logging.format = format
        if datefmt is not None:
            Logging.datefmt = datefmt

    @staticmethod
    def getLogger(name):
        logging.basicConfig(level=Logging.level, format=Logging.format, datefmt=Logging.datefmt)
        return logging.getLogger(name)
