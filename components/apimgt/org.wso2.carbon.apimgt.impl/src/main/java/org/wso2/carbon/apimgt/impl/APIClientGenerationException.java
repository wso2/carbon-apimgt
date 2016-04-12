package org.wso2.carbon.apimgt.impl;

/**
 * This is the custom exception class for API Client Generation Manager
 */
public class APIClientGenerationException extends Exception {
    public APIClientGenerationException(String msg) {
        super(msg);
    }

    public APIClientGenerationException(String msg, Throwable e) {
        super(msg, e);
    }

    public APIClientGenerationException(Throwable throwable) {
        super(throwable);
    }

    public String getErrorMessage(){
        return super.getMessage();
    }
}
