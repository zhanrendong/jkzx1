from market_report_import.db_model import *
from config.data_source_config import HIVE_TRADE_SCHEMA, HIVE_TRADEDATE_SCHEMA
import uuid

date_fmt = '%Y-%m-%d'


def get_single_report_by_date(fields, report_schema, report_table, trade_date_schema, trade_date_table,
                              start_date, end_date):
    print('start_date:{}, end_date:{}'.format(start_date, end_date))
    conn = None
    cur = None
    try:
        conn = get_hive_connection()
        cur = conn.cursor()
        sql = "select {} from {}.{}  r inner join {}.{} s on s.TRADEDATE = r.STATDATE " \
              "where s.FLAG = 'Y' and r.STATDATE >= '{}' and r.STATDATE <= '{}'" \
            .format(','.join(fields), report_schema, report_table, trade_date_schema, trade_date_table,
                    start_date, end_date)
        cur.execute(sql)
        print(sql)
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


def update_to_pg(dbo_name, reports, fields, start_date, end_date):
    session = None
    try:
        session = get_pg_session()
        session.query(dbo_name).filter(dbo_name.STATDATE >= start_date, dbo_name.STATDATE <= end_date).delete()
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


def generate_dbo(dbo_name, report, fields):
    dbo = dbo_name()
    dbo.UUID = uuid.uuid4()
    for field in fields:
        if dbo_name == OTCEtCusReport and field == 'OTCPOSAMOUNT':
            dbo.OTCCUSPOSAMOUNT = report.get(field)
            continue
        dbo.__setattr__(field, report.get(field))
    return dbo


def import_summary(start_date, end_date):
    fields = ['STATDATE', 'TRDNOTIONAMOUNT', 'TRDTRANSNUM', 'OPTFEEAMOUNT', 'TRDCUSNUM', 'POSNOTIONAMOUNT',
              'POSTRANSNUM', 'POSCUSNUM', 'INMARKETCUSNUM', 'FULLMARKETCUSNUM', 'POSVALUE']
    cur_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_SUMMARY,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    update_to_pg(OTCSummaryReport, cur_reports, fields, start_date, end_date)


def import_trade_summary(start_date, end_date):
    fields = ['STATDATE', 'OPENTRDNOTIONAMOUNT', 'CLOSETRDNOTIONAMOUNT', 'ENDTRDNOTIONAMOUNT', 'TRDNOTIONAMOUNT',
              'TRDOPENPREMIUM', 'TRDCLOSEPREMIUM', 'TRDENDPREMIUM', 'PREMIUMAMOUNT', 'TRDOPENCUSNUM', 'TRDCLOSECUSNUM',
              'TRDENDCUSNUM', 'TRDCUSNUM']
    cur_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_TRADE_SUMMARY,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    update_to_pg(OTCTradeSummaryReport, cur_reports, fields, start_date, end_date)


def import_position_summary(start_date, end_date):
    fields = ['STATDATE', 'POSCALLBUYAMOUNT', 'POSPUTBUYAMOUNT', 'POSOTHERBUYAMOUNT', 'POSBUYAMOUNTTOTAL',
              'POSCALLSELLAMOUNT', 'POSPUTSELLAMOUNT', 'POSSELLAMOUNTTOTAL', 'POSCALLBUYCVALUE', 'POSPUTBUYCVALUE',
              'POSOTHERBUYCVALUE', 'POSBUYVALUETOTAL', 'POSCALLSELLCVALUE', 'POSPUTSELLCVALUE', 'POSSELLVALUETOTAL']
    cur_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_POSITION_SUMMARY,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    update_to_pg(OTCPositionSummaryReport, cur_reports, fields, start_date, end_date)


def import_asset_tool(start_date, end_date):
    fields = ['STATDATE', 'ASSETTYPE', 'TOOLTYPE', 'TRDTRANSNUM', 'TRDNOTIONAMOUNT', 'POSTRANSNUM',
              'POSNOTIONAMOUNT', 'INMARKETCUSNUM']
    cur_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_ASSET_TOOL,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    update_to_pg(OTCAssetToolReport, cur_reports, fields, start_date, end_date)


