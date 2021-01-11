package org.wso2.carbon.apimgt.api.model;

import java.io.InputStream;

public class EndPointInfo {
    private String uuid;
    private InputStream endPointDef;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public InputStream getEndPointDef() {
        return endPointDef;
    }

    public void setEndPointDef(InputStream endPointDef) {
        this.endPointDef = endPointDef;
    }
}
