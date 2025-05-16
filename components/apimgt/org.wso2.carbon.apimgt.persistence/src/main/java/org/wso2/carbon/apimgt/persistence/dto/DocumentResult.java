package org.wso2.carbon.apimgt.persistence.dto;

public class DocumentResult {
    private String metadata;
    private String uuid;
    private String apiUuid;
    private String createdTime;
    private String lastUpdatedTime;

    public DocumentResult (String metadata, String uuid) {
        this.metadata = metadata;
        this.uuid = uuid;
    }

    public DocumentResult (String metadata, String uuid, String apiUuid) {
        this.metadata = metadata;
        this.uuid = uuid;
        this.apiUuid = apiUuid;
    }

    public DocumentResult (String metadata, String uuid, String apiUuid, String createdTime, String lastUpdatedTime) {
        this.metadata = metadata;
        this.uuid = uuid;
        this.apiUuid = apiUuid;
        this.createdTime = createdTime;
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getApiUuid() {
        return apiUuid;
    }

    public void setApiUuid(String apiUuid) {
        this.apiUuid = apiUuid;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(String lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }
}
