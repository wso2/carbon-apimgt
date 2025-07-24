package org.wso2.carbon.apimgt.persistence.dto;

public class ContentSearchResult {
    private String metadata;
    private String type;
    private String apiId;
    private String uuid;

    public ContentSearchResult(String metadata, String type, String apiId, String uuid) {
        this.metadata = metadata;
        this.type = type;
        this.apiId = apiId;
        this.uuid = uuid;
    }

    public ContentSearchResult(String metadata, String type, String apiId) {
        this.metadata = metadata;
        this.type = type;
        this.apiId = apiId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
