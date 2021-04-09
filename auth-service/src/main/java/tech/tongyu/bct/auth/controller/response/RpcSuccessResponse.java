package tech.tongyu.bct.auth.controller.response;

import java.util.Map;

public class RpcSuccessResponse<T> extends RpcResponse{

    private T result;

    public RpcSuccessResponse(Integer serviceId, T result){
        super(RpcReturnCode.SUCCESS, serviceId);
        this.result = result;
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> resultMap = super.toMap();
        resultMap.put(RpcConstants.JSON_RPC_RESPONSE_RESULT, this.result);
        return resultMap;
    }

    public T getResult() {
        return result;
    }

}
