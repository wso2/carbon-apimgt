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
import java.util.List;

public class Policy implements Serializable{
    private String policyName;
    private String policyLevel;
    private boolean acrossAllUsers;
    private String description;
    private List<Pipeline> pipelines;
    private QuotaPolicy defaultQuotaPolicy;

    public Policy(String name){
        this.policyName=name;
    }

    public boolean isAcrossAllUsers() {
        return acrossAllUsers;
    }

    public void setAcrossAllUsers(boolean acrossAllUsers) {
        this.acrossAllUsers = acrossAllUsers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public List<Pipeline> getPipelines() {
        return pipelines;
    }

    public void setPipelines(List<Pipeline> pipelines) {
        this.pipelines = pipelines;
    }

    public String getPolicyLevel() {
        return policyLevel;
    }

    public void setPolicyLevel(String policyLevel) {
        this.policyLevel = policyLevel;
    }

    public void setDefaultQuotaPolicy(QuotaPolicy defaultQuotaPolicy) {
        this.defaultQuotaPolicy = defaultQuotaPolicy;
    }

    public QuotaPolicy getDefaultQuotaPolicy() {
        return defaultQuotaPolicy;
    }
}
