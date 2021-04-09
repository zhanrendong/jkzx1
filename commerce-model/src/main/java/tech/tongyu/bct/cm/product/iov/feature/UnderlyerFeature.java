package tech.tongyu.bct.cm.product.iov.feature;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;

public interface UnderlyerFeature<U extends InstrumentOfValue> extends Feature {
    U underlyer();
}
