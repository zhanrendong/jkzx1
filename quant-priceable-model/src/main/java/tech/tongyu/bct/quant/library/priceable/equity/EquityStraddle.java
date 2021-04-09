package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.Straddle;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.List;

public class EquityStraddle<U extends Equity & ExchangeListed>
        extends Straddle
        implements Equity, Priceable, HasUnderlyer, Decomposable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityStraddle(U underlyer, LocalDateTime expiry, double lowStrike, double highStrike,
                          double lowParticipation, double highParticipation) {
        super(expiry, lowStrike, highStrike, lowParticipation, highParticipation);
        this.underlyer = underlyer;
    }

    @Override
    public List<Position> decompose(String positionId) {
        return Lists.newArrayList(
                new Position(positionId, lowParticipation,
                        new EquityVanillaEuropean<>(underlyer, lowStrike, expiry, OptionTypeEnum.PUT)),
                new Position(positionId, highParticipation,
                        new EquityVanillaEuropean<>(underlyer, highStrike, expiry, OptionTypeEnum.CALL)));
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_STRADDLE;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
