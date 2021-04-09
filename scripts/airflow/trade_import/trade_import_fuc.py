# -*- coding: utf-8 -*-
from trade_import.db_config import *
from trade_import import origin_data_filter
from trade_import import origin_data_trans
from trade_import import instrument_crud
from trade_import import trade_templates
from trade_import import db_utils
from trade_import import utils
from datetime import datetime
from multiprocessing import Pool
from market_data.market_db_utils import get_session as get_wind_session, get_wind_code_and_exchange_relation
from config.data_source_config import wind_oracle_database_url

_UNIT_PERCENT = 'PERCENT'
_UNIT_LOT = 'LOT'
_UNIT_CNY = 'CNY'
RESOURCE_PERMISSIONS = ['READ_BOOK', 'READ_TRADE']
business_role_name = 'business'
BATCH_SIZE = 1000
_datetime_fmt = '%Y-%m-%d'


def get_multiplier_by_underlyer(underlyer, headers):
    instrument = instrument_crud.get_instrument_by_id(bct_ip, headers, underlyer)
    if instrument is None:
        raise RuntimeError('标的信息不存在,标的合约:' + underlyer)
    return instrument['multiplier'] or 1


def get_all_instruments(headers):
    return instrument_crud.get_instruments(bct_ip, headers, {})


def get_department_id(headers):
    department_info = utils.call_request(bct_ip, 'auth-service', 'authAllDepartmentGet', {}, headers)
    return department_info['children'][0]['id']


def get_all_book(headers):
    book_list = []
    books = utils.call_request(bct_ip, 'auth-service', 'authBookGetCanRead', {}, headers)
    for book in books:
        book_list.append(book.get('resourceName'))
    return book_list


def get_all_party(headers):
    party_list = []
    parties = utils.call_request(bct_ip, 'reference-data-service', 'refPartyList', {}, headers)
    for party in parties:
        party_list.append(party.get('legalName'))
    return party_list


def create_book(book_name, department_id, headers):
    params = {
        'resourceType': 'BOOK',
        'resourceName': book_name,
        'departmentId': department_id
    }
    return utils.call_request(bct_ip, 'auth-service', 'authNonGroupResourceAdd', params, headers)


def set_role_permission(role_id, resource_ids, resource_permission_dict, headers):
    params = {
        'roleId': role_id,
        'permissions': [{'resourceId': i, 'resourcePermission': resource_permission_dict.get(i)} for i in resource_ids]
    }
    return utils.call_request(bct_ip, 'auth-service', 'authRolePermissionsModify', params, headers)


def get_role_info_by_role_name(role_name, headers):
    return utils.call_request(bct_ip, 'auth-service', 'authRoleGetByRoleName', {'roleName': role_name}, headers)


def get_resource_info_by_role_id(role_id, headers):
    return utils.call_request(bct_ip, 'auth-service', 'authResourceGetByRoleId', {'roleId': role_id}, headers)


def create_party(legal_name, master_agreement_id, headers):
    params = {
        'clientType': 'INSTITUTION',
        'legalName': legal_name,
        'legalRepresentative': legal_name,
        'address': legal_name,
        'contact': legal_name,
        'warrantor': legal_name,
        'warrantorAddress': legal_name,
        'tradePhone': '123456789',
        'tradeEmail': '123456789@163.com',
        'subsidiaryName': '分公司000',
        'branchName': '营业部000',
        'salesName': '销售000',
        'investorType': 'FINANCIAL_INSTITUTIONAL_INVESTOR',
        'masterAgreementId': master_agreement_id
    }
    return utils.call_request(bct_ip, 'reference-data-service', 'refPartySave', params, headers)


