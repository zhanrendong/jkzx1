package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface PremiumFeature extends AnnualizedFeature, NotionalFeature {

    UnitOfValue<BigDecimal> frontPremium();

    default BigDecimal actualFrontPremium() {
        if (frontPremium() == null || frontPremium().value() == null)
            return BigDecimal.valueOf(0);

        if (frontPremium().unit() instanceof Percent) {
            return frontPremium().value().multiply(notionalAmountValue());

        } else return frontPremium().value();
    }

    UnitOfValue<BigDecimal> minimumPremium();


    default BigDecimal actualMinimumPremium() {
        if (minimumPremium() == null || minimumPremium().value() == null)
            return BigDecimal.valueOf(0);
        if (minimumPremium().unit() instanceof Percent) {
            return minimumPremium().value().multiply(notionalAmountValue());

        } else return minimumPremium().value();
    }


    UnitOfValue<BigDecimal> premium();


    default BigDecimal actualPremium() {
        if (premium() == null || premium().value() == null)
            return BigDecimal.valueOf(0);

        if (premium().unit() instanceof Percent) {
            return premium().value().multiply(notionalAmountValue());

        } else return premium().value();
    }

    default BigDecimal actualInitialPremium(UnitOfValue<BigDecimal> initialNotional){
        if (premium() == null || premium().value() == null){
            return BigDecimal.ZERO;
        }
        if (premium().unit() instanceof Percent){
            return premium().value().multiply(annualizedValue(computeCurrencyValue(initialNotional)));
        }
        return premium().value();
    }


    default BigDecimal totalPremiumValue() {
        return actualFrontPremium().add(actualPremium()).add(actualMinimumPremium());
    }

    default BigDecimal premiumAmount() {
        return totalPremiumValue().multiply(notionalAmountValue());
    }
}