def import_cus_type(start_date, end_date):
    fields = ['STATDATE', 'CUSTYPE', 'ASSETTYPE', 'TRDTRANSNUM', 'TRDNOTIONAMOUNT', 'POSTRANSNUM',
              'POSNOTIONAMOUNT', 'INMARKETCUSNUM']
    cur_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_CUS_TYPE,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    update_to_pg(OTCCusTypeReport, cur_reports, fields, start_date, end_date)


def add_attr_batch(reports, field, value):
    if reports is not None and len(reports) > 0:
        for report in reports:
            report[field] = value


def import_market_dist(start_date, end_date):
    fields = ['STATDATE', 'TOTALPOS', 'TOP3POS', 'TOP3POSDIST', 'TOP5POS', 'TOP5POSDIST', 'TOP10POS', 'TOP10POSDIST']
    cus_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_MARKET_DIST_CUS,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    sub_company_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_MARKET_DIST_SUBCOMPANY,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    variety_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_MARKET_DIST_VARIETY,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    cur_reports = []
    add_attr_batch(cus_reports, 'DISTTYPE', 'CUS')
    add_attr_batch(sub_company_reports, 'DISTTYPE', 'SUBCOMPANY')
    add_attr_batch(variety_reports, 'DISTTYPE', 'VARIETY')

    cur_reports.extend(cus_reports)
    cur_reports.extend(sub_company_reports)
    cur_reports.extend(variety_reports)
    fields.append('DISTTYPE')
    update_to_pg(OTCMarketDistReport, cur_reports, fields, start_date, end_date)


def import_et_commodity(start_date, end_date):
    fields = ['STATDATE', 'COMMODITYID', 'OTCPOSAMOUNT', 'OTCPOSRATIO', 'ETPOSAMOUNT', 'ETPOSRATIO', 'OTCETRATIO']
    cur_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_OTC_ET_COMMODITY,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    update_to_pg(OTCEtCommodityReport, cur_reports, fields, start_date, end_date)


def import_et_cus(start_date, end_date):
    fields = ['STATDATE', 'ETACCOUNTCUSNUM', 'OTCPOSAMOUNT', 'ETCUSPOSAMOUNT', 'ETCUSRIGHT']
    cur_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_OTC_ET_CUS,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    update_to_pg(OTCEtCusReport, cur_reports, fields, start_date, end_date)


def import_et_sub_company(start_date, end_date):
    fields = ['STATDATE', 'COMMODITYID', 'MAINBODYNAME', 'OTCSUBPOSAMOUNT', 'ETSUBPOSAMOUNT']
    cur_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_OTC_ET_SUBCOMPANY,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    update_to_pg(OTCEtSubCompanyReport, cur_reports, fields, start_date, end_date)


def import_market_manipulate(start_date, end_date):
    fields = ['STATDATE', 'INTERCOMPCUSNUM', 'INTERCOMPTRD', 'INTERCOMPPOS']
    cur_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_MARKET_MANIPULATE,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    update_to_pg(OTCMarketManipulateReport, cur_reports, fields, start_date, end_date)


def import_comp_propagate(start_date, end_date):
    fields = ['STATDATE', 'INTERCOMPNUM', 'INTERCOMPTRDAMOUNT', 'COMPTRDAMOUNTTOTAL', 'TRDRATIO',
              'INTERCOMPPOSAMOUNT', 'COMPPOSAMOUNTTOTAL', 'POSRATIO']
    cur_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_COMP_PROPAGATE,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    update_to_pg(OTCCompPropagateReport, cur_reports, fields, start_date, end_date)


def import_cus_pos_percentage(start_date, end_date):
    fields = ['STATDATE', 'ANALOGUENAME', 'UNDERASSVARIT', 'CUSPOSITIVEDELTA', 'CUSSHORTPOSITION', 'CUSNEGATIVEDELTA',
              'CUSLONGPOSITION', 'EXCHANGEMAXPOS', 'EXCHANGEPOS', 'CUSEXGOTCRATIO']
    cur_reports = get_single_report_by_date(fields, HIVE_TRADE_SCHEMA, TABLE_NAME_CUS_POS_PERCENTAGE,
                                            HIVE_TRADEDATE_SCHEMA, TABLE_NAME_TRADE_DATE, start_date, end_date)
    update_to_pg(OTCCusPosPercentageReport, cur_reports, fields, start_date, end_date)
