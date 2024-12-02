package org.wso2.carbon.apimgt.api.model.policy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ExtendedSubscriptionPolicy extends SubscriptionPolicy{
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
    private String usageMetric;

    public ExtendedSubscriptionPolicy(String name) {
        super(name);
    }

    public String getUsageMetric() {
        return usageMetric;
    }

    public void setUsageMetric(String usageMetric) {
        this.usageMetric = usageMetric;
    }

    @Override
    public int getGraphQLMaxComplexity() {
        return graphQLMaxComplexity;
    }

    @Override
    public void setGraphQLMaxComplexity(int graphQLMaxComplexity) {
        this.graphQLMaxComplexity = graphQLMaxComplexity;
    }

    @Override
    public int getGraphQLMaxDepth() {
        return graphQLMaxDepth;
    }

    @Override
    public void setGraphQLMaxDepth(int graphQLMaxDepth) {
        this.graphQLMaxDepth = graphQLMaxDepth;
    }

    @Override
    public String getTierQuotaType() {
        return tierQuotaType;
    }

    @Override
    public void setTierQuotaType(String tierQuotaType) {
        this.tierQuotaType = tierQuotaType;
    }

    @Override
    public Map<String, String> getMonetizationPlanProperties() {
        return monetizationPlanProperties;
    }

    @Override
    public void setMonetizationPlanProperties(Map<String, String> monetizationPlanProperties) {
        this.monetizationPlanProperties = monetizationPlanProperties;
    }

    @Override
    public String getMonetizationPlan() {
        return monetizationPlan;
    }

    @Override
    public void setMonetizationPlan(String monetizationPlan) {
        this.monetizationPlan = monetizationPlan;
    }

    @Override
    public String getBillingPlan() {
        return billingPlan;
    }

    @Override
    public void setBillingPlan(String billingPlan) {
        this.billingPlan = billingPlan;
    }

    @Override
    public boolean isStopOnQuotaReach() {
        return stopOnQuotaReach;
    }

    @Override
    public void setStopOnQuotaReach(boolean stopOnQuotaReach) {
        this.stopOnQuotaReach = stopOnQuotaReach;
    }

    @Override
    public byte[] getCustomAttributes() {
        return customAttributes;
    }

    @Override
    public void setCustomAttributes(byte[] customAttributes) {
        this.customAttributes = customAttributes;
    }

    @Override
    public int getSubscriberCount() {
        return subscriberCount;
    }

    @Override
    public void setSubscriberCount(int subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    @Override
    public String getRateLimitTimeUnit() {
        return rateLimitTimeUnit;
    }

    @Override
    public void setRateLimitTimeUnit(String rateLimitTimeUnit) {
        this.rateLimitTimeUnit = rateLimitTimeUnit;
    }

    @Override
    public int getRateLimitCount() {
        return rateLimitCount;
    }

    @Override
    public void setRateLimitCount(int rateLimitCount) {
        this.rateLimitCount = rateLimitCount;
    }

    @Override
    public String toString() {
        return "ExtendedSubscriptionPolicy{" +
                "rateLimitCount=" + rateLimitCount +
                ", rateLimitTimeUnit='" + rateLimitTimeUnit + '\'' +
                ", subscriberCount=" + subscriberCount +
                ", customAttributes=" + Arrays.toString(customAttributes) +
                ", stopOnQuotaReach=" + stopOnQuotaReach +
                ", billingPlan='" + billingPlan + '\'' +
                ", monetizationPlan='" + monetizationPlan + '\'' +
                ", monetizationPlanProperties=" + monetizationPlanProperties +
                ", tierQuotaType='" + tierQuotaType + '\'' +
                ", graphQLMaxDepth=" + graphQLMaxDepth +
                ", graphQLMaxComplexity=" + graphQLMaxComplexity +
                ", usageMetric='" + usageMetric + '\'' +
                '}';
    }
}
