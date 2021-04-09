package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.product.Forward;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CommodityForward<U extends Commodity & ExchangeListed>
        extends Forward
        implements Commodity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityForward(U underlyer, double strike, LocalDateTime expiry, LocalDate deliveryDate) {
        super(expiry, deliveryDate, strike);
        this.underlyer = underlyer;
    }

    @Override
    public U getUnderlyer() {
        return underlyer;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_FORWARD;
    }
}
