import numpy as np
from math import *


# price the option price using Monte Carlo Method


# generate n random numbers that follow the geometric  brownian motion ds = (r-q)dt + vol SdZ
def gbm_rand(s, tau, r, q, vol, n):
    mu = r - q
    mean = log(s) + (mu - 0.5 * vol ** 2) * tau
    sigma = vol * sqrt(tau)
    return np.random.lognormal(mean, sigma, n)


# compute the covariance matrix using correlation coefficients and the variances
def cov_matrix(rho, sigma):
    """

    :type rho: np.array
    :type sigma: np.array
    :rtype np.ndarray
    """
    n = sigma.size
    cov = np.ndarray(shape=(n, n))

    for i in range(n):
        for j in range(n):
            cov.itemset((i, j), rho.item((i, j)) * sigma.item(i) * sigma.item(j))
    return cov


# vanilla European option price using Monte Carlo simulation
def black_mc(option_type, spot, strike, tau, r, q, vol, n):
    st = gbm_rand(spot, tau, r, q, vol, n)
    df = exp(-r * tau)
    temp = 0.0
    for i in range(n):
        if option_type == 'call':
            if st[i] - strike > 0:
                temp += st[i] - strike
        else:
            if strike - st[i] > 0:
                temp += strike - st[i]
    return df * temp / n


# spread option price using the correlated log normal random number generator
def spread_mc(spot1, spot2, strike, tau, r, q1, q2, vol1, vol2, rho, n):
    df = exp(-r * tau)
    mu1 = r - q1
    mean1 = mu1 - 0.5 * vol1 ** 2
    var1 = vol1 / sqrt(tau)
    mu2 = r - q2
    mean2 = mu2 - 0.5 * vol2 ** 2
    var2 = vol2 / sqrt(tau)
    mean = [mean1, mean2]
    rho_matrix = np.array([[1.0, rho], [rho, 1.0]])
    sigma = np.array([[var1, var2]])
    cov = cov_matrix(rho_matrix, sigma)
    x1, x2 = np.random.multivariate_normal(mean, cov, n).T
    payoff = 0
    for i in range(n):
        temp = spot1 * exp(x1[i] * tau) - spot2 * exp(x2[i] * tau)
        if temp - strike > 0:
            payoff += (temp - strike)
    return payoff * df / n


# basket option price by Monte Carlo method, the payoff is defined as [Q(\sum_i^N w_i*s_i-k)]+
# (Q=1, for call, Q = -1 for put)
# basket option price by Monte Carlo method, the payoff is defined as [Q(\sum_i^N w_i*s_i-k)]+
# (Q=1, for call, Q = -1 for put)
def basket_mc(option_type, spot_array, weight_array, rho_matrix, strike, vol_array, tau, r, q_array, n):
    if option_type == 'call':
        q = 1.0
    else:
        q = -1.0
    df = exp(-r * tau)
    dim = spot_array.size
    mean = []
    sigma = np.ndarray(shape=(1, dim))
    for i in range(dim):
        sigma[0, i] = vol_array[0, i] / sqrt(tau)
        mean.append(r - q_array[0, i] - 0.5 * vol_array[0, i] ** 2)

    cov = cov_matrix(rho_matrix, sigma)
    x = np.random.multivariate_normal(mean, cov, n).T
    payoff = 0
    for i in range(n):
        temp = 0
        for j in range(dim):
            temp += weight_array[0, j] * spot_array[0, j] * exp(x[j, i] * tau)
        if q * (temp - strike) > 0:
            payoff += q * (temp - strike)
    return payoff * df / n


# quanto option price by Monte Carlo Method
# the payoff of this option is Ep*[Q(spot-strike)]+ , Q = 1, for call, Q = -1 for put, and spot, strike are all in
# foreign currencies
def quanto_mc(option_type, spot, strike, rf, q, vols, r, Ep, vole, rho, tau, n):
    """

    :param option_type: 'call' or 'put'
    :param spot: underlying asset price in foreign currency
    :param strike: strike price in foreign currency
    :param rf: foreign interest rate
    :param q: annually dividend yield
    :param vols: the underlying asset's volatility
    :param r: domestic risk free interest
    :param Ep:  predetermined exchange rate specified in units of domestic currency per unit of foreign currency
    :param vole: exchange rate volatility
    :param rho: correlation between asset and domestic exchange rate
    :param tau: time to maturity in years
    :param n: number of samples
    :return: the option price by Monte Carlo method
    """
    df = exp(-r * tau)
    qf = q + r - rf + rho * vols * vole
    s = gbm_rand(spot, tau, r, qf, vols, n)
    payoff = 0
    for i in range(n):
        if option_type == 'call':
            temp = s[i] - strike
        else:
            temp = strike - s[i]
        if temp > 0:
            payoff += temp
    return payoff * Ep * df / n
