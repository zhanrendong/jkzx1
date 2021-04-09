# -*- coding: utf-8 -*-
import utils
from init_params import *


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


def docPoiTemplateCreate(docType, tradeType):
    try:
        params = {
            'tradeType': tradeType,
            'docType': docType
        }
        return utils.call('docPoiTemplateCreate', params, 'document-service', host, token)
    except Exception:
        print("该模板创建失败" + tradeType + "--" + docType)


if __name__ == '__main__':
    token = utils.login(admin_user, admin_password, host)
    for t in template:
        docPoiTemplateCreate('SETTLE_NOTIFICATION', t)
        docPoiTemplateCreate('SUPPLEMENTARY_AGREEMENT', t)

