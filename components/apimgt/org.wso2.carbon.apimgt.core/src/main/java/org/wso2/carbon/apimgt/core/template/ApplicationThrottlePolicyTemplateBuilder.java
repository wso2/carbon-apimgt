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
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;

import java.io.StringWriter;

/**
 * Siddhi query builder for application throttle policy.
 */

public class ApplicationThrottlePolicyTemplateBuilder extends ThrottlePolicyTemplateBuilder {

    private static final Log log = LogFactory.getLog(ApplicationThrottlePolicyTemplateBuilder.class);
    private static final String POLICY_VELOCITY_APP = "throttle_policy_template_app";
    private ApplicationPolicy applicationPolicy;

    public ApplicationThrottlePolicyTemplateBuilder(ApplicationPolicy applicationPolicy) {
        this.applicationPolicy = applicationPolicy;
    }

    /**
     * Generate application level policy.
     *
     * @return throttle policies for app level
     * @throws APITemplateException throws if generation failure occur
     */
    public String getThrottlePolicyForAppLevel() throws APITemplateException {

        if (log.isDebugEnabled()) {
            log.debug("Generating Siddhi app for appLevel :" + applicationPolicy.toString());
        }
        //get velocity template for Application policy and generate the template
        StringWriter writer = new StringWriter();
        VelocityEngine velocityengine = initVelocityEngine();
        Template template = velocityengine.getTemplate(getTemplatePathForApplication());
        VelocityContext context = new VelocityContext();
        setConstantContext(context);
        //set values for velocity context
        context.put(POLICY, applicationPolicy);
        context.put(QUOTA_POLICY, applicationPolicy.getDefaultQuotaPolicy());
        template.merge(context, writer);
        if (log.isDebugEnabled()) {
            log.debug("Generated Siddhi app for policy : " + writer.toString());
        }
        return writer.toString();
    }

    /**
     * Get the template path for the application policy.
     *
     * @return Path as a string
     */
    private String getTemplatePathForApplication() {
        return policyTemplateLocation + POLICY_VELOCITY_APP + XML_EXTENSION;
    }

}
