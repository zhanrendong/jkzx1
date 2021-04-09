package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.commodity.CommodityDigitalCash;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.DigitalConvex;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.List;

public class EquityDigitalConvex<U extends Equity & ExchangeListed>
    extends DigitalConvex
    implements Equity, Priceable, HasUnderlyer, Decomposable {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityDigitalConvex(U underlyer, LocalDateTime expiry, double lowStrike, double highStrike, double payment){
        super(expiry, lowStrike, highStrike, payment);
        this.underlyer = underlyer;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_DIGITAL_CONVEX;
    }

    @Override
    public List<Position> decompose(String positionId) {
        return Lists.newArrayList(
                new Position(positionId, 1.,
                        new EquityDigitalCash<>(underlyer, lowStrike, expiry, OptionTypeEnum.CALL, payment)),
                new Position(positionId, -1.,
                        new EquityDigitalCash<>(underlyer, highStrike, expiry, OptionTypeEnum.CALL, payment))
        );
    }

    @Override
    public Priceable getUnderlyer() {
        return this.underlyer;
    }
}
