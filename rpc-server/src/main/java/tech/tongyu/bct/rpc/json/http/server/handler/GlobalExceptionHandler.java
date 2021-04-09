package tech.tongyu.bct.rpc.json.http.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import tech.tongyu.bct.common.api.response.JsonRpcResponse;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.rpc.json.http.server.util.ExceptionUtils;

import javax.servlet.ServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseBody
    @ExceptionHandler(NullPointerException.class)
    public Object handlerNullPointerException(ServletRequest request, NullPointerException e) {
        e.printStackTrace();
        logger.error(ExceptionUtils.getRpcExceptionInfo(request, e).toString());
        return new JsonRpcResponse(JsonRpcResponse.ErrorCode.RUNTIME_ERROR, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    public Object handlerIllegalArgumentException(ServletRequest request, IllegalArgumentException e) {
        e.printStackTrace();
        logger.error(ExceptionUtils.getRpcExceptionInfo(request, e).toString());
        return new JsonRpcResponse(JsonRpcResponse.ErrorCode.RUNTIME_ERROR, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(CustomException.class)
    public Object handlerCustomException(ServletRequest request, CustomException e) {
        e.printStackTrace();
        logger.error(ExceptionUtils.getRpcExceptionInfo(request, e).toString());
        return new JsonRpcResponse(JsonRpcResponse.ErrorCode.RUNTIME_ERROR, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Object handlerAllException(ServletRequest request, Exception e) {
        e.printStackTrace();
        logger.error(ExceptionUtils.getRpcExceptionInfo(request, e).toString());
        return new JsonRpcResponse(JsonRpcResponse.ErrorCode.RUNTIME_ERROR, e.getMessage());
    }
}
