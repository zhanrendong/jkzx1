# -*- coding: utf-8 -*-
import utils
from init_params import *


def create_department(department, parent, sort, host, token):
    """Create a department and return id of created department."""
    dept_result = utils.call('authDepartmentCreate', {
        'departmentName': department,
        'departmentType': 'Secondary',
        'parentId': parent,
        'sort': sort
    }, 'auth-service', host, token)
    return list(filter(lambda x: x['departmentName'] == department, dept_result['children']))[0]['id']


def create_user(username, password, userType, department_id, host, token):
    """Create a user and return id of create user."""
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
    """Grant user permissions."""
    return utils.call('authPermissionsAdd', {
        'userId': user_id,
        'resourceId': resource_id,
        'permissions': permissions
    }, 'auth-service', host, token)


def create_book(bookname, departmentId, host, token):
    """Create a book."""
    return utils.call('authNonGroupResourceAdd', {
        'resourceType': 'BOOK',
        'resourceName': bookname,
        'departmentId': departmentId
    }, 'auth-service', host, token)


def create_role(name, alias, host, token):
    """Create a role."""
    return utils.call('authRoleCreate', {
        'roleName': name,
        'alias': alias,
        'remark': ''
    }, 'auth-service', host, token)


def get_page_component_ids(host, token):
    """Return all page component ids."""
    page_components = utils.call('authPageComponentList', {}, 'auth-service', host, token)
    ids = []
    for group in page_components['children']:
        ids.append(group['id'])
        if group['children'] is not None:
            ids.extend([page['id'] for page in group['children']])
    return ids


def set_page_permission(role_ids, page_component_ids, host, token):
    """Set page permission for each role."""
    return utils.call('authPagePermissionSet', {
        'permissions': [{
            'roleId': i,
            'pageComponentId': page_component_ids
        } for i in role_ids]
    }, 'auth-service', host, token)


def set_role_permission(role_id, resource_ids, resource_permission, host, token):
    # resourcePermission = ["READ_BOOK", "READ_TRADE"]
    params = {
        'roleId': role_id,
        'permissions': [{'resourceId': i, 'resourcePermission': resource_permission} for i in resource_ids]
    }
    return utils.call('authRolePermissionsModify', params, 'auth-service', host, token)


def set_user_role(user_id, role_ids, host, token):
    """Set user to roles."""
    return utils.call('authUserRoleModify', {
        'userId': user_id,
        'roleIds': role_ids
    }, 'auth-service', host, token)


def get_role_id(role_name, host, token):
    """Return role id of given role."""
    role_infos = utils.call('authRoleList', {}, 'auth-service', host, token)
    for i in role_infos:
        if i['roleName'] == role_name:
            return i['id']
    return None


def init_auth():
    # log in as admin
    admin_token = utils.login(admin_user, admin_password, host)

    print('========== Creating trading department ==========')
    # get root department id
    root_id = utils.call('authAllDepartmentGet', {}, 'auth-service', host, admin_token)['id']
    # create Trading department
    trading_dept_id = create_department(trading_dept_name, root_id, 1000, host, admin_token)
    print('Created department: ' + trading_dept_name)

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

    # create trader
    print('========== Creating trader users ==========')
    trader_ids = {}
    for trader_name in trader_names:
        trader_id = create_user(trader_name, trader_password, 'NORMAL', trading_dept_id, host, admin_token)
        trader_ids[trader_id] = trader_name
        print('Created user: ' + trader_name)
        add_permission(trader_id, client_resource_id, ['READ_CLIENT'], host, admin_token)

    # get resource id associated with Trading department
    trading_resource_id = list(filter(lambda x: x['departmentId'] == trading_dept_id,
                                      resource_result['children']))[0]['id']
    # give traders permissions to trading department
    for trader_id in trader_ids:
        add_permission(trader_id, trading_resource_id, ['CREATE_BOOK'], host, admin_token)

    # trader1 logs in
    trader1_token = utils.login(trader_names[0], trader_password, host)

    # create books
    print('========== Creating books ==========')
    book_ids = []
    for book in books:
        r = create_book(book, trading_dept_id, host, trader1_token)
        print('Created book: ', book)
        book_id = r['id']
        book_ids.append(book_id)
    # give traders permission to the book
    admin_token = utils.login(admin_user, admin_password, host)
    for book_id in book_ids:
        for trader_id in trader_ids:
            add_permission(trader_id, book_id, ['READ_BOOK', 'CREATE_TRADE', 'READ_TRADE'], host, admin_token)
            print('Granted {user} permissions to {book}'.format(user=trader_ids[trader_id], book=book))

    # create role
    print('========== Creating roles ==========')
    role_result = create_role(all_page_role_name, all_page_role_name, host, admin_token)
    trader_role_id = role_result['id']
    print('Created role: ' + all_page_role_name)
    create_role(financial_department_employees_role_name, financial_department_employees_role_name, host, admin_token)
    print('Created role: ' + financial_department_employees_role_name)
    create_role(system_operation_and_maintenance_role_name, system_operation_and_maintenance_role_name, host, admin_token)
    print('Created role: ' + system_operation_and_maintenance_role_name)
    business_role_result = create_role(business_role_name, business_role_name, host, admin_token)
    print('Created role: ' + business_role_name)
    business_role_id = business_role_result['id']

    resource_permissions = ['READ_BOOK', 'READ_TRADE']
    set_role_permission(business_role_id, book_ids, resource_permissions, host, admin_token)

    # find page component id
    page_ids = get_page_component_ids(host, admin_token)
    # give trader role page permissions
    set_page_permission([trader_role_id], page_ids, host, admin_token)
    print('Set page permissions for role: ' + all_page_role_name)
    # set traders as trader role
    for t in trader_ids:
        set_user_role(t, [trader_role_id], host, admin_token)
        print('Set {user} as {role}'.format(user=trader_ids[t], role=all_page_role_name))

if __name__ == '__main__':
    init_auth()