package tech.tongyu.bct.auth.controller.response;

import com.google.common.collect.Maps;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.AuthConstants;

import java.util.Map;

public class RpcResponseGenerator {

    public static ResponseEntity<String> getErrorResponseEntity(ReturnMessageAndTemplateDef.Errors error, Object... templateParams){
        return new ResponseEntity<>(
                new RpcErrorResponse<>(
                        RpcReturnCode.SERVICE_FAILED
                        , AuthConstants.SERVICE_ID
                        , error.getDetailedErrorCode()
                        , error.getMessage(templateParams)
                ).toString(),
                HttpStatus.OK
        );
    }

    public static ResponseEntity<String> getErrorResponseEntity(CustomException e){
        return getErrorResponseEntity(e, "");
    }

    public static ResponseEntity<String> getErrorResponseEntity(CustomException e, String errorCode){
        return new ResponseEntity<>(
                new RpcErrorResponse<>(
                        RpcReturnCode.SERVICE_FAILED
                        , AuthConstants.SERVICE_ID
                        , errorCode
                        , e.getMessage()
                ).toString(),
                HttpStatus.OK
        );
    }

    public static ResponseEntity<String> getResponseEntity(String entity){
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    public static ResponseEntity<String> getOldResponseEntity(String entity){
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", 1);
        map.put("jsonrpc", "2.0");
        map.put("result", JsonUtils.fromJson(entity));
        return new ResponseEntity<>(JsonUtils.toJson(map), HttpStatus.OK);
    }
}
