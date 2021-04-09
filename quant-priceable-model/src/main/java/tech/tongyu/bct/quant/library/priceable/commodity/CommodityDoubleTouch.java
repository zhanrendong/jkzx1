package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.DoubleTouch;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;

public class CommodityDoubleTouch<U extends Commodity & ExchangeListed>
        extends DoubleTouch
        implements Commodity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityDoubleTouch(
            U underlyer, LocalDateTime expiry, RebateTypeEnum rebateType, double lowBarrier,
            double highBarrier, double lowRebate, double highRebate, double noTouchRebate) {
        super(expiry, rebateType, lowBarrier, highBarrier, lowRebate, highRebate, noTouchRebate);
        this.underlyer = underlyer;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_DOUBLE_TOUCH;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
