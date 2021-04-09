package tech.tongyu.bct.pricing.common;

import tech.tongyu.bct.common.Locator;

import java.util.List;
import java.util.Objects;

public class CorrelationMatrixLocator implements Locator {
    private final List<String> instrumentIds;

    public CorrelationMatrixLocator(List<String> instrumentIds) {
        this.instrumentIds = instrumentIds;
    }

    public List<String> getInstrumentIds() {
        return instrumentIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CorrelationMatrixLocator)) return false;
        CorrelationMatrixLocator that = (CorrelationMatrixLocator) o;
        return getInstrumentIds().equals(that.getInstrumentIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInstrumentIds());
    }
}
