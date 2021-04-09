# -*- coding: utf-8 -*-

import time
import json
from utils import utils

_datetime_fmt = '%Y-%m-%dT%H:%M:%S'
INTRADAY_QUEUE = "intraday:queue"

#ip地址
ip = 'localhost'
#报告名称
report_name = 'report'
#交易簿
book_name = 'book1'
#修改人
modified_by = 'user1'
#报告内容
report_data = [
    {
        'field1': 'value1',
        'field2': 'value2',
        'field3': 'value3'
    },
    {
        'field1': 'value4',
        'field2': 'value5',
        'field3': 'value6'
    }
]


def get_json():
    now = time.strftime(_datetime_fmt, time.localtime())
    report = []
    for data in report_data:
        res = {"reportName": report_name, "bookName": book_name, "modifiedBy": modified_by, "reportData": data, "createdAt": now}
        report.append(res)
    return json.dumps(report)


if __name__ == '__main__':
    js = get_json()
    r = utils.get_redis_conn(ip)
    r.set(INTRADAY_QUEUE, str(js))
    r.publish(INTRADAY_QUEUE, str(js))
    print("报告已储存")
