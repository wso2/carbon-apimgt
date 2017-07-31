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

/**
 * Generate throttle policy using velocity template
 */

abstract class ThrottlePolicyTemplateBuilder {

    protected String policyTemplateLocation =
            "resources" + File.separator + "template" + File.separator + "policy_templates" + File.separator;
    public static final String PIPELINE_ITEM = "pipelineItem";
    public static final String PIPELINE = "pipeline";
    public static final String POLICY = "policy";
    public static final String QUOTA_POLICY = "quotaPolicy";
    public static final String CONDITION = "condition";
    public static final String UNDERSCORE = "_";
    public static final String AND = " AND ";
    public static final String OR = " OR ";
    public static final String ELSE_CONDITION = "elseCondition";
    public static final String XML_EXTENSION = ".xml";
    public static final String CLASS_PATH = "classpath";
    public static final String CLASS_PATH_RESOURCE_LOADER = "classpath.resource.loader.class";
    public static final String ACROSS_ALL = "ACROSS_ALL";
    public static final String PER_USER = "PER_USER";
    public static final String POLICY_LEVEL_API = "POLICY_LEVEL_API";
    public static final String POLICY_LEVEL_APP = "POLICY_LEVEL_APP";
    public static final String POLICY_LEVEL_SUB = "POLICY_LEVEL_SUB";
    public static final String POLICY_LEVEL_CUSTOM = "POLICY_LEVEL_GLOBAL";
    public static final String REQUEST_COUNT_TYPE = "REQUEST_COUNT_TYPE";
    public static final String BANDWIDTH_TYPE = "BANDWIDTH_TYPE";

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
        context.put(ACROSS_ALL, PolicyConstants.ACROSS_ALL);
        context.put(PER_USER, PolicyConstants.PER_USER);
        context.put(POLICY_LEVEL_API, PolicyConstants.POLICY_LEVEL_API);
        context.put(POLICY_LEVEL_APP, PolicyConstants.POLICY_LEVEL_APP);
        context.put(POLICY_LEVEL_SUB, PolicyConstants.POLICY_LEVEL_SUB);
        context.put(POLICY_LEVEL_CUSTOM, PolicyConstants.POLICY_LEVEL_GLOBAL);
        context.put(REQUEST_COUNT_TYPE, PolicyConstants.REQUEST_COUNT_TYPE);
        context.put(BANDWIDTH_TYPE, PolicyConstants.BANDWIDTH_TYPE);
    }

    /**
     * Init velocity engine.
     *
     * @return Velocity engine for each throttle template
     */
    public VelocityEngine initVelocityEngine() {
        VelocityEngine velocityengine = new VelocityEngine();
        velocityengine.setProperty(RuntimeConstants.RESOURCE_LOADER, CLASS_PATH);
        velocityengine.setProperty(CLASS_PATH_RESOURCE_LOADER, ClasspathResourceLoader.class.getName());
        velocityengine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new CommonsLogLogChute());
        velocityengine.init();
        return velocityengine;
    }
    
}
