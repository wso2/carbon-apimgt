package org.wso2.carbon.apimgt.rest.api.authenticator;

public class ApiException extends Exception{
    private int code;
    public ApiException (int code, String msg) {
        super(msg);
        this.code = code;
    }
}
