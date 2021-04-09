package tech.tongyu.bct.pricing.scenario;

import io.vavr.control.Either;
import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.pricing.common.Diagnostic;

import java.util.List;
import java.util.Map;

/**
 * A scenario is a series of bumps applied in sequence
 */
public class Scenario {
    private final String id;
    private final List<ScenarioAction> actions;

    public Scenario(String id, List<ScenarioAction> actions) {
        this.id = id;
        this.actions = actions;
    }

    public Either<Diagnostic, Map<Locator, Object>> apply(Map<Locator, Object> orig) {
        Either<Diagnostic, Map<Locator, Object>> bumped = Either.right(orig);
        for (ScenarioAction s : this.actions) {
            bumped = bumped.flatMap(s::bump);
        }
        return bumped;
    }

    public String getId() {
        return id;
    }

    public List<ScenarioAction> getActions() {
        return actions;
    }
}
