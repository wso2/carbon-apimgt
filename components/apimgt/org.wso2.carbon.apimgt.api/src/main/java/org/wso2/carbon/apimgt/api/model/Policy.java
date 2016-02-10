package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class "Policy" contains the newly created policy's attributes.
 */
public class Policy implements Serializable{
    private String policyName;
    private String policyLevel;
    private Pipelines pipelines;
    private ArrayList<Condition>  conditions;
    private QuotaPolicy defaultQuotaPolicy;

    public void setDefaultQuotaPolicy(QuotaPolicy defaultQuotaPolicy) {
        this.defaultQuotaPolicy = defaultQuotaPolicy;
    }

    public Pipelines getPipelines() {
        return pipelines;
    }

    public void setPipelines(Pipelines pipelines) {
        this.pipelines = pipelines;
    }

    public ArrayList<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<Condition> conditions) {
        this.conditions = conditions;
    }

    public QuotaPolicy getDefaultQuotaPolicy() {
        return defaultQuotaPolicy;
    }

    public String getPolicyLevel() {
        return policyLevel;
    }

    public void setPolicyLevel(String policyLevel) {
        this.policyLevel = policyLevel;
    }

    public Policy(String name){
        this.policyName=name;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getDefaultRequestCount() {
        return defaultRequestCount;
    }

    public void setDefaultRequestCount(String defaultRequestCount) {
        this.defaultRequestCount = defaultRequestCount;
    }

    public String getDefaultUnitTime() {
        return defaultUnitTime;
    }

    public void setDefaultUnitTime(String defaultUnitTime) {
        this.defaultUnitTime = defaultUnitTime;
    }

    public String getDefaultTimeUnit() {
        return defaultTimeUnit;
    }

    public void setDefaultTimeUnit(String defalutTimeUnit) {
        this.defaultTimeUnit = defalutTimeUnit;
    }
    private String defaultRequestCount;
    private String defaultUnitTime;
    private String defaultTimeUnit;
}
