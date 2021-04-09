# -*- coding: utf-8 -*-
from datetime import datetime

_EXERCISE_EUROPEAN = 'EUROPEAN'
_EXERCISE_AMERICAN = 'AMERICAN'
_OBSERVATION_DAILY = 'DAILY'
_OBSERVATION_TERMINAL = 'TERMINAL'
_REBATE_PAY_AT_EXPIRY = 'PAY_AT_EXPIRY'
_date_fmt = '%Y-%m-%d'


def trade(positions, trade_id, book, trade_date, trader, sales):
    return {
        'bookName': book,
        'tradeId': trade_id,
        'trader': trader,
        'comment': 'empty',
        'tradeStatus': 'LIVE',
        'tradeDate': trade_date.strftime(_date_fmt),
        'partyCode': 'empty',
        'partyName': 'empty',
        'salesCode': sales,
        'salesName': sales,
        'salesCommission': 0,
        'positions': positions
    }


def position(asset, product_type, counter_party):
    return {
        'productType': product_type,
        'lcmEventType': 'OPEN',
        'counterPartyCode': counter_party,
        'counterPartyName': counter_party,
        'positionAccountCode': 'empty',
        'positionAccountName': 'empty',
        'counterPartyAccountCode': 'empty',
        'counterPartyAccountName': 'empty',
        'asset': asset
    }


def asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date, notional_amount_type,
                  notional, participation_rate, annualized, term, days_in_year, premium_type, premium, effective_date):
    return {
        'direction': direction,
        'underlyerInstrumentId': underlyer,
        'underlyerMultiplier': multiplier,
        'initialSpot': init_spot,
        'specifiedPrice': specified_price,
        'expirationDate': expiration_date.strftime(_date_fmt),
        'settlementDate': expiration_date.strftime(_date_fmt),
        'effectiveDate': effective_date.strftime(_date_fmt),
        'premiumType': premium_type,
        'premium': premium,
        'notionalAmountType': notional_amount_type,
        'notionalAmount': notional,
        'participationRate': participation_rate,
        'annualized': annualized,
        'term': term,
        'daysInYear': days_in_year,
        'frontPremium': 0,
        'minimumPremium': 0
    }


# 美式
def vanilla_american(trade_id, book, trade_date,
                     option_type, strike_type, strike,
                     direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                     notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                     premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'optionType': option_type,
        'exerciseType': _EXERCISE_AMERICAN,
        'strikeType': strike_type,
        'strike': strike
    })
    return trade([position(asset, 'VANILLA_AMERICAN', counter_party)], trade_id, book, trade_date, trader, sales)


# 欧式
def vanilla_european(trade_id, book, trade_date,
                     option_type, strike_type, strike,
                     direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                     notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                     premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'optionType': option_type,
        'exerciseType': _EXERCISE_EUROPEAN,
        'strikeType': strike_type,
        'strike': strike,
    })
    return trade([position(asset, 'VANILLA_EUROPEAN', counter_party)], trade_id, book, trade_date, trader, sales)


# 一触即付
def one_touch(trade_id, book, trade_date,
              option_type, strike_type, strike, payment_unit, payment, rebate_type,
              direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
              notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
              premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'optionType': option_type,
        'exerciseType': _EXERCISE_AMERICAN,
        'strikeType': strike_type,
        'strike': strike,
        'rebateUnit': payment_unit,
        'rebate': payment,
        'rebateType': rebate_type,
        'observationType': _OBSERVATION_DAILY
    })
    return trade([position(asset, 'DIGITAL', counter_party)], trade_id, book, trade_date, trader, sales)


# 欧式二元
def digital(trade_id, book, trade_date,
            option_type, strike_type, strike, payment_unit, payment,
            direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
            notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
            premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'optionType': option_type,
        'exerciseType': _EXERCISE_EUROPEAN,
        'strikeType': strike_type,
        'strike': strike,
        'paymentType': payment_unit,
        'payment': payment,
        'rebateType': None,
        'observationType': _OBSERVATION_TERMINAL
    })
    return trade([position(asset, 'DIGITAL', counter_party)], trade_id, book, trade_date, trader, sales)


# 欧式价差
def vertical_spread(trade_id, book, trade_date,
                    option_type, strike_type, low_strike, high_strike,
                    direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                    notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                    premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'optionType': option_type,
        'exerciseType': _EXERCISE_EUROPEAN,
        'strikeType': strike_type,
        'lowStrike': low_strike,
        'highStrike': high_strike
    })
    return trade([position(asset, 'VERTICAL_SPREAD', counter_party)], trade_id, book, trade_date, trader, sales)


