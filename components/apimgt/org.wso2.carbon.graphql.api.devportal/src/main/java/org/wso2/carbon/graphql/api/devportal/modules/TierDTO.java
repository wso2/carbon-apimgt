package org.wso2.carbon.graphql.api.devportal.modules;

public class TierDTO {

    private String name;
    private String tierPlan;
    private String monetizationAttributes;

    public TierDTO(String name,String tierPlan,String monetizationAttributes){
        this.name = name;
        this.tierPlan = tierPlan;
        this.monetizationAttributes = monetizationAttributes;
    }
}
