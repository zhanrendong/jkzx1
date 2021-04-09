# -*- coding: utf-8 -*-
from history_data_archive.db_utils import *

MAX_LIMIT_SIZE = 3000000
SCHEMA_REPORT_SERVICE = 'report_service'
TABLE_MARKET_RISK = 'market_risk_report'
TABLE_MARKET_RISK_DETAIL = 'market_risk_detail_report'
TABLE_SUBSIDIARY_MARKET_RISK = 'subsidiary_market_risk_report'
TABLE_SUBSIDIARY_MARKET_RISK_BY_UNDERLYER = 'market_risk_by_sub_underlyer_report'
TABLE_COUNTER_PARTY_MARKET_RISK = 'counter_party_market_risk_report'
TABLE_COUNTER_PARTY_MARKET_RISK_BY_UNDERLYER = 'counter_party_market_risk_by_underlyer_report'
TABLE_SPOT_SCENARIOS = 'spot_scenarios_report'


# archive table which must have field: valuation_date
def archive_single_table(schema_name, table_name):
    conn = None
    cur = None
    try:
        conn = get_pg_connection()

        cur = conn.cursor()
        sql = 'SELECT COUNT(1) from {}.{}'.format(schema_name, table_name)
        cur.execute(sql)
        total_rows = cur.fetchall()[0][0]
        if total_rows > MAX_LIMIT_SIZE:
            cur = conn.cursor()
            sql = 'SELECT valuation_date FROM {}.{} ORDER BY valuation_date desc limit {}'\
                .format(schema_name, table_name, MAX_LIMIT_SIZE)
            cur.execute(sql)
            min_date_str = str(cur.fetchall()[-1][0])
            min_date_str_ = min_date_str.replace('-', '_')

            new_table_name = table_name + '__' + min_date_str_
            cur = conn.cursor()
            sql = "SELECT * into {}.{} from {}.{} where valuation_date <= '{}'"\
                .format(schema_name, new_table_name, schema_name, table_name, min_date_str)
            cur.execute(sql)

            cur = conn.cursor()
            sql = "DELETE from {}.{} where valuation_date <= '{}'" \
                .format(schema_name, table_name, min_date_str)
            cur.execute(sql)

            conn.commit()
            print('表 {} 在 {} 及之前的数据已经归档为表 {}'.format(table_name, min_date_str, new_table_name))
        else:
            print('当前表 {} 数据有 {} 条，未达到 {} 条归档要求！'.format(table_name, total_rows, MAX_LIMIT_SIZE))
    except Exception as error:
        conn.rollback()
        print(error)
        raise RuntimeError(error)
    finally:
        if cur is not None:
            cur.close()
        if conn is not None:
            conn.close()


def archive__market_risk_report_run():
    archive_single_table(SCHEMA_REPORT_SERVICE, TABLE_MARKET_RISK)


def archive__market_risk_detail_report_run():
    archive_single_table(SCHEMA_REPORT_SERVICE, TABLE_MARKET_RISK_DETAIL)


def archive__subsidiary_market_risk_report_run():
    archive_single_table(SCHEMA_REPORT_SERVICE, TABLE_SUBSIDIARY_MARKET_RISK)


def archive__subsidiary_market_risk_by_underlyer_report_run():
    archive_single_table(SCHEMA_REPORT_SERVICE, TABLE_SUBSIDIARY_MARKET_RISK_BY_UNDERLYER)


def archive__counter_party_market_risk_report_run():
    archive_single_table(SCHEMA_REPORT_SERVICE, TABLE_COUNTER_PARTY_MARKET_RISK)


def archive__counter_party_market_risk_underlyer_report_run():
    archive_single_table(SCHEMA_REPORT_SERVICE, TABLE_COUNTER_PARTY_MARKET_RISK_BY_UNDERLYER)


def archive__table_spot_scenarios_report_run():
    archive_single_table(SCHEMA_REPORT_SERVICE, TABLE_SPOT_SCENARIOS)


if __name__ == '__main__':
    archive__market_risk_report_run()
    archive__market_risk_detail_report_run()
    archive__subsidiary_market_risk_report_run()
    archive__subsidiary_market_risk_by_underlyer_report_run()
    archive__counter_party_market_risk_report_run()
    archive__counter_party_market_risk_underlyer_report_run()
    archive__table_spot_scenarios_report_run()
