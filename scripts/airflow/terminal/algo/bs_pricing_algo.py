import math
from scipy.stats import norm
from terminal.dto import OptionType
import numpy as np


class Constants:
    SMALL_NUMBER = 1e-14


class BlackScholes(object):
    @staticmethod
    def calc_call_price(S, K, vol, tau, r, q):
        """
        Call price by Black formula.
        Refer to tech.tongyu.bct.service.quantlib.common.numerics.black,Black
        :param S: Underlying price
        :param K: Strike
        :param vol: Volatility (annualized)
        :param tau: Time to expiry (in years)
        :param r: Risk free rate (annualized)
        :param q: Dividend yield/Borrow cost etc. combined (annualized)
        :return: Call price discounted to valuation date
        """
        if vol == 0.0 or tau == 0.0 or S < Constants.SMALL_NUMBER:
            return max(S * math.exp(-q * tau) - K * math.exp(-r * tau), 0.0)
        forward = S * math.exp((r - q) * tau)
        df = math.exp(-r * tau)
        var = vol * math.sqrt(tau)
        if K < 0.0:
            ln_moneyness = 40.0
        else:
            ln_moneyness = math.log(forward / K)
        dp = ln_moneyness / var + 0.5 * var
        dm = dp - var
        return df * ((forward * norm.cdf(dp)) - K * norm.cdf(dm))

    @staticmethod
    def calc_put_price(S, K, vol, tau, r, q):
        """
        Call price by Black formula.
        Refer to tech.tongyu.bct.service.quantlib.common.numerics.black,Black
        :param S: Underlying price
        :param K: Strike
        :param vol: Volatility (annualized)
        :param tau: Time to expiry (in years)
        :param r: Risk free rate (annualized)
        :param q: Dividend yield/Borrow cost etc. combined (annualized)
        :return: Call price discounted to valuation date
        """
        if vol == 0.0 or tau == 0.0 or S < Constants.SMALL_NUMBER:
            return max(K * math.exp(-r * tau) - S * math.exp(-q * tau), 0.0)
        forward = S * math.exp((r - q) * tau)
        df = math.exp(-r * tau)
        var = vol * math.sqrt(tau)
        ln_moneyness = math.log(forward / K)
        dp = ln_moneyness / var + 0.5 * var
        dm = dp - var
        return df * (K * norm.cdf(-dm) - forward * norm.cdf(-dp))


class BS1993(object):
    @staticmethod
    def BS1993call(S, K, vol, tau, r, q):
        """
        Call price by Black formula.
        Refer to tech.tongyu.bct.service.quantlib.common.numerics.black,Black
        :param S: Underlying price
        :param K: Strike
        :param vol: Volatility (annualized)
        :param tau: Time to expiry (in years)
        :param r: Risk free rate (annualized)
        :param q: Dividend yield/Borrow cost etc. combined (annualized)
        :return: Call price discounted to valuation date
        """
        if q >= r:
            return BlackScholes.calc_call_price(OptionType.CALL, K, S, tau, vol, r, r - q)
        beta = (0.5 - q / vol ** 2) + np.sqrt((q / vol ** 2 - 0.5) ** 2 + 2 * r / vol ** 2)
        B_inf = beta * K / (beta - 1)
        B_0 = max([K, (r * K / (r - q))])
        h_T = -(q * tau + 2 * vol * np.sqrt(tau)) * B_0 / (B_inf - B_0)
        I = B_0 + (B_inf - B_0) * (1 - np.exp(h_T))
        alpha = (I - K) * I ** (-beta)

        def phi(s, t, gamma, h, i):
            lamb = (-r + gamma * q + 0.5 * gamma * (gamma - 1) * vol ** 2) * t
            d = -(np.log(s / h) + ((q + (gamma - 0.5) * vol ** 2) * t)) / vol / np.sqrt(t)
            kappa = 2 * q / vol ** 2 + 2 * gamma - 1
            ret = norm.cdf(d)
            ret -= (i / s) ** kappa * norm.cdf(d - 2 * np.log(i / s) / vol / np.sqrt(t))
            return ret * np.exp(lamb) * s ** gamma

        call = alpha * S ** beta - alpha * phi(S, tau, beta, I, I)
        call += phi(S, tau, 1, I, I) - phi(S, tau, 1, K, I)
        call += -K * phi(S, tau, 0, I, I)
        call += K * phi(S, tau, 0, K, I)
        return call

    @staticmethod
    def calc_call_price(S, K, vol, tau, r, q):
        return BS1993.BS1993call(S, K, vol, tau, r, r - q)

    @staticmethod
    def calc_put_price(S, K, vol, tau, r, q):
        return BS1993.BS1993call(K, S, vol, tau, q, q - r)


if __name__ == '__main__':
    # print(BS1993.calc_call_price(0.043, 10, 9, 1, 0.1, 0.05, OptionType.PUT))
    print(BS1993.calc_call_price(10, 12, 0.3, 1, 0.1, 0.05))
    print(BS1993.calc_put_price(10, 9, 0.3, 1, 0.1, 0.05))
