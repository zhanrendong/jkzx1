package tech.tongyu.bct.quant.library.priceable.common.product.basket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasBasketUnderlyer;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleDelivery;
import tech.tongyu.bct.quant.library.priceable.feature.HasSingleStrike;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class RatioVanillaEuropean implements Priceable, HasExpiry, HasBasketUnderlyer,
        HasSingleStrike, HasSingleDelivery {

    private final Priceable underlyer1;
    private final Priceable underlyer2;
    private final LocalDateTime expiry;
    private final double strike;
    private final OptionTypeEnum optionType;
    private final LocalDate deliveryDate;

    public RatioVanillaEuropean(Priceable underlyer1, Priceable underlyer2,
                                LocalDateTime expiry, double strike, OptionTypeEnum optionType) {
        this.underlyer1 = underlyer1;
        this.underlyer2 = underlyer2;
        this.expiry = expiry;
        this.strike = strike;
        this.optionType = optionType;
        this.deliveryDate = expiry.toLocalDate();
    }

    @JsonCreator
    public RatioVanillaEuropean(Priceable underlyer1, Priceable underlyer2,
                                LocalDateTime expiry, double strike, OptionTypeEnum optionType,
                                LocalDate deliveryDate) {
        this.underlyer1 = underlyer1;
        this.underlyer2 = underlyer2;
        this.expiry = expiry;
        this.strike = strike;
        this.optionType = optionType;
        this.deliveryDate = deliveryDate;
    }

    @Override
    public List<Priceable> underlyers() {
        return Arrays.asList(underlyer1, underlyer2);
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return null;
    }

    public Priceable getUnderlyer1() {
        return underlyer1;
    }

    public Priceable getUnderlyer2() {
        return underlyer2;
    }

    @Override
    public LocalDateTime getExpiry() {
        return expiry;
    }

    @Override
    public double getStrike() {
        return strike;
    }

    public OptionTypeEnum getOptionType() {
        return optionType;
    }

    @Override
    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.RATIO_VANILLA_EUROPEAN;
    }
}
