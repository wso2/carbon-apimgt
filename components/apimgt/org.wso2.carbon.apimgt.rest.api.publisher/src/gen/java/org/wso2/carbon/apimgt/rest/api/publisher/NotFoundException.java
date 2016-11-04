package org.wso2.carbon.apimgt.rest.api.publisher;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-04T10:24:27.156+05:30")
public class NotFoundException extends ApiException {
    private int code;
    public NotFoundException (int code, String msg) {
        super(code, msg);
        this.code = code;
    }
}
