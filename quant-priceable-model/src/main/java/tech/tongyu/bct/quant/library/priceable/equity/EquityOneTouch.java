package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.ObservationTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.OneTouch;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;

public class EquityOneTouch<U extends Equity & ExchangeListed>
        extends OneTouch
        implements Equity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityOneTouch(U underlyer, LocalDateTime expiry, double barrier,
                          BarrierDirectionEnum barrierDirection, double rebate, RebateTypeEnum rebateType,
                          ObservationTypeEnum observationType, double daysInYear) {
        super(expiry, barrier, barrierDirection, rebate, rebateType, observationType, daysInYear);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_ONE_TOUCH;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
