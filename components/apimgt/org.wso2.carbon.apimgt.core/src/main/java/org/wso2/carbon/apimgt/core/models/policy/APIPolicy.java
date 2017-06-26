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
 * Contains {@link APIPolicy} attributes
 */
public class APIPolicy extends Policy {

    private List<Pipeline> pipelines;
    private String userLevel;

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public APIPolicy(String name) {
        super(name);
    }

    public APIPolicy(String uuid, String policyName) {
        super(uuid, policyName);
    }

    public List<Pipeline> getPipelines() {
        return pipelines;
    }

    public void setPipelines(List<Pipeline> pipelines) {
        this.pipelines = pipelines;
    }

    @Override
    public String toString() {
        return "APIPolicy [policyName=" + getPolicyName() + ", userLevel=" + getUserLevel()
                + ", description=" + getDescription() + ", pipelines=" + pipelines + ", defaultQuotaPolicy="
                + getDefaultQuotaPolicy() + "]";
    }
}
