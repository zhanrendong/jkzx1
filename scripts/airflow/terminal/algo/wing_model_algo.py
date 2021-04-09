from math import *
import logging
from scipy.optimize import brentq, curve_fit
from terminal.algo import ImpliedVolAlgo
from terminal.dto import OptionType, OptionProductType, FittingModelDTO, FittingModelStrikeType
from terminal.utils import DateTimeUtils
import numpy as np
from collections import Counter
import scipy


class WingModelFormula(object):
    @staticmethod
    def _relu(x):
        return np.maximum(x, 0)

    # 分段函数: [0,uc]段，下同
    @staticmethod
    def _piecewise_function_0_uc(x, vc, sc, cc):
        return WingModelFormula._relu(vc + sc * x + cc * x ** 2)

    @staticmethod
    def _piecewise_function_uc_usm(x, uc, usm, vc, sc, cc):
        return WingModelFormula._relu(vc - (1 + 1 / usm) * cc * pow(uc, 2) - sc * uc / (2 * usm) + (1 + 1 / usm) * (
                2 * cc * uc + sc) * x - (cc / usm + sc / (2 * uc * usm)) * (x ** 2))

    @staticmethod
    def _piecewise_function_dc_0(x, vc, sc, pc):
        return WingModelFormula._relu(vc + sc * x + pc * x ** 2)

    @staticmethod
    def _piecewise_function_dsm_dc(x, dc, dsm, pc, vc, sc):
        return WingModelFormula._relu(vc - (1 + 1 / dsm) * pc * pow(dc, 2) - sc * dc / (2 * dsm) + (1 + 1 / dsm) * (
                2 * pc * dc + sc) * x - (pc / dsm + sc / (2 * dc * dsm)) * (x ** 2))

    @staticmethod
    def _piecewise_function_dsm(x, vc, dc, dsm, sc, pc):
        return WingModelFormula._relu(vc + (dc * (2 + dsm) * sc) / 2 + (1 + dsm) * pc * pow(dc, 2) + x - x)

    @staticmethod
    def _piecewise_function_usm(x, vc, uc, usm, sc, cc):
        return WingModelFormula._relu(vc + (uc * (2 + usm) * sc) / 2 + (1 + usm) * cc * pow(uc, 2) + x - x)

    @staticmethod
    def wing_model_formula(x, dc, uc, dsm, usm, pc, vc, sc, cc):
        ret = np.piecewise(x, [np.logical_and(x <= dc, x > dc * (1 + dsm)),
                               np.logical_and(x > dc, x <= 0),
                               np.logical_and(x > 0, x <= uc),
                               np.logical_and(x > uc, x <= uc * (1 + usm)),
                               (x < dc * (1 + dsm)),
                               (x > uc * (1 + usm))],
                           [
                               lambda x: WingModelFormula._piecewise_function_dsm_dc(x, dc, dsm, pc, vc, sc),
                               lambda x: WingModelFormula._piecewise_function_dc_0(x, vc, sc, pc),
                               lambda x: WingModelFormula._piecewise_function_0_uc(x, vc, sc, cc),
                               lambda x: WingModelFormula._piecewise_function_uc_usm(x, uc, usm, vc, sc, cc),
                               lambda x: WingModelFormula._piecewise_function_dsm(x, vc, dc, dsm, sc, pc),
                               lambda x: WingModelFormula._piecewise_function_usm(x, vc, uc, usm, sc, cc)
                           ])
        return ret


