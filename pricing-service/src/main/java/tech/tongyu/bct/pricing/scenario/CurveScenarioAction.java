package tech.tongyu.bct.pricing.scenario;

import io.vavr.control.Either;
import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.pricing.common.Diagnostic;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.util.HashMap;
import java.util.Map;

public class CurveScenarioAction implements ScenarioAction {
    private final Locator key;
    private final boolean isPercentage;
    private final double delta;

    public CurveScenarioAction(Locator key, boolean isPercentage, double delta) {
        this.key = key;
        this.isPercentage = isPercentage;
        this.delta = delta;
    }

    @Override
    public Either<Diagnostic, Map<Locator, Object>> bump(Map<Locator, Object> baseMarket) {
        Object k = baseMarket.get(key);
        if (k == null)
            return Either.left(Diagnostic.of(key.toString(), Diagnostic.Type.ERROR,
                    "failed to find the curve to bump: " + key));
        Object origCurve = QuantlibObjectCache.Instance.get((String)k);
        if (!(origCurve instanceof DiscountingCurve)) {
            return Either.left(Diagnostic.of(key.toString(), Diagnostic.Type.ERROR,
                    "the object is not a curve: " + key));
        }
        try {
            DiscountingCurve bumpedCurve = isPercentage ?
                    ((DiscountingCurve) origCurve).bumpPercent(delta) :
                    ((DiscountingCurve) origCurve).bump(delta);
            Map<Locator, Object> bumped = new HashMap<>(baseMarket);
            String id = QuantlibObjectCache.Instance.put(bumpedCurve, null);
            bumped.put(key, id);
            return Either.right(bumped);
        } catch (Exception e) {
            return Either.left(Diagnostic.of(key.toString(), Diagnostic.Type.ERROR,
                    "failed to bump the curve " + key.toString()));
        }
    }
}
