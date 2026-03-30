package org.wso2.carbon.apimgt.impl.notifier.events;

public class APIKeyAssociationEvent extends Event {
    private String apiKeyHash;
    private String applicationUUId;
    private String apiUUId;
    private int apiId;
    private int applicationId;

    public APIKeyAssociationEvent(String apiKeyHash, String applicationUUId, String apiUUId, int apiId,
                                  int applicationId) {
        this.apiKeyHash = apiKeyHash;
        this.applicationUUId = applicationUUId;
        this.apiUUId = apiUUId;
        this.apiId = apiId;
        this.applicationId = applicationId;
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }

    public String getApplicationUUId() {
        return applicationUUId;
    }

    public void setApplicationUUId(String applicationUUId) {
        this.applicationUUId = applicationUUId;
    }

    public String getApiUUId() {
        return apiUUId;
    }

    public void setApiUUId(String apiUUId) {
        this.apiUUId = apiUUId;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }
}
