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
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;

import java.io.StringWriter;
import java.util.Map;

/**
 * Siddhi query builder for Subscription throttle policy.
 */

public class SubscriptionThrottlePolicyTemplateBuilder extends ThrottlePolicyTemplateBuilder {

    private static final Log log = LogFactory.getLog(SubscriptionThrottlePolicyTemplateBuilder.class);
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

        if (log.isDebugEnabled()) {
            log.debug("Generating Siddhi app for subscriptionLevel :" + policy.toString());
        }
        //get velocity template for Subscription policy and generate the template
        StringWriter writer = new StringWriter();
        VelocityEngine velocityengine = initVelocityEngine();
        Template template = velocityengine.getTemplate(getTemplatePathForSubscription());
        VelocityContext context = new VelocityContext();
        setConstantContext(context);
        //set values for velocity context
        context.put(POLICY, policy);
        context.put(QUOTA_POLICY, policy.getDefaultQuotaPolicy());
        template.merge(context, writer);
        if (log.isDebugEnabled()) {
            log.debug("Generated Siddhi app for policy : " + writer.toString());
        }
        return writer.toString();
    }

    /**
     * Get template path for subscription policy.
     *
     * @return Path as a string
     */
    private String getTemplatePathForSubscription() {
        return policyTemplateLocation + POLICY_VELOCITY_SUB + XML_EXTENSION;
    }

    @Override public Map<String, String> getThrottlePolicyTemplate() throws APITemplateException {
        try {
            templateMap
                    .put(subscriptionPolicy.getPolicyName(), getThrottlePolicyForSubscriptionLevel(subscriptionPolicy));
        } catch (APITemplateException e) {
            String errorMessage = "Error while creating template for subscription throttle policy.";
            log.error(errorMessage, e);
            throw new APITemplateException(errorMessage, ExceptionCodes.THROTTLE_TEMPLATE_EXCEPTION);
        }
        return templateMap;
    }
}
