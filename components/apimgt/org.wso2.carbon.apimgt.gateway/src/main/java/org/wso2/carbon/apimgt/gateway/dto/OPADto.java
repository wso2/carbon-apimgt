package org.wso2.carbon.apimgt.gateway.dto;

public class OPADto{

    private String username;

    private String[] scopes;

    private String APIName;

    private String version;

    private String contextPath;

    private String resourcePath;

    private String httpMethod;

    private String apiType;

    private String applicationName;

    private String clientIp;

    public OPADto(String apiType) {
        this.apiType = apiType;
    }

    public OPADto(String username, String[] scopes,
                  String APIName, String apiVersion,
                  String contextPath, String resourcePath,
                  String httpMethod, String apiType, String applicationName, String clientIp) {
        this.username = username;
        this.scopes = scopes;
        this.APIName = APIName;
        this.version = apiVersion;
        this.contextPath = contextPath;
        this.resourcePath = resourcePath;
        this.httpMethod = httpMethod;
        this.apiType = apiType;
        this.applicationName = applicationName;
        this.clientIp = clientIp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }

    public String[] getScopes() {
        return scopes;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }

    public String getAPIName() {
        return APIName;
    }

    public void setAPIName(String APIName) {
        this.APIName = APIName;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
