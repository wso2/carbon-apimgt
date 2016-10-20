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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThrottlePolicyTemplateBuilder {

    private static final Log log = LogFactory.getLog(ThrottlePolicyTemplateBuilder.class);
    private static final String POLICY_VELOCITY_RESOURCE = "throttle_policy_template_resource";
    private static final String POLICY_VELOCITY_RESOURCE_DEFAULT = "throttle_policy_template_resource_default";
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
     * @return
     * @throws APITemplateException
     */
    public Map<String, String> getThrottlePolicyForAPILevel(APIPolicy policy) throws APITemplateException {

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for apiLevel :" + policy.toString());
        }
        Map<String, String> policyArray = new HashMap<String, String>();
        Set<String> conditionsSet = new HashSet<String>();
        
        if(!(policy instanceof APIPolicy)){
            throw new APITemplateException("Invalid  policy level : Has to be 'api'");
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

            if (policy.getPipelines() != null) {

                for (Pipeline pipeline : policy.getPipelines()) {
                    context = new VelocityContext();
                    setConstantContext(context);
                    context.put("pipelineItem", pipeline);
                    context.put("policy", policy);

                    context.put("quotaPolicy", pipeline.getQuotaPolicy());
                    context.put("pipeline", "condition_" + pipeline.getId());

                    String conditionString = getPolicyCondition(pipeline.getConditions());
                    conditionsSet.add(conditionString);

                    context.put("condition", " AND " + conditionString);
                    writer = new StringWriter();
                    template.merge(context, writer);
                    if (log.isDebugEnabled()) {
                        log.debug("Policy : " + writer.toString());
                    }

                    String policyName = policy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_RESOURCE + "_" +
                                        policy.getPolicyName() + "_condition_" + pipeline.getId();
                    policyArray.put(policyName, writer.toString());
                }
            }
        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }

        return policyArray;
    }

    /**
     * Generate default policy for api level throttling
     *
     * @param policy Policy with level 'api'. isAcrossAllUsers() method in policy is used to identify the level in
     *            the api level. Policy can have multiple pipelines and a default condition which will be used as
     *            else condition
     * @return
     * @throws APITemplateException
     */
    public String getThrottlePolicyForAPILevelDefault(APIPolicy policy) throws APITemplateException {

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for apiLevel :" + policy.toString());
        }
        Set<String> conditionsSet = new HashSet<String>();

        if(!(policy instanceof APIPolicy)){
            throw new APITemplateException("Invalid policy level : Has to be 'api'");
        }

        try {
            VelocityEngine velocityengine = new VelocityEngine();
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                        "org.apache.velocity.runtime.log.Log4JLogChute");
                velocityengine.setProperty("runtime.log.logsystem.log4j.logger", getVelocityLogger());
            }
            velocityengine.init();
            Template template = velocityengine.getTemplate(getTemplatePathForAPIDefaultPolicy());
            StringWriter writer;
            VelocityContext context;

            List<Pipeline> pipelines = policy.getPipelines();

            if (pipelines != null) {
                for (Pipeline pipeline : pipelines) {
                    String conditionString = getPolicyConditionForDefault(pipeline.getConditions());
                    if(!StringUtils.isEmpty(conditionString)) {
                        conditionsSet.add(conditionString);
                    }
                }
            }

            // for default one
            context = new VelocityContext();
            setConstantContext(context);
            //default policy is defined as 'elseCondition'
            context.put("pipeline", "elseCondition"); //// constant
            context.put("pipelineItem", null);
            context.put("policy", policy);

            context.put("quotaPolicy", policy.getDefaultQuotaPolicy());
            String conditionSetString = getConditionForDefault(conditionsSet);
            if(!StringUtils.isEmpty(conditionSetString)) {
                context.put("condition", " AND " + conditionSetString);
            } else {
                context.put("condition", "");
            }
            writer = new StringWriter();
            template.merge(context, writer);
            if (log.isDebugEnabled()) {
                log.debug("Policy : " + writer.toString());
            }
            return writer.toString();
        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }
    }

    /**
     * Generate policy for global level. 
     * @param policy policy with level 'global'. Multiple pipelines are not allowed. Can define more than one condition
     * as set of conditions. all these conditions should be passed as a single pipeline 
     * @return
     * @throws APITemplateException
     */
    public String getThrottlePolicyForGlobalLevel(GlobalPolicy policy) throws APITemplateException {
        StringWriter writer = new StringWriter();

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for globalLevel :" + policy.toString());
        }

        if (!(policy instanceof GlobalPolicy)) {
            throw new APITemplateException("Invalid policy level : Has to be 'global'");
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
            setConstantContext(context);
            context.put("policy", policy);
           /* if (policy.getPipelines() != null && !policy.getPipelines().isEmpty()) {
                String conditionString = getPolicyCondition(policy.getPipelines().get(0).getConditions());
                context.put("condition", conditionString);
            } else {
                context.put("condition", "");
            }*/
            if (log.isDebugEnabled()) {
                log.debug("Policy : " + writer.toString());
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
    public String getThrottlePolicyForAppLevel(ApplicationPolicy policy) throws APITemplateException {
        StringWriter writer = new StringWriter();

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for appLevel :" + policy.toString());
        }

        if (!(policy instanceof ApplicationPolicy)) {
            throw new APITemplateException("Invalid policy level : Has to be 'app'");
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
            setConstantContext(context);
            context.put("policy", policy);
            context.put("quotaPolicy", policy.getDefaultQuotaPolicy());
            template.merge(context, writer);
            if (log.isDebugEnabled()) {
                log.debug("Policy : " + writer.toString());
            }

        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }
        String str = writer.toString();
        return writer.toString();
    }

    /**
     * Generate policy for subscription level.
     * @param policy policy with level 'sub'. Multiple pipelines are not allowed. Can define more than one condition
     * as set of conditions. all these conditions should be passed as a single pipeline 
     * @return
     * @throws APITemplateException
     */
    public String getThrottlePolicyForSubscriptionLevel(SubscriptionPolicy policy) throws APITemplateException {
        StringWriter writer = new StringWriter();

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for subscriptionLevel :" + policy.toString());
        }

        if (!(policy instanceof SubscriptionPolicy)) {
            throw new APITemplateException("Invalid policy level :  Has to be 'sub'");
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
            setConstantContext(context);
            context.put("policy", policy);
            context.put("quotaPolicy", policy.getDefaultQuotaPolicy());
            t.merge(context, writer);
            if (log.isDebugEnabled()) {
                log.debug("Policy : " + writer.toString());
            }
        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }

        return writer.toString();
    }

    private  String getTemplatePathForAPI() {
        return policyTemplateLocation + ThrottlePolicyTemplateBuilder.POLICY_VELOCITY_RESOURCE + ".xml";
    }

    private  String getTemplatePathForAPIDefaultPolicy() {
        return policyTemplateLocation + ThrottlePolicyTemplateBuilder.POLICY_VELOCITY_RESOURCE_DEFAULT + ".xml";
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
     * Produces final condition inside a pipeline for default policy with null string
     * @param conditions
     * @return
     */
    private static String getPolicyConditionForDefault(List<Condition> conditions) {
        String conditionString = null;
        int i = 0;
        for (Condition condition : conditions) {
            String conditionStringComplete = condition.getCondition();
            if (i == 0) {
                conditionString = conditionStringComplete;
            } else {
                conditionString = conditionString + " AND " + conditionStringComplete;
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
        String conditionString = "";
        int i = 0;
        for (String condition : conditionsSet) {
            String conditionIsolated = PolicyConstants.OPEN_BRACKET + condition + PolicyConstants.CLOSE_BRACKET;
            if (i == 0) {
                conditionString = conditionIsolated;
            } else {
                conditionString = conditionString + " OR " + conditionIsolated;
            }
            i++;
        }

        if(!StringUtils.isEmpty(conditionString)) {
            conditionString = PolicyConstants.INVERT_CONDITION + "(" + conditionString + ")";
        }

        return conditionString;
    }
    
    private static void setConstantContext(VelocityContext context){
        context.put("ACROSS_ALL",PolicyConstants.ACROSS_ALL);
        context.put("PER_USER",PolicyConstants.PER_USER);        
        context.put("POLICY_LEVEL_API",PolicyConstants.POLICY_LEVEL_API);
        context.put("POLICY_LEVEL_APP",PolicyConstants.POLICY_LEVEL_APP);
        context.put("POLICY_LEVEL_SUB",PolicyConstants.POLICY_LEVEL_SUB);
        context.put("POLICY_LEVEL_GLOBAL",PolicyConstants.POLICY_LEVEL_GLOBAL);
        context.put("REQUEST_COUNT_TYPE",PolicyConstants.REQUEST_COUNT_TYPE);
        context.put("BANDWIDTH_TYPE",PolicyConstants.BANDWIDTH_TYPE);
        
    }
}