# 单鲨
def single_shark_fin(trade_id, book, trade_date,
                     strike_type, strike, barrier_type, barrier, rebate_unit, rebate, rebate_type, observation_type,
                     direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                     notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                     premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    if barrier > strike:
        option_type = 'CALL'
        knock_direction = 'UP'
    else:
        option_type = 'PUT'
        knock_direction = 'DOWN'
    asset.update({
        'optionType': option_type,
        'strikeType': strike_type,
        'strike': strike,
        'rebateType': rebate_type,
        'rebateUnit': rebate_unit,
        'rebate': rebate,
        'barrierType': barrier_type,
        'barrier': barrier,
        'barrierShift': 0,
        'knockOutObservationStep': '1M',
        'fixingObservations': {},
        'knockDirection': knock_direction,
        'observationType': observation_type
    })
    return trade([position(asset, 'BARRIER', counter_party)], trade_id, book, trade_date, trader, sales)


# 双鲨
def double_shark_fin(trade_id, book, trade_date,
                     strike_type, low_strike, high_strike, barrier_type, low_barrier, high_barrier,
                     rebate_unit, low_rebate, high_rebate, rebate_type, observation_type,
                     direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                     notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                     premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    del asset['participationRate']
    asset.update({
        'strikeType': strike_type,
        'lowStrike': low_strike,
        'highStrike': high_strike,
        'lowParticipationRate': participation_rate,
        'highParticipationRate': participation_rate,
        'rebateType': rebate_type,
        'rebateUnit': rebate_unit,
        'lowRebate': low_rebate,
        'highRebate': high_rebate,
        'barrierType': barrier_type,
        'lowBarrier': low_barrier,
        'highBarrier': high_barrier,
        'observationType': observation_type
    })
    return trade([position(asset, 'DOUBLE_SHARK_FIN', counter_party)], trade_id, book, trade_date, trader, sales)


# 鹰式
def eagle(trade_id, book, trade_date,
          strike_type, strike1, strike2, strike3, strike4,
          direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
          premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    del asset['participationRate']
    asset.update({
        'strikeType': strike_type,
        'participationRate1': participation_rate,
        'participationRate2': participation_rate,
        'strike1': strike1,
        'strike2': strike2,
        'strike3': strike3,
        'strike4': strike4
    })
    return trade([position(asset, 'EAGLE', counter_party)], trade_id, book, trade_date, trader, sales)


# 美式双触碰
def double_touch(trade_id, book, trade_date,
                 barrier_type, low_barrier, high_barrier, payment_unit, payment, rebate_type,
                 direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                 notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                 premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'barrierType': barrier_type,
        'lowBarrier': low_barrier,
        'highBarrier': high_barrier,
        'rebateType': rebate_type,
        'paymentType': payment_unit,
        'payment': payment,
        'touched': True
    })
    return trade([position(asset, 'DOUBLE_TOUCH', counter_party)], trade_id, book, trade_date, trader, sales)


# 美式双不触碰
def double_no_touch(trade_id, book, trade_date,
                    barrier_type, low_barrier, high_barrier, payment_unit, payment,
                    direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                    notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                    premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
            'barrierType': barrier_type,
            'lowBarrier': low_barrier,
            'highBarrier': high_barrier,
            'rebateType': _REBATE_PAY_AT_EXPIRY,
            'paymentType': payment_unit,
            'payment': payment,
            'touched': False
        })
    return trade([position(asset, 'DOUBLE_NO_TOUCH', counter_party)], trade_id, book, trade_date, trader, sales)


# 二元凹式
def concava(trade_id, book, trade_date,
            barrier_type, low_barrier, high_barrier, payment_unit, payment,
            direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
            notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
            premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'barrierType': barrier_type,
        'lowBarrier': low_barrier,
        'highBarrier': high_barrier,
        'paymentType': payment_unit,
        'payment': payment,
        'concavaed': True
    })
    return trade([position(asset, 'CONCAVA', counter_party)], trade_id, book, trade_date, trader, sales)


# 二元凸式
def convex(trade_id, book, trade_date,
           barrier_type, low_barrier, high_barrier, payment_unit, payment,
           direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
           notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
           premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'barrierType': barrier_type,
        'lowBarrier': low_barrier,
        'highBarrier': high_barrier,
        'paymentType': payment_unit,
        'payment': payment,
        'concavaed': False
    })
    return trade([position(asset, 'CONVEX', counter_party)], trade_id, book, trade_date, trader, sales)


# 三层阶梯
def double_digital(trade_id, book, trade_date,
                   option_type, strike_unit, low_strike, high_strike, payment_unit, low_payment, high_payment,
                   direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                   notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                   premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'optionType': option_type,
        'paymentType': payment_unit,
        'lowPayment': low_payment,
        'highPayment': high_payment,
        'strikeType': strike_unit,
        'lowStrike': low_strike,
        'highStrike': high_strike
    })
    return trade([position(asset, 'DOUBLE_DIGITAL', counter_party)], trade_id, book, trade_date, trader, sales)


