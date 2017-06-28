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
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;

import java.io.StringWriter;
import java.util.Map;

/**
 * Siddhi query template builder for Custom throttle policy.
 */
public class CustomThrottlePolicyTemplateBuilder extends ThrottlePolicyTemplateBuilder {
    private static final String POLICY_VELOCITY_GLOBAL = "throttle_policy_template_global";
    private static final Log log = LogFactory.getLog(ThrottlePolicyTemplateBuilder.class);
    private CustomPolicy customPolicy;

    public CustomThrottlePolicyTemplateBuilder(CustomPolicy customPolicy) {
        this.customPolicy = customPolicy;
    }

    /**
     * Generate policy for global level.
     *
     * @param policy policy with level 'global'. Multiple pipelines are not allowed. Can define more than one condition
     *               as set of conditions. all these conditions should be passed as a single pipeline
     * @return policy template as a string
     * @throws APITemplateException throws if any error occurred
     */
    public String getThrottlePolicyForGlobalLevel(CustomPolicy policy) throws APITemplateException {
        StringWriter writer = new StringWriter();

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for globalLevel :" + policy.toString());
        }

        try {
            VelocityEngine velocityengine = initVelocityEngine();

            Template template = velocityengine.getTemplate(getTemplatePathForGlobal());

            VelocityContext context = new VelocityContext();
            setConstantContext(context);
            context.put("policy", policy);
            if (log.isDebugEnabled()) {
                log.debug("Policy : " + writer.toString());
            }
            template.merge(context, writer);
        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", ExceptionCodes.THROTTLE_TEMPLATE_EXCEPTION);
        }

        return writer.toString();
    }

    private String getTemplatePathForGlobal() {
        return policyTemplateLocation + POLICY_VELOCITY_GLOBAL + ".xml";
    }

    @Override public Map<String, String> getThrottlePolicyTemplate() {
        try {
            templateMap.put("default", getThrottlePolicyForGlobalLevel(customPolicy));
        } catch (APITemplateException e) {
            String errorMessage = "Error while creating template for advaced throttle policy.";
            log.error(errorMessage, e);
        }
        return templateMap;
    }
}
