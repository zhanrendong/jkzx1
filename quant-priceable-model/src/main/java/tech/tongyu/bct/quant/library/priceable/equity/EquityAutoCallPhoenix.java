package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.AutoCallPhoenix;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EquityAutoCallPhoenix<U extends Equity & ExchangeListed>
        extends AutoCallPhoenix
        implements Equity, HasUnderlyer, Priceable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityAutoCallPhoenix(U underlyer,
                                 LocalDateTime expiry,
                                 List<LocalDate> observationDates, List<Double> observed,
                                 BarrierDirectionEnum direction,
                                 List<Double> barriers, List<Double> couponBarriers,
                                 List<LocalDate> paymentDates, List<Double> coupons,
                                 List<LocalDate> knockInObservationDates,
                                 List<Double> knockInBarriers,
                                 boolean knockedIn,
                                 OptionTypeEnum knockedInOptionType, double knockedInOptionStrike,
                                 LocalDate finalPaymentDate) {
        super(expiry,
                observationDates, observed,
                direction, barriers, couponBarriers,
                paymentDates, coupons,
                knockInObservationDates,knockInBarriers,
                knockedIn,
                knockedInOptionType, knockedInOptionStrike, finalPaymentDate);
        this.underlyer = underlyer;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_AUTOCALL;
    }

    @Override
    public U getUnderlyer() {
        return underlyer;
    }
}
