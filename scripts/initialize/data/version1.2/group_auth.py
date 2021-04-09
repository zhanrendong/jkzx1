import logging
import requests
from json import JSONEncoder, JSONDecoder

ip = 'localhost'
login_body = {
    'userName': 'admin',
    'password': '12345'
}

def login(login_ip, login_body):
    login_url = 'http://' + login_ip + ':16016/auth-service/users/login'
    login_res = requests.post(login_url, json=login_body)
    return login_res

def call_request(ip, service, method, params, headers):
    url = 'http://' + ip + ':16016/' + service + '/api/rpc'
    body = {
        'method': method,
        'params': params
     }
    try:
        res = requests.post(url, json=body, headers=headers)
        json = res.json()
        if 'error' in json:
            print("failed execute " + method + " to:" + ip + ",error:" + json['error']['message'])
            return 'error'
        else:
            print("success execute " + method + ",callRequest:" + str(len(params)) + " to " + ip)
            return json
    except Exception as e:
        print("failed execute " + method + " to:" + ip + "Exception:" + str(e))
        raise e

def get_group_resource():
    user = login(ip, login_body)
    headers = {
        'Authorization': 'Bearer ' + user.json()['token']
    }
    result = call_request(ip,'auth-service','authResourceGet',{},headers)
    for children in result.get('result').get('children'):
        if children.get('resourceType') == 'PROCESS_DEFINITION':
           return children.get('id')

def set_space_auth(name,list):
    user = login(ip, login_body)
    headers = {
        'Authorization': 'Bearer ' + user.json()['token']
    }
    body = {
        'userId': user.json()['userId'],
        'resourceId': name,
        'permissions': list,
        }
    return call_request(ip,'auth-service','authPermissionsAdd', body, headers)    




if __name__ == '__main__':
    user = login(ip, login_body)
    headers = {
        'Authorization': 'Bearer ' + user.json()['token']
    }
    names = ['财务出入金','授信额度变更','交易录入']
    parentId = get_group_resource()
    set_space_auth(parentId,['CREATE_NAMESPACE'])
    for name in names:
        body = {
            'resourceType': 'PROCESS_DEFINITION_INFO',
            'resourceName': name,
            'parentId': parentId,
            'sort': 0
            }
        result = call_request(ip,'auth-service','authResourceCreate', body, headers)
        list = ['GRANT_ACTION', 'UPDATE_PROCESS_DEFINITION']
        try:
            set_space_auth(result.get('result').get('id'),list)
        except Exception as e:
            print(e)



