package tech.tongyu.bct.workflow.process.repo.entities;

import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABLE_NAME_$REQUEST)
public class RequestDbo extends BaseEntity {

    public RequestDbo() {
    }

    public RequestDbo(String processId, String service, String method) {
        this.processId = processId;
        this.service = service;
        this.method = method;
    }

    @Column
    private String processId;

    @Column
    private String service;

    @Column
    private String method;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
