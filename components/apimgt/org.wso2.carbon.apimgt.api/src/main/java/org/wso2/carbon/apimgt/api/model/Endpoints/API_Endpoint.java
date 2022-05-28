package org.wso2.carbon.apimgt.api.model.Endpoints;


import java.io.ByteArrayInputStream;

public class API_Endpoint {
    private int EndpointId;
    private int apiId;
    private String EndpointUuid;
    private String revisionUuid;
    private String endpointName;
    private ByteArrayInputStream endpointConfig;
    private String organization;
    private String endpointType;

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getRevisionUuid() {
        return revisionUuid;
    }

    public void setRevisionUuid(String revisionUuid) {
        this.revisionUuid = revisionUuid;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public ByteArrayInputStream getEndpointConfig() {
        return endpointConfig;
    }

    public void setEndpointConfig(ByteArrayInputStream endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getEndpointType() { return endpointType; }

    public void setEndpointType(String endpointType) { this.endpointType = endpointType; }

    public int getEndpointId() {return EndpointId; }

    public void setEndpointId(int endpointId) {EndpointId = endpointId; }

    public String getEndpointUuid() { return EndpointUuid; }

    public void setEndpointUuid(String endpointUuid) { EndpointUuid = endpointUuid; }
}