def trans_trade(trade_data):
    product_type = trade_data['product_type']
    if product_type == 'VANILLA_EUROPEAN':
        trade_dto = trade_templates.vanilla_european(trade_data['trade_id'], trade_data['book_name'],
                                                     trade_data['trade_date'],
                                                     trade_data['option_type'], _UNIT_CNY, trade_data['strike'],
                                                     trade_data['direction'],
                                                     trade_data['underlyer'], trade_data['multiplier'],
                                                     trade_data['specified_price'],
                                                     trade_data['init_spot'], trade_data['expire_date'],
                                                     trade_data['settle_date'],
                                                     _UNIT_CNY, trade_data['notional'],
                                                     trade_data['participation_rate'],
                                                     trade_data['annualized'], trade_data['term'],
                                                     trade_data['days_in_year'],
                                                     _UNIT_CNY, trade_data['premium'], trade_data['front_premium'],
                                                     trade_data['minimum_premium'], trade_data['effective_date'],
                                                     trade_data['trader'], trade_data['counter_party'], '销售000',
                                                     trade_data['ann_val_ratio'])
    elif product_type == 'VANILLA_AMERICAN':
        trade_dto = trade_templates.vanilla_american(trade_data['trade_id'], trade_data['book_name'],
                                                     trade_data['trade_date'],
                                                     trade_data['option_type'], _UNIT_CNY, trade_data['strike'],
                                                     trade_data['direction'],
                                                     trade_data['underlyer'], trade_data['multiplier'],
                                                     trade_data['specified_price'],
                                                     trade_data['init_spot'], trade_data['expire_date'],
                                                     trade_data['settle_date'],
                                                     _UNIT_CNY, trade_data['notional'],
                                                     trade_data['participation_rate'],
                                                     trade_data['annualized'], trade_data['term'],
                                                     trade_data['days_in_year'],
                                                     _UNIT_CNY, trade_data['premium'], trade_data['front_premium'],
                                                     trade_data['minimum_premium'], trade_data['effective_date'],
                                                     trade_data['trader'], trade_data['counter_party'], '销售000',
                                                     trade_data['ann_val_ratio'])

    return trade_dto


def trade_import_generate(oracle_session, trans_code_list_sub, all_instruments_dict, code_and_exchange_relation_dict):
    trades = []
    try:
        trans_codes = list(map(lambda x: x[0], trans_code_list_sub))
        positions_data = origin_data_filter.get_positions_by_trans_codes(oracle_session, trans_codes)
        for trans_item in trans_code_list_sub:
            try:
                trans_code = trans_item[0]
                position_data = positions_data.get(trans_code)
                if position_data is None:
                    continue
                trade_origin_data = origin_data_trans.organize_trade_data(position_data,
                                                                          code_and_exchange_relation_dict)
                if trade_origin_data is None:
                    continue
                # 获取标的物合约乘数
                instrument = all_instruments_dict.get(trade_origin_data['underlyer'].upper())
                if instrument is None:
                    raise RuntimeError('标的信息不存在,标的合约:' + trade_origin_data['underlyer'])
                multiplier = instrument['multiplier'] or 1
                trade_origin_data['multiplier'] = multiplier
                trades.append(trade_origin_data)
            except Exception as e:
                print(str(e))
    except Exception as e:
        print(str(e))
    return trades


def resolve_book_from_parent(parent, book_list):
    if parent is not None and parent.get('children') is not None:
        for c in parent.get('children'):
            if c.get('resourceType') == 'NAMESPACE':
                resolve_book_from_parent(c, book_list)
            if c.get('resourceType') == 'BOOK':
                book_list.append(c)


