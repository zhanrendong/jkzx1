package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Equity;
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

public class EquityDoubleSharkFinTerminal<U extends Equity & ExchangeListed>
        extends DoubleSharkFinTerminal
        implements Equity, Priceable, HasUnderlyer, Decomposable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityDoubleSharkFinTerminal(
            U underlyer, double lowStrike, double highStrike, LocalDateTime expiry,
            double lowBarrier, double highBarrier, BarrierTypeEnum barrierType, double lowRebate,
            double highRebate, double lowParticipationRate, double highParticipationRate) {
        super(lowStrike, highStrike, expiry, lowBarrier, highBarrier, barrierType, lowRebate, highRebate,
                lowParticipationRate, highParticipationRate);
        this.underlyer = underlyer;
    }

    @Override
    public List<Position> decompose(String positionId) {
        EquityVanillaEuropean<U> put1 = new EquityVanillaEuropean<>(
                underlyer, lowStrike, expiry, OptionTypeEnum.PUT);
        EquityVanillaEuropean<U> put2 = new EquityVanillaEuropean<>(
                underlyer, lowBarrier, expiry, OptionTypeEnum.PUT);
        EquityDigitalCash<U> putDigi = new EquityDigitalCash<>(underlyer, lowBarrier, expiry,
                OptionTypeEnum.PUT, lowStrike - lowBarrier - lowRebate);
        EquityVanillaEuropean<U> call1 = new EquityVanillaEuropean<>(
                underlyer, highStrike, expiry, OptionTypeEnum.CALL);
        EquityVanillaEuropean<U> call2 = new EquityVanillaEuropean<>(
                underlyer, highBarrier, expiry, OptionTypeEnum.CALL);
        EquityDigitalCash<U> callDigi = new EquityDigitalCash<>(underlyer, highBarrier, expiry,
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
        return PriceableTypeEnum.EQUITY_DOUBLE_SHARK_FIN_TERMINAL;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
