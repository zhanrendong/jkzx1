package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.vavr.Tuple2;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.Eagle;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class EquityEagle<U extends Equity & ExchangeListed>
        extends Eagle
        implements Equity, Priceable, HasExpiry, HasUnderlyer, Decomposable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    @JsonCreator
    public EquityEagle(U underlyer,
                       double strike1, double strike2,
                       double strike3, double strike4,
                       double payoff, LocalDateTime expiry) {
        super(strike1, strike2, strike3, strike4, payoff, expiry);
        this.underlyer = underlyer;
    }

    @Override
    public List<Position> decompose(String positionId) {
        return decompose().stream()
                .map(p -> new Position(positionId, p._1, new EquityVanillaEuropean<>(
                        underlyer, p._2.getStrike(), p._2.getExpiry(), OptionTypeEnum.CALL)))
                .collect(Collectors.toList());

    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_EAGLE;
    }

    @Override
    public U getUnderlyer() {
        return underlyer;
    }
}
