package tech.tongyu.bct.common.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Json rpc 返回需满足如下格式：
 * 调用成功: {
 * "jsonrpc" :"2.0" // 不可改
 * "id": "1.0", // 目前未使用
 * "result": (调用结果) // json
 * "diagnostics": (诊断信息) // json
 * }
 * 调用失败: {
 * "jsonrpc" :"2.0" // 不可改
 * "id": "1.0", // 目前未使用
 * "error": {
 * "code": (错误代码), // {@link ErrorCode}
 * "message": (错误信息)
 * }
 * }
 * 调用只能成功（存在result字段）或者失败（存在error字段）。诊断信息（diagnostics）非必须，
 * 其主要目的是为了返回额外信息，比如调用中出现的警告，部分失败的定价等。
 *
 * @param <R> 调用成功结果
 * @param <D> 诊断信息
 */
public class JsonRpcResponse<R, D> {
    public enum ErrorCode {
        RUNTIME_ERROR(1),
        MALFORMED_INPUT(100),
        METHOD_NOT_FOUND(101),
        MISSING_PARAM(102),
        CAPTCHA_INVALID(103),
        USERNAME_OR_PASSWORD_INVALID(104),
        TOKEN_INVALID(107);

        private int code;

        @JsonValue
        public int getCode() {
            return code;
        }

        ErrorCode(int code) {
            this.code = code;
        }
    }

    public static class Error {
        private final ErrorCode code;
        private final String message;

        Error(ErrorCode code, String message) {
            this.code = code;
            this.message = message;
        }

        public ErrorCode getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    private final String jsonrpc = "2.0";
    private final String id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final R result;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final D diagnostics;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Error error;

    public JsonRpcResponse(R result) {
        this.id = "1";
        this.result = result;
        this.diagnostics = null;
        this.error = null;
    }

    public JsonRpcResponse(R result, D diagnostics) {
        this.id = "1";
        this.result = result;
        this.diagnostics = diagnostics;
        this.error = null;
    }

    public JsonRpcResponse(ErrorCode code, String message) {
        this.id = "1";
        this.result = null;
        this.diagnostics = null;
        this.error = new Error(code, message);
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public String getId() {
        return id;
    }

    public R getResult() {
        return result;
    }

    public D getDiagnostics() {
        return diagnostics;
    }

    public Error getError() {
        return error;
    }
}
