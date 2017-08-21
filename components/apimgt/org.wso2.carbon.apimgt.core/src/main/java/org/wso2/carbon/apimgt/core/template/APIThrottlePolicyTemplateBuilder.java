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


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Condition;
import org.wso2.carbon.apimgt.core.models.policy.Pipeline;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Siddhi query builder for API throttle policy.
 */

public class APIThrottlePolicyTemplateBuilder extends ThrottlePolicyTemplateBuilder {

    private static final Log log = LogFactory.getLog(APIThrottlePolicyTemplateBuilder.class);
    private static final String POLICY_VELOCITY_RESOURCE = "throttle_policy_template_resource";
    private static final String POLICY_VELOCITY_RESOURCE_DEFAULT = "throttle_policy_template_resource_default";
    private static final String EMPTY_STRING = "";
    private APIPolicy apiPolicy;

    public APIThrottlePolicyTemplateBuilder(APIPolicy apiPolicy) {
        this.apiPolicy = apiPolicy;
    }

    /**
     * Generate policy for api level throttling
     *
     * @return throttle policies for api level
     * @throws APITemplateException throws if generation failure occur
     */
    public Map<String, String> getThrottlePolicyTemplateForPipelines() throws APITemplateException {

        if (log.isDebugEnabled()) {
            log.debug("Generating Siddhi App for apiLevel :" + apiPolicy.toString());
        }
        //get velocity template for API policy pipeline and generate the template
        Map<String, String> policyArray = new HashMap<String, String>();
        StringWriter writer;
        VelocityContext context;
        VelocityEngine velocityengine = initVelocityEngine();
        Template template = velocityengine.getTemplate(getTemplatePathForAPI());
        //Generate template for pipeline conditions if pipelines not null
        if (apiPolicy.getPipelines() != null) {
            for (Pipeline pipeline : apiPolicy.getPipelines()) {
                //set values for velocity context
                context = new VelocityContext();
                setConstantContext(context);
                context.put(PIPELINE_ITEM, pipeline);
                context.put(POLICY, apiPolicy);
                context.put(QUOTA_POLICY, pipeline.getQuotaPolicy());
                context.put(PIPELINE,
                        CONDITION + UNDERSCORE + pipeline.getId());
                String conditionString = getPolicyCondition(pipeline.getConditions());
                context.put(CONDITION, AND + conditionString);
                writer = new StringWriter();
                template.merge(context, writer);
                if (log.isDebugEnabled()) {
                    log.debug("Generated Siddhi App : " + writer.toString());
                }
                String policyName =
                        PolicyConstants.POLICY_LEVEL_RESOURCE + UNDERSCORE + apiPolicy.getPolicyName() + UNDERSCORE
                                + CONDITION + UNDERSCORE + pipeline.getId();
                policyArray.put(policyName, writer.toString());
            }
        }

        return policyArray;
    }

    /**
     * Generate default policy for api level throttling
     *
     * @return throttle policies for default api
     * @throws APITemplateException throws if generation failure occur
     */
    public String getThrottlePolicyTemplateForAPILevelDefaultCondition() throws APITemplateException {

        if (log.isDebugEnabled()) {
            log.debug("Generating Siddhi App for apiLevel :" + apiPolicy.toString());
        }
        //get velocity template for API policy and generate the template
        Set<String> conditionsSet = new HashSet<String>();
        List<Pipeline> pipelines = apiPolicy.getPipelines();
        VelocityEngine velocityengine = initVelocityEngine();
        Template template = velocityengine.getTemplate(getTemplatePathForAPIDefaultPolicy());
        StringWriter writer;
        VelocityContext context;
        //when APIPolicy contains pipelines, get template as a string
        if (pipelines != null) {
            for (Pipeline pipeline : pipelines) {
                String conditionString = getPolicyConditionForDefault(pipeline.getConditions());
                if (!StringUtils.isEmpty(conditionString)) {
                    conditionsSet.add(conditionString);
                }
            }
        }
        // for default API policy
        context = new VelocityContext();
        setConstantContext(context);
        //default policy is defined as 'elseCondition' , set values for velocity context
        context.put(PIPELINE, ELSE_CONDITION);
        context.put(PIPELINE_ITEM, null);
        context.put(POLICY, apiPolicy);
        context.put(QUOTA_POLICY, apiPolicy.getDefaultQuotaPolicy());
        String conditionSetString = getConditionForDefault(conditionsSet);
        if (!StringUtils.isEmpty(conditionSetString)) {
            context.put(CONDITION, AND + conditionSetString);
        } else {
            context.put(CONDITION, EMPTY_STRING);
        }
        writer = new StringWriter();
        template.merge(context, writer);
        if (log.isDebugEnabled()) {
            log.debug("Generated Siddhi App : " + writer.toString());
        }
        return writer.toString();
    }

    /**
     * Api policy template path for pipelines.
     *
     * @return path as a string
     */
    private String getTemplatePathForAPI() {
        return policyTemplateLocation + POLICY_VELOCITY_RESOURCE + XML_EXTENSION;
    }

    /**
     * Api policy template path for default condition.
     *
     * @return path as a string
     */
    private String getTemplatePathForAPIDefaultPolicy() {
        return policyTemplateLocation + POLICY_VELOCITY_RESOURCE_DEFAULT + XML_EXTENSION;
    }

    /**
     * Produces final condition inside a pipeline.
     *
     * @param conditions list of throttle conditions
     * @return formatted condition list
     */
    private static String getPolicyCondition(List<Condition> conditions) {
        String conditionString = null;
        int i = 0;
        for (Condition condition : conditions) {
            if (i == 0) {
                conditionString = condition.getCondition();
            } else {
                conditionString = conditionString + AND + condition.getCondition();
            }
            i++;
        }
        return conditionString;
    }

    /**
     * Produces final condition inside a pipeline for default policy with null string
     *
     * @param conditions list of default throttle conditions
     * @return formatted condition list
     */
    private static String getPolicyConditionForDefault(List<Condition> conditions) {
        String conditionString = null;
        int i = 0;
        for (Condition condition : conditions) {
            String conditionStringComplete = condition.getCondition();
            if (i == 0) {
                conditionString = conditionStringComplete;
            } else {
                conditionString = conditionString + AND + conditionStringComplete;
            }
            i++;
        }
        return conditionString;
    }

    /**
     * Generate the condition for the default query. This returns the condition to check thing that are not in
     * any of the other conditions
     *
     * @param conditionsSet list of default throttle conditions
     * @return formatted condition list
     */
    private static String getConditionForDefault(Set<String> conditionsSet) {
        String conditionString = "";
        int i = 0;
        for (String condition : conditionsSet) {
            String conditionIsolated = PolicyConstants.OPEN_BRACKET + condition + PolicyConstants.CLOSE_BRACKET;
            if (i == 0) {
                conditionString = conditionIsolated;
            } else {
                conditionString = conditionString + OR + conditionIsolated;
            }
            i++;
        }
        if (!StringUtils.isEmpty(conditionString)) {
            conditionString = PolicyConstants.INVERT_CONDITION + "(" + conditionString + ")";
        }
        return conditionString;
    }

}
