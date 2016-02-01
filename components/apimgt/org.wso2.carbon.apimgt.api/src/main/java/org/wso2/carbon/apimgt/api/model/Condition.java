package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;

/**
 * This class,"Condition" contains the attributes which are defined when the policy is defined.
 */
public class Condition implements Serializable {
    private String requestCount;
    private String timeUnit;
    private String unitTime;
    private String httpVerb;
    private String startingIP;
    private String endingIP;

    public String getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(String requestCount) {
        this.requestCount = requestCount;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getUnitTime() {
        return unitTime;
    }

    public void setUnitTime(String unitTime) {
        this.unitTime = unitTime;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getStartingIP() {
        return startingIP;
    }

    public void setStartingIP(String startingIP) {
        this.startingIP = startingIP;
    }

    public String getEndingIP() {
        return endingIP;
    }

    public void setEndingIP(String endingIP) {
        this.endingIP = endingIP;
    }
}
