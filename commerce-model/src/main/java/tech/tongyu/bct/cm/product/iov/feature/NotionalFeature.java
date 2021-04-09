package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.Lot;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public interface NotionalFeature extends AnnualizedFeature, InitialSpotFeature {

    BigDecimal underlyerMultiplier();

    UnitOfValue<BigDecimal> notionalAmount();

    void notionalChangeLCMEvent(UnitOfValue<BigDecimal> notional);

    //those are defaulting rules, should be extracted and customized for different user
    default BigDecimal notionalAmountValue() {
        return annualizedValue(computeCurrencyValue(notionalAmount()));
    }

    default BigDecimal notionalAmountFaceValue() {
        return computeCurrencyValue(notionalAmount());
    }

    default BigDecimal notionalLotAmountValue(){
        return annualizedValue(computeLotValue(notionalAmount()));
    }

    default BigDecimal computeCurrencyValue(UnitOfValue<BigDecimal> notional){
        BigDecimal currencyValue;
        if (notional.unit() instanceof Lot)
            currencyValue = notional.value().multiply(initialSpot()).multiply(underlyerMultiplier());
        else
            currencyValue = notional.value();
        return currencyValue;
    }

    default BigDecimal annualizedValue(BigDecimal currencyValue){
        if (!isAnnualized()){
            return currencyValue;
        }
        if (annValRatio() != null){
            return currencyValue.multiply(annValRatio()).setScale(10, BigDecimal.ROUND_DOWN);
        }
        return currencyValue.multiply(term()).divide(daysInYear(), 10, BigDecimal.ROUND_DOWN);
    }

    default BigDecimal computeLotValue(UnitOfValue<BigDecimal> notional){
        if(notional.unit() instanceof Lot){
            return notional.value();
        } else {
            BigDecimal initialSpot = BigDecimal.ZERO.compareTo(initialSpot()) == 0 ? BigDecimal.ONE : initialSpot();
            BigDecimal underlyerMultiplier = Objects.isNull(underlyerMultiplier()) ? BigDecimal.ONE :
                    BigDecimal.ZERO.compareTo(underlyerMultiplier()) == 0 ? BigDecimal.ONE : underlyerMultiplier();
            return notional.value().divide(initialSpot, 12, RoundingMode.DOWN)
                    .divide(underlyerMultiplier, 12, RoundingMode.DOWN);
        }
    }
}
