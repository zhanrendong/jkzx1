package tech.tongyu.bct.auth.controller.response;

public interface RpcConstants {

    String JSON_RPC_REQUEST_METHOD = "method";
    String JSON_RPC_REQUEST_PARAMS = "params";

    String JSON_RPC_RESPONSE_RESULT = "result";
    String JSON_RPC_RESPONSE_ERR = "error";
    String JSON_RPC_RESPONSE_ERR_CODE = "code";
    String JSON_RPC_RESPONSE_ERR_MESSAGE = "message";
    String JSON_RPC_RESPONSE_CODE = "code";
    String JSON_RPC_RESPONSE_SERVICE_ID = "service";
    String JSON_RPC_RESPONSE_DIAGNOSTICS = "diagnostics";

    /*------------------------ excel related  --------------------------*/

    String JSON_RPC_DESCRIPTION_EXCEL = "excel";
    /*------------------------ excel related  --------------------------*/

    /*------------------------ token related  --------------------------*/
    String BEARER = "Bearer ";
    String AUTHORIZATION_DESP = "Authorization";
    /*------------------------ token related  --------------------------*/

}
