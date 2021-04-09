package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.DoubleSharkFinContinuous;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class EquityDoubleSharkFinConinuous<U extends Equity & ExchangeListed>
        extends DoubleSharkFinContinuous
        implements Equity, Priceable, HasUnderlyer, Decomposable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityDoubleSharkFinConinuous(
            U underlyer, double lowStrike, double highStrike, LocalDateTime expiry, double lowBarrier,
            double highBarrier, BarrierTypeEnum barrierType, double lowRebate, double highRebate,
            RebateTypeEnum rebateType, double lowParticipationRate, double highParticipationRate) {
        super(lowStrike, highStrike, expiry, lowBarrier, highBarrier, barrierType, lowRebate, highRebate, rebateType,
                lowParticipationRate, highParticipationRate);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_DOUBLE_SHARK_FIN_CONTINUOUS;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }

    @Override
    public List<Position> decompose(String positionId) {
        EquityDoubleKnockOutContinuous<U> lower = new EquityDoubleKnockOutContinuous<>(underlyer, lowStrike,
                OptionTypeEnum.PUT, expiry, lowBarrier, highBarrier, barrierType, lowRebate, 0.0, rebateType);
        EquityDoubleKnockOutContinuous<U> higher = new EquityDoubleKnockOutContinuous<>(underlyer, highStrike,
                OptionTypeEnum.CALL, expiry, lowBarrier, highBarrier, barrierType, 0.0, highRebate, rebateType);
        return Arrays.asList(
                new Position(positionId, lowParticipationRate, lower),
                new Position(positionId, highParticipationRate, higher));
    }
}
