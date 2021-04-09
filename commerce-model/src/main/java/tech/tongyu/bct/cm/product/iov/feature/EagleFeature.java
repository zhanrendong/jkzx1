package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public interface EagleFeature extends DoubleParticipationRateFeature {

    UnitOfValue<BigDecimal> strike1();
    UnitOfValue<BigDecimal> strike2();
    UnitOfValue<BigDecimal> strike3();
    UnitOfValue<BigDecimal> strike4();

    default BigDecimal strike1Value() {
        if (strike1().unit() instanceof Percent)
            return strike1().value().multiply(initialSpot());
        else {
            return strike1().value();
        }
    }

    default BigDecimal strike2Value() {
        if (strike2().unit() instanceof Percent)
            return strike2().value().multiply(initialSpot());
        else {
            return strike2().value();
        }
    }

    default BigDecimal strike3Value() {
        if (strike3().unit() instanceof Percent)
            return strike3().value().multiply(initialSpot());
        else {
            return strike3().value();
        }
    }

    default BigDecimal strike4Value() {
        if (strike4().unit() instanceof Percent)
            return strike4().value().multiply(initialSpot());
        else {
            return strike4().value();
        }
    }

    default Boolean validateStrikes(){
        // 行权价2 - 行权价1 = 行权价4 - 行权价3
        BigDecimal resultLeft = strike2Value().subtract(strike1Value());
        BigDecimal resultRight = strike4Value().subtract(strike3Value());
        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-2180):鹰式行权价验证应该添加参与率
        return true;
    }

}
