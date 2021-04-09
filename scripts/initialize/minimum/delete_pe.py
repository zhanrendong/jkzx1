import utils
from init_params import *


def list_pricing_environment(host, token):
    return utils.call('prcPricingEnvironmentsList', {}, 'pricing-service', host, token)


def delete_pricing_environment(name, host, token):
    return utils.call('prcPricingEnvironmentDelete', {
        'pricingEnvironmentId': name
    }, 'pricing-service', host, token)


if __name__ == '__main__':
    token = utils.login(admin_user, admin_password, host)

    pes = list_pricing_environment(host, token)
    for pe in pes:
        delete_pricing_environment(pe, host, token)
        print('Deleted pricing environment: ' + pe)
