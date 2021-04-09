package tech.tongyu.bct.quant.api;

import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.quant.library.common.EnumUtils;
import tech.tongyu.bct.quant.library.numerics.model.Heston;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

@Service
public class HestonCalculator {
    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public double qlHestonEuropeanCalc(
            @BctMethodArg double S,
            @BctMethodArg double K,
            @BctMethodArg double tau,
            @BctMethodArg double r,
            @BctMethodArg double q,
            @BctMethodArg double v0,
            @BctMethodArg double omega,
            @BctMethodArg double kappa,
            @BctMethodArg double theta,
            @BctMethodArg double rho,
            @BctMethodArg String type
    ) {
        OptionTypeEnum optionTypeEnum = EnumUtils.fromString(type, OptionTypeEnum.class);
        Heston heston = new Heston(v0, omega, kappa, theta, rho);
        return heston.calc(S, K, tau, r, q, optionTypeEnum);
    }
}
