/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt.model.entity;

/**
 * Entity for keeping Application Policy
 */
public class ApplicationPolicy extends Policy {

    private static final String type = "APPLICATION";

    BurstLimit burstLimit = new BurstLimit();
    private Integer rateLimitCount = null;
    private String rateLimitTimeUnit = null;

    public BurstLimit getBurstLimit() {
        return burstLimit;
    }
    public void setBurstLimit(BurstLimit burstLimit) {
        this.burstLimit = burstLimit;
    }
    public int getRateLimitCount() {

        return rateLimitCount;
    }

    public void setRateLimitCount(int rateLimitCount) {

        this.rateLimitCount = rateLimitCount;
    }

    public String getRateLimitTimeUnit() {

        return rateLimitTimeUnit;
    }

    public void setRateLimitTimeUnit(String rateLimitTimeUnit) {

        this.rateLimitTimeUnit = rateLimitTimeUnit;
    }

    @Override
    public String getCacheKey() {

        return POLICY_TYPE.APPLICATION + DELEM_PERIOD + super.getCacheKey();
    }

    @Override
    public String toString() {
        return "ApplicationPolicy [getId()=" + getId() + ", getQuotaType()=" + getQuotaType() + ", isContentAware()="
                + isContentAware() + ", getTenantId()=" + getTenantId() + ", getName()=" + getName() +
                ", rateLimitCount=" + burstLimit.getRateLimitCount() + ", rateLimitTimeUnit=" + rateLimitTimeUnit + "]";
    }
}
