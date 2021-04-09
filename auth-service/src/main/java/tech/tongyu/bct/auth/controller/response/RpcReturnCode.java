package tech.tongyu.bct.auth.controller.response;

import tech.tongyu.bct.common.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum RpcReturnCode {
    SUCCESS(0),
    UNKNOWN_ERROR(1),
    INPUT_NOT_VALID(100),
    METHOD_NOT_FOUND(101),
    PARAM_NOT_VALID(102),
    USERNAME_OR_PASSWORD_INVALID(103),
    CAPTCHA_INVALID(104),
    UNABLE_TO_LOGIN(105),
    TOKEN_NOT_FOUND(201),
    TOKEN_NOT_VALID(202),
    TOKEN_HAS_EXPIRED(203),
    REMOTE_ERROR(500),
    DATABASE_ERROR(600),
    REDIS_ERROR(700),

    SERVICE_FAILED(1000);

    private Integer code;
    public Integer getCode() {
        return code;
    }

    RpcReturnCode(Integer code) {
        this.code = code;
    }

    /**
     * 获取给定参数的代码想对应的枚举类实例
     * @param paramCode 参数代码
     * @return RpcReturnCode 返回与给定参数的代码想对应的枚举类实例
     * @throws IllegalArgumentException 当给定的代码未在enum类实例中找到
     */
    public static RpcReturnCode of(Integer paramCode) throws IllegalArgumentException{
        List<RpcReturnCode> codes = Arrays.stream(RpcReturnCode.values())
                .filter(rpcReturnCode -> Objects.equals(rpcReturnCode.code, paramCode))
                .collect(Collectors.toList());

        if(CollectionUtils.isEmpty(codes))
            throw new IllegalArgumentException(
                    String.format("param: rpcReturnCode[%d] does not exist", paramCode));

        return codes.get(0);
    }

    public Boolean isSuccess(){
        return Objects.equals(this, RpcReturnCode.SUCCESS);
    }
}
