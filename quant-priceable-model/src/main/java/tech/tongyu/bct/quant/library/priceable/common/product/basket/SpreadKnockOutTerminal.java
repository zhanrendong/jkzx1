package tech.tongyu.bct.quant.library.priceable.common.product.basket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * European knock-out on spread w1 * S1 - w2 * S2
 */
public class SpreadKnockOutTerminal implements Priceable, HasExpiry, HasBasketUnderlyer,
        HasSingleStrike, HasSingleDelivery, Decomposable {
    private final Priceable underlyer1;
    private final double weight1;
    private final Priceable underlyer2;
    private final double weight2;
    private final LocalDateTime expiry;
    private final double strike;
    private final OptionTypeEnum optionType;
    private final double barrier;
    private final BarrierDirectionEnum direction;
    private final double rebate;
    private final LocalDate deliveryDate;

    public SpreadKnockOutTerminal(Priceable underlyer1, double weight1, Priceable underlyer2, double weight2,
                                  LocalDateTime expiry, double strike, OptionTypeEnum optionType,
                                  double barrier, BarrierDirectionEnum direction,
                                  double rebate,
                                  LocalDate deliveryDate) {
        this.underlyer1 = underlyer1;
        this.weight1 = weight1;
        this.underlyer2 = underlyer2;
        this.weight2 = weight2;
        this.expiry = expiry;
        this.strike = strike;
        this.optionType = optionType;
        this.barrier = barrier;
        this.direction = direction;
        this.rebate = rebate;
        this.deliveryDate = deliveryDate;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.SPREAD_KNOCK_OUT_TERMINAL;
    }

    @Override
    public List<Position> decompose(String positionId) {
        List<Position> ret = new ArrayList<>();
        if ((optionType == OptionTypeEnum.CALL && direction == BarrierDirectionEnum.UP && barrier >= strike) ||
                (optionType == OptionTypeEnum.PUT && direction == BarrierDirectionEnum.DOWN && barrier <= strike)) {
            ret.add(new Position(positionId, 1.0,
                    new SpreadVanillaEuropean(underlyer1, weight1, underlyer2, weight2,
                            expiry, strike, optionType, deliveryDate)));
            ret.add(new Position(positionId, -1.0,
                    new SpreadVanillaEuropean(underlyer1, weight1, underlyer2, weight2,
                            expiry, barrier, optionType, deliveryDate)));
            ret.add(new Position(positionId, -1.0,
                    new SpreadDigitalCash(underlyer1, weight1, underlyer2, weight2,
                            expiry, barrier, optionType,
                            optionType == OptionTypeEnum.CALL ? barrier - strike - rebate
                                    : strike - barrier - rebate, deliveryDate)));
        }  else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "价差欧式敲出仅支持单鲨结构");
        }
        return ret;
    }

    @Override
    public List<Priceable> underlyers() {
        return Arrays.asList(underlyer1, underlyer2);
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return expiry.toLocalDate();
    }

    public Priceable getUnderlyer1() {
        return underlyer1;
    }

    public double getWeight1() {
        return weight1;
    }

    public Priceable getUnderlyer2() {
        return underlyer2;
    }

    public double getWeight2() {
        return weight2;
    }

    @Override
    public LocalDateTime getExpiry() {
        return expiry;
    }

    @Override
    public double getStrike() {
        return strike;
    }

    public OptionTypeEnum getOptionType() {
        return optionType;
    }

    public double getBarrier() {
        return barrier;
    }

    public BarrierDirectionEnum getDirection() {
        return direction;
    }

    public double getRebate() {
        return rebate;
    }

    @Override
    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }
}
