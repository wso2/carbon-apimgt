package org.wso2.carbon.apimgt.api.model;

public class UnDeployedAPIRevision {
    private static final long serialVersionUID = 1L;
    private String apiUUID;
    private String revisionUUID;
    private String environment;

    public String getApiUUID() {
        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {
        this.apiUUID = apiUUID;
    }

    public String getRevisionUUID() {
        return revisionUUID;
    }

    public void setRevisionUUID(String revisionUUID) {
        this.revisionUUID = revisionUUID;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
