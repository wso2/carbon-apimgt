/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.gateway.throttling.dto;

import org.wso2.carbon.apimgt.gateway.throttling.constants.APIThrottleConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains resource details, including throttling, authentication
 */
public class VerbInfoDTO {

    private String httpVerb;

    private String authType;

    private String throttling;

    private String applicableLevel;

    private List<String> throttlingConditions = new ArrayList<String>();

    private String requestKey;

    private ConditionGroupDTO[] conditionGroups;

    public String getThrottling() {
        return throttling;
    }

    public void setThrottling(String throttling) {
        this.throttling = throttling;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public boolean requiresAuthentication() {
        return !APIThrottleConstants.AUTH_TYPE_NONE.equalsIgnoreCase(authType);
    }

    public List<String> getThrottlingConditions() {
        return throttlingConditions;
    }

    public void setThrottlingConditions(List<String> throttlingConditions) {
        this.throttlingConditions = throttlingConditions;
    }

    public String getApplicableLevel() {
        return applicableLevel;
    }

    public void setApplicableLevel(String applicableLevel) {
        this.applicableLevel = applicableLevel;
    }

    public void setConditionGroups(ConditionGroupDTO[] conditionGroups) {
        this.conditionGroups = conditionGroups.clone();
    }

    public ConditionGroupDTO[] getConditionGroups() {

        if (conditionGroups != null) {
            return conditionGroups.clone();
        }
        return new ConditionGroupDTO[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VerbInfoDTO that = (VerbInfoDTO) o;

        if (httpVerb != null ? !httpVerb.equals(that.getHttpVerb()) : that.getHttpVerb() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return httpVerb != null ? httpVerb.hashCode() : 0;
    }
}
