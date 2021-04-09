package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.KnockOutTerminal;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum.DOWN;
import static tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum.UP;
import static tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum.CALL;
import static tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum.PUT;

public class EquityKnockOutTerminal<U extends Equity & ExchangeListed>
        extends KnockOutTerminal
        implements Equity, Priceable, HasUnderlyer, Decomposable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public EquityKnockOutTerminal(U underlyer, LocalDateTime expiry, double strike, OptionTypeEnum optionType,
            double barrier, BarrierDirectionEnum barrierDirection, double rebate) {
        super(expiry, strike, optionType, barrier, barrierDirection, rebate);
        this.underlyer = underlyer;
    }

    @Override
    public List<Position> decompose(String positionId) {
        List<Position> positions = new ArrayList<>();
        if (optionType == CALL && barrierDirection == UP && barrier > strike) {
            EquityVanillaEuropean<U> vanillaEuropeanStrike =
                    new EquityVanillaEuropean<>(underlyer, strike, expiry, CALL);
            EquityVanillaEuropean<U> vanillaEuropeanBarrier =
                    new EquityVanillaEuropean<>(underlyer, barrier, expiry, CALL);
            EquityDigitalCash<U> digitalCash =
                    new EquityDigitalCash<>(underlyer, barrier, expiry, CALL, barrier - strike - rebate);
            positions.addAll(Arrays.asList(
                    new Position(positionId, 1.0, vanillaEuropeanStrike),
                    new Position(positionId, -1.0, vanillaEuropeanBarrier),
                    new Position(positionId, -1.0, digitalCash)));
        } else if (optionType == PUT && barrierDirection == DOWN && barrier < strike) {
            EquityVanillaEuropean<U> vanillaEuropeanStrike =
                    new EquityVanillaEuropean<>(underlyer, strike, expiry, PUT);
            EquityVanillaEuropean<U> vanillaEuropeanBarrier =
                    new EquityVanillaEuropean<>(underlyer, barrier, expiry, PUT);
            EquityDigitalCash<U> digitalCash =
                    new EquityDigitalCash<>(underlyer, barrier, expiry, PUT, strike - barrier - rebate);
            positions.addAll(Arrays.asList(
                    new Position(positionId, 1.0, vanillaEuropeanStrike),
                    new Position(positionId, -1.0, vanillaEuropeanBarrier),
                    new Position(positionId, -1.0, digitalCash)));
        } else if (optionType == CALL && barrierDirection == DOWN && barrier <= strike) {
            EquityVanillaEuropean<U> vanillaEuropean =
                    new EquityVanillaEuropean<>(underlyer, strike, expiry, CALL);
            positions.add(new Position(positionId, 1.0, vanillaEuropean));
            if (rebate != 0) {
                EquityDigitalCash<U> rebateDigital =
                        new EquityDigitalCash<>(underlyer, barrier, expiry, PUT, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else if (optionType == PUT && barrierDirection == UP && barrier >= strike) {
            EquityVanillaEuropean<U> vanillaEuropean =
                    new EquityVanillaEuropean<>(underlyer, strike, expiry, PUT);
            positions.add(new Position(positionId, 1.0, vanillaEuropean));
            if (rebate != 0) {
                EquityDigitalCash<U> rebateDigital =
                        new EquityDigitalCash<>(underlyer, barrier, expiry, CALL, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else if (optionType == CALL && barrierDirection == DOWN && barrier > strike) {
            EquityVanillaEuropean<U> vanillaEuropean =
                    new EquityVanillaEuropean<>(underlyer, barrier, expiry, CALL);
            EquityDigitalCash<U> digitalCash =
                    new EquityDigitalCash<>(underlyer, barrier, expiry, CALL, barrier - strike);
            positions.addAll(Arrays.asList(
                    new Position(positionId, 1.0, vanillaEuropean),
                    new Position(positionId, 1.0, digitalCash)));
            if (rebate != 0) {
                EquityDigitalCash<U> rebateDigital =
                        new EquityDigitalCash<>(underlyer, barrier, expiry, PUT, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else if (optionType == PUT && barrierDirection == UP && barrier < strike) {
            EquityVanillaEuropean<U> vanillaEuropean =
                    new EquityVanillaEuropean<>(underlyer, barrier, expiry, PUT);
            EquityDigitalCash<U> digitalCash =
                    new EquityDigitalCash<>(underlyer, barrier, expiry, PUT, strike - barrier);
            positions.addAll(Arrays.asList(
                    new Position(positionId, 1.0, vanillaEuropean),
                    new Position(positionId, 1.0, digitalCash)));
            if (rebate != 0) {
                EquityDigitalCash<U> rebateDigital =
                        new EquityDigitalCash<>(underlyer, barrier, expiry, CALL, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else if (optionType == CALL && barrierDirection == UP && barrier <= strike) {
            if (rebate != 0) {
                EquityDigitalCash<U> rebateDigital =
                        new EquityDigitalCash<>(underlyer, barrier, expiry, CALL, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else {//optionType == PUT && barrierDirection == DOWN && barrier >= strike
            if (rebate != 0) {
                EquityDigitalCash<U> rebateDigital =
                        new EquityDigitalCash<>(underlyer, barrier, expiry, PUT, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        }
        return positions;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.EQUITY_KNOCK_OUT_TERMINAL;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
