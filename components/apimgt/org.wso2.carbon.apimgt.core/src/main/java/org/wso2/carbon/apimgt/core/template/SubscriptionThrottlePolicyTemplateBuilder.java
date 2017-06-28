/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.template;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;

import java.io.StringWriter;
import java.util.Map;
/**
 * Siddhi query builder for Subscription throttle policy.
 */
public class SubscriptionThrottlePolicyTemplateBuilder extends ThrottlePolicyTemplateBuilder {

    private static final Log log = LogFactory.getLog(ThrottlePolicyTemplateBuilder.class);
    private static final String POLICY_VELOCITY_SUB = "throttle_policy_template_sub";
    private SubscriptionPolicy subscriptionPolicy;

    public SubscriptionThrottlePolicyTemplateBuilder(SubscriptionPolicy subscriptionPolicy) {
        this.subscriptionPolicy = subscriptionPolicy;
    }

    /**
     * Generate policy for subscription level.
     *
     * @param policy policy with level 'sub'. Multiple pipelines are not allowed. Can define more than one condition
     *               as set of conditions. all these conditions should be passed as a single pipeline
     * @return throttle policies for subscription level
     * @throws APITemplateException throws if generation failure occur
     */
    public String getThrottlePolicyForSubscriptionLevel(SubscriptionPolicy policy) throws APITemplateException {
        StringWriter writer = new StringWriter();

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for subscriptionLevel :" + policy.toString());
        }

        VelocityEngine velocityengine = initVelocityEngine();
        Template t = velocityengine.getTemplate(getTemplatePathForSubscription());

        VelocityContext context = new VelocityContext();
        setConstantContext(context);
        context.put("policy", policy);
        context.put("quotaPolicy", policy.getDefaultQuotaPolicy());
        t.merge(context, writer);
        if (log.isDebugEnabled()) {
            log.debug("Policy : " + writer.toString());
        }

        return writer.toString();
    }

    private String getTemplatePathForSubscription() {
        return policyTemplateLocation + POLICY_VELOCITY_SUB + ".xml";
    }

    @Override public Map<String, String> getThrottlePolicyTemplate() {
        try {
            templateMap.put("default", getThrottlePolicyForSubscriptionLevel(subscriptionPolicy));
        } catch (APITemplateException e) {
            String errorMessage = "Error while creating template for application throttle policy.";
            log.error(errorMessage, e);
        }
        return templateMap;
    }
}
