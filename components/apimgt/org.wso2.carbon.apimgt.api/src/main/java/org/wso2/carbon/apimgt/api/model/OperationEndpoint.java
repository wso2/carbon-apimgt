package org.wso2.carbon.apimgt.api.model;


public class OperationEndpoint {
    private int operationEndpointId;
    private String apiId;
    private String operationEndpointUuid;
    private String revisionUuid;
    private String endpointName;
    private String endpointConfig;
    private String securityConfig;
    private String organization;

    public int getOperationEndpointId() {
        return operationEndpointId;
    }

    public void setOperationEndpointId(int operationEndpointId) {
        this.operationEndpointId = operationEndpointId;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
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

    public String getEndpointConfig() {
        return endpointConfig;
    }

    public void setEndpointConfig(String endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

    public String getSecurityConfig() {
        return securityConfig;
    }

    public void setSecurityConfig(String securityConfig) {
        this.securityConfig = securityConfig;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
