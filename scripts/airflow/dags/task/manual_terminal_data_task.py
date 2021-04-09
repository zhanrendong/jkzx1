from dags.service.future_contract_info_service import FutureContractInfoService
from dags.service.r_otc_position_service import ROTCPositionService
from dags.service.realized_vol_service import RealizedVolService
from dags.service.trading_calendar_service import TradingCalendarService
from dags.service.vol_surface_service import VolSurfaceService
from dags.utils.airflow_utils import AirflowVariableUtils
from dags.service.otc_maket_data_service import OTCMarketDataService
from dags.service.otc_trade_snapshots_service import OTCTradeSnapshotsService


def manual_wind_market_data_update_run():
    eod_start_date = AirflowVariableUtils.get_eod_manual_date()
    eod_end_date = AirflowVariableUtils.get_eod_manual_date()
    OTCMarketDataService.backfill_otc_market_data(eod_start_date, eod_end_date, force_update=True)


def manual_otc_trade_snapshot_update_run():
    eod_start_date = AirflowVariableUtils.get_eod_manual_date()
    eod_end_date = AirflowVariableUtils.get_eod_manual_date()
    OTCTradeSnapshotsService.import_otc_trade_snapshots(eod_start_date, eod_end_date, force_update=True)


def manual_otc_future_contract_update_run():
    eod_start_date = AirflowVariableUtils.get_eod_manual_date()
    eod_end_date = AirflowVariableUtils.get_eod_manual_date()
    FutureContractInfoService.update_all_future_contract_info(eod_start_date, eod_end_date, force_update=True)


def manual_otc_position_update_run():
    eod_start_date = AirflowVariableUtils.get_eod_manual_date()
    eod_end_date = AirflowVariableUtils.get_eod_manual_date()
    ROTCPositionService.update_implied_vol(eod_start_date, eod_end_date, force_update=True)


def manual_vol_surface_update_run():
    eod_start_date = AirflowVariableUtils.get_eod_manual_date()
    eod_end_date = AirflowVariableUtils.get_eod_manual_date()
    VolSurfaceService.update_all_vol_surface(eod_start_date, eod_end_date)


def manual_realized_vol_update_run():
    eod_start_date = AirflowVariableUtils.get_eod_manual_date()
    eod_end_date = AirflowVariableUtils.get_eod_manual_date()
    RealizedVolService.update_days_instrument_realized_vol(eod_start_date, eod_end_date, force_update=True)


def manual_trading_calendar_update_run():
    TradingCalendarService.update_trading_calendar()
