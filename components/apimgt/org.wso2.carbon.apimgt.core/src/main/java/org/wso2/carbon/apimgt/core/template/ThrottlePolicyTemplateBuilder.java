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
*
*/
package org.wso2.carbon.apimgt.core.template;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Generate throttle policy using velocity template
 */
abstract class ThrottlePolicyTemplateBuilder {

    protected String policyTemplateLocation =
            "resources" + File.separator + "template" + File.separator + "policy_templates" + File.separator;
    protected Map<String, String> templateMap = new HashMap<>();

    /**
     * Set the location of the policy templates. If not set, default location is used
     *
     * @param path custom policy path
     */
    protected void setPolicyTemplateLocation(String path) {
        policyTemplateLocation = path;
    }

    /**
     * Set velocity context.
     *
     * @param context VelocityContext object to be set
     */
    protected static void setConstantContext(VelocityContext context) {
        context.put("ACROSS_ALL", PolicyConstants.ACROSS_ALL);
        context.put("PER_USER", PolicyConstants.PER_USER);
        context.put("POLICY_LEVEL_API", PolicyConstants.POLICY_LEVEL_API);
        context.put("POLICY_LEVEL_APP", PolicyConstants.POLICY_LEVEL_APP);
        context.put("POLICY_LEVEL_SUB", PolicyConstants.POLICY_LEVEL_SUB);
        context.put("POLICY_LEVEL_GLOBAL", PolicyConstants.POLICY_LEVEL_GLOBAL);
        context.put("REQUEST_COUNT_TYPE", PolicyConstants.REQUEST_COUNT_TYPE);
        context.put("BANDWIDTH_TYPE", PolicyConstants.BANDWIDTH_TYPE);
    }

    /**
     * Init velocity engine.
     *
     * @return Velocity engine for each throttle template
     */
    public VelocityEngine initVelocityEngine() {
        VelocityEngine velocityengine = new VelocityEngine();
        velocityengine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityengine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new CommonsLogLogChute());
        velocityengine.init();
        return velocityengine;
    }

    /**
     * Return siddhi query template for policy level.
     *
     * @return Map with policy file(siddhi app name) and siddhi query
     */
    public abstract Map<String, String> getThrottlePolicyTemplate();
}
