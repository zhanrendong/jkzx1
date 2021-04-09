import requests
import time
import utils
from init_params import *

def docTemplateDirectoryCreate(name, description, createdBy):
    params = {
        'name': name,
        'tags': [],
        'description': description,
        'createdBy': createdBy
    }

    return utils.call('docTemplateDirectoryCreate', params, 'document-service', host, token)['uuid']


def docBctTemplateCreate(directoryId, category, transactType, docType, fileType, typeSuffix, groupName):
    params = {
        'directoryId': directoryId,
        'category': category,
        'transactType': transactType,
        'docType': docType,
        'fileType': fileType,
        'typeSuffix': typeSuffix,
        'groupName': groupName
    }
    return utils.call('docBctTemplateCreate', params, 'document-service', host, token)


def docBctTemplateCreateOrUpdate(host, token, uuid, fileName):
    boundary = '----------%s' % hex(int(time.time() * 1000))
    data = []
    data.append('------%s' % boundary)

    data.append('Content-Disposition: form-data; name="%s"\r\n' % 'method')
    data.append('docBctTemplateCreateOrUpdate')
    data.append('------%s' % boundary)

    data.append('Content-Disposition: form-data; name="%s"\r\n' % 'params')
    data.append('{"uuid": "' + uuid + '"}')
    data.append('------%s' % boundary)

    fr = open('./' + fileName + '', 'r', encoding='latin-1')
    data.append('Content-Disposition: form-data; name="file"; filename=' + fileName)
    data.append('Content-Type: %s\r\n' % 'application/xml')
    data.append(str(fr.read().replace("\\n", "").replace("\r\n", "").replace("\n", "").encode('utf-8'), encoding="utf8"))
    fr.close()
    data.append('------%s--\r\n' % boundary)

    http_url = "http://" + host + ":16016/document-service/api/upload/rpc"
    http_body = "\r\n".join(data)

    headers = {
        'content-type': "multipart/form-data; boundary=----" + boundary,
        'Authorization': "Bearer " + token,
        'cache-control': "no-cache",
        'Postman-Token': "5ea34b81-d729-44cd-8089-af1c48aced6f"
    }

    response = requests.request("POST", http_url, data=http_body, headers=headers)

    print(response.text)


if __name__ == '__main__':

    token = utils.login(admin_user, admin_password, host)

    diId = docTemplateDirectoryCreate("交易确认书", "", "admin")
    doc1 = docBctTemplateCreate(diId, "TRADE_TEMPLATE", "EUROPEAN", "SUPPLEMENTARY_AGREEMENT", "WORD_2003", "doc", "交易确认书")
    docBctTemplateCreateOrUpdate(host, token, str(doc1['uuid']), 'trade.xml')

    diId = docTemplateDirectoryCreate("结算通知书", "", "admin")
    doc2 = docBctTemplateCreate(diId, "TRADE_TEMPLATE", "EUROPEAN", "SETTLE_NOTIFICATION", "WORD_2003", "doc", "结算通知书")
    docBctTemplateCreateOrUpdate(host, token, str(doc2['uuid']), 'settle.xml')

    diId = docTemplateDirectoryCreate("估值报告", "", "admin")
    doc3 = docBctTemplateCreate(diId, "CLIENT_TEMPLATE", "", "VALUATION_REPORT", "EXCEL_2003", "xls", "估值报告")
    docBctTemplateCreateOrUpdate(host, token, str(doc3['uuid']), 'valuation.xml')
