package tech.tongyu.bct.quant.api;

import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.EnumUtils;
import tech.tongyu.bct.quant.library.numerics.black.Black76;
import tech.tongyu.bct.quant.library.numerics.black.BlackScholes;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

@Service
public class BlackCalculator {
    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public double qlBlackScholesCalc(
            @BctMethodArg String req,
            @BctMethodArg double S,
            @BctMethodArg double K,
            @BctMethodArg double vol,
            @BctMethodArg double tau,
            @BctMethodArg double r,
            @BctMethodArg double q,
            @BctMethodArg String type) {
        CalcTypeEnum reqType = CalcTypeEnum.valueOf(req.toUpperCase());
        OptionTypeEnum typeEnum = OptionTypeEnum.valueOf(type.toUpperCase());
        return BlackScholes.calc(reqType, S, K, vol, tau, r, q, typeEnum);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public double qlBlack76Calc(
            @BctMethodArg String req,
            @BctMethodArg double S,
            @BctMethodArg double K,
            @BctMethodArg double vol,
            @BctMethodArg double tau,
            @BctMethodArg double r,
            @BctMethodArg String type) {
        CalcTypeEnum reqType = CalcTypeEnum.valueOf(req.toUpperCase());
        OptionTypeEnum typeEnum = OptionTypeEnum.valueOf(type.toUpperCase());
        return Black76.calc(reqType, S, K, vol, tau, r, typeEnum);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public double qlBlackScholesImpliedVol(
            @BctMethodArg double price,
            @BctMethodArg double S,
            @BctMethodArg double K,
            @BctMethodArg double tau,
            @BctMethodArg double r,
            @BctMethodArg double q,
            @BctMethodArg String type
    ) {
        OptionTypeEnum typeEnum = EnumUtils.fromString(type, OptionTypeEnum.class);
        return BlackScholes.iv(price, S, K, tau, r, q, typeEnum);
    }
}
