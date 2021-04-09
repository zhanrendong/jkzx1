# -*- encoding: utf-8 -*-
import utils
from init_params import *

black_analytic_pricer_params = {
    '@class': 'tech.tongyu.bct.quant.library.common.impl.BlackAnalyticPricerParams',
    'calendars': [calendar_name],
    'useCalendar': False,
    'daysInYear': 252
}

black_pricer_class = 'tech.tongyu.bct.quant.library.common.impl.BlackAnalyticPricerParams'
model_xy_pricer_class = 'tech.tongyu.bct.quant.library.common.impl.ModelXYPricer'

def list_pricing_environment(host, token):
    return utils.call('prcPricingEnvironmentsList', {}, 'pricing-service', host, token)


def get_pricing_environment(name, host, token):
    return utils.call('prcPricingEnvironmentGet', {
        'pricingEnvironmentId': name
    }, 'pricing-service', host, token)


def delete_pricing_environment(name, host, token):
    return utils.call('prcPricingEnvironmentDelete', {
        'pricingEnvironmentId': name
    }, 'pricing-service', host, token)


def create_pricing_environment(name, cash_rule, linear_preduct_rule, option_rule, host, token):
    return utils.call('prcPricingEnvironmentCreate', {
        'pricingEnvironmentId': name,
        'cashRule': cash_rule,
        'linearProductRules': linear_preduct_rule,
        'optionRules': option_rule
    }, 'pricing-service', host, token)


if __name__ == '__main__':
    token = utils.login(admin_user, admin_password, host)

    pes = list_pricing_environment(host, token)

    for pe in pes:
        data = get_pricing_environment(pe, host, token)
        if 'singleAssetOptionRules' in data:
            found_model_xy = False
            instance = 'intraday'
            rules = data['singleAssetOptionRules']
            for rule in rules:
                if 'underlyerInstance' in rule:
                    instance = 'intraday' if rule['underlyerInstance'] == 'intraday' else 'close'
                if 'pricer' in rule and rule['pricer']['@class'] == model_xy_pricer_class:
                    found_model_xy = True
                if ('pricer' in rule
                        and 'pricerParams' in rule['pricer']
                        and rule['pricer']['pricerParams']['@class'] == black_pricer_class):
                    rule['pricer']['pricerParams'] = black_analytic_pricer_params
            if not found_model_xy:
                rules.append({
                    'assetClass': 'ANY',
                    'productType': 'CUSTOM_MODEL_XY',
                    'underlyerType': 'ANY',
                    'underlyerInstance': instance,
                    "underlyerField": 'close' if instance == 'close' else 'last',
                    "discountingCurveName": "PLACEHOLDER",
                    "discountingCurveInstance": instance,
                    "volSurfaceName": "PLACEHOLDER",
                    "volSurfaceInstance": instance,
                    "dividendCurveName": "PLACEHOLDER",
                    "dividendCurveInstance": instance,
                    "pricer": {
                        "@class": "tech.tongyu.bct.quant.library.common.impl.ModelXYPricer",
                        "pricerParams": {
                            "@class": "tech.tongyu.bct.quant.library.common.impl.ModelXYPricerParams",
                            "modelId": "PLACEHOLDER"
                        },
                        "pricerName": "MODEL_XY"
                    }
                })
            delete_pricing_environment(pe, host, token)
            create_pricing_environment(
                pe, data['cashRule'], data['linearProductRules'], data['singleAssetOptionRules'], host, token)
            print('Updated pricing environment: ' + pe)
