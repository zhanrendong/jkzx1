package tech.tongyu.bct.pricing.scenario;

import io.vavr.Tuple2;
import org.springframework.lang.Nullable;
import tech.tongyu.bct.pricing.common.config.PricingConfig;
import tech.tongyu.bct.quant.library.priceable.Position;

public class SpotScenarioDescription {
    private final String scenarioId;
    private final String positionId;
    private final String instrumentId;
    @Nullable
    private final Tuple2<Position, PricingConfig> toPrice;

    public SpotScenarioDescription(
            String scenarioId,
            String positionId,
            String instrumentId,
            Tuple2<Position, PricingConfig> toPrice) {
        this.scenarioId = scenarioId;
        this.positionId = positionId;
        this.instrumentId = instrumentId;
        this.toPrice = toPrice;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getPositionId() {
        return positionId;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    @Nullable
    public Tuple2<Position, PricingConfig> getToPrice() {
        return toPrice;
    }
}
