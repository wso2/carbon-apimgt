package org.wso2.carbon.apimgt.usage.client.pojo;

/**
 * Created by rukshan on 10/9/15.
 */
public class APIUsageByUserName {
    private String apiName;
    private String apiVersion;
    private String context;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getApipublisher() {
        return apipublisher;
    }

    public void setApipublisher(String apipublisher) {
        this.apipublisher = apipublisher;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    private String userID;
    private String apipublisher;
    private long requestCount;
}
