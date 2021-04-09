package tech.tongyu.bct.risk.repo.entities;

import tech.tongyu.bct.risk.dto.OperationEnum;

import javax.persistence.*;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABlE_NAME_$CONDITION_GROUP)
public class ConditionGroupDbo extends BaseEntity {

    public ConditionGroupDbo() {
    }

    public ConditionGroupDbo(String conditionGroupName, String description, OperationEnum operation) {
        this.conditionGroupName = conditionGroupName;
        this.description = description;
        this.operation = operation;
    }

    @Column
    private String conditionGroupName;

    @Column
    private String description;

    @Column
    @Enumerated(EnumType.STRING)
    private OperationEnum operation;

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
}
