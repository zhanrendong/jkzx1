package tech.tongyu.bct.cm.product.iov.feature;

import java.math.BigDecimal;

public interface ParticipationRateFeature extends NotionalFeature{
    BigDecimal participationRate();

    default BigDecimal notionalWithParticipation(){
        return notionalAmountValue().multiply(participationRate());
    }
}
