package tech.tongyu.bct.quant.library.common.impl;

import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.QuantlibCalcResultsBlack;

import java.util.Objects;
import java.util.Optional;

public class BlackResults implements QuantlibCalcResultsBlack {
    private String positionId;
    private String underlyerInstrumentId;
    // results
    private Double price;
    private Double delta;
    private Double gamma;
    private Double vega;
    private Double theta;
    private Double rhoR;
    private Double rhoQ;
    // black params
    private Double underlyerPrice;
    private Double underlyerForward;
    private Double vol;
    private Double r;
    private Double q;
    // base contracts
    private String baseContractInstrumentId;
    private Double baseContractPrice;
    private Double baseContractDelta;
    private Double baseContractGamma;
    private Double baseContractTheta;
    private Double baseContractRhoR;
    // quantity
    private double quantity = 1.0; // must exist

    public BlackResults() {
    }

    public void setResult(CalcTypeEnum calcType, Double value) {
        if (Objects.isNull(value) || value.isNaN() || value.isInfinite()) {
            return;
        }
        value *= this.quantity;
        switch (calcType) {
            case PRICE:
                this.price = value;
                break;
            case DELTA:
                this.delta = value;
                break;
            case GAMMA:
                this.gamma = value;
                break;
            case VEGA:
                this.vega = value;
                break;
            case THETA:
                this.theta = value;
                break;
            case RHO_R:
                this.rhoR = value;
                break;
            case RHO_Q:
                this.rhoQ = value;
                break;
        }
    }

    public void setUnderlyerPrice(Double underlyerPrice) {
        if (!Objects.isNull(underlyerPrice) && !underlyerPrice.isNaN() && !underlyerPrice.isInfinite()) {
            this.underlyerPrice = underlyerPrice;
        }
    }

    public void setVol(Double vol) {
        if (!Objects.isNull(vol) && !vol.isNaN() && !vol.isInfinite()) {
            this.vol = vol;
        }
    }

    public void setR(Double r) {
        if (!Objects.isNull(r) && !r.isNaN() && !r.isInfinite()) {
            this.r = r;
        }
    }

    public void setQ(Double q) {
        if (!Objects.isNull(q) && !q.isNaN() && !r.isInfinite()) {
            this.q = q;
        }
    }

    public void setBaseContractInstrumentId(String baseContractInstrumentId) {
        this.baseContractInstrumentId = baseContractInstrumentId;
    }

    public void setBaseContractPrice(Double baseContractPrice) {
        this.baseContractPrice = baseContractPrice;
    }

    public void setBaseContractDelta(Double baseContractDelta) {
        this.baseContractDelta = baseContractDelta;
    }

    public void setBaseContractGamma(Double baseContractGamma) {
        this.baseContractGamma = baseContractGamma;
    }

    public void setBaseContractTheta(Double baseContractTheta) {
        this.baseContractTheta = baseContractTheta;
    }

    public void setBaseContractRhoR(Double baseContractRhoR) {
        this.baseContractRhoR = baseContractRhoR;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    public void setUnderlyerInstrumentId(String underlyerInstrumentId) {
        this.underlyerInstrumentId = underlyerInstrumentId;
    }

    @Override
    public Optional<Double> getValue(CalcTypeEnum calcType) {
        switch (calcType) {
            case PRICE:
                return Optional.ofNullable(this.price);
            case DELTA:
                return Optional.ofNullable(this.delta);
            case GAMMA:
                return Optional.ofNullable(this.gamma);
            case VEGA:
                return Optional.ofNullable(this.vega);
            case THETA:
                return Optional.ofNullable(this.theta);
            case RHO_R:
                return Optional.ofNullable(this.rhoR);
            case RHO_Q:
                return Optional.ofNullable(this.rhoQ);
            default:
                return Optional.empty();
        }
    }

    @Override
    public Optional<Double> underlyerPrice() {
        return Optional.ofNullable(this.underlyerPrice);
    }

    @Override
    public Optional<Double> vol() {
        return Optional.ofNullable(this.vol);
    }

    @Override
    public Optional<Double> r() {
        return Optional.ofNullable(this.r);
    }

    @Override
    public Optional<Double> q() {
        return Optional.ofNullable(this.q);
    }

    @Override
    public void scale(double quantity) {
        this.quantity *= quantity;
        if (!Objects.isNull(this.price)) {
            this.price *= quantity;
        }
        if (!Objects.isNull(this.delta)) {
            this.delta *= quantity;
        }
        if (!Objects.isNull(this.gamma)) {
            this.gamma *= quantity;
        }
        if (!Objects.isNull(this.vega)) {
            this.vega *= quantity;
        }
        if (!Objects.isNull(this.theta)) {
            this.theta *= quantity;
        }
        if (!Objects.isNull(this.rhoR)) {
            this.rhoR *= quantity;
        }
        if (!Objects.isNull(this.rhoQ)) {
            this.rhoQ *= quantity;
        }
    }

    @Override
    public double quantity() {
        return this.quantity;
    }

    @Override
    public String positionId() {
        return this.positionId;
    }

    public Double getPrice() {
        return price;
    }

    public Double getDelta() {
        return delta;
    }

    public Double getGamma() {
        return gamma;
    }

    public Double getVega() {
        return vega;
    }

    public Double getTheta() {
        return theta;
    }

    public Double getRhoR() {
        return rhoR;
    }

    public Double getRhoQ() {
        return rhoQ;
    }

    public Double getUnderlyerPrice() {
        return underlyerPrice;
    }

    public Double getVol() {
        return vol;
    }

    public Double getR() {
        return r;
    }

    public Double getQ() {
        return q;
    }

    public String getBaseContractInstrumentId() {
        return baseContractInstrumentId;
    }

    public Double getBaseContractPrice() {
        return baseContractPrice;
    }

    public Double getBaseContractDelta() {
        return baseContractDelta;
    }

    public Double getBaseContractGamma() {
        return baseContractGamma;
    }

    public Double getBaseContractTheta() {
        return baseContractTheta;
    }

    public double getQuantity() {
        return quantity;
    }

    public Double getBaseContractRhoR() {
        return baseContractRhoR;
    }

    public Double getUnderlyerForward() {
        return underlyerForward;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUnderlyerForward(Double underlyerForward) {
        this.underlyerForward = underlyerForward;
    }
}
