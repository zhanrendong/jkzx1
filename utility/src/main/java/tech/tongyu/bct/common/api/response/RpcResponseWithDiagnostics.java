package tech.tongyu.bct.common.api.response;

public interface RpcResponseWithDiagnostics<R, D> {
    R getResult();
    D getDiagnostics();
}
