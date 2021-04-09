package tech.tongyu.bct.common.api.request;

import java.util.Map;

/**
 * json rpc 请求需满足如下格式：
 * {
 *     "jsonrpc" :"2.0" // 不可改
 *     "id": "1.0", // 目前未使用
 *     "method": (被调用API名),
 *     "params": (调用参数) // json 可为空
 * }
 *
 * @author Lu Lu
 */
public class JsonRpcRequest {
    private String method;
    private Map<String, Object> params;

    public JsonRpcRequest() {
    }

    public JsonRpcRequest(String method, Map<String, Object> params) {
        this.method = method;
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "JsonRpcRequest{" +
                "method='" + method + '\'' +
                ", params=" + params +
                '}';
    }
}
