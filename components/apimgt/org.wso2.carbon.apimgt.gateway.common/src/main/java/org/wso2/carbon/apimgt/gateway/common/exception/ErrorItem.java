package org.wso2.carbon.apimgt.gateway.common.exception;

/**
 * ErrorHandler implementation for dynamic error items
 */
public class ErrorItem implements ErrorHandler {

    private String message;
    private String description;
    private long errorCode;
    private int statusCode;
    private boolean stackTrace = false;


    public ErrorItem(String message, String description, long errorCode, int statusCode, boolean stackTrace) {
        this.message = message;
        this.errorCode = errorCode;
        this.statusCode = statusCode;
        this.description = description;
        this.stackTrace = stackTrace;
    }

    public void setErrorCode(long errorCode) {
        this.errorCode = errorCode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public ErrorItem() {
    }

    public ErrorItem(String message, String description, long errorCode, int statusCode) {
        this.message = message;
        this.errorCode = errorCode;
        this.statusCode = statusCode;
        this.description = description;
    }

    @Override
    public long getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public String getErrorDescription() {
        return description;
    }

    @Override
    public int getHttpStatusCode() {
        return statusCode;
    }

    @Override
    public boolean printStackTrace() {
        return stackTrace;
    }
}
