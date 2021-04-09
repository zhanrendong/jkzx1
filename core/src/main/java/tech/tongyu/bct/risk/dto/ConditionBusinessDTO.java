package tech.tongyu.bct.risk.dto;

import java.util.Map;

public class ConditionBusinessDTO {
    public ConditionBusinessDTO(String description, String leftIndex, String rightIndex,
                                Map<String, Object> leftValue, Map<String, Object> rightValue,
                                String conditionId, String symbol) {
        this.description = description;
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.conditionId = conditionId;
        this.symbol = symbol;
    }

    private String description;
    private String leftIndex;
    private String rightIndex;
    private Map<String, Object> leftValue;
    private Map<String, Object> rightValue;
    private String conditionId;
    private String symbol;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLeftIndex() {
        return leftIndex;
    }

    public void setLeftIndex(String leftIndex) {
        this.leftIndex = leftIndex;
    }

    public String getRightIndex() {
        return rightIndex;
    }

    public void setRightIndex(String rightIndex) {
        this.rightIndex = rightIndex;
    }

    public Map<String, Object> getLeftValue() {
        return leftValue;
    }

    public void setLeftValue(Map<String, Object> leftValue) {
        this.leftValue = leftValue;
    }

    public Map<String, Object> getRightValue() {
        return rightValue;
    }

    public void setRightValue(Map<String, Object> rightValue) {
        this.rightValue = rightValue;
    }

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
