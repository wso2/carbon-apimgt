/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.template;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

public class ThrottlePolicyTemplateBuilder {

    private static final Log log = LogFactory.getLog(ThrottlePolicyTemplateBuilder.class);
    private static final String POLICY_VELOCITY = "throttle_policy_template";
    private static final String POLICY_VELOCITY_API = "throttle_policy_template_api";
    private static final String POLICY_VELOCITY_GLOBAL = "throttle_policy_template_global";
    private static final String POLICY_VELOCITY_APP = "throttle_policy_template_app";
    private static final String POLICY_VELOCITY_SUB = "throttle_policy_template_sub";
    private String policyTemplateLocation = "repository" + File.separator + "resources" + File.separator
            + "policy_templates" + File.separator;
    private static String velocityLogPath = "not-defined";
    
    /**
     * Set the location of the policy templates. If not set, default location is used
     * @param path
     */
    public void setPolicyTemplateLocation(String path){
        policyTemplateLocation = path;
    }

    

    /**
     * Generate policy for api level throttling
     * 
     * @param policy Policy with level 'api'. isAcrossAllUsers() method in policy is used to identify the level in 
     *            the api level. Policy can have multiple pipelines and a default condition which will be used as 
     *            else condition
     * @param apiName
     * @param apiVersion
     * @param apiContext
     * @return
     * @throws APITemplateException
     */
    public List<String> getThrottlePolicyForAPILevel(Policy policy, String apiName, String apiVersion,
            String apiContext) throws APITemplateException {

        List<String> policyArray = new ArrayList<String>();
        Set<String> conditionsSet = new HashSet<String>();
        
        //TODO move to constant
        if(!"api".equals(policy.getPolicyLevel())){
            throw new APITemplateException("Invalid policy level :" + policy.getPolicyLevel() + ". Has to be 'api'");
        }

        try {
            VelocityEngine velocityengine = new VelocityEngine();
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                        "org.apache.velocity.runtime.log.Log4JLogChute");
                velocityengine.setProperty("runtime.log.logsystem.log4j.logger", getVelocityLogger());
            }
            velocityengine.init();
            Template template = velocityengine.getTemplate(getTemplatePathForAPI());
            StringWriter writer;
            VelocityContext context;

            // for pipelines
            int condition = 0;
            for (Pipeline pipeline : policy.getPipelines()) {
                context = new VelocityContext();
                context.put("pipelineItem", pipeline);
                context.put("policy", policy);

                // stuff from the api
                context.put("apiname", apiName);
                context.put("apicontext", apiContext);
                context.put("apiversion", apiVersion);

                context.put("quotaPolicy", pipeline.getQuotaPolicy());
                //pipeline name is defined as 'condition0' 'condition1' etc
                context.put("pipeline", "condition" + condition);

                String conditionString = getPolicyCondition(pipeline.getCondition());
                conditionsSet.add(conditionString);

                context.put("condition", " AND " + conditionString);
                writer = new StringWriter();
                template.merge(context, writer);
                policyArray.add(writer.toString());

                condition++;
            }

            // for default one

            context = new VelocityContext();
           //default policy is defined as 'elseCondition' 
            context.put("pipeline", "elseCondition"); //// constant
            context.put("pipelineItem", null);
            context.put("policy", policy);
            // stuff from the api
            context.put("apiname", apiName);
            context.put("apicontext", apiContext);
            context.put("apiversion", apiVersion);

