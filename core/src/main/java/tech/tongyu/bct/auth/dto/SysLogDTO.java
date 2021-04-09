package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.LocalDateTime;


/**
 * 系统日志
 *
 * @author yyh
 */


public class SysLogDTO {
    @BctField(description = "日志唯一标识")
    private String uuid;
    @BctField(description = "用户名")
    private String username;
    @BctField(description = "用户操作")
    private String operation;
    @BctField(description = "服务名")
    private String service;
    @BctField(description = "请求方法")
    private String method;
    @BctField(description = "请求参数")
    private Object params;
    @BctField(description = "执行时长(毫秒)")
    private Long executionTimeInMillis;
    @BctField(description = "创建时间")
    private LocalDateTime createdAt;

    public SysLogDTO() {
    }

    public SysLogDTO(String username, String operation, String service, String method, Object params, Long executionTimeInMillis) {
        this.username = username;
        this.operation = operation;
        this.service = service;
        this.method = method;
        this.params = params;
        this.executionTimeInMillis = executionTimeInMillis;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

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

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public Long getExecutionTimeInMillis() {
        return executionTimeInMillis;
    }

    public void setExecutionTimeInMillis(Long executionTimeInMillis) {
        this.executionTimeInMillis = executionTimeInMillis;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
