/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;
import java.util.Map;

/**
 * This class represent the Tier
 */
@SuppressWarnings("unused")
public class Tier implements Serializable, Comparable<Tier>{

    private String name;
    private String displayName;
    private String description;
    private byte[] policyContent;
    private Map<String,Object> tierAttributes;

    private long requestsPerMin = 0;
    private long requestCount = 0;
    private long unitTime = 0;
    private String timeUnit = "ms";
    private String tierPlan;
    // The default value would be "true" since the default behavior is to stop when the quota is reached
    private boolean stopOnQuotaReached = true;

    public Tier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getPolicyContent() {
        return policyContent;
    }

    public void setPolicyContent(byte[] policyContent) {
        this.policyContent = policyContent;
    }
    public Map<String,Object> getTierAttributes() {
        return tierAttributes;
    }

    public void setTierAttributes(Map<String,Object> tierAttributes) {
        this.tierAttributes = tierAttributes;
    }

    public long getRequestsPerMin() {
        return requestsPerMin;
    }

    public void setRequestsPerMin(long requestsPerMin) {
        this.requestsPerMin = requestsPerMin;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public long getUnitTime() {
        return unitTime;
    }

    public void setUnitTime(long unitTime) {
        this.unitTime = unitTime;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getTierPlan() {
        return tierPlan;
    }

    public void setTierPlan(String tierPlan) {
        this.tierPlan = tierPlan;
    }

    public boolean isStopOnQuotaReached() {
        return stopOnQuotaReached;
    }

    public void setStopOnQuotaReached(boolean stopOnQuotaReached) {
        this.stopOnQuotaReached = stopOnQuotaReached;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tier tier = (Tier) o;

        return !(name != null ? !name.equals(tier.name) : tier.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public int compareTo(Tier tier) {
        if(tier.getRequestsPerMin() == Long.MAX_VALUE || tier.getRequestsPerMin() == Integer.MAX_VALUE){
            return 1;
        }
        else if(this.getRequestsPerMin() == Long.MAX_VALUE || this.getRequestsPerMin() == Integer.MAX_VALUE){
            return -1;
        }
        return new Long(tier.getRequestsPerMin() - this.getRequestsPerMin()).intValue();
    }
}
