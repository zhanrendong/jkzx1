package tech.tongyu.bct.workflow.process.repo.entities;

import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABlE_NAME_$TRIGGER_CONDITION)
public class TriggerConditionDbo extends BaseEntity {

    public TriggerConditionDbo() {
    }

    public TriggerConditionDbo(String triggerId, String conditionId) {
        this.triggerId = triggerId;
        this.conditionId = conditionId;
    }

    @Column
    private String triggerId;

    @Column
    private String conditionId;

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }
}
