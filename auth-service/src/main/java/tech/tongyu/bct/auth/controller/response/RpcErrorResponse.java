package tech.tongyu.bct.auth.controller.response;

import com.google.common.collect.Maps;

import java.util.Map;

public class RpcErrorResponse<T> extends RpcResponse{
    private final T msg;
    private final String detailedErrorCode;

    public T getMsg() {
        return msg;
    }

    public String getDetailedErrorCode() {
        return detailedErrorCode;
    }

    public RpcErrorResponse(RpcReturnCode code, Integer serviceId, String detailedErrorCode, T msg) {
        super(code, serviceId);
        this.msg = msg;
        this.detailedErrorCode = detailedErrorCode;
    }

    public Map<String,Object> toMap() {
        Map<String, Object> resultMap = super.toMap();

        Map<String, Object> errorMap = Maps.newHashMap();
        errorMap.put(RpcConstants.JSON_RPC_RESPONSE_ERR_CODE, this.detailedErrorCode);
        errorMap.put(RpcConstants.JSON_RPC_RESPONSE_ERR_MESSAGE, this.msg.toString());
        resultMap.put(RpcConstants.JSON_RPC_RESPONSE_ERR, errorMap);

        return resultMap;
    }
}
