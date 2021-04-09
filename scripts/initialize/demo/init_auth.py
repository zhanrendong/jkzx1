# -*- coding: utf-8 -*-
import utils

host = 'localhost'
admin_user = 'admin'
admin_password = '12345'
administrator_user_name = 'administrator'
administrator_user_password = '12345'
script_user_name = 'script'
script_user_password = '123456a.'
trade_user_name = 'trader1'
trade_user_password = '123456a.'
risk_user_name = 'risk1'
risk_user_password = '123456a.'


def create_department(department, parent, sort, host, token):
    '''Create a department and return id of created department.'''
    dept_result = utils.call('authDepartmentCreate', {
        'departmentName': department,
        'departmentType': 'Secondary',
        'parentId': parent,
        'sort': sort
    }, 'auth-service', host, token)
    return list(filter(lambda x: x['departmentName'] == department, dept_result['children']))[0]['id']


def create_user(username, password, userType, department_id, host, token):
    '''Create a user and return id of create user.'''
    return utils.call('authUserCreate', {
        'username': username,
        'password': password,
        'confirmPass': password,
        'departmentId': department_id,
        'userType': userType,
        'nickName': '',
        'contactEmail': '',
        'roleIds': []
    }, 'auth-service', host, token)['id']


def add_permission(user_id, resource_id, permissions, host, token):
    '''Grant user permissions.'''
    return utils.call('authPermissionsAdd', {
        'userId': user_id,
        'resourceId': resource_id,
        'permissions': permissions
    }, 'auth-service', host, token)

def add_role_permission(role_id, permissions, host, token):
    '''Grant user permissions.'''
    return utils.call('authRolePermissionsModify', {
        'roleId': role_id,
        'permissions': permissions
    }, 'auth-service', host, token)

def create_book(bookname, departmentId, host, token):
    '''Create a book.'''
    return utils.call('authNonGroupResourceAdd', {
        'resourceType': 'BOOK',
        'resourceName': bookname,
        'departmentId': departmentId
    }, 'auth-service', host, token)


def create_role(name, alias, host, token):
    '''Create a role.'''
    return utils.call('authRoleCreate', {
        'roleName': name,
        'alias': alias,
        'remark': ''
    }, 'auth-service', host, token)


def get_page_component_ids(host, token):
    '''Return all page component ids.'''
    page_components = utils.call('authPageComponentList', {}, 'auth-service', host, token)
    ids = []
    for group in page_components['children']:
        ids.append(group['id'])
        if group['children'] is not None:
            ids.extend([page['id'] for page in group['children']])
    return ids

def get_page_component(host, token):
    return utils.call('authPageComponentList', {
    }, 'auth-service', host, token)

def set_page_permission(role_ids, page_component_ids, host, token):
    '''Set page permission for each role.'''
    return utils.call('authPagePermissionSet', {
        'permissions': [{
            'roleId': i,
            'pageComponentId': page_component_ids
        } for i in role_ids]
    }, 'auth-service', host, token)


def set_user_role(user_id, role_ids, host, token):
    '''Set user to roles.'''
    return utils.call('authUserRoleModify', {
        'userId': user_id,
        'roleIds': role_ids
    }, 'auth-service', host, token)


def get_role_id(role_name, host, token):
    '''Return role id of given role.'''
    role_infos = utils.call('authRoleList', {}, 'auth-service', host, token)
    for i in role_infos:
        if i['roleName'] == role_name:
            return i['id']
    return None


