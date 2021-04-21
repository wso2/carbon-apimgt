package org.wso2.carbon.graphql.api.devportal.modules.api;

public class ContextDTO {

    private String uuid;
    private String createdTime;
    private String lastUpdate;
    private String type;

    public ContextDTO(String uuid , String createdTime, String lastUpdate,String type) {
        this.uuid = uuid;
        this.createdTime = createdTime;
        this.lastUpdate = lastUpdate;
        this.type = type;
    }

    public ContextDTO() {
    }

    public String getId() {
        return uuid;
    }

    public void setId(String uuid) {
        this.uuid = uuid;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
