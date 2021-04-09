package tech.tongyu.bct.cm.product.iov.feature;

import java.math.BigDecimal;

public interface BarrierStepFeature extends BarrierFeature, FixingFeature{

    BigDecimal step();

}
