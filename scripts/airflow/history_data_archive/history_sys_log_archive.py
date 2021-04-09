# -*- coding: utf-8 -*-
from pathlib import Path
import zipfile
from datetime import datetime
from history_data_archive.db_utils import *
from datetime import date

MAX_LIMIT_SIZE = 500000
SCHEMA_AUTH_SERVICE = 'auth_service'
TABLE_SYS_LOG = 'sys_log'
ARCHIVE_LOG_FILE_PREFIX = 'JKZX_SYSLOG_'
LOGS_PATH = '/home/airflow/sys_logs'
LOG_FILE_SUFFIX = '.txt'
TIME_FORMAT = '%H.%M.%S'


def archive_sys_log(fields):
    conn = None
    cur = None
    try:
        conn = get_pg_connection()

        cur = conn.cursor()
        sql = 'SELECT COUNT(1) from {}.{}'.format(SCHEMA_AUTH_SERVICE, TABLE_SYS_LOG)
        cur.execute(sql)
        total_rows = cur.fetchall()[0][0]
        if total_rows >= MAX_LIMIT_SIZE:
            cur = conn.cursor()
            sql = 'SELECT create_time from {}.{} ORDER BY create_time  LIMIT 1 OFFSET {}'\
                .format(SCHEMA_AUTH_SERVICE, TABLE_SYS_LOG, int(total_rows / 2))
            cur.execute(sql)
            remain_min_datetime_str = str(cur.fetchall()[0][0])

            cur = conn.cursor()
            sql = "SELECT {} from {}.{} where create_time < '{}' ORDER BY create_time"\
                .format(','.join(fields), SCHEMA_AUTH_SERVICE, TABLE_SYS_LOG, remain_min_datetime_str)
            cur.execute(sql)
            records = covert_to_dict(cur.fetchall(), fields)
            if records is not None and len(records) > 0:
                file_full_name, file_name = save_logs_to_file(records)
                log_file_name = compress_file(file_full_name, file_name)
                os.remove(file_full_name)

                cur = conn.cursor()
                sql = "DELETE from {}.{} where create_time < '{}'" \
                    .format(SCHEMA_AUTH_SERVICE, TABLE_SYS_LOG, remain_min_datetime_str)
                cur.execute(sql)

                conn.commit()
                print('表 {} 在 {} 之前的数据已经归档到文件 {} 中'.format(TABLE_SYS_LOG, remain_min_datetime_str, log_file_name))
        else:
            print('当前表 {} 数据有 {} 条，未达到 {} 条归档要求！'.format(TABLE_SYS_LOG, total_rows, MAX_LIMIT_SIZE))
    except Exception as error:
        conn.rollback()
        print(error)
        raise RuntimeError(error)
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
                if type(row[i]) == datetime or type(row[i]) == date:
                    report[field] = str(row[i])
            reports.append(report)
    return reports


def save_logs_to_file(records):
    log_path = Path(LOGS_PATH)
    if not log_path.exists():
        os.makedirs(log_path)
    file_name = ARCHIVE_LOG_FILE_PREFIX + str(date.today()) + LOG_FILE_SUFFIX
    file_full_name = LOGS_PATH + '/' + file_name

    with open(file_full_name, "a+", encoding='utf-8') as f:
        for r in records:
            f.write(str(r) + '\n')
    return file_full_name, file_name


def compress_file(file_full_name, file_name):
    zip_file = file_full_name[:-len(LOG_FILE_SUFFIX)] + '.zip'
    if Path(zip_file).exists():
        zip_file = file_full_name[:-len(LOG_FILE_SUFFIX)] + '_' + datetime.now().strftime(TIME_FORMAT) + '.zip'
    f_zip = zipfile.ZipFile(zip_file, 'a')
    f_zip.write(file_full_name, file_name, compress_type=zipfile.ZIP_LZMA)
    f_zip.close()
    return zip_file


def archive_sys_log_run():
    fields = ['id', 'create_time', 'method', 'operation', 'params', 'service', 'username', 'revoke_time', 'revoked',
              'update_time', 'created_date_at', 'execution_time_in_millis']
    archive_sys_log(fields)


if __name__ == '__main__':
    archive_sys_log_run()