            context.put("quotaPolicy", policy.getDefaultQuotaPolicy());
            context.put("condition", " AND " + getConditionForDefault(conditionsSet));
            writer = new StringWriter();
            template.merge(context, writer);
            policyArray.add(writer.toString());

        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }

        return policyArray;
    }

    /**
     * Generate policy for global level. 
     * @param policy policy with level 'global'. Multiple pipelines are not allowed. Can define more than one condition
     * as set of conditions. all these conditions should be passed as a single pipeline 
     * @return
     * @throws APITemplateException
     */
    public String getThrottlePolicyForGlobalLevel(Policy policy) throws APITemplateException {
        StringWriter writer = new StringWriter();
        //TODO move to constant
        if(!"global".equals(policy.getPolicyLevel())){
            throw new APITemplateException("Invalid policy level :" + policy.getPolicyLevel() + ". Has to be 'global'");
        }
        try {
            VelocityEngine velocityengine = new VelocityEngine();
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                        "org.apache.velocity.runtime.log.Log4JLogChute");
                velocityengine.setProperty("runtime.log.logsystem.log4j.logger", getVelocityLogger());
            }
            velocityengine.init();

            Template template = velocityengine.getTemplate(getTemplatePathForGlobal());

            VelocityContext context = new VelocityContext();
            context.put("policy", policy);
            if (policy.getPipelines() != null && !policy.getPipelines().isEmpty()) {
                String conditionString = getPolicyCondition(policy.getPipelines().get(0).getCondition());
                context.put("condition", conditionString);
            } else {
                context.put("condition", "");
            }

            template.merge(context, writer);
        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }

        return writer.toString();
    }

    /**
     * Generate application level policy.
     * 
     * @param policy policy with level 'app'. Multiple pipelines are not allowed. Can define more than one condition
     *            as set of conditions. all these conditions should be passed as a single pipeline
     * @return
     * @throws APITemplateException
     */
    public String getThrottlePolicyForAppLevel(Policy policy) throws APITemplateException {
        StringWriter writer = new StringWriter();
        // TODO move to constant
        if (!"app".equals(policy.getPolicyLevel())) {
            throw new APITemplateException("Invalid policy level :" + policy.getPolicyLevel() + ". Has to be 'app'");
        }
        try {
            VelocityEngine velocityengine = new VelocityEngine();
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                        "org.apache.velocity.runtime.log.Log4JLogChute");
                velocityengine.setProperty("runtime.log.logsystem.log4j.logger", getVelocityLogger());
            }
            velocityengine.init();
            Template template = velocityengine.getTemplate(getTemplatePathForApplication());

            VelocityContext context = new VelocityContext();
            context.put("policy", policy);
            if (policy.getPipelines() != null && !policy.getPipelines().isEmpty()) {
                String conditionString = getPolicyCondition(policy.getPipelines().get(0).getCondition());
                context.put("condition", " AND " + conditionString);
            } else {
                context.put("condition", "");
            }
            context.put("quotaPolicy", policy.getDefaultQuotaPolicy());
            template.merge(context, writer);

        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }

        return writer.toString();
    }

    /**
     * Generate policy for subscription level.
     * @param policy policy with level 'sub'. Multiple pipelines are not allowed. Can define more than one condition
     * as set of conditions. all these conditions should be passed as a single pipeline 
     * @return
     * @throws APITemplateException
     */
    public String getThrottlePolicyForSubscriptionLevel(Policy policy) throws APITemplateException {
        StringWriter writer = new StringWriter();
        //TODO move to constant
        if(!"sub".equals(policy.getPolicyLevel())){
            throw new APITemplateException("Invalid policy level :" + policy.getPolicyLevel() + ". Has to be 'sub'");
        }
        try {
            VelocityEngine velocityengine = new VelocityEngine();
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                        "org.apache.velocity.runtime.log.Log4JLogChute");
                velocityengine.setProperty("runtime.log.logsystem.log4j.logger", getVelocityLogger());
            }
            velocityengine.init();
            Template t = velocityengine.getTemplate(getTemplatePathForSubscription());

            VelocityContext context = new VelocityContext();
            context.put("policy", policy);
            if (policy.getPipelines() != null && !policy.getPipelines().isEmpty()) {
                String conditionString = getPolicyCondition(policy.getPipelines().get(0).getCondition());
                context.put("condition", " AND " + conditionString);
            } else {
                context.put("condition", "");
            }

            context.put("quotaPolicy", policy.getDefaultQuotaPolicy());
            t.merge(context, writer);
        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }

        return writer.toString();
    }

    private  String getTemplatePathForAPI() {
        return policyTemplateLocation + ThrottlePolicyTemplateBuilder.POLICY_VELOCITY_API + ".xml";
    }

    private  String getTemplatePathForGlobal() {
        return policyTemplateLocation + ThrottlePolicyTemplateBuilder.POLICY_VELOCITY_GLOBAL + ".xml";
    }

    private  String getTemplatePathForApplication() {
        return policyTemplateLocation + ThrottlePolicyTemplateBuilder.POLICY_VELOCITY_APP + ".xml";
    }

    private  String getTemplatePathForSubscription() {
        return policyTemplateLocation + ThrottlePolicyTemplateBuilder.POLICY_VELOCITY_SUB + ".xml";
    }

    private static String getVelocityLogger() {
        if (velocityLogPath != null) {
            return velocityLogPath;
        } else {
            APIManagerConfigurationService config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService();
            String logPath = config.getAPIManagerConfiguration().getFirstProperty(APIConstants.VELOCITY_LOGGER);
            if (logPath != null && !logPath.isEmpty()) {
                velocityLogPath = logPath;
            }
            return velocityLogPath;
        }
    }

    /**
     * Produces final condition inside a pipeline
     * @param conditions
     * @return
     */
    private static String getPolicyCondition(List<Condition> conditions) {
        String conditionString = null;
        int i = 0;
        for (Condition condition : conditions) {
            if (i == 0) {
                conditionString = condition.getCondition();
            } else {
                conditionString = conditionString + " AND " + condition.getCondition();
            }
            i++;
        }
        return conditionString;
    }

    /**
     * Generate the condition for the default query. This returns the condition to check thing that are not in 
     * any of the other conditions
     * @param conditionsSet
     * @return
     */
    private static String getConditionForDefault(Set<String> conditionsSet) {
        String conditionString = null;
        int i = 0;
        for (String condition : conditionsSet) {
            if (i == 0) {
                conditionString = condition;
            } else {
                conditionString = conditionString + " OR " + condition;
            }
            i++;
        }

        conditionString = "!(" + conditionString + ")";
        return conditionString;
    }
}
