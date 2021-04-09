package tech.tongyu.bct.auth.controller.response;

import java.util.Map;

public class RpcDiagnosticsResponse<T, R> extends RpcSuccessResponse<T> {

    private final R diagnostics;

    public RpcDiagnosticsResponse(Integer serviceId, T result, R diagnostics){
        super(serviceId, result);
        this.diagnostics = diagnostics;
    }

    @Override
    protected Map<String, Object> toMap(){
        Map<String, Object> resultMap = super.toMap();
        resultMap.put(RpcConstants.JSON_RPC_RESPONSE_DIAGNOSTICS, this.diagnostics);
        return resultMap;
    }

    public R getDiagnostics() {
        return diagnostics;
    }
}
