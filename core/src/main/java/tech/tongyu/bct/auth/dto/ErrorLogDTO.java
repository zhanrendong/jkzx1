package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.LocalDateTime;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

public class ErrorLogDTO {

    @BctField(description = "id")
    private String id;
    @BctField(description = "操作人")
    private String username;
    @BctField(description = "错误信息")
    private String errorMessage;
    @BctField(description = "请求方法")
    private String requestMethod;
    @BctField(description = "请求参数")
    private String requestParams;
    @BctField(description = "具体报错信息")
    private String errorStackTrace;
    @BctField(description = "创建时间")
    private LocalDateTime createdAt;

    public ErrorLogDTO() {
    }

    public ErrorLogDTO(String username, String errorMessage, String requestMethod, String requestParams, String errorStackTrace) {
        this.username = username;
        this.errorMessage = errorMessage;
        this.requestMethod = requestMethod;
        this.requestParams = requestParams;
        this.errorStackTrace = errorStackTrace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
