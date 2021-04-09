package tech.tongyu.bct.quant.library.common.impl;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.QuantlibCalcResults;

import java.util.Optional;

public class QuantlibCalcResultsSwap implements QuantlibCalcResults {
    private final Double npv;
    private final Double dv01;

    public QuantlibCalcResultsSwap(Double npv, Double dv01) {
        this.npv = npv;
        this.dv01 = dv01;
    }

    @Override
    public Optional<Double> getValue(CalcTypeEnum calcType) {
        switch (calcType) {
            case NPV:
                return Optional.of(npv);
            case DV01:
                return Optional.of(dv01);
            default:
                return Optional.empty();
        }
    }

    @Override
    public void scale(double quantity) {
        throw new CustomException("not implemented");
    }

    @Override
    public double quantity() {
        throw new CustomException("not implemented");
    }

    @Override
    public String positionId() {
        return null;
    }

    public Double getNpv() {
        return npv;
    }

    public Double getDv01() {
        return dv01;
    }
}