# 四层阶梯
def triple_digital(trade_id, book, trade_date,
                   option_type, strike_unit, strike1, strike2, strike3, payment_unit, payment1, payment2, payment3,
                   direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                   notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                   premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'optionType': option_type,
        'paymentType': payment_unit,
        'payment1': payment1,
        'payment2': payment2,
        'payment3': payment3,
        'strikeType': strike_unit,
        'strike1': strike1,
        'strike2': strike2,
        'strike3': strike3
    })
    return trade([position(asset, 'TRIPLE_DIGITAL', counter_party)], trade_id, book, trade_date, trader, sales)


# 区间累积
def range_accruals(trade_id, book, trade_date,
                   barrier_unit, low_barrier, high_barrier, payment_unit, payment, observation_fixings,
                   direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                   notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                   premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'paymentType': payment_unit,
        'payment': payment,
        'barrierType': barrier_unit,
        'lowBarrier': low_barrier,
        'highBarrier': high_barrier,
        'fixingObservations': observation_fixings
    })
    return trade([position(asset, 'RANGE_ACCRUALS', counter_party)], trade_id, book, trade_date, trader, sales)


# 跨式
def straddle(trade_id, book, trade_date,
             strike_unit, low_strike, high_strike,
             direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
             notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
             premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    del asset['participationRate']
    asset.update({
        'lowParticipationRate': participation_rate,
        'highParticipationRate': participation_rate,
        'strikeType': strike_unit,
        'lowStrike': low_strike,
        'highStrike': high_strike
    })
    return trade([position(asset, 'STRADDLE', counter_party)], trade_id, book, trade_date, trader, sales)


# 亚式
def asian(trade_id, book, trade_date,
          option_type, strike_unit, strike, observation_step, observation_weights, observation_fixings,
          direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
          premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'optionType': option_type,
        'strikeType': strike_unit,
        'strike': strike,
        'observationStep': observation_step,
        'fixingWeights': observation_weights,
        'fixingObservations': observation_fixings
    })
    return trade([position(asset, 'ASIAN', counter_party)], trade_id, book, trade_date, trader, sales)


# 雪球式AutoCall
def autocall(trade_id, book, trade_date,
             observation_dates, observation_step, knock_out_direction, barrier_unit, barrier, coupon,
             final_payment_type, final_fixed_payment, final_strike_unit, final_strike,
             direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
             notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
             premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'fixingObservations': {d: None for d in observation_dates},
        'observationBarriers': {d: None for d in observation_dates},
        'step': observation_step,
        'knockDirection': knock_out_direction,
        'barrier': barrier,
        'barrierType': barrier_unit,
        'couponPayment': coupon,
        'autoCallPaymentType': final_payment_type,
        'fixedPayment': final_fixed_payment,
        'autoCallStrikeUnit': final_strike_unit,
        'autoCallStrike': final_strike
    })
    return trade([position(asset, 'AUTOCALL', counter_party)], trade_id, book, trade_date, trader, sales)


# 凤凰式AutoCall
def autocall_phoenix(trade_id, book, trade_date,
                     knock_out_direction, knock_out_barrier_unit, knock_out_barrier, coupon_barrier, coupon,
                     observation_fixings, knock_in_observation_dates, knock_in_observation_step, knocked_in_date,
                     knock_in_barrier_unit, knock_in_barrier, knock_in_option_type, knock_in_strike_unit,
                     knock_in_strike,
                     direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                     notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                     premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, participation_rate, annualized, term, days_in_year,
                          premium_type, premium, effective_date)
    asset.update({
        'fixingObservations': observation_fixings,
        'knockDirection': knock_out_direction,
        'barrier': knock_out_barrier,
        'barrierType': knock_out_barrier_unit,
        'couponBarrier': coupon_barrier,
        'couponPayment': coupon,
        'autoCallPaymentType': None,
        'knockedIn': False,
        'knockInDate': knocked_in_date,
        'knockInBarrierType': knock_in_barrier_unit,
        'knockInBarrier': knock_in_barrier,
        'knockInOptionType': knock_in_option_type,
        'knockInStrikeType': knock_in_strike_unit,
        'knockInStrike': knock_in_strike,
        'knockInObservationStep': knock_in_observation_step,
        'knockInObservationDates': knock_in_observation_dates
    })
    return trade([position(asset, 'AUTOCALL_PHOENIX', counter_party)], trade_id, book, trade_date, trader, sales)


# 远期Forward
def forward(trade_id, book, trade_date,
            strike_unit, strike, delivery_date,
            direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
            notional_amount_type, notional,
            premium_type, premium, effective_date, trader, counter_party, sales):
    asset = asset_generic(direction, underlyer, multiplier, specified_price, init_spot, expiration_date,
                          notional_amount_type, notional, 1, False, None, 365,
                          premium_type, premium, effective_date)
    asset.update({
        'strikeType': strike_unit,
        'settlementDate': delivery_date.strftime(_date_fmt),
        'strike': strike
    })
    del asset['participationRate']
    del asset['term']
    del asset['daysInYear']
    del asset['frontPremium']
    del asset['minimumPremium']
    return trade([position(asset, 'FORWARD', counter_party)], trade_id, book, trade_date, trader, sales)
