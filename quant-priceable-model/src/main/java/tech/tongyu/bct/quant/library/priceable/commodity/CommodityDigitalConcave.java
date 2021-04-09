package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.DigitalConcave;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.List;

public class CommodityDigitalConcave<U extends Commodity & ExchangeListed>
    extends DigitalConcave
    implements Commodity, Priceable, HasUnderlyer, Decomposable {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityDigitalConcave(U underlyer, LocalDateTime expiry, double lowStrike, double highStrike, double payment){
        super(expiry, lowStrike, highStrike, payment);
        this.underlyer = underlyer;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_DIGITAL_CONCAVE;
    }

    @Override
    public List<Position> decompose(String positionId) {
        return Lists.newArrayList(
                new Position(positionId, 1.,
                        new CommodityDigitalCash<>(underlyer, lowStrike, expiry, OptionTypeEnum.PUT, payment)),
                new Position(positionId, 1.,
                        new CommodityDigitalCash<>(underlyer, highStrike, expiry, OptionTypeEnum.CALL, payment))
        );
    }

    @Override
    public Priceable getUnderlyer() {
        return this.underlyer;
    }
}
