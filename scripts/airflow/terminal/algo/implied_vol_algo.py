import logging
from scipy.optimize import brentq
from terminal.dto import OptionType, OptionProductType
from .bs_pricing_algo import BlackScholes, BS1993
import math


class OptionParity(object):
    def __init__(self, call_price, put_price, spot, strike, tau):
        self.C = call_price
        self.P = put_price
        self.S = spot
        self.K = strike
        # TODO: daysInYear需要能够变动
        self.T = tau

    def formula(self, r):
        return self.C + self.K * math.exp(-r * self.T) - self.P - self.S


class DateUtils:
    @staticmethod
    def expiredate_count(observed_date, expiredate):
        # TODO 交易日相减or日期相减
        date_diff = expiredate - observed_date
        date_cnt = date_diff.days
        if date_cnt < 0:
            raise Exception("datadate < expiredate")
        return date_cnt


class ImpliedVolAlgo:
    @staticmethod
    def calc_implied_q(call_options, put_options, spot, r, expiration_date, valuation_date=None):
        expire_time = DateUtils.expiredate_count(valuation_date, expiration_date)
        call_price, put_price, exercise_price = ImpliedVolAlgo.calc_closest_putcall(call_options, put_options, spot)
        return r - ImpliedVolAlgo.calc_r_minus_q(expire_time, call_price, put_price, spot, exercise_price)

    @staticmethod
    def calc_implied_r(call_options, put_options, spot, q, expire_time):
        call_price, put_price, exercise_price = ImpliedVolAlgo.calc_closest_putcall(call_options, put_options, spot)
        return q + ImpliedVolAlgo.calc_r_minus_q(expire_time, call_price, put_price, spot, exercise_price)

    @staticmethod
    def calc_interest_rate(option_expire, tau, spot, q_value):
        # option_expire : list(option_data)
        call_options = [x for x in option_expire if x.optionType == OptionType.CALL.name and x.strike > spot]
        put_options = [x for x in option_expire if x.optionType == OptionType.PUT.name and x.strike < spot]
        # 期权平价公式
        r_value = ImpliedVolAlgo.calc_implied_r(call_options,put_options, spot, q_value, tau)
        return r_value


    @staticmethod
    def calc_closest_intrument(obj_list, spot):
        minimum = float('inf')
        price = None
        strike = None
        for item in obj_list:
            diff = abs(float(item.strike) - spot)
            if diff < minimum:
                minimum = diff
                price = float(item.price)
                strike = float(item.strike)
        return price, strike

    @staticmethod
    def calc_closest_putcall(call_obj_list, put_obj_list, spot):
        call_price, call_strike = ImpliedVolAlgo.calc_closest_intrument(call_obj_list, spot)
        put_price, put_strike = ImpliedVolAlgo.calc_closest_intrument(put_obj_list, spot)
        return call_price, put_price, (call_strike + put_strike)/2

    @classmethod
    def calc_r_minus_q(cls, expire_time, call_price, put_price, spot, exercise_price):
        option_parity = OptionParity(call_price, put_price, spot,
                                     exercise_price, expire_time)
        try:
            r_q_value = brentq(option_parity.formula, -15, 15)
            return r_q_value
        except Exception as ex:
            logging.error(ex)
            return None

    @staticmethod
    def calc_implied_vol(price, S, K, tau, r, q, option_type, product_type):
        """
        Calculate the implied vol of an option given its price
        Refer to tech.tongyu.bct.service.quantlib.common.numerics.black,Black iv
        :param price: Option price
        :param S:     Underlying price
        :param K:     Option strike
        :param tau:   Time to expiry (in years)
        :param r:     Risk free rate (annualized)
        :param q:     Dividend yield/Borrow cost etc. combined (annualized)
        :param option_type: Option type (CALL or PUT)
        :return: Implied Black vol
        """
        # 欧式
        if product_type is OptionProductType.VANILLA_EUROPEAN:
            dfr = math.exp(-r * tau)
            dfq = math.exp(-q * tau)
            forward = S * dfq / dfr
            price_to_use = price
            type_to_use = option_type
            if option_type == OptionType.CALL and forward > K:
                type_to_use = OptionType.PUT
                price_to_use = price - S * dfq + K * dfr
            if option_type == OptionType.PUT and forward < K:
                type_to_use = OptionType.CALL
                price_to_use = price + S * dfq - K * dfr
            p = price_to_use

            def value(x):
                if type_to_use is OptionType.CALL:
                    return BlackScholes.calc_call_price(S, K, x, tau, r, q) - p
                else:
                    return BlackScholes.calc_put_price(S, K, x, tau, r, q) - p
        # TODO: 美式, 需要优化算法
        if product_type is OptionProductType.VANILLA_AMERICAN:
            def value(x):
                if option_type is OptionType.CALL:
                    return BS1993.calc_call_price(S, K, x, tau, r, q) - price
                else:
                    return BS1993.calc_put_price(S, K, x, tau, r, q) - price

        return brentq(value, 0.0001, 1.0)

