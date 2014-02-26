package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.Serializable;

public class VerbInfoDTO implements Serializable {

    private String httpVerb;

    private String authType;

    private String throttling;


    private String requestKey;

    public String getThrottling() {
        return throttling;
    }

    public void setThrottling(String throttling) {
        this.throttling = throttling;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public boolean requiresAuthentication() {
        return !APIConstants.AUTH_TYPE_NONE.equalsIgnoreCase(authType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VerbInfoDTO that = (VerbInfoDTO) o;

        if (httpVerb != null ? !httpVerb.equals(that.getHttpVerb()) : that.getHttpVerb() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return httpVerb != null ? httpVerb.hashCode() : 0;
    }
}
