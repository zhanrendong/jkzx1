package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.DigitalCash;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EquityDigitalCash<U extends Equity & ExchangeListed>
    extends DigitalCash
    implements Equity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    @JsonCreator
    public EquityDigitalCash(U underlyer,
                             double strike, LocalDateTime expiry,
                             OptionTypeEnum optionType,
                             double payment, LocalDate deliveryDate) {
        super(strike,  expiry, optionType, payment, deliveryDate);
        this.underlyer = underlyer;
    }

    public EquityDigitalCash(U underlyer,
                             double strike, LocalDateTime expiry,
                             OptionTypeEnum optionType,
                             double payment) {
        super(strike,  expiry, optionType, payment);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_DIGITAL_CASH;
    }

    @Override
    public U getUnderlyer() {
        return underlyer;
    }
}
