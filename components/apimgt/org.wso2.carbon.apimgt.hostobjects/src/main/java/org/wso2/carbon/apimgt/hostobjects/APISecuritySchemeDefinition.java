package org.wso2.carbon.apimgt.hostobjects;

import io.swagger.models.auth.SecuritySchemeDefinition;

import java.util.HashMap;
import java.util.Map;

public class APISecuritySchemeDefinition implements SecuritySchemeDefinition  {

    String type;
    String authorizationUrl;
    String flow;
    String description;
    Map<String,String> scopes = new HashMap<String, String>();

    public String getType() {
        return type;
    }

    public void setType(String s) {
        this.type = s;
    }

    public Map<String, Object> getVendorExtensions() {
        return null;
    }

    public void setVendorExtension(String s, Object o) {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String s) {
        this.description= s;
    }

    public void setFlow(String flow){
        this.flow = flow;
    }

    public String getFlow(){
        return flow;
    }

    public void setAuthorizationUrl(String authorizationUrl){
        this.authorizationUrl = authorizationUrl;
    }

    public String getAuthorizationUrl(){
        return  authorizationUrl;
    }

    public void setScopes(String key,String value){
        scopes.put(key, value);
    }
    public Map<String,String> getScopes(){
        return scopes;
    }
}
