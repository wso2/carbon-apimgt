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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Contains subscription policy based attributes
 */
public class SubscriptionPolicy extends Policy {
    private int rateLimitCount;
    private String rateLimitTimeUnit;
    private byte[] customAttributes;
    private boolean stopOnQuotaReach;
    private String billingPlan;

    public SubscriptionPolicy(String name) {
        super(name);
        customAttributes = null;
    }

    public SubscriptionPolicy(String uuid, String policyName) {
        super(uuid, policyName);
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

    public String getBillingPlan() {
        return billingPlan;
    }

    public void setBillingPlan(String billingPlan) {
        this.billingPlan = billingPlan;
    }

    public boolean isStopOnQuotaReach() {
        return stopOnQuotaReach;
    }

    public void setStopOnQuotaReach(boolean stopOnQuotaReach) {
        this.stopOnQuotaReach = stopOnQuotaReach;
    }

    public byte[] getCustomAttributes() {
        return customAttributes != null ? Arrays.copyOf(customAttributes, customAttributes.length) : new byte[0];
    }

    public void setCustomAttributes(byte[] customAttributes) {
        this.customAttributes = Arrays.copyOf(customAttributes, customAttributes.length);
    }

    @Override
    public void populateDataInPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(9, getRateLimitCount());
        preparedStatement.setString(10, getRateLimitTimeUnit());
        preparedStatement.setBytes(11, getCustomAttributes());
        preparedStatement.setBoolean(12, isStopOnQuotaReach());
        preparedStatement.setString(13, getBillingPlan());
    }

    @Override
    public String toString() {
        return "SubscriptionPolicy [policyName=" + getPolicyName()
                + ", description=" + getDescription() + ", defaultQuotaPolicy=" + getDefaultQuotaPolicy() +
                "rateLimitCount=" + ", ratelimitTimeUnit=" + rateLimitTimeUnit + "]";
    }
}
