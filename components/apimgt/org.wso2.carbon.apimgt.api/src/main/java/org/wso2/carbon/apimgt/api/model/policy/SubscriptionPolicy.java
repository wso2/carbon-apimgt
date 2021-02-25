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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SubscriptionPolicy extends Policy {
    private int rateLimitCount;
    private String rateLimitTimeUnit;
    private int subscriberCount;
    private byte[] customAttributes;
    private boolean stopOnQuotaReach;
    private String billingPlan;
    private String monetizationPlan = null;
    private Map<String, String> monetizationPlanProperties = new HashMap<String, String>();
    private String tierQuotaType;
    private int graphQLMaxDepth;
    private int graphQLMaxComplexity;

    public SubscriptionPolicy(String name) {
        super(name);
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
        return customAttributes;
    }

    public void setCustomAttributes(byte[] customAttributes) {
        this.customAttributes = customAttributes;
    }

    public String getMonetizationPlan() {
        return monetizationPlan;
    }

    public void setMonetizationPlan(String monetizationPlan) {
        this.monetizationPlan = monetizationPlan;
    }

    public Map<String, String> getMonetizationPlanProperties() {
        return monetizationPlanProperties;
    }

    public void setMonetizationPlanProperties(Map<String, String> monetizationPlanProperties) {
        this.monetizationPlanProperties = monetizationPlanProperties;
    }

    public String getTierQuotaType() {
        return tierQuotaType;
    }

    public void setTierQuotaType(String tierQuotaType) {
        this.tierQuotaType = tierQuotaType;
    }

    public int getGraphQLMaxDepth() {
        return graphQLMaxDepth;
    }

    public void setGraphQLMaxDepth(int graphQLMaxDepth) {
        this.graphQLMaxDepth = graphQLMaxDepth;
    }

    public int getGraphQLMaxComplexity() {
        return graphQLMaxComplexity;
    }

    public void setGraphQLMaxComplexity(int graphQLMaxComplexity) {
        this.graphQLMaxComplexity = graphQLMaxComplexity;
    }

    @Override
    public String toString() {
        return "SubscriptionPolicy [rateLimitCount=" + rateLimitCount + ", rateLimitTimeUnit=" + rateLimitTimeUnit
                + ", customAttributes=" + Arrays.toString(customAttributes) + ", stopOnQuotaReach=" + stopOnQuotaReach
                + ", billingPlan=" + billingPlan + ", monetizationPlan=" + monetizationPlan
                + ", monetizationPlanProperties=" + monetizationPlanProperties + ", tierQuotaType=" + tierQuotaType
                + ", maxDepth=" + graphQLMaxDepth + ", maxComplexity=" + graphQLMaxComplexity
                + ", subscriberCount= " + subscriberCount + "]";
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(int subscriberCount) {
        this.subscriberCount = subscriberCount;
    }
}
