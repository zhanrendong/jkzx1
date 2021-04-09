package tech.tongyu.bct.quant.library.priceable.commodity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.priceable.Commodity;
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

public class CommodityKnockOutTerminal<U extends Commodity & ExchangeListed>
        extends KnockOutTerminal
        implements Commodity, Priceable, HasUnderlyer, Decomposable {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final U underlyer;

    public CommodityKnockOutTerminal(U underlyer, LocalDateTime expiry, double strike, OptionTypeEnum optionType,
            double barrier, BarrierDirectionEnum barrierDirection, double rebate) {
        super(expiry, strike, optionType, barrier, barrierDirection, rebate);
        this.underlyer = underlyer;
    }

    @Override
    public List<Position> decompose(String positionId) {
        List<Position> positions = new ArrayList<>();
        if (optionType == CALL && barrierDirection == UP && barrier > strike) {
            CommodityVanillaEuropean<U> vanillaEuropeanStrike =
                    new CommodityVanillaEuropean<>(underlyer, strike, expiry, CALL);
            CommodityVanillaEuropean<U> vanillaEuropeanBarrier =
                    new CommodityVanillaEuropean<>(underlyer, barrier, expiry, CALL);
            CommodityDigitalCash<U> digitalCash =
                    new CommodityDigitalCash<>(underlyer, barrier, expiry, CALL, barrier - strike);
            positions.addAll(Arrays.asList(
                    new Position(positionId, 1.0, vanillaEuropeanStrike),
                    new Position(positionId, -1.0, vanillaEuropeanBarrier),
                    new Position(positionId, -1.0, digitalCash)));
            if (rebate != 0) {
                CommodityDigitalCash<U> rebateDigital =
                        new CommodityDigitalCash<>(underlyer, barrier, expiry, CALL, this.rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else if (optionType == PUT && barrierDirection == DOWN && barrier < strike) {
            CommodityVanillaEuropean<U> vanillaEuropeanStrike =
                    new CommodityVanillaEuropean<>(underlyer, strike, expiry, PUT);
            CommodityVanillaEuropean<U> vanillaEuropeanBarrier =
                    new CommodityVanillaEuropean<>(underlyer, barrier, expiry, PUT);
            CommodityDigitalCash<U> digitalCash =
                    new CommodityDigitalCash<>(underlyer, barrier, expiry, PUT, strike - barrier);
            positions.addAll(Arrays.asList(
                    new Position(positionId, 1.0, vanillaEuropeanStrike),
                    new Position(positionId, -1.0, vanillaEuropeanBarrier),
                    new Position(positionId, -1.0, digitalCash)));
            if (rebate != 0) {
                CommodityDigitalCash<U> rebateDigital =
                        new CommodityDigitalCash<>(underlyer, barrier, expiry, PUT, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else if (optionType == CALL && barrierDirection == DOWN && barrier <= strike) {
            CommodityVanillaEuropean<U> vanillaEuropean =
                    new CommodityVanillaEuropean<>(underlyer, strike, expiry, CALL);
            positions.add(new Position(positionId, 1.0, vanillaEuropean));
            if (rebate != 0) {
                CommodityDigitalCash<U> rebateDigital =
                        new CommodityDigitalCash<>(underlyer, barrier, expiry, PUT, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else if (optionType == PUT && barrierDirection == UP && barrier >= strike) {
            CommodityVanillaEuropean<U> vanillaEuropean =
                    new CommodityVanillaEuropean<>(underlyer, strike, expiry, PUT);
            positions.add(new Position(positionId, 1.0, vanillaEuropean));
            if (rebate != 0) {
                CommodityDigitalCash<U> rebateDigital =
                        new CommodityDigitalCash<>(underlyer, barrier, expiry, CALL, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else if (optionType == CALL && barrierDirection == DOWN && barrier > strike) {
            CommodityVanillaEuropean<U> vanillaEuropean =
                    new CommodityVanillaEuropean<>(underlyer, barrier, expiry, CALL);
            CommodityDigitalCash<U> digitalCash =
                    new CommodityDigitalCash<>(underlyer, barrier, expiry, CALL, barrier - strike);
            positions.addAll(Arrays.asList(
                    new Position(positionId, 1.0, vanillaEuropean),
                    new Position(positionId, 1.0, digitalCash)));
            if (rebate != 0) {
                CommodityDigitalCash<U> rebateDigital =
                        new CommodityDigitalCash<>(underlyer, barrier, expiry, PUT, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else if (optionType == PUT && barrierDirection == UP && barrier < strike) {
            CommodityVanillaEuropean<U> vanillaEuropean =
                    new CommodityVanillaEuropean<>(underlyer, barrier, expiry, PUT);
            CommodityDigitalCash<U> digitalCash =
                    new CommodityDigitalCash<>(underlyer, barrier, expiry, PUT, strike - barrier);
            positions.addAll(Arrays.asList(
                    new Position(positionId, 1.0, vanillaEuropean),
                    new Position(positionId, 1.0, digitalCash)));
            if (rebate != 0) {
                CommodityDigitalCash<U> rebateDigital =
                        new CommodityDigitalCash<>(underlyer, barrier, expiry, CALL, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else if (optionType == CALL && barrierDirection == UP && barrier <= strike) {
            if (rebate != 0) {
                CommodityDigitalCash<U> rebateDigital =
                        new CommodityDigitalCash<>(underlyer, barrier, expiry, CALL, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        } else {//optionType == PUT && barrierDirection == DOWN && barrier >= strike
            if (rebate != 0) {
                CommodityDigitalCash<U> rebateDigital =
                        new CommodityDigitalCash<>(underlyer, barrier, expiry, PUT, rebate);
                positions.add(new Position(positionId, 1.0, rebateDigital));
            }
        }
        return positions;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.COMMODITY_KNOCK_OUT_TERMINAL;
    }

    @Override
    public Priceable getUnderlyer() {
        return underlyer;
    }
}
