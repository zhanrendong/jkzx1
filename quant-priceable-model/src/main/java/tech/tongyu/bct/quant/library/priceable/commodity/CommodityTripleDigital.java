package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.TripleDigital;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class CommodityTripleDigital<U extends Commodity & ExchangeListed>
        extends TripleDigital
        implements Commodity, Priceable, HasUnderlyer, Decomposable {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityTripleDigital(U underlyer, LocalDateTime expiry, OptionTypeEnum optionTypeEnum, double firstStrike, double firstPayment, double secondStrike, double secondPayment, double thirdStrike, double thirdPayment) {
        super(expiry, optionTypeEnum, firstStrike, firstPayment, secondStrike, secondPayment, thirdStrike, thirdPayment);
        this.underlyer = underlyer;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_THIRD_DIGITAL;
    }

    @Override
    public List<Position> decompose(String positionId) {
        if(Objects.equals(OptionTypeEnum.CALL, optionTypeEnum)){
            return Lists.newArrayList(
                    new Position(positionId, 1.,
                            new CommodityDigitalCash<>(underlyer, firstStrike, expiry, OptionTypeEnum.CALL, firstPayment)),
                    new Position(positionId, 1.,
                            new CommodityDigitalCash<>(underlyer, secondStrike, expiry, OptionTypeEnum.CALL,
                                    secondPayment - firstPayment)),
                    new Position(positionId, 1.,
                            new CommodityDigitalCash<>(underlyer, thirdStrike, expiry, OptionTypeEnum.CALL,
                                    thirdPayment - secondPayment))
            );
        }

        else {
            return Lists.newArrayList(
                    new Position(positionId, 1.,
                            new CommodityDigitalCash<>(underlyer, firstStrike, expiry, OptionTypeEnum.PUT, firstPayment)),
                    new Position(positionId, 1.,
                            new CommodityDigitalCash<>(underlyer, secondStrike, expiry, OptionTypeEnum.PUT,
                                    secondPayment - firstPayment)),
                    new Position(positionId, 1.,
                            new CommodityDigitalCash<>(underlyer, thirdStrike, expiry, OptionTypeEnum.PUT,
                                    thirdPayment - secondPayment))
            );
        }
    }

    @Override
    public Priceable getUnderlyer() {
        return this.underlyer;
    }
}
