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

package org.wso2.carbon.apimgt.core.models.policy;

import java.util.List;

/**
 * Contains the condition base pipelines in policy.
 */
public class Pipeline {

    private QuotaPolicy quotaPolicy;
    private List<Condition> conditions;
    private boolean enabled;
    private int id;
    private String description;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> condition) {
        this.conditions = condition;
    }

    public QuotaPolicy getQuotaPolicy() {
        return quotaPolicy;
    }

    public void setQuotaPolicy(QuotaPolicy quotaPolicy) {
        this.quotaPolicy = quotaPolicy;
    }

    @Override
    public String toString() {
        return "Pipeline [quotaPolicy=" + quotaPolicy + ", conditions=" + conditions + "]";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
