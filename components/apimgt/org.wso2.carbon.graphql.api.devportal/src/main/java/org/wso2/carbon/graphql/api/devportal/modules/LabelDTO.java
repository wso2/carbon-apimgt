package org.wso2.carbon.graphql.api.devportal.modules;

public class LabelDTO {

    private String name;
    private String description;
    private String accessUrls;

    public LabelDTO(String name , String description, String accessUrls){
        this.name = name;
        this.description = description;
        this.accessUrls = accessUrls;
    }
}
