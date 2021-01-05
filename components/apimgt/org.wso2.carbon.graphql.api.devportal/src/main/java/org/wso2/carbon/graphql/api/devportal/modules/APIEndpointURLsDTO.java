package org.wso2.carbon.graphql.api.devportal.modules;

public class APIEndpointURLsDTO {
    private String apiId;
    private String environmentName;
    private String environmentType;

    public APIEndpointURLsDTO(String apiId,String environmentName, String environmentType) {
        this.apiId = apiId;
        this.environmentName = environmentName;
        this.environmentType = environmentType;

    }

    public String getApiId() {
        return apiId;
    }
}