class WingModelDataAlgo(object):
    @staticmethod
    def group_x_input(wingmodels, spots_dict, strike_type):
        x_axis_total = []
        for wingmodel in wingmodels:
            expire = wingmodel.expiry
            x_axis = np.array(wingmodel.scatter)[:, 0]
            if strike_type == FittingModelStrikeType.STRIKE:
                x_axis_total.extend(x_axis)
            elif strike_type == FittingModelStrikeType.PERCENT:
                x_axis_total.extend(x_axis / spots_dict[expire])
            elif strike_type == FittingModelStrikeType.LOGMONEYNESS:
                x_axis_total.extend(np.log(x_axis / spots_dict[expire]))
            else:
                logging.error("不支持的strike type")
                return None
        return x_axis_total

    @staticmethod
    def bound_parse(dic):
        keys = ['dc', 'uc', 'dsm', 'usm', 'pc', 'vc', 'sc', 'cc']
        min_list = []
        max_list = []
        for i in keys:
            min_list.append(min(dic[i]))
            max_list.append(max(dic[i]))
        return min_list, max_list

    @staticmethod
    def bound_gen(tuple):
        keys = ['dc', 'uc', 'dsm', 'usm', 'pc', 'vc', 'sc', 'cc']
        min, max = tuple
        dic = {}
        for cnt, item in enumerate(keys):
            dic[item] = [min[cnt],max[cnt]]
        return dic

    @staticmethod
    def fit_strike_type(data, strike_type, spots):
        if strike_type == FittingModelStrikeType.STRIKE:
            pass
        elif strike_type == FittingModelStrikeType.LOGMONEYNESS:
            for key, value in data.items():
                value = np.array(value)
                value[:, 0] = np.log(value[:, 0] / spots[int(key)])
                data[key] = value.T.tolist()
        elif strike_type == FittingModelStrikeType.PERCENT:
            for key, value in data.items():
                value = np.array(value)
                value[:, 0] = value[:, 0] / spots[int(key)]
                data[key] = value.tolist()
        return data

    @staticmethod
    def rerange_x_input_to_percent(x_input, strike_type, spots_dict, expire):
        if strike_type == FittingModelStrikeType.STRIKE:
            x_input = x_input / spots_dict[expire]
        elif strike_type == FittingModelStrikeType.PERCENT:
            x_input = x_input
        elif strike_type == FittingModelStrikeType.LOGMONEYNESS:
            x_input = np.exp(x_input)
        return x_input

    @staticmethod
    def fit_all_wing_models(spots, wingmodels, start_strike, end_strike, strike_type, point_num, step):
        spots_dict = {}
        for item in spots:
            spots_dict[item.effectiveDays] = item.spot
        x_input = WingModelDataAlgo.group_x_input(wingmodels, spots_dict, strike_type)
        result = {}
        if start_strike is None:
            left = np.min(x_input)
        else:
            left = start_strike
        if end_strike is None:
            right = np.max(x_input)
        else:
            right = end_strike
        x_extra = WingModelDataAlgo.gen_x_input(left, right, step, point_num)
        x_input = WingModelDataAlgo.merge_x_input(x_input, x_extra)
        x_input = np.unique(x_input)
        for wingmodel in wingmodels:
            expire = wingmodel.expiry
            # TODO 此处x_output被归一化为PERCENTAGE
            x_output, z_output = WingModelAlgo.fit_one_wing_model(x_input, wingmodel.params, strike_type, spots_dict, expire)
            z_new = np.round(z_output, 3)
            result[expire] = list(map(list, zip(x_output, z_new)))
        return result

    @staticmethod
    def gen_x_input_by_step(start_strike, end_strike, step):
        if not (start_strike is not None and end_strike is not None and step is not None):
            return []
        else:
            return np.arange(start_strike, end_strike, step)

    @staticmethod
    def gen_x_input_by_point(start_strike, end_strike, point_num):
        if not (start_strike is not None and end_strike is not None and point_num is not None):
            return []
        else:
            return np.linspace(start_strike, end_strike, point_num)


    @staticmethod
    def merge_x_input(x_input, x_board=None):
        if x_board is None:
            x_board = []
        return np.append(x_board, x_input)

    @staticmethod
    def gen_x_input(start_strike, end_strike, step, point_num):
        if step is not None:
            return WingModelDataAlgo.gen_x_input_by_step(start_strike, end_strike, step)
        if point_num is not None:
            return WingModelDataAlgo.gen_x_input_by_point(start_strike, end_strike, point_num)
        else:
            return None


