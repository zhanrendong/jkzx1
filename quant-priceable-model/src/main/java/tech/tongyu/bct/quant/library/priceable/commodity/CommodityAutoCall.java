package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.AutoCall;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CommodityAutoCall<U extends Commodity & ExchangeListed>
        extends AutoCall
        implements Commodity, HasUnderlyer, Priceable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    @JsonCreator

    public CommodityAutoCall(U underlyer,
                             LocalDateTime expiry, List<LocalDate> observationDates,
                             BarrierDirectionEnum direction, List<Double> barriers,
                             List<LocalDate> paymentDates, List<Double> payments,
                             boolean finalFixedPayment,
                             LocalDate finalPaymentDate,
                             double finalPayment,
                             OptionTypeEnum finalOptionType, double finalOptionStrike,
                             boolean knockedOut, double knockedOutPayment, LocalDate knockedOutPaymentDate) {
        super(expiry, observationDates, direction, barriers, paymentDates, payments, finalFixedPayment,
                finalPaymentDate, finalPayment, finalOptionType, finalOptionStrike,
                knockedOut, knockedOutPayment, knockedOutPaymentDate);
        this.underlyer = underlyer;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_AUTOCALL;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
