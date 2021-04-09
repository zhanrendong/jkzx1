package tech.tongyu.bct.workflow.process.repo.entities;

import tech.tongyu.bct.workflow.process.enums.OperationEnum;
import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.*;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABlE_NAME_$TRIGGER)
public class TriggerDbo extends BaseEntity {

    public TriggerDbo() {
    }

    public TriggerDbo(String triggerName, String description, OperationEnum operation) {
        this.triggerName = triggerName;
        this.description = description;
        this.operation = operation;
    }

    @Column
    private String triggerName;

    @Column
    private String description;

    @Column
    @Enumerated(EnumType.STRING)
    private OperationEnum operation;

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
}
