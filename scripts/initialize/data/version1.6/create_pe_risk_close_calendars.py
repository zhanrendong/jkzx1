# -*- encoding: utf-8 -*-
"""Create pricing environment RISK_CLOSE_CALENDARS, a copy of DEFAULT_CLOSE_CALENDARS."""
import utils

calendar_name = 'DEFAULT_CALENDAR'

black_analytic_pricer = {
    '@class': 'tech.tongyu.bct.quant.library.common.impl.BlackScholesAnalyticPricer',
    'pricerParams': {
        '@class': 'tech.tongyu.bct.quant.library.common.impl.BlackAnalyticPricerParams',
        'calendars': [calendar_name],
        'useCalendar': False,
        'daysInYear': 365
    }
}

black_analytic_pricer_w_calendars = {
    '@class': 'tech.tongyu.bct.quant.library.common.impl.BlackScholesAnalyticPricer',
    'pricerParams': {
        '@class': 'tech.tongyu.bct.quant.library.common.impl.BlackAnalyticPricerParams',
        'calendars': [calendar_name],
        'useCalendar': True,
        'daysInYear': 245
    }
}

black76_analytic_pricer = {
    '@class': 'tech.tongyu.bct.quant.library.common.impl.Black76AnalyticPricer',
    'pricerParams': {
        '@class': 'tech.tongyu.bct.quant.library.common.impl.BlackAnalyticPricerParams',
        'calendars': [calendar_name],
        'useCalendar': False,
        'daysInYear': 365
    }
}

black76_analytic_pricer_w_calendars = {
    '@class': 'tech.tongyu.bct.quant.library.common.impl.Black76AnalyticPricer',
    'pricerParams': {
        '@class': 'tech.tongyu.bct.quant.library.common.impl.BlackAnalyticPricerParams',
        'calendars': [calendar_name],
        'useCalendar': True,
        'daysInYear': 245
    }
}

black_mc_pricer = {
    '@class': 'tech.tongyu.bct.quant.library.common.impl.BlackMcPricer',
    'pricerParams': {
        '@class': 'tech.tongyu.bct.quant.library.common.impl.BlackMcPricerParams',
        'seed': 1234,
        'numPaths': 1000,
        'stepSize': 7 / 365.,
        'includeGridDates': False,
        'addVolSurfaceDates': False,
        'brownianBridgeAdj': False

    }
}

black76_mc_pricer = {
    '@class': 'tech.tongyu.bct.quant.library.common.impl.Black76McPricer',
    'pricerParams': {
        '@class': 'tech.tongyu.bct.quant.library.common.impl.BlackMcPricerParams',
        'seed': 1234,
        'numPaths': 1000,
        'stepSize': 7 / 365.,
        'includeGridDates': False,
        'addVolSurfaceDates': False,
        'brownianBridgeAdj': False

    }
}

model_xy_pricer = {
    "@class": "tech.tongyu.bct.quant.library.common.impl.ModelXYPricer",
    "pricerParams": {
        "@class": "tech.tongyu.bct.quant.library.common.impl.ModelXYPricerParams",
        "modelId": "PLACEHOLDER"
    }
}


def get_cash_rules(risk_free_curve, instance):
    return {
        'discounted': True,
        'curveName': risk_free_curve,
        'instance': instance
    }


def get_linear_product_rules(instance):
    return [{
        'assetClass': 'equity',
        'instrumentType': 'equity_stock',
        'instance': instance,
        'field': 'close' if instance == 'close' else 'last'
    }, {
        'assetClass': 'equity',
        'instrumentType': 'equity_index',
        'instance': instance,
        'field': 'close' if instance == 'close' else 'last'
    }, {
        'assetClass': 'equity',
        'instrumentType': 'equity_index_futures',
        'instance': instance,
        'field': 'settle' if instance == 'close' else 'last'
    }, {
        'assetClass': 'commodity',
        'instrumentType': 'commodity_spot',
        'instance': instance,
        'field': 'settle' if instance == 'close' else 'last'
    }, {
        'assetClass': 'commodity',
        'instrumentType': 'commodity_futures',
        'instance': instance,
        'field': 'settle' if instance == 'close' else 'last'
    }]


