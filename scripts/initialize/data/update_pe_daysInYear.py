# -*- encoding: utf-8 -*-
"""Update daysInYear in pricer parameters. Affects only pricers using calendar."""
import utils


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


def create_pricing_environment(name, cash_rule, linear_product_rule, option_rule, description, host, token):
    return utils.call('prcPricingEnvironmentCreate', {
        'pricingEnvironmentId': name,
        'cashRule': cash_rule,
        'linearProductRules': linear_product_rule,
        'optionRules': option_rule,
        'description': description
    }, 'pricing-service', host, token)


if __name__ == '__main__':
    days_in_year = 245.0  # new daysInYear

    host = 'localhost'
    token = utils.login('script', '123456a.', host)

    pes = list_pricing_environment(host, token)
    for pe_id in pes:
        pe = get_pricing_environment(pe_id, host, token)
        if 'singleAssetOptionRules' in pe:
            updated = False
            for rule in pe['singleAssetOptionRules']:
                if 'pricer' in rule:
                    pricer_params = rule['pricer']['pricerParams']
                    if 'useCalendar' in pricer_params and pricer_params['useCalendar']:
                        pricer_params['daysInYear'] = days_in_year
                        updated = True
            if updated:
                delete_pricing_environment(pe_id, host, token)
                create_pricing_environment(pe_id, pe['cashRule'], pe['linearProductRules'],
                                           pe['singleAssetOptionRules'], pe['description'], host, token)
                print('Updated pricing environment: ' + pe_id)
