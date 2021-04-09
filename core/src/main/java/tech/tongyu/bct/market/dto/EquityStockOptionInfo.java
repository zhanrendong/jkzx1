package tech.tongyu.bct.market.dto;

import tech.tongyu.bct.quant.library.priceable.common.flag.ExerciseTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 个股/ETF期权
 */
public class EquityStockOptionInfo extends InstrumentCommonInfo implements InstrumentInfo {

    /**
     * 标的代码
     */
    private String underlyerInstrumentId;
    /**
     * 合约乘数
     */
    private int multiplier;
    /**
     * 行权方式
     */
    private ExerciseTypeEnum exerciseType;
    /**
     * 期权类型
     */
    private OptionTypeEnum optionType;
    /**
     * strike
     */
    private Double strike;
    /**
     * 到期日
     */
    private LocalDate expirationDate;
    /**
     * 到期时间
     */
    private LocalTime expirationTime;

    @Override
    public Integer multiplier() {
        return multiplier;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    public void setUnderlyerInstrumentId(String underlyerInstrumentId) {
        this.underlyerInstrumentId = underlyerInstrumentId;
    }

    public ExerciseTypeEnum getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(ExerciseTypeEnum exerciseType) {
        this.exerciseType = exerciseType;
    }

    public OptionTypeEnum getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionTypeEnum optionType) {
        this.optionType = optionType;
    }

    public Double getStrike() {
        return strike;
    }

    public void setStrike(Double strike) {
        this.strike = strike;
    }

    public LocalTime getExpirationTime() {
        return expirationTime;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setExpirationTime(LocalTime expirationTime) {
        this.expirationTime = expirationTime;
    }
}