class WingModelAlgo(object):
    @staticmethod
    def calc_implied_scatter(option_structure_dto_list, r_value, q_value, spot, tau, threshold=1e-10):
        strike_list = []
        date_list = []
        vol_list = []
        for item in option_structure_dto_list:
            if (item.strike > spot and item.optionType == OptionType.CALL.name) or\
                    (item.strike < spot and item.optionType == OptionType.PUT.name):
                try:
                    vol = ImpliedVolAlgo.calc_implied_vol(item.price, spot, item.strike, tau, r_value, q_value,
                                                          OptionType.valueOf(item.optionType), OptionProductType.VANILLA_EUROPEAN)
                    logging.info('implied vol is %f' % vol + ' @ %s' % str(item.strike))
                    if vol > threshold:
                        vol_list.append(vol)
                        date_list.append(tau)
                        strike_list.append(item.strike)
                except Exception as ex:
                    logging.error(ex)
        x_axis = np.array(strike_list, dtype=float)
        y_axis = np.array(date_list, dtype=float)
        z_axis = np.array(vol_list, dtype=float)
        axis = np.c_[x_axis.T, y_axis.T, z_axis.T]
        axis = WingModelAlgo.scatter_cleanning(axis)
        return axis


    @staticmethod
    def calc_multiple_wing_models(option_data_list, spots, observed_date, contract_type, days_in_year, holidays, bounds_dict):
        try:
            underlyer_expire_dict = {}
            # TODO Q(分红)值默认为0
            q_value = 0
            for item in spots:
                underlyer_expire_dict[item.underlyer] = {**underlyer_expire_dict.get(item.underlyer, {}),
                                                         **({item.effectiveDays: item.spot})}
            # underlyer_expire_dict = sorted(underlier_expire_dict.items(), key=lambda x:x[1].keys())
            # x : strikePrice y : expiredate z :implied vol
            # option_data : 标的物为key的dict
            # expire_days: 出现过的失效日期
            # spot: 失效日期为key的value
            # 计算R-Q
            bound_dict = WingModelDataAlgo.bound_gen(bounds_dict[contract_type])

            wing_model_list = []
            for underlyer, expire_days in underlyer_expire_dict.items():
                for expiry in expire_days.keys():
                    effective_days_num = DateTimeUtils.get_effective_days_num(observed_date, expiry, holidays)
                    tau = effective_days_num / days_in_year
                    dto_wing_model = WingModelAlgo.calc_one_wing_model(tau, underlyer, option_data_list, expire_days[expiry],
                                                                       q_value, bound_dict)
                    dto_wing_model.underlyer = underlyer
                    dto_wing_model.tenor = str(effective_days_num)+ 'D'
                    dto_wing_model.daysInYear = days_in_year
                    dto_wing_model.expiry = expiry
                    wing_model_list.append(dto_wing_model)
            return wing_model_list
        except Exception as ex:
            logging.error(ex)

    @staticmethod
    # TODO 当天到期的期权没有意义，可以单独处理 但没有必要
    def calc_one_wing_model(tau, underlyer, option_data_list, spot, q_value, bound_dict):
        """
        :param bound_dict: {
        'dc': [min, max],
        'uc' :,
        'dsm': ,
        'usm': ,
        'pc': ,
        'vc': ,
        'sc': ,
        'cc':
        }
        """
        if tau == 0:
            return None
        # option_expire = sorted([x for x in option_data if x.expirationDate == expire],
        #                        key=lambda x: x.strike)
        r_minus_q_value = ImpliedVolAlgo.calc_interest_rate(option_data_list, tau, spot, q_value)
        # 计算对应expired date 的 scatter
        axis = WingModelAlgo.calc_implied_scatter(option_data_list, r_minus_q_value, q_value, spot, tau)
        # 计算 wingmodel
        params = WingModelAlgo.calc_wing_model(axis, spot, bounds=WingModelDataAlgo.bound_parse(bound_dict))
        if params:
            dto_wing_model = FittingModelDTO()
            axis = np.round(axis, 3)
            axis = axis[np.argsort(axis[:, 0])]
            dto_wing_model.scatter = axis.tolist()
            dto_wing_model.params = params
            dto_wing_model.spotPrice = spot
            dto_wing_model.q = q_value
            dto_wing_model.r = r_minus_q_value + q_value
            # dto_wing_model.tenor = tenor
            # dto_wing_model.expiry = DateTimeUtils.calc_expiry(observed_date, expire)
            # 强假设 : 一个到期日只会有一种underlyer
            dto_wing_model.underlyer = underlyer
            return dto_wing_model
        else:
            return None


    @staticmethod
    def scatter_cleanning(scatter):
        vol = scatter[:, 2]
        outlier = []
        change_flag = False
        # 筛选规则 ：
        # 每个点与和它相邻的点的vol比值应该在合理范围
        # 两端的端点应为(1/3,3)内，中间的点应为(1/2,2)内
        if len(vol) > 3:
            if vol[0] / vol[1] > 3 or vol[0] / vol[1] < 1 / 3:
                outlier.append(0)
                change_flag = True
            if vol[-1] / vol[-2] > 3 or vol[-1] / vol[-2] < 1 / 3:
                outlier.append(len(vol) - 1)
                change_flag = True
            for index in range(len(vol) - 2):
                if (vol[index + 1] / vol[index] > 2 or vol[index + 1] / vol[index] < 0.5) or \
                        (vol[index + 2] / vol[index + 1] > 2 or vol[index + 2] / vol[index + 1] < 0.5):
                    outlier.append(index + 1)
                    change_flag = True
            inlier = [x for x in range(len(vol)) if x not in outlier]
            scatter = scatter[inlier]
            if change_flag:
                scatter = WingModelAlgo.scatter_cleanning(scatter)
        else:
            scatter = scatter
        return scatter

    @staticmethod
    def calc_wing_model(np_points, spot, bounds):
        x_axis = np_points[:, 0]
        z_axis = np_points[:, 2]
        x_log = np.log(x_axis / spot)
        try:
            popt, pcov = curve_fit(WingModelFormula.wing_model_formula, x_log, z_axis,
                                   bounds=bounds, method='dogbox')

            code = ['dc', 'uc', 'dsm', 'usm', 'pc', 'vc', 'sc', 'cc']
            params = dict(zip(code, np.round(popt, 6)))
            logging.info('Wingmodel params : ' + str(params))
            return params
        except Exception as ex:
            logging.error(ex)
            return None

    @staticmethod
    def fit_one_wing_model(x_input, params, strike_type, spots_dict, expire):
        if strike_type == FittingModelStrikeType.STRIKE:
            z_output = WingModelFormula.wing_model_formula(np.log(x_input/spots_dict[expire]) , *params.values())
        elif strike_type == FittingModelStrikeType.PERCENT:
            z_output = WingModelFormula.wing_model_formula(np.log(x_input), *params.values())
        elif strike_type == FittingModelStrikeType.LOGMONEYNESS:
            z_output = WingModelFormula.wing_model_formula(x_input, *params.values())
        else:
            return None
        x_output = WingModelDataAlgo.rerange_x_input_to_percent(x_input, strike_type, spots_dict, expire)
        return x_output, z_output

    @staticmethod
    def fit_echarts(data):
        order_dict = {}
        points_ls = []
        for key in sorted(data.keys(), reverse=False, key=lambda x: int(x)):
            order_dict[key] = data[key]
        for key, value in order_dict.items():
            value.sort()
            for point in value:
                point[0] = round(point[0], 3)
                point[1] = round(point[1], 3)
                point.insert(1, float(key))
            points_ls.extend(value)
        return points_ls


if __name__ == '__main__':
    x = np.array([1,2,3,4,-5])
    print(WingModelFormula._relu(x))

