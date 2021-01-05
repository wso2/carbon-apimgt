package org.wso2.carbon.graphql.api.devportal.modules;

public class ScopesDTO {

    private String key;
    private String name;
    private String role;
    private String description;

    public ScopesDTO(String key,String name, String role, String description){
        this.key = key;
        this.name = name;
        this.role = role;
        this.description = description;
    }
}
