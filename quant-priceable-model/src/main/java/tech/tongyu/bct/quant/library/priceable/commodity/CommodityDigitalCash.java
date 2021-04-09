package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.DigitalCash;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CommodityDigitalCash<U extends Commodity & ExchangeListed>
        extends DigitalCash
        implements Commodity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    @JsonCreator
    public CommodityDigitalCash(U underlyer,
                                double strike, LocalDateTime expiry,
                                OptionTypeEnum optionType,
                                double payment, LocalDate deliveryDate) {
        super(strike,  expiry, optionType, payment, deliveryDate);
        this.underlyer = underlyer;
    }

    public CommodityDigitalCash(U underlyer,
                                double strike, LocalDateTime expiry,
                                OptionTypeEnum optionType, double payment) {
        super(strike,  expiry, optionType, payment);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_DIGITAL_CASH;
    }

    @Override
    public U getUnderlyer() {
        return underlyer;
    }
}
