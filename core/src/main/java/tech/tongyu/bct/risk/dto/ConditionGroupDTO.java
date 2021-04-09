package tech.tongyu.bct.risk.dto;

import java.util.Collection;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public class ConditionGroupDTO {

    private String conditionGroupId;
    private String conditionGroupName;
    private String description;
    private OperationEnum operation;
    private Collection<ConditionDTO> conditions;

    public ConditionGroupDTO(String conditionGroupId, String conditionGroupName, String description, OperationEnum operation) {
        this.conditionGroupId = conditionGroupId;
        this.conditionGroupName = conditionGroupName;
        this.description = description;
        this.operation = operation;
    }

    public String getConditionGroupId() {
        return conditionGroupId;
    }

    public void setConditionGroupId(String conditionGroupId) {
        this.conditionGroupId = conditionGroupId;
    }

    public String getConditionGroupName() {
        return conditionGroupName;
    }

    public void setConditionGroupName(String conditionGroupName) {
        this.conditionGroupName = conditionGroupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OperationEnum getOperation() {
        return operation;
    }

    public void setOperation(OperationEnum operation) {
        this.operation = operation;
    }

    public Collection<ConditionDTO> getConditions() {
        return conditions;
    }

    public void setConditions(Collection<ConditionDTO> conditions) {
        this.conditions = conditions;
    }

    @Override
    public String toString() {
        return "ConditionGroupDTO{" +
                "conditionGroupId='" + conditionGroupId + '\'' +
                ", conditionGroupName='" + conditionGroupName + '\'' +
                ", description='" + description + '\'' +
                ", operation=" + operation +
                ", conditions=" + conditions +
                '}';
    }
}
