package tech.tongyu.bct.pricing.scenario;

import io.vavr.control.Either;
import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.pricing.common.Diagnostic;

import java.util.Map;

/**
 * A scenario may be built out of the base market data by a series of bumpings.
 * Each bumping is represented as a scenario action.
 */
public interface ScenarioAction {
    /**
     * Bumps a base market data collection.
     * A market data collection is a key -> handle/quote map
     * The bumping may fail. For example a vol surface may fail to be bumped as instructed by the scenario.
     * @param baseMarket the original unbumped market data collection
     * @return the bumped market data if successful otherwise a diagnostic
     */
    Either<Diagnostic, Map<Locator, Object>> bump(Map<Locator, Object> baseMarket);
}
