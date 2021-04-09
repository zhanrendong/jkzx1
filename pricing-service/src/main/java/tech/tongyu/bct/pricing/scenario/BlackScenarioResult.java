package tech.tongyu.bct.pricing.scenario;

import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.QuantlibCalcResultsBlack;
import tech.tongyu.bct.quant.library.common.impl.QuantlibCalcResultsBlackBasket;

import java.util.List;
import java.util.Objects;

public class BlackScenarioResult implements ScenarioResult{
    private final String positionId;
    private final String scenarioId;
    private final String bumpedInstrumentId;
    private final double quantity;
    private final Double price;
    private final Double delta;
    private final Double gamma;
    private final Double vega;
    private final Double theta;
    private final Double rhoR;
    private final Double underlyerPrice;
    private final Double vol;
    private final Double r;

    private final BlackScenarioResult baseline;

    public BlackScenarioResult(String positionId,
                               String scenarioId,
                               String bumpedInstrumentId,
                               double quantity,
                               Double price, Double delta, Double gamma, Double vega, Double theta, Double rhoR,
                               Double underlyerPrice, Double vol, Double r,
                               BlackScenarioResult baseline) {
        this.quantity = quantity;
        this.positionId = positionId;
        this.scenarioId = scenarioId;
        this.bumpedInstrumentId = bumpedInstrumentId;
        this.price = price;
        this.delta = delta;
        this.gamma = gamma;
        this.vega = vega;
        this.theta = theta;
        this.rhoR = rhoR;
        this.underlyerPrice = underlyerPrice;
        this.vol = vol;
        this.r = r;
        this.baseline = baseline;
    }

    public static BlackScenarioResult from(String underlyer, String scenarioId, QuantlibCalcResultsBlack r) {
        return new BlackScenarioResult(r.positionId(),
                scenarioId,
                underlyer,
                r.quantity(),
                r.getValue(CalcTypeEnum.PRICE).orElse(0.),
                r.getValue(CalcTypeEnum.DELTA).orElse(0.),
                r.getValue(CalcTypeEnum.GAMMA).orElse(0.),
                r.getValue(CalcTypeEnum.VEGA).orElse(0.),
                r.getValue(CalcTypeEnum.THETA).orElse(0.),
                r.getValue(CalcTypeEnum.RHO_R).orElse(0.),
                r.underlyerPrice().orElse(0.),
                r.vol().orElse(0.),
                r.r().orElse(0.),
                null);
    }

    public static BlackScenarioResult from(String underlyer, String scenarioId, QuantlibCalcResultsBlackBasket r) {
        List<String> underlyers = r.getUnderlyerInstrumentIds();
        int idx = underlyers.indexOf(underlyer);
        if (idx == -1) {
            return null;
        }
        return new BlackScenarioResult(
                r.positionId(),
                scenarioId,
                underlyer,
                r.getQuantity(),
                r.getPrice(),
                r.getDeltas().get(idx),
                r.getGammas().get(idx).get(idx),
                r.getVegas().get(idx),
                r.getTheta(),
                r.getRhoR(),
                r.getUnderlyerPrices().get(idx),
                r.getVols().get(idx),
                r.getR(),
                null
        );
    }

    @Override
    public String positionId() {
        return positionId;
    }

    @Override
    public String scenarioId() {
        return scenarioId;
    }

    public String getPositionId() {
        return positionId;
    }

    public String getBumpedInstrumentId() {
        return bumpedInstrumentId;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public Double getPrice() {
        return price;
    }

    public double getPriceChange() {
        if (!Objects.isNull(baseline) && !Objects.isNull(price) && !Objects.isNull(baseline.getPrice())) {
            return price - baseline.getPrice();
        }
        return 0.;
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

    public double getDeltaChange() {
        if (!Objects.isNull(baseline) && !Objects.isNull(delta) && !Objects.isNull(baseline.getDelta())) {
            return delta - baseline.getDelta();
        }
        return 0.;
    }

    public Double getGammaChange() {
        if (!Objects.isNull(baseline) && !Objects.isNull(gamma) && !Objects.isNull(baseline.getGamma())) {
            return gamma - baseline.getGamma();
        }
        return 0.;
    }

    public Double getVegaChange() {
        if (!Objects.isNull(baseline) && !Objects.isNull(vega) && !Objects.isNull(baseline.getVega())) {
            return vega - baseline.getVega();
        }
        return 0.;
    }

    public Double getThetaChange() {
        if (!Objects.isNull(baseline) && !Objects.isNull(theta) && !Objects.isNull(baseline.getTheta())) {
            return theta - baseline.getTheta();
        }
        return 0.;
    }

    public Double getUnderlyerPrice() {
        return underlyerPrice;
    }

    public double getUnderlyerPriceChange() {
        if (!Objects.isNull(baseline) && !Objects.isNull(underlyerPrice)
                && !Objects.isNull(baseline.getUnderlyerPrice())) {
            return underlyerPrice - baseline.getUnderlyerPrice();
        }
        return 0.;
    }

    public Double getVol() {
        return vol;
    }

    public Double getVolChange() {
        if (!Objects.isNull(baseline) && !Objects.isNull(vol) && !Objects.isNull(baseline.getVol())) {
            return vol - baseline.getVol();
        }
        return 0.;
    }

    public Double getR() {
        return r;
    }

    public Double getRChange() {
        if (!Objects.isNull(baseline) && !Objects.isNull(r) && !Objects.isNull(baseline.getR())) {
            return r - baseline.getR();
        }
        return 0.;
    }

    public Double getRhoR() {
        return rhoR;
    }

    public Double getRhoRChange() {
        if (!Objects.isNull(baseline) && !Objects.isNull(r) && !Objects.isNull(baseline.getRhoR())) {
            return rhoR - baseline.getRhoR();
        }
        return 0.;
    }
}
