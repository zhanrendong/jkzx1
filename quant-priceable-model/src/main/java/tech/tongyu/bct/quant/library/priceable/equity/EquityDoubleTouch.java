package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.DoubleTouch;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;

public class EquityDoubleTouch<U extends Equity & ExchangeListed>
        extends DoubleTouch
        implements Equity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityDoubleTouch(
            U underlyer, LocalDateTime expiry, RebateTypeEnum rebateType, double lowBarrier,
            double highBarrier, double lowRebate, double highRebate, double noTouchRebate) {
        super(expiry, rebateType, lowBarrier, highBarrier, lowRebate, highRebate, noTouchRebate);
        this.underlyer = underlyer;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_DOUBLE_TOUCH;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
