package tech.tongyu.bct.workflow.process.repo.entities;

import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABLE_NAME_$PROCESS_CONFIG)
public class ProcessConfigDbo extends BaseEntity {

    @Column
    private String configName;

    @Column
    private String configNickName;

    @Column
    private String processId;

    @Column
    private Boolean status;

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getConfigNickName() {
        return configNickName;
    }

    public void setConfigNickName(String configNickName) {
        this.configNickName = configNickName;
    }
}
