package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class "Policy" contains the newly created policy's attributes.
 */
public class Policy implements Serializable{
    private String policyName;
    private ArrayList<Condition>  conditions;
    private String defaultRequestCount;
    private String defaultUnitTime;
    private String defalutTimeUnit;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public ArrayList<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<Condition> conditions) {
        this.conditions = conditions;
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

    public String getDefalutTimeUnit() {
        return defalutTimeUnit;
    }

    public void setDefalutTimeUnit(String defalutTimeUnit) {
        this.defalutTimeUnit = defalutTimeUnit;
    }
}
