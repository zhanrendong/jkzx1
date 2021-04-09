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
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABlE_NAME_$PROCESS_TRIGGER)
public class ProcessTriggerDbo extends BaseEntity {

    public ProcessTriggerDbo() {
    }

    public ProcessTriggerDbo(String processId, String triggerId) {
        this.processId = processId;
        this.triggerId = triggerId;
    }

    @Column
    private String processId;

    @Column
    private String triggerId;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }
}
