package org.wso2.carbon.graphql.api.devportal.modules;

public class TierDTO {
    private String apiId;
    private String displayName;
    private String description;
    private String policyContent;
    private String tierAttributes;
    private int requestsPerMin;
    private int requestCount;
    private int unitTime;
    private String timeUnit;
    private String tierPlan;
    private boolean stopOnQuotaReached;
    private String monetizationAttributes;

    public TierDTO( String displayName,String description,String policyContent,String tierAttributes,int requestsPerMin,int requestCount,int unitTime,String timeUnit,String tierPlan,boolean stopOnQuotaReached, String monetizationAttributes){

        this.displayName = displayName;
        this.description = description;
        this.policyContent = policyContent;
        this.tierAttributes = tierAttributes;
        this.requestsPerMin = requestsPerMin;
        this.requestCount = requestCount;
        this.unitTime = unitTime;
        this.timeUnit = timeUnit;

        this.tierPlan = tierPlan;

        this.stopOnQuotaReached = stopOnQuotaReached;
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
