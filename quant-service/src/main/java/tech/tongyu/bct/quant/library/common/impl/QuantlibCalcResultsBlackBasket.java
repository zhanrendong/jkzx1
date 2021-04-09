package tech.tongyu.bct.quant.library.common.impl;


import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.common.QuantlibCalcResults;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class QuantlibCalcResultsBlackBasket implements QuantlibCalcResults {
    private final String positionId;
    private final double quantity;
    private final List<String> underlyerInstrumentIds;
    private final Double price;
    private final List<Double> deltas;
    private final List<List<Double>> gammas;
    private final List<Double> vegas;
    private final Double theta;
    private final Double rhoR;
    private final List<List<Double>> cegas;
    // pricing parameters
    private final List<Double> underlyerPrices;
    private final List<Double> vols;
    private final double r;
    private final List<Double> qs;

    public QuantlibCalcResultsBlackBasket(String positionId, double quantity,
                                          List<String> underlyerInstrumentIds,
                                          Double price,
                                          List<Double> deltas,
                                          List<List<Double>> gammas,
                                          List<Double> vegas,
                                          Double theta,
                                          Double rhoR,
                                          List<List<Double>> cegas,
                                          List<Double> underlyerPrices,
                                          List<Double> vols,
                                          double r,
                                          List<Double> qs) {
        this.positionId = positionId;
        this.quantity = quantity;
        this.underlyerInstrumentIds = underlyerInstrumentIds;
        this.price = price;
        this.deltas = deltas;
        this.gammas = gammas;
        this.vegas = vegas;
        this.theta = theta;
        this.rhoR = rhoR;
        this.cegas = cegas;
        this.underlyerPrices = underlyerPrices;
        this.vols = vols;
        this.r = r;
        this.qs = qs;
    }

    public QuantlibCalcResultsBlackBasket addPositionId(String positionId) {
        return new QuantlibCalcResultsBlackBasket(
                positionId,
                this.quantity,
                this.underlyerInstrumentIds,
                this.price,
                this.deltas,
                this.gammas,
                this.vegas,
                this.theta,
                this.rhoR,
                this.cegas,
                this.underlyerPrices,
                this.vols,
                this.r,
                this.qs
        );
    }

    public QuantlibCalcResultsBlackBasket scaleBy(double factor) {
        return new QuantlibCalcResultsBlackBasket(
                this.positionId,
                this.quantity * factor,
                this.underlyerInstrumentIds,
                this.price * factor,
                Objects.isNull(deltas) ? null : DoubleUtils.scaleList(deltas, factor),
                Objects.isNull(gammas) ? null : DoubleUtils.scaleMatrix(gammas, factor),
                Objects.isNull(vegas) ? null : DoubleUtils.scaleList(vegas, factor),
                Objects.isNull(theta) ? null : this.theta * factor,
                Objects.isNull(rhoR) ? null : this.rhoR * factor,
                Objects.isNull(cegas) ? null : DoubleUtils.scaleMatrix(cegas, factor),
                underlyerPrices,
                vols,
                r,
                qs
        );
    }

    @Override
    public void scale(double quantity) {
        throw new CustomException("not implemented");
    }

    // for decomposed ones: components values add up, but the quantity should stay the same as the undecopmosed
    public QuantlibCalcResultsBlackBasket sumWithoutQuantity(QuantlibCalcResultsBlackBasket r) {
        if (this.underlyerInstrumentIds.size() != r.getUnderlyerInstrumentIds().size()) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    "quantlib: QuantlibCalcResultsBlackBasket: 标的物数量必须完全一致");
        }
        for (int i = 0; i < r.getUnderlyerInstrumentIds().size(); ++i) {
            if (!Objects.equals(this.underlyerInstrumentIds.get(i), r.getUnderlyerInstrumentIds().get(i))) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        "quantlib: QuantlibCalcResultsBlackBasket: 标的物顺序必须完全一致");
            }
        }
        return new QuantlibCalcResultsBlackBasket(
                this.positionId,
                this.quantity,
                this.underlyerInstrumentIds,
                this.price + r.getPrice(),
                Objects.isNull(deltas) ? null : DoubleUtils.sumList(deltas, r.getDeltas()),
                Objects.isNull(gammas) ? null : DoubleUtils.sumMatrix(gammas, r.getGammas()),
                Objects.isNull(vegas) ? null : DoubleUtils.sumList(vegas, r.getVegas()),
                Objects.isNull(theta) ? null : theta + r.getTheta(),
                Objects.isNull(rhoR) ? null : rhoR + r.getRhoR(),
                Objects.isNull(cegas) ? null : DoubleUtils.sumMatrix(cegas, r.getCegas()),
                this.underlyerPrices,
                this.vols,
                this.r,
                this.qs
        );
    }

    public QuantlibCalcResultsBlackBasket changeQuantity(double quantity) {
        return new QuantlibCalcResultsBlackBasket(
                this.positionId,
                quantity,
                this.underlyerInstrumentIds,
                this.price,
                this.deltas,
                this.gammas,
                this.vegas,
                this.theta,
                this.rhoR,
                this.cegas,
                this.underlyerPrices,
                this.vols,
                this.r,
                this.qs
        );
    }

    @Override
    public Optional<Double> getValue(CalcTypeEnum calcType) {
        switch (calcType) {
            case PRICE:
                return Optional.of(this.price);
            case RHO_R:
                return Optional.of(this.rhoR);
            case THETA:
                return Optional.of(this.theta);
            default:
                return Optional.empty();
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

    public String getPositionId() {
        return positionId;
    }

    public double getQuantity() {
        return quantity;
    }

    public List<String> getUnderlyerInstrumentIds() {
        return underlyerInstrumentIds;
    }

    public Double getPrice() {
        return price;
    }

    public List<Double> getDeltas() {
        return deltas;
    }

    public List<List<Double>> getGammas() {
        return gammas;
    }

    public List<Double> getVegas() {
        return vegas;
    }

    public Double getTheta() {
        return theta;
    }

    public Double getRhoR() {
        return rhoR;
    }

    public List<List<Double>> getCegas() {
        return cegas;
    }

    public List<Double> getUnderlyerPrices() {
        return underlyerPrices;
    }

    public List<Double> getVols() {
        return vols;
    }

    public double getR() {
        return r;
    }

    public List<Double> getQs() {
        return qs;
    }
}
