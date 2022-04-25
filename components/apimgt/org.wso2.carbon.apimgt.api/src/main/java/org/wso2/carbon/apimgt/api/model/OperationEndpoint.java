package org.wso2.carbon.apimgt.api.model;


import java.io.ByteArrayInputStream;

public class OperationEndpoint {
    private int operationEndpointId;
    private int apiId;
    private String operationEndpointUuid;
    private String revisionUuid;
    private String endpointName;
    private ByteArrayInputStream endpointConfig;
    //private ByteArrayInputStream securityConfig;
    private String organization;

    public int getOperationEndpointId() {
        return operationEndpointId;
    }

    public void setOperationEndpointId(int operationEndpointId) {
        this.operationEndpointId = operationEndpointId;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getOperationEndpointUuid() {
        return operationEndpointUuid;
    }

    public void setOperationEndpointUuid(String operationEndpointUuid) {
        this.operationEndpointUuid = operationEndpointUuid;
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
}
