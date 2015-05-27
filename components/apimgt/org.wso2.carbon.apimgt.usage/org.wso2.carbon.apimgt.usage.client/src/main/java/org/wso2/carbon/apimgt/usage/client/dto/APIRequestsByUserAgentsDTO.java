package org.wso2.carbon.apimgt.usage.client.dto;

/**
 * Created by nisala on 3/13/15.
 */
public class APIRequestsByUserAgentsDTO {
    private String userAgent;
    private int count;

    public String getUserAgent() {
        return userAgent;
    }

    public int getCount() {
        return count;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
