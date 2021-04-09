package tech.tongyu.bct.market.dto;

import tech.tongyu.bct.quant.library.priceable.common.flag.ExerciseTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

import java.time.LocalDate;
import java.time.LocalTime;

public class InstrumentInfoDTO {
    private String instrumentId;
    private AssetClassEnum assetClass;
    private AssetSubClassEnum assetSubClass;
    private InstrumentTypeEnum instrumentType;
    private String name;
    private ExchangeEnum exchange;
    private Integer multiplier;
    private LocalDate maturity;
    private String unit;
    private String tradeUnit;
    private String tradeCategory;
    private String underlyerInstrumentId;
    private ExerciseTypeEnum exerciseType;
    private OptionTypeEnum optionType;
    private Double strike;
    private LocalDate expirationDate;
    private LocalTime expirationTime;


    public InstrumentInfoDTO() {
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public AssetClassEnum getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(AssetClassEnum assetClass) {
        this.assetClass = assetClass;
    }

    public InstrumentTypeEnum getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(InstrumentTypeEnum instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExchangeEnum getExchange() {
        return exchange;
    }

    public void setExchange(ExchangeEnum exchange) {
        this.exchange = exchange;
    }

    public Integer getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Integer multiplier) {
        this.multiplier = multiplier;
    }

    public LocalDate getMaturity() {
        return maturity;
    }

    public void setMaturity(LocalDate maturity) {
        this.maturity = maturity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getTradeUnit() {
        return tradeUnit;
    }

    public void setTradeUnit(String tradeUnit) {
        this.tradeUnit = tradeUnit;
    }

    public String getTradeCategory() {
        return tradeCategory;
    }

    public void setTradeCategory(String tradeCategory) {
        this.tradeCategory = tradeCategory;
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

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public AssetSubClassEnum getAssetSubClass() {
        return assetSubClass;
    }

    public void setAssetSubClass(AssetSubClassEnum assetSubClass) {
        this.assetSubClass = assetSubClass;
    }
}
