# -*- coding: utf-8 -*-
import utils


# vanilla European option
def vanilla_european(option_property):
    method = 'qlOptionVanillaEuropeanCreate'
    params = {
        'type': option_property['type'],
        'strike': option_property['strike'],
        'expiry': option_property['expiry'],
        'delivery': option_property['delivery'],
    }
    return utils.call(method, params)


# vanilla American option
def vanilla_american(option_property):
    method = 'qlOptionVanillaAmericanCreate'
    params = {
        'type': option_property['type'],
        'strike': option_property['strike'],
        'expiry': option_property['expiry'],
    }
    return utils.call(method, params)


# digital cash option
def digital_cash(option_property):
    method = 'qlOptionDigitalCashCreate'
    params = {
        'type': option_property['type'],
        'strike': option_property['strike'],
        'payment': option_property['payment'],
        'expiry': option_property['expiry'],
        'delivery': option_property['delivery']
    }
    return utils.call(method, params)


# knock-out option (with continuous monitoring)
def knock_out_continuous(option_property):
    method = 'qlOptionKnockOutContinuousCreate'
    params = {
        'type': option_property['type'],
        'strike': option_property['strike'],
        'delivery': option_property['delivery'],
        'expiry': option_property['expiry'],
        'barrier': option_property['barrier'],
        'direction': option_property['direction'],
        'barrierStart': option_property['barrier_start'],
        'barrierEnd': option_property['barrier_end'],
        'rebateAmount': option_property['rebate_amount'],
        'rebateType': option_property['rebate_type']
    }
    return utils.call(method, params)


# knock-in option (with continuous monitoring)
def knock_in_continuous(option_property):
    method = 'qlOptionKnockInContinuousCreate'
    params = {
        'type': option_property['type'],
        'strike': option_property['strike'],
        'delivery': option_property['delivery'],
        'expiry': option_property['expiry'],
        'barrier': option_property['barrier'],
        'direction': option_property['direction'],
        'barrierStart': option_property['barrier_start'],
        'barrierEnd': option_property['barrier_end'],
        'rebateAmount': option_property['rebate_amount'],
    }
    return utils.call(method, params)


# knock-out option (with terminal monitoring)
def knock_out_terminal(option_property):
    method = 'qlOptionKnockOutTerminalCreate'
    params = {
        'type': option_property['type'],
        'strike': option_property['strike'],
        'expiry': option_property['expiry'],
        'delivery': option_property['delivery'],
        'barrier': option_property['barrier'],
        'direction': option_property['direction'],
        'rebateAmount': option_property['rebate_amount']
    }
    return utils.call(method, params)


# knock-in option (with terminal monitoring)
def knock_in_terminal(option_property):
    method = 'qlOptionKnockInTerminalCreate'
    params = {
        'type': option_property['type'],
        'strike': option_property['strike'],
        'expiry': option_property['expiry'],
        'delivery': option_property['delivery'],
        'barrier': option_property['barrier'],
        'direction': option_property['direction'],
        'rebateAmount': option_property['rebate_amount']
    }
    return utils.call(method, params)


# discrete Asian option
def average_rate_arithmetic(option_property):
    method = 'qlOptionAverageRateArithmeticCreate'
    params = {
        'type': option_property['type'],
        'strike': option_property['strike'],
        'expiry': option_property['expiry'],
        'delivery': option_property['delivery'],
        'schedule': option_property['schedule'],
        'weights': option_property['weights'],
        'fixings': option_property['fixings']
    }
    return utils.call(method, params)


# double shark fin option (with continuous monitoring)
def double_shark_fin(option_property):
    method = 'qlOptionDoubleSharkFinContinuousCreate'
    params = {
        'lowerStrike': option_property['lower_strike'],
        'lowerBarrier': option_property['lower_barrier'],
        'lowerRebate': option_property['lower_rebate'],
        'upperStrike': option_property['upper_strike'],
        'upperBarrier': option_property['upper_barrier'],
        'upperRebate': option_property['upper_rebate'],
        'barrierStart': option_property['barrier_start'],
        'barrierEnd': option_property['barrier_end'],
        'expiry': option_property['expiry'],
        'delivery': option_property['delivery']
    }
    return utils.call(method, params)


# double knock-out option(with continuous monitoring)
def double_knock_out_continuous(option_property):
    method = 'qlOptionDoubleKnockOutContinuousCreate'
    params = {
        'type': option_property['type'],
        'strike': option_property['strike'],
        'lowerBarrier': option_property['lower_barrier'],
        'lowerRebate': option_property['lower_rebate'],
        'upperBarrier': option_property['upper_barrier'],
        'upperRebate': option_property['upper_rebate'],
        'barrierStart': option_property['barrier_start'],
        'barrierEnd': option_property['barrier_end'],
        'expiry': option_property['expiry'],
        'delivery': option_property['delivery'],
        'rebateType': option_property['rebate_type']
    }
    return utils.call(method, params)


# range accrual option
def range_accrual(option_property):
    method = 'qlOptionRangeAccrualCreate'
    params = {
        'cumulative': option_property['cumulative'],
        'payoff': option_property['payoff'],
        'expiry': option_property['expiry'],
        'delivery': option_property['delivery'],
        'schedule': option_property['schedule'],
        'fixings': option_property['fixings'],
        'min': option_property['min'],
        'max': option_property['max']

    }
    return utils.call(method, params)

