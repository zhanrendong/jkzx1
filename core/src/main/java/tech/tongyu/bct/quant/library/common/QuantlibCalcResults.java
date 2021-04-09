package tech.tongyu.bct.quant.library.common;

import java.util.Optional;

public interface QuantlibCalcResults {
    Optional<Double> getValue(CalcTypeEnum calcType);
    void scale(double quantity);
    double quantity();
    String positionId();
}
