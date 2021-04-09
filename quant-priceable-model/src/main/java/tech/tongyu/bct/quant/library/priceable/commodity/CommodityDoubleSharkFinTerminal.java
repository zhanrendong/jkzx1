package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Commodity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.DoubleSharkFinTerminal;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class CommodityDoubleSharkFinTerminal<U extends Commodity & ExchangeListed>
        extends DoubleSharkFinTerminal
        implements Commodity, Priceable, HasUnderlyer, Decomposable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityDoubleSharkFinTerminal(
            U underlyer, double lowStrike, double highStrike, LocalDateTime expiry,
            double lowBarrier, double highBarrier, BarrierTypeEnum barrierType, double lowRebate,
            double highRebate, double lowParticipationRate, double highParticipationRate) {
        super(lowStrike, highStrike, expiry, lowBarrier, highBarrier, barrierType, lowRebate, highRebate,
                lowParticipationRate, highParticipationRate);
        this.underlyer = underlyer;
    }

    @Override
    public List<Position> decompose(String positionId) {
        CommodityVanillaEuropean<U> put1 = new CommodityVanillaEuropean<>(
                underlyer, lowStrike, expiry, OptionTypeEnum.PUT);
        CommodityVanillaEuropean<U> put2 = new CommodityVanillaEuropean<>(
                underlyer, lowBarrier, expiry, OptionTypeEnum.PUT);
        CommodityDigitalCash<U> putDigi = new CommodityDigitalCash<>(underlyer, lowBarrier, expiry,
                OptionTypeEnum.PUT, lowStrike - lowBarrier - lowRebate);
        CommodityVanillaEuropean<U> call1 = new CommodityVanillaEuropean<>(
                underlyer, highStrike, expiry, OptionTypeEnum.CALL);
        CommodityVanillaEuropean<U> call2 = new CommodityVanillaEuropean<>(
                underlyer, highBarrier, expiry, OptionTypeEnum.CALL);
        CommodityDigitalCash<U> callDigi = new CommodityDigitalCash<>(underlyer, highBarrier, expiry,
                OptionTypeEnum.CALL, highBarrier - highStrike - highRebate);
        return Arrays.asList(
                new Position(positionId, lowParticipationRate, put1),
                new Position(positionId, -lowParticipationRate, put2),
                new Position(positionId, -lowParticipationRate, putDigi),
                new Position(positionId, highParticipationRate, call1),
                new Position(positionId, -highParticipationRate, call2),
                new Position(positionId, -highParticipationRate, callDigi));
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_DOUBLE_SHARK_FIN_TERMINAL;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