def get_option_rules(risk_free_curve, dividend_curve, vol_surface, instance, use_calendars):
    return [{
        "assetClass": "equity",
        "productType": "generic_single_asset_option",
        "underlyerType": "equity_stock",
        "underlyerInstance": instance,
        "underlyerField": 'close' if instance == 'close' else 'last',
        "discountingCurveName": risk_free_curve,
        "discountingCurveInstance": instance,
        "volSurfaceName": vol_surface,
        "volSurfaceInstance": instance,
        "dividendCurveName": dividend_curve,
        "dividendCurveInstance": instance,
        "pricer": black_analytic_pricer if not use_calendars else black_analytic_pricer_w_calendars
    }, {
        "assetClass": "equity",
        "productType": "generic_single_asset_option",
        "underlyerType": "equity_index",
        "underlyerInstance": instance,
        "underlyerField": 'close' if instance == 'close' else 'last',
        "discountingCurveName": risk_free_curve,
        "discountingCurveInstance": instance,
        "volSurfaceName": vol_surface,
        "volSurfaceInstance": instance,
        "dividendCurveName": dividend_curve,
        "dividendCurveInstance": instance,
        "pricer": black_analytic_pricer if not use_calendars else black_analytic_pricer_w_calendars
    }, {
        "assetClass": "equity",
        "productType": "generic_single_asset_option",
        "underlyerType": "equity_index_futures",
        "underlyerInstance": instance,
        "underlyerField": 'close' if instance == 'close' else 'last',
        "discountingCurveName": risk_free_curve,
        "discountingCurveInstance": instance,
        "volSurfaceName": vol_surface,
        "volSurfaceInstance": instance,
        "dividendCurveName": dividend_curve,
        "dividendCurveInstance": instance,
        "pricer": black76_analytic_pricer if not use_calendars else black76_analytic_pricer_w_calendars
    }, {
        "assetClass": "equity",
        "productType": "equity_autocall",
        "underlyerType": "equity_stock",
        "underlyerInstance": instance,
        "underlyerField": 'close' if instance == 'close' else 'last',
        "discountingCurveName": risk_free_curve,
        "discountingCurveInstance": instance,
        "volSurfaceName": vol_surface,
        "volSurfaceInstance": instance,
        "dividendCurveName": dividend_curve,
        "dividendCurveInstance": instance,
        "pricer": black_mc_pricer
    }, {
        "assetClass": "equity",
        "productType": "equity_autocall",
        "underlyerType": "equity_index",
        "underlyerInstance": instance,
        "underlyerField": 'close' if instance == 'close' else 'last',
        "discountingCurveName": risk_free_curve,
        "discountingCurveInstance": instance,
        "volSurfaceName": vol_surface,
        "volSurfaceInstance": instance,
        "dividendCurveName": dividend_curve,
        "dividendCurveInstance": instance,
        "pricer": black_mc_pricer
    }, {
        "assetClass": "equity",
        "productType": "equity_autocall",
        "underlyerType": "equity_index_futures",
        "underlyerInstance": instance,
        "underlyerField": 'close' if instance == 'close' else 'last',
        "discountingCurveName": risk_free_curve,
        "discountingCurveInstance": instance,
        "volSurfaceName": vol_surface,
        "volSurfaceInstance": instance,
        "dividendCurveName": dividend_curve,
        "dividendCurveInstance": instance,
        "pricer": black76_mc_pricer
    }, {
        "assetClass": "commodity",
        "productType": "generic_single_asset_option",
        "underlyerType": "commodity_futures",
        "underlyerInstance": instance,
        "underlyerField": 'close' if instance == 'close' else 'last',
        "discountingCurveName": risk_free_curve,
        "discountingCurveInstance": instance,
        "volSurfaceName": vol_surface,
        "volSurfaceInstance": instance,
        "pricer": black76_analytic_pricer if not use_calendars else black76_analytic_pricer_w_calendars
    }, {
        "assetClass": "commodity",
        "productType": "commodity_autocall",
        "underlyerType": "commodity_futures",
        "underlyerInstance": instance,
        "underlyerField": 'close' if instance == 'close' else 'last',
        "discountingCurveName": risk_free_curve,
        "discountingCurveInstance": instance,
        "volSurfaceName": vol_surface,
        "volSurfaceInstance": instance,
        "pricer": black76_mc_pricer
    }, {
        "assetClass": "commodity",
        "productType": "generic_single_asset_option",
        "underlyerType": "commodity_spot",
        "underlyerInstance": instance,
        "underlyerField": 'close' if instance == 'close' else 'last',
        "discountingCurveName": risk_free_curve,
        "discountingCurveInstance": instance,
        "volSurfaceName": vol_surface,
        "volSurfaceInstance": instance,
        "dividendCurveName": dividend_curve,
        "dividendCurveInstance": instance,
        "pricer": black_analytic_pricer if not use_calendars else black_analytic_pricer_w_calendars
    }, {
        "assetClass": "commodity",
        "productType": "commodity_autocall",
        "underlyerType": "commodity_spot",
        "underlyerInstance": instance,
        "underlyerField": 'close' if instance == 'close' else 'last',
        "discountingCurveName": risk_free_curve,
        "discountingCurveInstance": instance,
        "volSurfaceName": vol_surface,
        "volSurfaceInstance": instance,
        "dividendCurveName": dividend_curve,
        "dividendCurveInstance": instance,
        "pricer": black_mc_pricer
    }, {
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
        "pricer": model_xy_pricer
    }]


