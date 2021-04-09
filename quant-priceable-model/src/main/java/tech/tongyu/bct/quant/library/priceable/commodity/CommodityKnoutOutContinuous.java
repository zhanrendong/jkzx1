package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.ObservationTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.KnockOutContinuous;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;

public class CommodityKnoutOutContinuous<U extends Commodity & ExchangeListed>
        extends KnockOutContinuous
        implements Commodity, Priceable, HasUnderlyer {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityKnoutOutContinuous(U underlyer, LocalDateTime expiry, double strike, OptionTypeEnum optionType,
                                       double barrier, BarrierDirectionEnum barrierDirection, double rebate, RebateTypeEnum rebateType,
                                       ObservationTypeEnum observationType, double daysInYear) {
        super(expiry, strike, optionType, barrier, barrierDirection, rebate, rebateType, observationType, daysInYear);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_KNOCK_OUT_CONTINUOUS;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
