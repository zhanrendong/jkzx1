package tech.tongyu.bct.auth.controller.response;

import java.util.List;
import java.util.Map;

public class RpcParallelResponse extends RpcResponse {

    private List<Object> results;
    private List<Object> errors;

    public RpcParallelResponse(Integer serviceId, List<Object> results, List<Object> errors) {
        super(RpcReturnCode.SUCCESS, serviceId);
        this.results = results;
        this.errors = errors;
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put(RpcConstants.JSON_RPC_RESPONSE_RESULT, this.results);
        result.put(RpcConstants.JSON_RPC_RESPONSE_ERR, this.errors);
        return result;
    }

    public List<Object> getResults() {
        return results;
    }

    public List<Object> getErrors() {
        return errors;
    }
}
