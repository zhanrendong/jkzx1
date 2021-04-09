package tech.tongyu.bct.workflow.dto;

import tech.tongyu.bct.workflow.process.enums.SymbolEnum;

import java.util.Map;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public class ConditionDTO {

    public ConditionDTO(String conditionId, String description, IndexDTO leftIndex,
                        IndexDTO rightIndex, Map<String,Object> leftValue, Map<String,Object> rightValue, SymbolEnum symbol) {
        this.conditionId = conditionId;
        this.description = description;
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.symbol = symbol;
    }

    private String conditionId;
    private String description;
    private IndexDTO leftIndex;
    private IndexDTO rightIndex;
    private Map<String,Object> leftValue;
    private Map<String,Object> rightValue;
    private SymbolEnum symbol;

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IndexDTO getLeftIndex() {
        return leftIndex;
    }

    public void setLeftIndex(IndexDTO leftIndex) {
        this.leftIndex = leftIndex;
    }

    public IndexDTO getRightIndex() {
        return rightIndex;
    }

    public void setRightIndex(IndexDTO rightIndex) {
        this.rightIndex = rightIndex;
    }

    public SymbolEnum getSymbol() {
        return symbol;
    }

    public void setSymbol(SymbolEnum symbol) {
        this.symbol = symbol;
    }

    public Map<String,Object> getLeftValue() {
        return leftValue;
    }

    public void setLeftValue(Map<String,Object> leftValue) {
        this.leftValue = leftValue;
    }

    public Map<String,Object> getRightValue() {
        return rightValue;
    }

    public void setRightValue(Map<String,Object> rightValue) {
        this.rightValue = rightValue;
    }

    @Override
    public String toString() {
        return "ConditionDTO{" +
                "conditionId='" + conditionId + '\'' +
                ", description='" + description + '\'' +
                ", leftIndex=" + leftIndex +
                ", rightIndex=" + rightIndex +
                ", leftValue=" + leftValue +
                ", rightValue=" + rightValue +
                ", symbol=" + symbol +
                '}';
    }
}
