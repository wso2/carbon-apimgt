package org.wso2.carbon.apimgt.api.model;

import java.io.InputStream;

public class CustomBackendData {
    private String Id;
    private InputStream sequence;
    private String type;
    private String name;
    private String apiUUID;
    private String revisionUUID;

    public InputStream getSequence() {
        return sequence;
    }

    public void setSequence(InputStream sequence) {
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
