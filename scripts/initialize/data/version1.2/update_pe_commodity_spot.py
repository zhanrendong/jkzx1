# -*- encoding: utf-8 -*-
import utils
import init_params


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
    host = init_params.host
    token = utils.login(init_params.admin_user, init_params.admin_password, host)

    pes = list_pricing_environment(host, token)
    for pe in pes:
        data = get_pricing_environment(pe, host, token)
        if 'singleAssetOptionRules' in data:
            rules = data['singleAssetOptionRules']
            for rule in rules:
                if rule.get('underlyerType').upper() == 'EQUITY_STOCK':
                    new_rule = rule.copy()
                    new_rule['underlyerType'] = 'COMMODITY_SPOT'
                    new_rule['assetClass'] = 'COMMODITY'
                    new_rule['productType'] = new_rule['productType'].replace('EQUITY', 'COMMODITY')
                    rules.append(new_rule)
            delete_pricing_environment(pe, host, token)
            create_pricing_environment(pe, data['cashRule'], data['linearProductRules'], data['singleAssetOptionRules'],
                                       host, token)
            print('Updated pricing environment: ' + pe)
