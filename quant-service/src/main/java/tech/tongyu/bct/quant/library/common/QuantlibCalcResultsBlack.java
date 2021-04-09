package tech.tongyu.bct.quant.library.common;

import java.util.Optional;

public interface QuantlibCalcResultsBlack extends QuantlibCalcResults {
    Optional<Double> underlyerPrice();
    Optional<Double> vol();
    Optional<Double> r();
    Optional<Double> q();
}
