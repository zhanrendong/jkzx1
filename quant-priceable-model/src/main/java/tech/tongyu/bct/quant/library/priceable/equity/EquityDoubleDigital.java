package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.commodity.CommodityDigitalCash;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.DoubleDigital;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class EquityDoubleDigital<U extends Equity & ExchangeListed>
        extends DoubleDigital
        implements Equity, Priceable, HasUnderlyer, Decomposable {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityDoubleDigital(U underlyer, OptionTypeEnum optionTypeEnum, LocalDateTime expiry, double firstStrike, double firstPayment, double secondStrike, double secondPayment) {
        super(expiry, optionTypeEnum, firstStrike, firstPayment, secondStrike, secondPayment);
        this.underlyer = underlyer;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_THIRD_DIGITAL;
    }

    @Override
    public List<Position> decompose(String positionId) {
        if(Objects.equals(OptionTypeEnum.CALL, optionTypeEnum)) {
            return Lists.newArrayList(
                    new Position(positionId, 1.,
                            new EquityDigitalCash<>(underlyer, firstStrike, expiry, OptionTypeEnum.CALL, firstPayment)),
                    new Position(positionId, 1.,
                            new EquityDigitalCash<>(underlyer, secondStrike, expiry, OptionTypeEnum.CALL,
                                    secondPayment - firstPayment))
            );
        }
        else{
            return Lists.newArrayList(
                    new Position(positionId, 1.,
                            new EquityDigitalCash<>(underlyer, firstStrike, expiry, OptionTypeEnum.PUT, firstPayment)),
                    new Position(positionId, 1.,
                            new EquityDigitalCash<>(underlyer, secondStrike, expiry, OptionTypeEnum.PUT,
                                    secondPayment - firstPayment))
            );
        }
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
