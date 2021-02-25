package org.wso2.carbon.graphql.api.devportal.modules.tag;

public class TagDTO {
    private String value;
    private int count;

    public TagDTO (String value,int count){
        this.value = value;
        this.count = count;
    }
}
