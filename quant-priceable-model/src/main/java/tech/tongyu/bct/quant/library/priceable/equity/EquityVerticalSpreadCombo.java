package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.common.product.VerticalSpread;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class EquityVerticalSpreadCombo<U extends Equity & ExchangeListed>
        extends VerticalSpread
        implements Equity, Priceable, Decomposable, HasExpiry, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    @JsonCreator
    public EquityVerticalSpreadCombo(U underlyer, double strikeLow, double strikeHigh,
                                     LocalDateTime expiry, OptionTypeEnum optionType) {
        super(strikeLow, strikeHigh, expiry, optionType);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_VERTICAL_SPREAD_COMBO;
    }

    @Override
    public U getUnderlyer() {
        return underlyer;
    }

    @Override
    public List<Position> decompose(String positionId) {
        EquityVanillaEuropean<U> lower = new EquityVanillaEuropean<>(underlyer, strikeLow, expiry, optionType);
        Position lowerPosition = new Position(positionId, 1.0, lower);
        EquityVanillaEuropean<U> upper = new EquityVanillaEuropean<>(underlyer, strikeHigh, expiry, optionType);
        Position upperPosition = new Position(positionId, -1.0, upper);
        return Arrays.asList(lowerPosition, upperPosition);
    }
}
