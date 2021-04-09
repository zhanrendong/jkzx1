package tech.tongyu.bct.pricing.scenario;

import io.vavr.control.Either;
import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.pricing.common.Diagnostic;

import java.util.HashMap;
import java.util.Map;

public class SpotScenarioAction implements ScenarioAction {
    private final Locator key;
    private final boolean isPercentage;
    private final double delta;

    public SpotScenarioAction(Locator key, double delta, boolean isPercentage) {
        this.key = key;
        this.isPercentage = isPercentage;
        this.delta = delta;
    }

    public Either<Diagnostic, Map<Locator, Object>> bump(Map<Locator, Object> orig) {
        Object origSpot = orig.get(key);
        if (origSpot == null)
            return Either.left(Diagnostic.of(key.toString(), Diagnostic.Type.ERROR,
                    "failed to find the underlyer to bump: " + key));
        try {
            double spot = ((Number)origSpot).doubleValue();
            spot = isPercentage ? spot * (1. + delta) : spot + delta;
            Map<Locator, Object> bumped = new HashMap<>(orig);
            bumped.put(key, spot);
            return Either.right(bumped);
        } catch (Exception e) {
            return Either.left(Diagnostic.of(key.toString(), Diagnostic.Type.ERROR,
                    "failed to bump the underlyer spot " + key.toString()));
        }
    }

    public Locator getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "SpotScenarioAction{" +
                "key='" + key + '\'' +
                ", isPercentage=" + isPercentage +
                ", delta=" + delta +
                '}';
    }
}
