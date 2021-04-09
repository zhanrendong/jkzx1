from datetime import date
from dags.service.base_recon_service import BaseReconService
from dags.dao import TickSnapshotReconRepo, BaseReconRepo
from terminal.dao import InstrumentRepo
from dags.dbo import TickSnapshot


class TickSnapshotReconService(BaseReconService):
    def upsert_missing_trading_dates(self, db_session, instrument, missing_trading_dates):
        TickSnapshotReconRepo.upsert_tick_snapshot_break(db_session, instrument, missing_trading_dates)

    def upsert_milestone(self, db_session, instrument, start_date, end_date, milestone):
        TickSnapshotReconRepo.upsert_tick_snapshot_milestone(db_session, instrument, start_date, end_date, milestone)

    def get_milestone(self, db_session, instrument):
        return TickSnapshotReconRepo.get_tick_snapshot_milestone(db_session, instrument)

    def get_existing_trading_dates(self, db_session, instrument):
        return BaseReconRepo.get_existing_trading_dates(db_session, instrument, TickSnapshot)

    def load_instruments(self, db_session, active_only):
        return InstrumentRepo.load_instruments_in_option_conf(db_session)


if __name__ == '__main__':
    TickSnapshotReconService().recon_all_instrument(start_date=date(2019, 8, 26), end_date=date(2019, 9, 10),
                                                    fail_fast=False,
                                                    dry_run=False, active_only=True, ignore_milestone=False)
