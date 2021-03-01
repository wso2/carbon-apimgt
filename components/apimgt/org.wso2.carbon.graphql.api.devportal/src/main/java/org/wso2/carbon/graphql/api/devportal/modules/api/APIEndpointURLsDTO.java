package org.wso2.carbon.graphql.api.devportal.modules.api;

public class APIEndpointURLsDTO {
    private String uuid;
    private String environmentName;
    private String environmentType;
    private APIURLsDTO urLs;
    private DefaultAPIURLsDTO defaultUrls;

    public APIEndpointURLsDTO(String uuid,String environmentName, String environmentType,APIURLsDTO urLs,DefaultAPIURLsDTO defaultUrls) {
        this.uuid = uuid;
        this.environmentName = environmentName;
        this.environmentType = environmentType;
        this.urLs = urLs;
        this.defaultUrls = defaultUrls;

    }

    public String getApiId() {
        return uuid;
    }
}
