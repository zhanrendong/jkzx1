package tech.tongyu.bct.service.quantlib.pricer.mc;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.instrument.*;
import tech.tongyu.bct.service.quantlib.financial.instruments.Portfolio;
import tech.tongyu.bct.service.quantlib.financial.instruments.Position;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Cash;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Forward;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;

import java.util.List;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection.UP_AND_OUT;

public class FactoryMcInstrument {
    private static McInstrument fromPortfolio(Portfolio portfolio) {
        List<Position<?>> positions = portfolio.getPositions();
        McInstrument[] instruments = new McInstrument[positions.size()];
        double[] weights = new double[positions.size()];
        for (int i = 0; i < positions.size(); ++i) {
            weights[i] = positions.get(i).getQuantity();
            instruments[i] = from(positions.get(i).getProduct());
        }
        return new McPortfolio(instruments, weights);
    }

    public static McInstrument from(Object instrument) {
        return Match(instrument).of(
                Case($(instanceOf(Cash.class)), i -> new McInstrumentCash(
                        i.getPayDate(),
                        i.getAmount()
                )),
                Case($(instanceOf(Forward.class)), i -> new McInstrumentForward(
                        i.getDeliveryDate(),
                        i.getStrike()
                )),
                Case($(instanceOf(VanillaEuropean.class)), i -> new McInstrumentEuropean(
                        i.getExpiry(),
                        s -> i.getType() == OptionType.CALL ?
                                FastMath.max(s - i.getStrike(), 0.0) :
                                FastMath.max(i.getStrike() - s, 0.0)
                )),
                Case($(instanceOf(KnockOutContinuous.class)), i -> new McInstrumentKnockOut(
                        i.getExpiry(),
                        s -> i.getType() == OptionType.CALL ?
                                FastMath.max(s - i.getStrike(), 0.0) :
                                FastMath.max(i.getStrike() - s, 0.0),
                        i.getBarrierDirection(),
                        i.getRebateAmount(),
                        i.getRebateType(),
                        i.getBarrier()
                )),
                Case($(instanceOf(KnockInContinuous.class)), i -> new McInstrumentKnockIn(
                        i.getExpiry(),
                        s -> i.getType() == OptionType.CALL ?
                                FastMath.max(s - i.getStrike(), 0.0) :
                                FastMath.max(i.getStrike() - s, 0.0),
                        i.getBarrierDirection(),
                        i.getRebateAmount(),
                        i.getBarrier()
                )),
                Case($(instanceOf(DigitalCash.class)), i -> new McInstrumentEuropean(
                        i.getExpiry(),
                        s -> i.getType() == OptionType.CALL ?
                                (s > i.getStrike() ? i.getPayment() : 0.0) :
                                (s < i.getStrike() ? i.getPayment() : 0.0)
                )),
                Case($(instanceOf(KnockOutTerminal.class)), i -> new McInstrumentEuropean(
                        i.getExpiry(),
                        s -> {
                            if (i.getBarrierDirection() == UP_AND_OUT) {
                                return s > i.getBarrier() ?
                                        i.getRebateAmount() :
                                        (i.getType() == OptionType.CALL ?
                                                FastMath.max(s - i.getStrike(), 0.0) :
                                                FastMath.max(i.getStrike() - s, 0.0));
                            }
                            else {
                                    return s < i.getBarrier() ?
                                            i.getRebateAmount() :
                                            (i.getType() == OptionType.CALL ?
                                                    FastMath.max(s - i.getStrike(), 0.0) :
                                                    FastMath.max(i.getStrike() - s, 0.0));
                            }
                        }
                )),
                Case($(instanceOf(DoubleSharkFinTerminal.class)), i -> new McInstrumentEuropean(
                        i.getExpiry(),
                        s -> {
                            if (s <= i.getLowerBarrier())
                                return i.getLowerRebate();
                            else if (s >= i.getUpperBarrier())
                                return i.getUpperRebate();
                            else if (s > i.getUpperStrike())
                                return s - i.getUpperStrike();
                            else if (s < i.getLowerStrike())
                                return i.getLowerStrike() - s;
                            else
                                return 0.0;
                        }
                )),
                Case($(instanceOf(DoubleSharkFinContinuous.class)), i -> new McInstrumentEuropean(
                        i.getExpiry(),
                        s -> {
                            if (s <= i.getLowerBarrier())
                                return i.getLowerRebate();
                            else if (s >= i.getUpperBarrier())
                                return i.getUpperRebate();
                            else if (s > i.getUpperStrike())
                                return s - i.getUpperStrike();
                            else if (s < i.getLowerStrike())
                                return i.getLowerStrike() - s;
                            else
                                return 0.0;
                        }
                )),
                Case($(instanceOf(VerticalSpread.class)), i -> new McInstrumentEuropean(
                        i.getExpiry(),
                        s -> {
                            double diff = i.getUpperStrike() - i.getLowerStrike();
                            double payout = 0;
                            if (s > i.getUpperStrike())
                                payout = diff;
                            else if (s < i.getLowerStrike())
                                payout = 0.0;
                            else
                                payout = s - i.getLowerStrike();
                            return i.getType() == OptionType.CALL ? payout : diff - payout;
                        }
                )),
                Case($(instanceOf(AverageRateArithmetic.class)), i -> new McInstrumentAverageRateArithmatic(
                        i.getExpiry(),
                        i.getSchedule(),
                        i.getWeights(),
                        i.getFixings(),
                        s -> i.getType() == OptionType.CALL ?
                                FastMath.max(s - i.getStrike(), 0.0) :
                                FastMath.max(i.getStrike() - s, 0.0)
                )),
                Case($(instanceOf(NoTouchDiscrete.class)), i -> new McInstrumentNoTouchDiscrete(i)),
                Case($(instanceOf(NoTouch.class)), i -> new McInstrumentKnockOut(
                        i.getExpiry(),
                        v -> i.getPayment(),
                        i.getBarrierDirection(),
                        0,
                        RebateType.PAY_NONE,
                        i.getBarrier()
                )),
                Case($(instanceOf(AutoCall.class)),
                        i -> new McInstrumentAutoCall((AutoCall)instrument)),
                Case($(instanceOf(AutoCallPhoenix.class)),
                        i -> new McInstrumentAutoCallPhoenix((AutoCallPhoenix)instrument))
        );
    }
}
