import utils
import requests
from init_params import *

def getAllTrade():
    params = {

    }
    return utils.call('trdTradeSearch', params, 'trade-service', host, token)


def getTradeDocument(tradeId, partyName):
    url = 'http://' + host + ':16016/document-service/bct/download/supplementary_agreement?' \
                             'tradeId=' + str(tradeId) + '&' \
                             'partyName=' + str(partyName) + '&' \
                             'description7=描述7&description8=描述8'
    res = requests.get(url)
    if (res.status_code == 500):
        json = res.json()
        raise RuntimeError('生成交易确认书错误：tradeId=' + tradeId + ' partyName=' + partyName + json['message'])
    print('生成文档成功: ' + tradeId)



if __name__ == '__main__':
    token = utils.login(admin_user, admin_password, host)
    trades = getAllTrade()
    for trade in trades:
        tradeId = trade['tradeId']
        partyName = trade['partyName']
        if (str(trade['positions'][0]['productType']) == "AUTOCALL"):
            continue
        getTradeDocument(tradeId, partyName)
