package tech.tongyu.bct.workflow.dto;

import tech.tongyu.bct.workflow.process.enums.OperationEnum;

import java.util.Collection;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public class TriggerDTO {

    private String triggerId;
    private String triggerName;
    private String description;
    private OperationEnum operation;
    private Collection<ConditionDTO> conditions;

    public TriggerDTO(String triggerId, String triggerName, String description, OperationEnum operation) {
        this.triggerId = triggerId;
        this.triggerName = triggerName;
        this.description = description;
        this.operation = operation;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
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
        return "TriggerDTO{" +
                "triggerId='" + triggerId + '\'' +
                ", triggerName='" + triggerName + '\'' +
                ", description='" + description + '\'' +
                ", operation=" + operation +
                ", conditions=" + conditions +
                '}';
    }
}
