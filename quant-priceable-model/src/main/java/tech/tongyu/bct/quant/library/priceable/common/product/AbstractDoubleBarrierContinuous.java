package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.ImmediateExercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 有两个障碍的期权结构的抽象类。两个障碍的障碍类型和补偿类型总是相同，高障碍方向总是向上，低障碍方向总是向下。
 */
public abstract class AbstractDoubleBarrierContinuous implements HasExpiry, ImmediateExercise {
    protected final LocalDateTime expiry;
    protected final double lowBarrier;
    protected final double highBarrier;
    protected final BarrierTypeEnum barrierType;
    protected final double lowRebate;
    protected final double highRebate;
    protected final RebateTypeEnum rebateType;

    public AbstractDoubleBarrierContinuous(
            LocalDateTime expiry, double lowBarrier, double highBarrier, BarrierTypeEnum barrierType,
            double lowRebate, double highRebate, RebateTypeEnum rebateType) {
        this.expiry = expiry;
        this.lowBarrier = lowBarrier;
        this.highBarrier = highBarrier;
        this.barrierType = barrierType;
        this.lowRebate = lowRebate;
        this.highRebate = highRebate;
        this.rebateType = rebateType;
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return expiry.toLocalDate();
    }

    @Override
    public LocalDateTime getExpiry() {
        return expiry;
    }

    public double getLowBarrier() {
        return lowBarrier;
    }

    public double getHighBarrier() {
        return highBarrier;
    }

    public BarrierTypeEnum getBarrierType() {
        return barrierType;
    }

    public double getLowRebate() {
        return lowRebate;
    }

    public double getHighRebate() {
        return highRebate;
    }

    public RebateTypeEnum getRebateType() {
        return rebateType;
    }
}
