package tech.tongyu.bct.workflow.process.repo.entities;

import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author david.yang
 *  - mailto: yangywiei@tongyu.tech
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABLE_NAME_$PROCESS)
public class ProcessDbo extends BaseEntity {

    public ProcessDbo() {
    }

    public ProcessDbo(String processName, Boolean status) {
        this.processName = processName;
        this.status = status;
    }

    @Column
    private String processName;

    @Column
    private Boolean status;

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
