package tech.tongyu.bct.auth.controller.response;

import com.google.common.collect.Maps;
import tech.tongyu.bct.common.util.JsonUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RpcResponse {
    private RpcReturnCode rpcReturnCode;
    private Integer serviceId;

    public RpcReturnCode getRpcReturnCode() {
        return rpcReturnCode;
    }

    protected RpcResponse(RpcReturnCode rpcReturnCode, Integer serviceId){
        this.rpcReturnCode = rpcReturnCode;
        this.serviceId = serviceId;
    }

    protected Map<String, Object> toMap(){
        Map<String, Object> result = Maps.newHashMap();
        result.put(RpcConstants.JSON_RPC_RESPONSE_SERVICE_ID, serviceId);
        result.put(RpcConstants.JSON_RPC_RESPONSE_CODE, rpcReturnCode.getCode());
        return result;
    }

    public Boolean isSuccess(){
        return getRpcReturnCode().isSuccess();
    }

    public Integer getServiceId() {
        return serviceId;
    }

    @Override
    public String toString(){
        return JsonUtils.toJson(this.toMap());
    }

    /**
     * get response info from string
     * @param source source String
     * @return RpcResponse
     */
    @SuppressWarnings("unchecked")
    public static <T> RpcResponse fromString(String source){
        Map<String, Object> map = JsonUtils.fromJson(source);
        RpcReturnCode rpcReturnCode = RpcReturnCode.of(
                (Integer) map.get(RpcConstants.JSON_RPC_RESPONSE_CODE));

        Integer serviceId = (Integer) map.get(RpcConstants.JSON_RPC_RESPONSE_SERVICE_ID);
        if(rpcReturnCode.isSuccess()){
            if(!Objects.isNull(map.get(RpcConstants.JSON_RPC_RESPONSE_DIAGNOSTICS))){
                return new RpcDiagnosticsResponse<>(serviceId
                        , map.get(RpcConstants.JSON_RPC_RESPONSE_RESULT)
                        , map.get(RpcConstants.JSON_RPC_RESPONSE_DIAGNOSTICS)
                );
            }

            if(!Objects.isNull(map.get(RpcConstants.JSON_RPC_RESPONSE_ERR))){
                return new RpcParallelResponse(serviceId
                        , (List) map.get(RpcConstants.JSON_RPC_RESPONSE_RESULT)
                        , (List) map.get(RpcConstants.JSON_RPC_RESPONSE_ERR)
                );
            }

            return new RpcSuccessResponse<>(serviceId
                    , (T) map.get(RpcConstants.JSON_RPC_RESPONSE_RESULT));
        } else
            return new RpcErrorResponse<T>(rpcReturnCode, serviceId
                    , (String) ((Map) map.get(RpcConstants.JSON_RPC_RESPONSE_ERR)).get(RpcConstants.JSON_RPC_RESPONSE_ERR_CODE)
                    , (T) ((Map) map.get(RpcConstants.JSON_RPC_RESPONSE_ERR)).get(RpcConstants.JSON_RPC_RESPONSE_ERR_MESSAGE)
            );
    }
}
