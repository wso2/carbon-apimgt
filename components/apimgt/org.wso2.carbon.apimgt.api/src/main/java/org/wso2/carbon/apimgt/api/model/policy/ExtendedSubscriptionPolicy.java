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