if __name__ == '__main__':
    # log in as admin
    admin_token = utils.login(admin_user, admin_password, host)

    print('========== Creating trading department ==========')
    # get root department id
    root_id = utils.call('authAllDepartmentGet', {}, 'auth-service', host, admin_token)['id']
    # create Trading department
    dept_id_t = create_department('交易部', root_id, 1000, host, admin_token)
    dept_id_r = create_department('风控部', root_id, 1000, host, admin_token)
    dept_id_f = create_department('财务部', root_id, 1000, host, admin_token)
    dept_id_o = create_department('运营部', root_id, 1000, host, admin_token)
    print('Created department: 交易部/风控部/财务部/运营部')

    print('========== Creating book ==========')
    b_1 = create_book('交易簿1', dept_id_t, host, admin_token)
    b_2 = create_book('交易簿2', dept_id_t, host, admin_token)
    b_3 = create_book('交易簿3', dept_id_t, host, admin_token)
    book_id_1 = b_1['id']
    book_id_2 = b_2['id']
    book_id_3 = b_3['id']
    print('Creating book 交易簿1/交易簿2/交易簿3')

    # create role
    print('========== Creating roles ==========')
    role_result_t = create_role('demo-交易员', 'demo-交易员', host, admin_token)
    role_result_r = create_role('demo-风控', 'demo-风控', host, admin_token)
    role_result_f = create_role('demo-财务', 'demo-财务', host, admin_token)
    role_result_o = create_role('demo-运营', 'demo-运营', host, admin_token)
    print('Creating roles demo-交易员/demo-风控/demo-财务/demo-运营')

    print('========== set permission ==========')
    resource_result = utils.call('authResourceGet', {}, 'auth-service', host, admin_token)
    children_list = resource_result['children']
    #data
    for children in children_list:
        if children['resourceName'] == '交易部':
            resourcePermission_trade = ['GRANT_ACTION','CREATE_NAMESPACE','UPDATE_NAMESPACE','DELETE_NAMESPACE','CREATE_DEPARTMENT','UPDATE_DEPARTMENT','DELETE_DEPARTMENT','READ_USER','CREATE_BOOK','CREATE_PORTFOLIO']
            permissions_trade = [{
                'resourcePermission' : resourcePermission_trade,
                'resourceId' : children['id']
            }]
            add_role_permission(role_result_t['id'], permissions_trade, host, admin_token)
            book_list = children['children']
            for book in book_list:
                resourcePermission_book = ['GRANT_ACTION','UPDATE_BOOK','READ_BOOK','DELETE_BOOK','CREATE_TRADE','UPDATE_TRADE','READ_TRADE','DELETE_TRADE']
                resourcePermission_read_book = ['READ_BOOK', 'READ_TRADE']
                permissions_book = [{
                    'resourcePermission' : resourcePermission_book,
                    'resourceId' : book['id']
                }]
                permissions_read_book = [{
                    'resourcePermission': resourcePermission_read_book,
                    'resourceId': book['id']
                }]
                add_role_permission(role_result_t['id'], permissions_book, host, admin_token)
                add_role_permission(role_result_r['id'], permissions_read_book, host, admin_token)
                add_role_permission(role_result_f['id'], permissions_read_book, host, admin_token)
                add_role_permission(role_result_o['id'], permissions_read_book, host, admin_token)
        if children['resourceName'] == '风控部':
            resourcePermission_trade = ['GRANT_ACTION','CREATE_NAMESPACE','UPDATE_NAMESPACE','DELETE_NAMESPACE','CREATE_DEPARTMENT','UPDATE_DEPARTMENT','DELETE_DEPARTMENT','READ_USER','CREATE_BOOK','CREATE_PORTFOLIO']
            permissions_trade = [{
                'resourcePermission' : resourcePermission_trade,
                'resourceId' : children['id']
            }]
            add_role_permission(role_result_r['id'], permissions_trade, host, admin_token)
        if children['resourceName'] == '财务部':
            resourcePermission_trade = ['GRANT_ACTION','CREATE_NAMESPACE','UPDATE_NAMESPACE','DELETE_NAMESPACE','CREATE_DEPARTMENT','UPDATE_DEPARTMENT','DELETE_DEPARTMENT','READ_USER','CREATE_BOOK','CREATE_PORTFOLIO']
            permissions_trade = [{
                'resourcePermission' : resourcePermission_trade,
                'resourceId' : children['id']
            }]
            add_role_permission(role_result_f['id'], permissions_trade, host, admin_token)
        if children['resourceName'] == '运营部':
            resourcePermission_trade = ['GRANT_ACTION','CREATE_NAMESPACE','UPDATE_NAMESPACE','DELETE_NAMESPACE','CREATE_DEPARTMENT','UPDATE_DEPARTMENT','DELETE_DEPARTMENT','READ_USER','CREATE_BOOK','CREATE_PORTFOLIO']
            permissions_trade = [{
                'resourcePermission' : resourcePermission_trade,
                'resourceId' : children['id']
            }]
            add_role_permission(role_result_o['id'], permissions_trade, host, admin_token)
        if children['resourceName'] == '保证金':
            resourcePermission_margin = ['UPDATE_MARGIN']
            permissions_margin = [{
                'resourcePermission' : resourcePermission_margin,
                'resourceId' : children['id']
            }]
            add_role_permission(role_result_t['id'], permissions_margin, host, admin_token)
            add_role_permission(role_result_r['id'], permissions_margin, host, admin_token)
            add_role_permission(role_result_o['id'], permissions_margin, host, admin_token)
        if children['resourceName'] == '客户信息':
            resourcePermission_party = ['READ_CLIENT', 'CREATE_CLIENT', 'UPDATE_CLIENT', 'DELETE_CLIENT']
            permissions_party = [{
                'resourcePermission' : resourcePermission_party,
                'resourceId' : children['id']
            }]
            add_role_permission(role_result_t['id'], permissions_party, host, admin_token)
            add_role_permission(role_result_r['id'], permissions_party, host, admin_token)
            add_role_permission(role_result_f['id'], permissions_party, host, admin_token)
            add_role_permission(role_result_o['id'], permissions_party, host, admin_token)
        if children['resourceName'] == '流程定义':
            process_list = children['children']
            for process in process_list:
                if process['resourceName'] == '触发器管理':
                    resourcePermission_trigger = ['GRANT_ACTION','CREATE_TRIGGER']
                    permissions_trigger = [{
                        'resourcePermission' : resourcePermission_trigger,
                        'resourceId' : process['id']
                    }]
                    add_role_permission(role_result_t['id'], permissions_trigger, host, admin_token)
                if process['resourceName'] != '触发器管理':
                    resourcePermission_process = ['GRANT_ACTION','UPDATE_PROCESS_DEFINITION','BIND_PROCESS_TRIGGER']
                    permissions_process = [{
                        'resourcePermission' : resourcePermission_process,
                        'resourceId' : process['id']
                    }]
                    add_role_permission(role_result_t['id'], permissions_process, host, admin_token)
    # trade page
    trade_page_component_ids = []
    fund_page_component_ids = []
    risk_page_component_ids = []
    operation_page_component_ids = []

    pages = get_page_component(host, admin_token)
    for page in pages['children']:
        if page['pageName'] == 'reports':
            trade_page_component_ids.append(page['id'])
            risk_page_component_ids.append(page['id'])
            fund_page_component_ids.append(page['id'])
            reports = page['children']
            for r in reports:
                if r['pageName'] != 'reportsCustomManagement':
                    trade_page_component_ids.append(r['id'])
                    risk_page_component_ids.append(r['id'])
                if r['pageName'] == 'tradingStatements':
                    fund_page_component_ids.append(r['id'])
                if r['pageName'] == 'fundsDetailedStatements':
                    fund_page_component_ids.append(r['id'])
                if r['pageName'] == 'customerFundsSummaryStatements':
                    fund_page_component_ids.append(r['id'])

        if page['pageName'] == 'tradeManagement':
            trade_page_component_ids.append(page['id'])
            risk_page_component_ids.append(page['id'])
            operation_page_component_ids.append(page['id'])
            if page['children'] is not None:
                trade_page_component_ids.extend([page['id'] for page in page['children']])
            trade_pages = page['children']
            for r in trade_pages:
                if r['pageName'] == 'contractManagement':
                    risk_page_component_ids.append(r['id'])
                if r['pageName'] == 'marketManagement':
                    risk_page_component_ids.append(r['id'])
                if r['pageName'] == 'subjectStore':
                    risk_page_component_ids.append(r['id'])
                if r['pageName'] == 'onBoardTransaction':
                    risk_page_component_ids.append(r['id'])
                if r['pageName'] == 'portfolioManagement':
                    risk_page_component_ids.append(r['id'])
                if r['pageName'] == 'tradeDocuments':
                    operation_page_component_ids.append(r['id'])

        if page['pageName'] == 'pricingSettings':
            trade_page_component_ids.append(page['id'])
            risk_page_component_ids.append(page['id'])
            if page['children'] is not None:
                trade_page_component_ids.extend([page['id'] for page in page['children']])
            pricing_pages = page['children']
            for r in pricing_pages:
                if r['pageName'] == 'volSurface':
                    risk_page_component_ids.append(r['id'])
                if r['pageName'] == 'riskFreeCurve':
                    risk_page_component_ids.append(r['id'])
                if r['pageName'] == 'dividendCurve':
                    risk_page_component_ids.append(r['id'])

        if page['pageName'] == 'clientManagement':
            trade_page_component_ids.append(page['id'])
            fund_page_component_ids.append(page['id'])
            risk_page_component_ids.append(page['id'])
            operation_page_component_ids.append(page['id'])
            if page['children'] is not None:
                trade_page_component_ids.extend([page['id'] for page in page['children']])
            party_pages = page['children']
            for r in party_pages:
                if r['pageName'] == 'clientInfo':
                    operation_page_component_ids.append(r['id'])
                if r['pageName'] == 'salesManagement':
                    operation_page_component_ids.append(r['id'])
                if r['pageName'] == 'fundStatistics':
                    risk_page_component_ids.append(r['id'])
                    operation_page_component_ids.append(r['id'])
                    fund_page_component_ids.append(r['id'])
                if r['pageName'] == 'marginManagement':
                    risk_page_component_ids.append(r['id'])
                    operation_page_component_ids.append(r['id'])
                if r['pageName'] == 'discrepancyManagement':
                    fund_page_component_ids.append(r['id'])
                if r['pageName'] == 'valuationManagement':
                    operation_page_component_ids.append(r['id'])
                    risk_page_component_ids.append(r['id'])
                if r['pageName'] == 'bankAccount':
                    operation_page_component_ids.append(r['id'])
                    fund_page_component_ids.append(r['id'])
                if r['pageName'] == 'ioglodManagement':
                    operation_page_component_ids.append(r['id'])
                    fund_page_component_ids.append(r['id'])

        if page['pageName'] == 'riskManager':
            trade_page_component_ids.append(page['id'])
            risk_page_component_ids.append(page['id'])
            if page['children'] is not None:
                trade_page_component_ids.extend([page['id'] for page in page['children']])
                risk_page_component_ids.extend([page['id'] for page in page['children']])

        if page['pageName'] == 'approvalProcess':
            trade_page_component_ids.append(page['id'])
            fund_page_component_ids.append(page['id'])
            operation_page_component_ids.append(page['id'])
            risk_page_component_ids.append(page['id'])
            process = page['children']
            for r in process:
                if r['pageName'] == 'approvalProcessManagement':
                    trade_page_component_ids.append(r['id'])
                    fund_page_component_ids.append(r['id'])
                    operation_page_component_ids.append(r['id'])
                    risk_page_component_ids.append(r['id'])
                if r['pageName'] == 'processConfiguration':
                    trade_page_component_ids.append(r['id'])

        if page['pageName'] == 'systemSettings':
            trade_page_component_ids.append(page['id'])
            risk_page_component_ids.append(page['id'])
            systems = page['children']
            for r in systems:
                if r['pageName'] == 'tradeBooks':
                    trade_page_component_ids.append(r['id'])
                if r['pageName'] == 'calendars':
                    trade_page_component_ids.append(r['id'])
                if r['pageName'] == 'riskSettings':
                    trade_page_component_ids.append(r['id'])
                    risk_page_component_ids.append(r['id'])

    set_page_permission([role_result_t['id']], trade_page_component_ids, host, admin_token)
    set_page_permission([role_result_r['id']], risk_page_component_ids, host, admin_token)
    set_page_permission([role_result_f['id']], fund_page_component_ids, host, admin_token)
    set_page_permission([role_result_o['id']], operation_page_component_ids, host, admin_token)
    print('========== set permission over ==========')

    print('========== create user ==========')
    trade_id = create_user(trade_user_name, trade_user_password, 'NORMAL', dept_id_t, host, admin_token)
    risk_id = create_user(risk_user_name, risk_user_password, 'NORMAL', dept_id_r, host, admin_token)
    account_id_1 = create_user('account1', '123456a.', 'NORMAL', dept_id_f, host, admin_token)
    account_id_2 = create_user('account2', '123456a.', 'NORMAL', dept_id_f, host, admin_token)
    operation_id = create_user('operation1', '123456a.', 'NORMAL', dept_id_o, host, admin_token)
    print('create user trader1/risk1/account1/operation1')
    print('========== user add role ==========')
    set_user_role(trade_id, [role_result_t['id']], host, admin_token)
    set_user_role(risk_id, [role_result_r['id']], host, admin_token)
    set_user_role(account_id_1, [role_result_f['id']], host, admin_token)
    set_user_role(account_id_2, [role_result_f['id']], host, admin_token)
    set_user_role(operation_id, [role_result_o['id']], host, admin_token)
    print('========== user add role over ==========')

    # create script user
    script_user_id = create_user(script_user_name, script_user_password, 'SCRIPT', root_id, host, admin_token)
    print('Create script user: ' + script_user_name)
    resource_result = utils.call('authResourceGet', {}, 'auth-service', host, admin_token)
    # set script user permissions
    add_permission(script_user_id, resource_result.get('id'), resource_result.get('resourcePermissions'), host, admin_token)
    children_list = resource_result['children']
    client_resource_id = ''
    for children in children_list:
        if children.get('resourceType') != 'PROCESS_DEFINITION':
            add_permission(script_user_id, children.get('id'), children.get('resourcePermissions'), host, admin_token)
        if children.get('resourceType') == 'CLIENT_INFO':
            client_resource_id = children.get('id')

    print('========== Creating administrator ==========')
    administrator_id = create_user(
        administrator_user_name, administrator_user_password, 'NORMAL', root_id, host, admin_token)
    admin_role_id = get_role_id('admin', host, admin_token)
    set_user_role(administrator_id, [admin_role_id], host, admin_token)
    add_permission(
        administrator_id, resource_result.get('id'), resource_result.get('resourcePermissions'), host, admin_token)
    print('Created user: ' + administrator_id)































