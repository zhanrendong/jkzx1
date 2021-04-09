# -*- coding: utf-8 -*-
import init_auth
import utils


template = [
    'VANILLA_EUROPEAN',
    'VANILLA_AMERICAN',
    'DIGITAL',
    'VERTICAL_SPREAD',
    'BARRIER',
    'DOUBLE_SHARK_FIN',
    'DOUBLE_DIGITAL',
    'DOUBLE_TOUCH',
    'DOUBLE_NO_TOUCH',
    'CONCAVA',
    'CONVEX',
    'EAGLE',
    'RANGE_ACCRUALS',
    'TRIPLE_DIGITAL',
    'MODEL_XY',
    'AUTOCALL',
    'AUTOCALL_PHOENIX',
    'ASIAN',
    'STRADDLE',
    'FORWARD'
]


def docPoiTemplateCreate(docType, tradeType, host, token):
    try:
        params = {
            'tradeType': tradeType,
            'docType': docType
        }
        return utils.call('docPoiTemplateCreate', params, 'document-service', host, token)
    except Exception:
        print("该模板创建失败" + tradeType + "--" + docType)


if __name__ == '__main__':
    host = init_auth.host
    token = utils.login(init_auth.admin_user, init_auth.admin_password, host)
    for t in template:
        docPoiTemplateCreate('SETTLE_NOTIFICATION', t, host, token)
        docPoiTemplateCreate('SUPPLEMENTARY_AGREEMENT', t, host, token)

