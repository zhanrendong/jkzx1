

package tech.tongyu.bct.rpc.json.http.server.dao.entity;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.rpc.json.http.server.dao.common.BaseEntity;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * 系统日志
 *
 * @author yyh
 */

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE, name = EntityConstants.SYS_LOG)
public class SysLogDbo extends BaseEntity {

    /**
     * 用户名
     */
    @Column
    private String username;
    /**
     * 用户操作
     */
    @Column
    private String operation;
    /**
     * 请求service
     */
    @Column
    private String service;

    /**
     * 请求方法
     */
    @Column
    private String method;
    /**
     * 请求参数
     */
    @Convert(converter = JsonConverter.class)
    @Column(length = JsonConverter.LENGTH)
    private JsonNode params;
    /**
     * 执行时长(毫秒)
     */
    @Column
    private Long executionTimeInMillis;

    @Column
    private LocalDate createdDateAt;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
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

    public JsonNode getParams() {
        return params;
    }

    public void setParams(JsonNode params) {
        this.params = params;
    }

    public Long getExecutionTimeInMillis() {
        return executionTimeInMillis;
    }

    public void setExecutionTimeInMillis(Long executionTimeInMillis) {
        this.executionTimeInMillis = executionTimeInMillis;
    }

    public LocalDate getCreatedDateAt() {
        return createdDateAt;
    }

    public void setCreatedDateAt(LocalDate createdDateAt) {
        this.createdDateAt = createdDateAt;
    }
}
