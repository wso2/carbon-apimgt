package org.wso2.carbon.apimgt.impl.portalNotifications;

public class PortalNotificationMetaData {

    private String api;
    private String apiVersion;
    private String apiContext;
    private String applicationName;
    private String requestedTier;
    private String revisionId;
    private String comment;

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getRequestedTier() { return requestedTier;}

    public void setRequestedTier(String requestedTier) { this.requestedTier = requestedTier;}

    public String getRevisionId() { return revisionId;}

    public void setRevisionId(String revisionId) { this.revisionId = revisionId;}

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }



}
