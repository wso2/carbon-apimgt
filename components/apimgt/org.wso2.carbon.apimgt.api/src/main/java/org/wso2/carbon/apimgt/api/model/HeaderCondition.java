package org.wso2.carbon.apimgt.api.model;

/**
 * Created by dinushad on 2/10/16.
 */
public class HeaderCondition extends Condition{
    private String header;
    private String value;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
