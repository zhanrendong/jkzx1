# -*- coding: utf-8 -*-

import utils
from json import JSONDecoder

domain = 'localhost'
login_body = {
    'userName': 'admin',
    'password': '12345'
}


def save_old_sales_data():
    r = utils.get_redis_conn(domain)
    sales_result = r.get('data:sales')
    sales = JSONDecoder().decode(bytes.decode(sales_result))
    sub_branch_result = r.get('data:sub_branch')
    sub_branch = JSONDecoder().decode(bytes.decode(sub_branch_result))

    headers = utils.login(domain, login_body)
    sub_map = {}
    branch_map = {}
    for branch in sub_branch:
        sub_name = branch['subsidiary']
        if sub_name not in sub_map:
            sub_result = utils.call_request(domain, 'reference-data-service', 'refSubsidiaryCreate',
                                            {'subsidiaryName': sub_name}, headers)['result']
            sub_map[sub_name] = sub_result['subsidiaryId']
        branch_name = branch['branch']
        branch_result = utils.call_request(domain, 'reference-data-service', 'refBranchCreate',
                                           {'subsidiaryId': sub_map[sub_name], 'branchName': branch_name},
                                           headers)['result']
        branch_map[branch_name] = branch_result['branchId']
    for sale in sales:
        sale_name = sale['salesName']
        branch_id = branch_map[sale['branch']]
        utils.call_request(domain, 'reference-data-service', 'refSalesCreate',
                           {'branchId': branch_id, 'salesName': sale_name}, headers)


save_old_sales_data()
