package org.wso2.carbon.apimgt.rest.api.admin;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-22T12:24:12.664+05:30")
public class ApiException extends Exception{
    private int code;
    public ApiException (int code, String msg) {
        super(msg);
        this.code = code;
    }
}
