from .db_model_utils import *
import json
from datetime import datetime, timedelta

date_fmt = '%Y-%m-%d'


def export_market_risk_run(valuation_date):
    fields = ['uuid', 'created_at', 'delta_cash', 'gamma_cash', 'pricing_environment', 'report_name', 'rho', 'theta',
              'valuation_date', 'vega']
    cur_reports = get_single_table_data_by_date(fields, SCHEMA_REPORT_SERVICE, TABLE_MARKET_RISK, valuation_date)
    update_to_oracle(OTCMarketRisk, cur_reports, fields, valuation_date)


def export_market_risk_detail_run(valuation_date):
    fields = ['uuid', 'created_at', 'delta', 'delta_cash', 'gamma', 'gamma_cash', 'party_name', 'pnl_change',
              'report_name', 'report_type', 'rho', 'scenario_type', 'subsidiary', 'theta', 'underlyer_instrument_id',
              'valuation_date', 'vega', 'exfsid']
    cur_reports = get_single_table_data_by_date(fields, SCHEMA_REPORT_SERVICE, TABLE_MARKET_RISK_DETAIL, valuation_date)
    update_to_oracle(OTCMarketRiskDetail, cur_reports, fields, valuation_date)


def export_subsidiary_market_risk_run(valuation_date):
    fields = ['uuid', 'created_at', 'delta_cash', 'gamma_cash', 'report_name', 'rho', 'subsidiary', 'theta',
              'valuation_date', 'vega']
    cur_reports = get_single_table_data_by_date(fields, SCHEMA_REPORT_SERVICE, TABLE_SUBSIDIARY_MARKET_RISK, valuation_date)
    update_to_oracle(OTCSubsidiaryMarketRisk, cur_reports, fields, valuation_date)


def export_subsidiary_market_risk_detail_run(valuation_date):
    fields = ['uuid', 'book_name', 'created_at', 'delta', 'delta_cash', 'gamma', 'gamma_cash', 'pricing_environment',
              'report_name', 'rho', 'theta', 'underlyer_instrument_id', 'valuation_date', 'vega', 'exfsid']
    cur_reports = get_single_table_data_by_date(fields, SCHEMA_REPORT_SERVICE, TABLE_SUBSIDIARY_MARKET_RISK_BY_UNDERLYER, valuation_date)
    update_to_oracle(OTCSubsidiaryMarketRiskDetail, cur_reports, fields, valuation_date)


def export_counter_party_market_risk_run(valuation_date):
    fields = ['uuid', 'created_at', 'delta_cash', 'gamma_cash', 'party_name', 'report_name', 'rho',
              'theta', 'valuation_date', 'vega']
    cur_reports = get_single_table_data_by_date(fields, SCHEMA_REPORT_SERVICE, TABLE_COUNTER_PARTY_MARKET_RISK, valuation_date)
    update_to_oracle(OTCCounterPartyMarketRisk, cur_reports, fields, valuation_date)


def export_counter_party_market_risk_detail_run(valuation_date):
    fields = ['uuid', 'created_at', 'delta', 'delta_cash', 'gamma', 'gamma_cash', 'party_name', 'report_name', 'rho',
              'theta', 'underlyer_instrument_id', 'valuation_date', 'vega', 'exfsid']
    cur_reports = get_single_table_data_by_date(fields, SCHEMA_REPORT_SERVICE, TABLE_COUNTER_PARTY_MARKET_RISK_BY_UNDERLYER, valuation_date)
    update_to_oracle(OTCCounterPartyMarketRiskDetail, cur_reports, fields, valuation_date)


def export_spot_scenarios_run(valuation_date):
    fields = ['uuid', 'asset_class', 'content_name', 'created_at', 'instrument_id', 'instrument_type',
              'report_name', 'report_type', 'valuation_date', 'scenarios', 'exfsid']
    cur_reports = get_single_table_data_by_date(fields, SCHEMA_REPORT_SERVICE, TABLE_SPOT_SCENARIOS, valuation_date)
    update_to_oracle(OTCSpotScenarios, cur_reports, fields, valuation_date)


def export_vol_surface_run(valuation_date):
    fields = ['uuid', 'instrument_id', 'valuation_date', 'instance', 'model_info', 'fitting_model', 'strike_type',
              'updated_at', 'tag', 'source']
    cur_reports = get_single_table_data_by_date(fields, SCHEMA_MARKET_DATA, TABLE_VOL_SURFACE, valuation_date)
    update_to_oracle(OTCVolSurface, cur_reports, fields, valuation_date)


def export_realized_vol_run(valuation_date):
    fields = ['uuid', 'instrument_id', 'valuation_date', 'vol', 'windows', 'update_at', 'exfsid']
    cur_reports = get_single_table_data_by_date(fields, SCHEMA_MARKET_DATA, TABLE_REALIZED_VOL, valuation_date)
    update_to_oracle(OTCRealizedVol, cur_reports, fields, valuation_date)


def update_to_oracle(dbo_name, reports, fields, valuation_date):
    session = None
    try:
        session = get_oracle_session()
        session.query(dbo_name).filter(dbo_name.valuation_date == valuation_date).delete()
        session.flush()
        if reports is not None and len(reports) > 0:
            for report in reports:
                dbo = generate_dbo(dbo_name, report, fields)
                session.add(dbo)
        session.commit()
    except Exception as e:
        session.rollback()
        print(e)
        raise RuntimeError(e)
    finally:
        if session is not None:
            session.close()


def get_single_table_data_by_date(fields, schema_name, table_name, valuation_date):
    conn = None
    cur = None
    try:
        if schema_name == SCHEMA_MARKET_DATA:
            conn = get_pg_connection(True)
        else:
            conn = get_pg_connection()
        cur = conn.cursor()
        sql = "SELECT {} FROM {}.{} where valuation_date = '{}'" \
            .format(','.join(fields), schema_name, table_name, valuation_date)
        cur.execute(sql)
        return covert_to_dict(cur.fetchall(), fields)
    finally:
        if cur is not None:
            cur.close()
        if conn is not None:
            conn.close()


def covert_to_dict(rows, fields):
    reports = []
    if rows is not None and len(rows) > 0:
        for row in rows:
            report = {}
            for i, field in enumerate(fields):
                report[field] = row[i]
            reports.append(report)
    return reports


def generate_dbo(dbo_name, report, fields):
    dbo = dbo_name()
    for field in fields:
        if field == 'valuation_date':
            dbo.__setattr__(field, str(report.get(field)))
            continue
        if field in ['model_info', 'fitting_model']:
            dbo.__setattr__(field, json.dumps(report.get(field), separators=(',', ':'), ensure_ascii=False))
            continue
        dbo.__setattr__(field, report.get(field))
    return dbo


if __name__ == '__main__':
    cur_valuation_date = datetime.now().strftime(date_fmt)
    export_market_risk_run(cur_valuation_date)
    export_market_risk_detail_run(cur_valuation_date)
    export_subsidiary_market_risk_run(cur_valuation_date)
    export_subsidiary_market_risk_detail_run(cur_valuation_date)
    export_counter_party_market_risk_run(cur_valuation_date)
    export_counter_party_market_risk_detail_run(cur_valuation_date)
    export_spot_scenarios_run(cur_valuation_date)
    export_vol_surface_run(cur_valuation_date)
    export_realized_vol_run(cur_valuation_date)
