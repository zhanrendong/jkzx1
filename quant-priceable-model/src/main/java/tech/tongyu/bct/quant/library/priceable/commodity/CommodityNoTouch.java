package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.NoTouch;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.awt.geom.NoninvertibleTransformException;
import java.time.LocalDateTime;

public class CommodityNoTouch<U extends Commodity & ExchangeListed>
        extends NoTouch
        implements Commodity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityNoTouch(U underlyer, LocalDateTime expiry, double barrier,
            BarrierDirectionEnum barrierDirection, double payment) {
        super(expiry, barrier, barrierDirection, payment);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_NO_TOUCH;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
