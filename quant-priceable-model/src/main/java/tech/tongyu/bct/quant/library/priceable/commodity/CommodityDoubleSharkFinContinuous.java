package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Commodity;
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

public class CommodityDoubleSharkFinContinuous<U extends Commodity & ExchangeListed>
        extends DoubleSharkFinContinuous
        implements Commodity, Priceable, HasUnderlyer, Decomposable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityDoubleSharkFinContinuous(
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
        return PriceableTypeEnum.COMMODITY_DOUBLE_SHARK_FIN_CONTINUOUS;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }

    @Override
    public List<Position> decompose(String positionId) {
        CommodityDoubleKnockOutContinuous<U> lower = new CommodityDoubleKnockOutContinuous<U>(underlyer, lowStrike,
                OptionTypeEnum.PUT, expiry, lowBarrier, highBarrier, barrierType, lowRebate, 0.0, rebateType);
        CommodityDoubleKnockOutContinuous<U> higher = new CommodityDoubleKnockOutContinuous<U>(underlyer, highStrike,
                OptionTypeEnum.CALL, expiry, lowBarrier, highBarrier, barrierType, 0.0, highRebate, rebateType);
        return Arrays.asList(
                new Position(positionId, lowParticipationRate, lower),
                new Position(positionId, highParticipationRate, higher));
    }
}
