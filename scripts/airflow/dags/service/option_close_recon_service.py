from dags.service.base_recon_service import BaseReconService
from terminal.utils import Logging
from datetime import date
from dags.dao import OptionReconRepo, BaseReconRepo
from terminal.dao import InstrumentRepo
from dags.dbo import OptionClose

logging = Logging.getLogger(__name__)


class OptionCloseReconService(BaseReconService):
    def upsert_missing_trading_dates(self, db_session, instrument, missing_trading_dates):
        OptionReconRepo.upsert_option_close_break(db_session, instrument, missing_trading_dates)

    def upsert_milestone(self, db_session, instrument, start_date, end_date, milestone):
        OptionReconRepo.upsert_option_close_milestone(db_session, instrument, start_date, end_date, milestone)

    def get_milestone(self, db_session, instrument):
        return OptionReconRepo.get_option_close_milestone(db_session, instrument)

    def get_existing_trading_dates(self, db_session, instrument):
        return BaseReconRepo.get_existing_trading_dates(db_session, instrument, OptionClose)

    def load_instruments(self, db_session, active_only):
        return InstrumentRepo.load_instruments_in_option_conf(db_session, active_only)


if __name__ == '__main__':
    OptionCloseReconService().recon_all_instrument(start_date=date(2019, 8, 27), end_date=date(2019, 8, 27),
                                                   fail_fast=False,
                                                   dry_run=False, active_only=True, ignore_milestone=False)
