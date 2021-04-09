package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.vavr.Tuple2;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.product.RangeAccrual;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EquityRangeAccrual<U extends Equity & ExchangeListed>
        extends RangeAccrual
        implements Equity, Priceable, HasUnderlyer, Decomposable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityRangeAccrual(U underlyer, LocalDateTime expiry, double lowBarrier, double highBarrier,
                              double maxPayment, List<Tuple2<LocalDateTime, Double>> observationDates,
                              Map<LocalDateTime, Double> fixings) {
        super(expiry, lowBarrier, highBarrier, maxPayment, observationDates, fixings);
        this.underlyer = underlyer;
    }

    @Override
    public List<Position> decompose(String positionId) {
        return decompose().stream()
                .map(p -> new Position(positionId, p._1, new EquityDigitalCash<>(underlyer, p._2.getStrike(),
                        p._2.getExpiry(), p._2.getOptionType(), p._2.getPayment(), p._2.getDeliveryDate())))
                .collect(Collectors.toList());
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_RANGE_ACCRUAL;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
