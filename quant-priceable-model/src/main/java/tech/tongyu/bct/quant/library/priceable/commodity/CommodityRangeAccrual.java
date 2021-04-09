package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.vavr.Tuple2;
import tech.tongyu.bct.quant.library.priceable.Commodity;
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

public class CommodityRangeAccrual<U extends Commodity & ExchangeListed>
        extends RangeAccrual
        implements Commodity, Priceable, HasUnderlyer, Decomposable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityRangeAccrual(U underlyer, LocalDateTime expiry, double lowBarrier, double highBarrier,
                                 double maxPayment, List<Tuple2<LocalDateTime, Double>> observationDates,
                                 Map<LocalDateTime, Double> fixings) {
        super(expiry, lowBarrier, highBarrier, maxPayment, observationDates, fixings);
        this.underlyer = underlyer;
    }

    @Override
    public List<Position> decompose(String positionId) {
        return decompose().stream()
                .map(p -> new Position(positionId, p._1, new CommodityDigitalCash<>(underlyer, p._2.getStrike(),
                        p._2.getExpiry(), p._2.getOptionType(), p._2.getPayment(), p._2.getDeliveryDate())))
                .collect(Collectors.toList());
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_RANGE_ACCRUAL;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
