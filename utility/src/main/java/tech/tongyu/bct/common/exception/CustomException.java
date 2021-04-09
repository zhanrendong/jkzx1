package tech.tongyu.bct.common.exception;

public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(String message) {
        super(message);
        this.errorCode = ErrorCode.UNKNOWN;
    }

    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
