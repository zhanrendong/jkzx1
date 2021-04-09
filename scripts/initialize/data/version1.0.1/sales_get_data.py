# -*- coding: utf-8 -*-

import utils
from json import JSONEncoder

domain = 'localhost'
login_body = {
    'userName': 'admin',
    'password': '12345'
}


def get_old_sales_data():
    headers = utils.login(domain, login_body)
    sales = utils.call_request(domain, 'reference-data-service', 'refSalesList', {}, headers)['result']
    sub_branch = utils.call_request(domain, 'reference-data-service', 'refSubsidiaryBranchList', {}, headers)['result']
    print(sub_branch)
    print(sales)

    r = utils.get_redis_conn(domain)
    sales_result = JSONEncoder().encode(sales)
    sub_branch_result = JSONEncoder().encode(sub_branch)
    r.set('data:sales', str(sales_result))
    r.set('data:sub_branch', str(sub_branch_result))


get_old_sales_data()


