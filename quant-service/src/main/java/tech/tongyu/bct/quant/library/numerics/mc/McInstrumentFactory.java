package tech.tongyu.bct.quant.library.numerics.mc;

import io.vavr.API;
import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.numerics.mc.impl.instrument.McInstrumentAutoCallPhoenix;
import tech.tongyu.bct.quant.library.numerics.mc.impl.instrument.McInstrumentAutocall;
import tech.tongyu.bct.quant.library.numerics.mc.impl.instrument.McInstrumentEuropean;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

public class McInstrumentFactory {
    public static McInstrument from(Priceable priceable) {
        return API.Match(priceable).of(
                Case($(instanceOf(Forward.class)), v -> new McInstrumentEuropean(
                        v.getExpiry(),
                        LocalDateTime.of(v.getDeliveryDate(), LocalTime.MIDNIGHT),
                        s -> s - v.getStrike()
                )),
                Case($(instanceOf(Vanilla.class)), v -> new McInstrumentEuropean(
                        v.getExpiry(),
                        s -> v.getOptionType() == OptionTypeEnum.CALL ?
                                FastMath.max(s - v.getStrike(), 0.) :
                                FastMath.max(v.getStrike() - s, 0.))),
                Case($(instanceOf(DigitalCash.class)), v -> new McInstrumentEuropean(
                        v.getExpiry(), v.getDeliveryDate().atTime(v.getExpiry().toLocalTime()),
                        s -> v.getOptionType() == OptionTypeEnum.CALL ? (s >= v.getStrike() ? v.getPayment() : 0.0) :
                                (s <= v.getStrike() ? v.getPayment() : 0.0)
                )),
                Case($(instanceOf(AutoCall.class)), v -> new McInstrumentAutocall(v.getExpiry(),
                        v.getObservationDates(), v.getDirection(),
                        v.getBarriers(), v.getPayments(), v.getPaymentDates(),
                        v.isFinalPayFixed(), v.getFinalPaymentDate(), v.getFinalPayment(),
                        v.getFinalOptionType(), v.getFinalOptionStrike(),
                        v.knockedOut(), v.getKnockedOutPayment(), v.getKnockedOutPaymentDate())),
                Case($(instanceOf(AutoCallPhoenix.class)), v -> new McInstrumentAutoCallPhoenix(
                        v.getExpiry(), v.getDirection(), v.getObservationDates(), v.getObserved(),
                        v.getBarriers(), v.getCouponBarriers(), v.getCoupons(), v.getPaymentDates(),
                        v.getKnockInObservationDates(), v.getKnockInBarriers(),
                        v.isKnockedIn(), v.getKnockedInOptionType(), v.getKnockedInOptionStrike(),
                        v.getFinalPaymentDate())),
                Case($(), o -> {
                    throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                            String.format("定价：MC不支持产品类型 %s", priceable.getPriceableTypeEnum()));
                })
        );
    }
}
