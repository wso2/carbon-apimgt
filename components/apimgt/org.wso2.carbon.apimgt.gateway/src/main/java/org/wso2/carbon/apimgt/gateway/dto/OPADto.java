package org.wso2.carbon.apimgt.gateway.dto;

public class OPADto{

    private String username;

    private String[] scopes;

    private String API_name;

    private String version;

    private String context_path;

    private String resource_path;

    private String http_method;

    private String api_type;

    private String application_name;

    public OPADto(String api_type) {
        this.api_type = api_type;
    }

    public OPADto(String username, String[] scopes,
                  String API_name, String api_version,
                  String context_path, String resource_path,
                  String http_method, String api_type, String application_name) {
        this.username = username;
        this.scopes = scopes;
        this.API_name = API_name;
        this.version = api_version;
        this.context_path = context_path;
        this.resource_path = resource_path;
        this.http_method = http_method;
        this.api_type = api_type;
        this.application_name = application_name;
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

    public String getApi_type() {
        return api_type;
    }

    public void setApi_type(String api_type) {
        this.api_type = api_type;
    }

    public String[] getScopes() {
        return scopes;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }

    public String getAPI_name() {
        return API_name;
    }

    public void setAPI_name(String API_name) {
        this.API_name = API_name;
    }

    public String getContext_path() {
        return context_path;
    }

    public void setContext_path(String context_path) {
        this.context_path = context_path;
    }

    public String getResource_path() {
        return resource_path;
    }

    public void setResource_path(String resource_path) {
        this.resource_path = resource_path;
    }

    public String getHttp_method() {
        return http_method;
    }

    public void setHttp_method(String http_method) {
        this.http_method = http_method;
    }

    public String getApplication_name() {
        return application_name;
    }

    public void setApplication_name(String application_name) {
        this.application_name = application_name;
    }

}
