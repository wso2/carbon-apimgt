package org.wso2.carbon.graphql.api.devportal.modules;

public class LabelNameDTO {

    private String id;
    private String name;


    public  LabelNameDTO(String id, String name){
        this.id = id;
        this.name = name;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
