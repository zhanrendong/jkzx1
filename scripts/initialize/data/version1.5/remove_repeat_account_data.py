from utils import utils

ip = 'localhost'
login_body = {
    'userName': 'script',
    'password': '123456a.'
}


def get_all_account_data(headers):
    response_data = utils.call_request(ip, 'reference-data-service', 'refBankAccountSearch', {}, headers)
    result = [(d['uuid'], d['bankAccount'], d['legalName']) for d in response_data['result']]
    print(result)
    return result


def get_repeat_account_data(datas):
    item_count = {}
    repeat_data = {}

    for item in datas:
        concat = item[1] + ' - ' + item[2]
        if item_count.get(concat) is None:
            item_count[concat] = 1
        else:
            item_count[concat] = item_count[concat] + 1

    print('每个元素出现的次数：' + item_count.__str__())

    for item in item_count:
        count = item_count[item]
        if count > 1:
            for num in range(count - 1):
                for data in datas:
                    concat = data[1] + ' - ' + data[2]
                    if concat == item:
                        repeat_data[item] = data[0]

    print('保留一个之后要删除的所有元素：' + repeat_data.__str__())
    return repeat_data


def delete_repeat_account_data(headers, datas):
    for item in datas:
        del_result = utils.call_request(ip, 'reference-data-service', 'refBankAccountDel', {'uuid': datas[item]},
                                        headers)
        print(datas[item] + ' : ' + del_result.__str__())


headers = utils.login(ip, login_body)
accounts = get_all_account_data(headers)
repeat_data = get_repeat_account_data(accounts)
delete_repeat_account_data(headers, repeat_data)
