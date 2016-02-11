/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.api.model.policy;

import java.io.Serializable;
import java.util.ArrayList;

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