def trade_data_import(valuation_date):
    try:
        oracle_session = None
        wind_oracle_session = None
        total_trades = []
        headers = utils.login(bct_ip, login_body)
        department_id = get_department_id(headers)
        party_list = get_all_party(headers)
        book_list = get_all_book(headers)
        all_instruments = get_all_instruments(headers)
        all_instruments_dict = {}
        for info in all_instruments:
            all_instruments_dict[info.get('instrumentId')] = info
        business_role_result = get_role_info_by_role_name(business_role_name, headers)
        if business_role_result is None or business_role_result.get('id') is None:
            raise Exception('查询角色 {} 失败，查询结果：{}'.format(business_role_name, business_role_result))

        wind_oracle_session = get_wind_session(wind_oracle_database_url)
        code_and_exchange_relation_dict = get_wind_code_and_exchange_relation(wind_oracle_session)

        oracle_session = db_utils.get_session()
        trans_code_list = origin_data_filter.get_active_trans_code_list(oracle_session, valuation_date)
        utils.call_request(bct_ip, 'trade-service', 'trdTradeDeleteAll', {}, headers)

        for start in range(0, len(trans_code_list), BATCH_SIZE):
            end = start + BATCH_SIZE
            total_trades.extend(trade_import_generate(oracle_session, trans_code_list[start:end],
                                                      all_instruments_dict, code_and_exchange_relation_dict))

        total_books = set(map(lambda t: t.get('book_name'), total_trades)).difference(set(book_list))
        total_parties = set(map(lambda t: t.get('counter_party'), total_trades)).difference(set(party_list))
        print('准备新增 交易 {}笔，交易簿 {}个，交易对手{}个。'.format(len(total_trades), len(total_books), len(total_parties)))

        # 创建交易簿
        books_add_succeed = set()
        for book in list(total_books):
            try:
                book_result = create_book(book, department_id, headers)
                if book_result.get('id') is not None:
                    books_add_succeed.add(book)
                else:
                    print('交易簿 {} 创建失败: {}'.format(book, book_result))
            except Exception as e:
                print('交易簿 {} 创建异常: {}'.format(book, e))
        print('准备新增 交易簿 {}个，成功创建 {}个，创建失败的有 {}'
              .format(len(total_books), len(books_add_succeed), list(total_books.difference(books_add_succeed))))
        # 添加业务角色权限
        try:
            resource_result = get_resource_info_by_role_id(business_role_result.get('id'), headers)
            if resource_result is not None and resource_result.get('resourceType') == 'ROOT':
                book_info_list = []
                resolve_book_from_parent(resource_result, book_info_list)
                book_ids = set()
                book_names = set()
                book_id_permissions_dict = {}
                for info in book_info_list:
                    resource_permissions = info.get('resourcePermissions') or []
                    if len(set(RESOURCE_PERMISSIONS).difference(set(resource_permissions))) > 0:
                        book_ids.add(info.get('id'))
                        book_names.add(info.get('resourceName'))
                        resource_permissions.extend(RESOURCE_PERMISSIONS)
                        book_id_permissions_dict[info.get('id')] = list(set(resource_permissions))
                if len(book_ids) > 0:
                    permission_set_result = set_role_permission(business_role_result['id'], list(book_ids),
                                                                book_id_permissions_dict, headers)
                    if permission_set_result is not None and permission_set_result.get('id') is not None:
                        print('业务角色 {} 为交易簿 {} 添加 {} 权限成功'
                              .format(business_role_name, list(book_names), RESOURCE_PERMISSIONS))
                    else:
                        print('业务角色 {} 设置权限失败：'.format(business_role_name, permission_set_result))
                else:
                    print('无交易簿需设置业务权限！')
            else:
                print('查询业务角色权限信息失败，查询结果：{}'.format(resource_result))
        except Exception as e:
            print('业务角色 {} 权限设置出现异常: {}'.format(business_role_name, e))
        # 创建交易对手
        parties_add_succeed = set()
        for party in list(total_parties):
            try:
                party_result = create_party(party, party, headers)
                if party_result.get('uuid') is not None:
                    parties_add_succeed.add(party)
                else:
                    print('交易对手 {} 创建失败: {}'.format(party, party_result))
            except Exception as e:
                print('交易对手 {} 创建异常: {}'.format(party, e))
        print('准备新增 交易对手 {}个，成功创建 {}个，创建失败的有 {}'
              .format(len(total_parties), len(parties_add_succeed),
                      list(total_parties.difference(parties_add_succeed))))
        # 创建交易
        trade_dtos = list(map(trans_trade, total_trades))

        failed = []
        pool = Pool(4)
        chunk_size = 100
        for start in range(0, len(trade_dtos), chunk_size):
            end = start + chunk_size
            params = {
                'trades': trade_dtos[start:end],
                'validTime': '2019-08-08T10:00:00'
            }
            pool.apply_async(func=utils.call_request,
                             args=(bct_ip, 'trade-service', 'trdTradeCreateBatchForJKZX', params, headers,),
                             callback=lambda res: failed.extend(res))
        pool.close()
        pool.join()

        for msg in failed:
            print(msg)

        su_trade_num = utils.call_request(bct_ip, 'trade-service', 'trdTradeListBySimilarTradeId',
                                              {'similarTradeId': ''}, headers)

        if 'error' not in su_trade_num:
            print('可创建交易总数：{}, 成功创建交易数：{}, 创建的交易包括：{}'.format(len(total_trades), len(su_trade_num), su_trade_num))

    finally:
        if oracle_session is not None:
            oracle_session.close()
        if wind_oracle_session is not None:
            wind_oracle_session.close()


if __name__ == '__main__':
    cur_date = datetime.now().strftime(_datetime_fmt)
    trade_data_import(cur_date)
