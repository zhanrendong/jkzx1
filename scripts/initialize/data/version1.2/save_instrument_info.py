import requests
import io

ip = 'localhost'
login_body = {
    'userName': 'script',
    'password': '123456a.'
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


if __name__ == '__main__':
    user = login(ip, login_body)
    headers = {
        'Authorization': 'Bearer ' + user.json()['token']
    }

    #save commodity unit info
    unit_file = io.open('../../minimum/instrument_unit.csv', 'r', encoding='utf8')
    unit_lines = unit_file.readlines()
    instrument_unit_map = {}
    for unit_line in unit_lines:
        data = unit_line.replace('\n', '').split(',')
        instrument_unit_map[data[0]] = {
            'tradeCategory' : data[1],
            'tradeUnit' : data[2],
            'unit' : data[3]
        }

    call_request(ip, 'market-data-service','mktSaveInstrumentInfoBatch', {
        'infoMap': instrument_unit_map
    }, headers)




