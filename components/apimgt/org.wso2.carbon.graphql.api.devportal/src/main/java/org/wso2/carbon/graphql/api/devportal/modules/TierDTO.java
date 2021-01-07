package org.wso2.carbon.graphql.api.devportal.modules;

public class TierDTO {
    private String apiId;
    private String tierPlan;
    private String monetizationAttributes;

    public TierDTO( String tierPlan, String monetizationAttributes){

        this.tierPlan = tierPlan;
        this.monetizationAttributes = monetizationAttributes;
    }
//    public String setApiId(String apiId){
//        this.apiId = apiId;
//        return apiId;
//    }
//
    public String getId() {
        return apiId;
    }
}
