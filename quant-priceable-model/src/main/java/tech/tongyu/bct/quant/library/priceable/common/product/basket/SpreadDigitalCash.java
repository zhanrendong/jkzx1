package tech.tongyu.bct.quant.library.priceable.common.product.basket;

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

/**
 * Pays a fixed amount when w1 * S1 - w2 * S2 is above the strike (call) or below (put)
 */
public class SpreadDigitalCash implements Priceable, HasExpiry, HasBasketUnderlyer,
        HasSingleStrike, HasSingleDelivery {
    private final Priceable underlyer1;
    private final double weight1;
    private final Priceable underlyer2;
    private final double weight2;
    private final LocalDateTime expiry;
    private final double strike;
    private final OptionTypeEnum optionType;
    private final double payment;
    private final LocalDate deliveryDate;

    public SpreadDigitalCash(Priceable underlyer1, double weight1, Priceable underlyer2, double weight2,
                             LocalDateTime expiry, double strike, OptionTypeEnum optionType, double payment,
                             LocalDate deliveryDate) {
        this.underlyer1 = underlyer1;
        this.weight1 = weight1;
        this.underlyer2 = underlyer2;
        this.weight2 = weight2;
        this.expiry = expiry;
        this.strike = strike;
        this.optionType = optionType;
        this.payment = payment;
        this.deliveryDate = deliveryDate;
    }

    @Override
    public List<Priceable> underlyers() {
        return Arrays.asList(underlyer1, underlyer2);
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return expiry.toLocalDate();
    }

    public Priceable getUnderlyer1() {
        return underlyer1;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.SPREAD_DIGITAL_CASH;
    }

    public double getWeight1() {
        return weight1;
    }

    public Priceable getUnderlyer2() {
        return underlyer2;
    }

    public double getWeight2() {
        return weight2;
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

    public double getPayment() {
        return payment;
    }

    @Override
    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }
}
