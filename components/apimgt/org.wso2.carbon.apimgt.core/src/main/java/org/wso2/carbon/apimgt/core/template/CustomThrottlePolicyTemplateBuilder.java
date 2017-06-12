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
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;

import java.io.StringWriter;

/**
 * Siddhi query template builder for Custom throttle policy.
 */

public class CustomThrottlePolicyTemplateBuilder extends ThrottlePolicyTemplateBuilder {
    private static final String POLICY_VELOCITY_GLOBAL = "throttle_policy_template_global";
    private static final Log log = LogFactory.getLog(CustomThrottlePolicyTemplateBuilder.class);
    private CustomPolicy customPolicy;

    public CustomThrottlePolicyTemplateBuilder(CustomPolicy customPolicy) {
        this.customPolicy = customPolicy;
    }

    /**
     * Generate policy for global level.
     *
     * @return policy template as a string
     * @throws APITemplateException throws if any error occurred
     */
    public String getThrottlePolicyTemplateForCustomPolicy() throws APITemplateException {

        if (log.isDebugEnabled()) {
            log.debug("Generating Siddhi app for custom policy :" + customPolicy.toString());
        }

        //get velocity template for custom throttle policy and generate the template
        StringWriter writer = new StringWriter();
        VelocityContext context = new VelocityContext();
        VelocityEngine velocityengine = initVelocityEngine();
        Template template = velocityengine.getTemplate(getTemplatePathForGlobal());
        setConstantContext(context);
        //set values for velocity context
        context.put(POLICY, customPolicy);
        template.merge(context, writer);
        if (log.isDebugEnabled()) {
            log.debug("Generated Siddhi app for policy : " + writer.toString());
        }
        return writer.toString();
    }

    /**
     * Get the template path for Custom Policy
     *
     * @return Path as a string
     */
    private String getTemplatePathForGlobal() {
        return policyTemplateLocation + POLICY_VELOCITY_GLOBAL + XML_EXTENSION;
    }

}
