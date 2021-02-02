package org.wso2.carbon.apimgt.gateway.common.exception;

public class JWTGeneratorException extends Exception {
    private ErrorHandler errorHandler;

    /**
     * Get error handler object.
     *
     * @return ErrorHandler
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public JWTGeneratorException(String msg) {
        super(msg);
        this.errorHandler = ExceptionCodes.INTERNAL_ERROR;
    }

    public JWTGeneratorException(String msg, Throwable e) {
        super(msg, e);
        this.errorHandler = ExceptionCodes.INTERNAL_ERROR;
    }

    public JWTGeneratorException(Throwable throwable) {
        super(throwable);
        this.errorHandler = ExceptionCodes.INTERNAL_ERROR;
    }

    /**
     * This is a default constructure where you can pass error code to error DTO
     *
     * @param message Error message
     * @param code    Exception code that need to pass to the error DTO
     */
    public JWTGeneratorException(String message, ErrorHandler code) {
        super(message);
        this.errorHandler = code;
    }

    /**
     * This is a default constructure where you can pass error code to error DTO
     *
     * @param code Exception code that need to pass to the error DTO
     */
    public JWTGeneratorException(ErrorHandler code) {
        super(code.getErrorCode() + ":" + code.getErrorMessage() + "::" + code.getErrorDescription());
        this.errorHandler = code;
    }

    /**
     * This is a default constructure where you can pass error code to error DTO
     *
     * @param message Error message
     * @param cause   throwable object.
     * @param code    Exception code that need to pass to the error DTO
     */
    public JWTGeneratorException(String message, Throwable cause, ErrorHandler code) {
        super(message, cause);
        this.errorHandler = code;
    }
}