def create_pricing_environment_from_params(name, instance, risk_free_curve, dividend_curve, vol_surface, use_calendars,
                                           description, host, token):
    return utils.call('prcPricingEnvironmentCreate', {
        'pricingEnvironmentId': name,
        'cashRule': get_cash_rules(risk_free_curve, instance),
        'linearProductRules': get_linear_product_rules(instance),
        'optionRules': get_option_rules(risk_free_curve, dividend_curve, vol_surface, instance, use_calendars),
        'description': description
    }, 'pricing-service', host, token)


def create_pricing_environment_from_rules(name, cash_rule, linear_product_rule, option_rule, description, host, token):
    return utils.call('prcPricingEnvironmentCreate', {
        'pricingEnvironmentId': name,
        'cashRule': cash_rule,
        'linearProductRules': linear_product_rule,
        'optionRules': option_rule,
        'description': description
    }, 'pricing-service', host, token)


def list_pricing_environment(host, token):
    return utils.call('prcPricingEnvironmentsList', {}, 'pricing-service', host, token)


def get_pricing_environment(name, host, token):
    return utils.call('prcPricingEnvironmentGet', {
        'pricingEnvironmentId': name
    }, 'pricing-service', host, token)


if __name__ == '__main__':
    risk_close_calendar_name = 'RISK_CLOSE_CALENDARS'
    risk_close_calendar_des = '风控-收盘-交易日'
    close_calendar_name = 'DEFAULT_CLOSE_CALENDARS'

    host = 'localhost'
    token = utils.login('script', '123456a.', host)

    pes = list_pricing_environment(host, token)
    if risk_close_calendar_name in pes:
        print(risk_close_calendar_name + ' already exists.')
    elif close_calendar_name in pes:
        pe = get_pricing_environment(close_calendar_name, host, token)
        pe['description'] = risk_close_calendar_des
        create_pricing_environment_from_rules(risk_close_calendar_name, pe['cashRule'], pe['linearProductRules'],
                                              pe['singleAssetOptionRules'], pe['description'], host, token)
        print('Created pricing environment ' + risk_close_calendar_name
              + ' according to pricing environment' + close_calendar_name)
    elif close_calendar_name not in pes:
        print(close_calendar_name + 'does not exist.')
        risk_free_curve = 'TRADER_RISK_FREE_CURVE'
        dividend_curve = 'TRADER_DIVIDEND_CURVE'
        vol_surface = 'TRADER_VOL'
        create_pricing_environment_from_params(
            risk_close_calendar_name, 'CLOSE', risk_free_curve, dividend_curve, vol_surface, True,
            risk_close_calendar_des, host, token)
        print('Created pricing environment ' + close_calendar_name
              + ' using {r}, {d} and {v}'.format(r=risk_free_curve, d=dividend_curve, v=vol_surface))
