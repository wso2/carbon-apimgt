package org.wso2.carbon.apimgt.api.model;

public class SequenceBackendData {
    private String Id;
    private String sequence;
    private String type;
    private String name;
    private String apiUUID;
    private String revisionUUID;

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
}
