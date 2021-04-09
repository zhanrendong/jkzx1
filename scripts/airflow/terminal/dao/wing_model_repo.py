from terminal.conf.settings import WingModelConfig
from terminal.dbo import Instrument
from terminal.utils.logging_utils import Logging

logging = Logging.getLogger(__name__)


class WingModelRepo:
    @staticmethod
    def get_option_close(db_session, underlyer, observed_date, option_structures):
        if option_structures:
            instruments = [x.instrumentId for x in option_structures]
            data_table = WingModelRepo.get_close_table(db_session, underlyer)
            logging.info("数据源 : %s" % str(data_table))
            option_close = db_session.query(data_table.instrumentId, data_table.closePrice).filter(
                    data_table.instrumentId.in_(instruments), data_table.tradeDate == observed_date).all()
            for option in option_structures:
                if option.instrumentId not in [x[0] for x in option_close]:
                    logging.error("%s未找到close" % option.instrumentId)
                else:
                    for item in option_close:
                        if option.instrumentId == item[0]:
                            option.price = float(item[1])
        return option_structures

    @staticmethod
    def get_close_table(db_session, underlyer):
        if WingModelConfig.data_source.get(underlyer) is not None:
            return eval(WingModelConfig.data_source[underlyer])
        else:
            underlyer = db_session.query(Instrument.contractType).filter(
                Instrument.instrumentId == underlyer).first()[0]
            return eval(WingModelConfig.data_source[underlyer])