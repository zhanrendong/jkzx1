package tech.tongyu.bct.rpc.json.http.server.dao.entity;

import tech.tongyu.bct.rpc.json.http.server.dao.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE, name = EntityConstants.ERROR_LOG)
public class ErrorLogDbo extends BaseEntity {

    @Column
    private String username;

    @Column(length = 4000)
    private String errorMessage;

    @Column
    private String requestMethod;

    @Column(length = 4000)
    private String requestParams;
    
    @Column(length = 4000)
    private String errorStackTrace;

    @Column
    private LocalDate createdDateAt;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

    public void setErrorStackTrace(String errorStackTrace) {
        this.errorStackTrace = errorStackTrace;
    }

    public LocalDate getCreatedDateAt() {
        return createdDateAt;
    }

    public void setCreatedDateAt(LocalDate createdDateAt) {
        this.createdDateAt = createdDateAt;
    }
}
