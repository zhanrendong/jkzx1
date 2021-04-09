package tech.tongyu.bct.pricing.scenario;

import tech.tongyu.bct.quant.library.common.QuantlibCalcResults;


public class SpotScenarioResult implements ScenarioResult {
    private final String positionId;
    private final String scenarioId;
    private final String bumpedInstrumentId;
    private final QuantlibCalcResults scenarioResult;

    public SpotScenarioResult(String positionId, String scenarioId, String bumpedInstrumentId,
                              QuantlibCalcResults result) {
        this.positionId = positionId;
        this.scenarioId = scenarioId;
        this.scenarioResult = result;
        this.bumpedInstrumentId = bumpedInstrumentId;
    }

    @Override
    public String positionId() {
        return this.positionId;
    }

    @Override
    public String scenarioId() {
        return this.scenarioId;
    }

    public String getPositionId() {
        return positionId;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public QuantlibCalcResults getScenarioResult() {
        return scenarioResult;
    }

    public String getBumpedInstrumentId() {
        return bumpedInstrumentId;
    }
}
