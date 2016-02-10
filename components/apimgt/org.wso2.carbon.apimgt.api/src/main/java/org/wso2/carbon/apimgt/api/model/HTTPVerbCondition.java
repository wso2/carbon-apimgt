package org.wso2.carbon.apimgt.api.model;

/**
 * Created by dinushad on 2/10/16.
 */
public class HTTPVerbCondition extends Condition {
    private String httpVerb;

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }
}
