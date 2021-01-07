package org.wso2.carbon.graphql.api.devportal.modules;

public class TierNameDTO {

    private String apiId;
    private String name;

    public TierNameDTO(String apiId,String name){
        this.apiId = apiId;
        this.name = name;
    }

    public String getApiId() {
        return apiId;
    }

    public String getName() {
        return name;
    }
}
