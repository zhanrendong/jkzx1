# -*- encoding: utf-8 -*-
import init_auth
import utils
from init_calendars import calendar_name
from init_models import model_name, model_instance

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


def create_pricing_environment(name, instance, risk_free_curve, dividend_curve, vol_surface, use_calendars, description,
                               host, token):
    return utils.call('prcPricingEnvironmentCreate', {
        'pricingEnvironmentId': name,
        'cashRule': get_cash_rules(risk_free_curve, instance),
        'linearProductRules': get_linear_product_rules(instance),
        'optionRules': get_option_rules(risk_free_curve, dividend_curve, vol_surface, instance, use_calendars),
        'description': description
    }, 'pricing-service', host, token)


if __name__ == '__main__':
    host = init_auth.host
    token = utils.login(init_auth.script_user_name, init_auth.script_user_password, host)

    print("========== Creating pricing environments =========")
    risk_free_curve = model_name
    dividend_curve = model_name + '分红曲线'
    vol_surface = model_name + '波动率曲面'

    name = 'DEFAULT_INTRADAY'
    description = '默认'
    create_pricing_environment(name, model_instance, risk_free_curve, dividend_curve,
                               vol_surface, True, description, host, token)
    print('Created: ' + name + '(' + description + ')')
