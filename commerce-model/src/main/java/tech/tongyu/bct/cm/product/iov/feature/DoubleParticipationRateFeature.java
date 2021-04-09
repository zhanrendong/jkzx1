package tech.tongyu.bct.cm.product.iov.feature;

import java.math.BigDecimal;

public interface DoubleParticipationRateFeature extends NotionalFeature{

    BigDecimal lowParticipationRate();

    BigDecimal highParticipationRate();

    default BigDecimal lowNotionalWithParticipation(){
        return notionalAmountValue().multiply(lowParticipationRate());
    }

    default BigDecimal highNotionalWithParticipation(){
        return notionalAmountValue().multiply(highParticipationRate());
    }
}
