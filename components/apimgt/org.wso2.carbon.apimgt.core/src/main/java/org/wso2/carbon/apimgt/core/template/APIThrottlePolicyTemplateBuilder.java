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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Siddhi query builder for API throttle policy.
 */
public class APIThrottlePolicyTemplateBuilder extends ThrottlePolicyTemplateBuilder {

    private static final Log log = LogFactory.getLog(ThrottlePolicyTemplateBuilder.class);
    private static final String POLICY_VELOCITY_RESOURCE = "throttle_policy_template_resource";
    private static final String POLICY_VELOCITY_RESOURCE_DEFAULT = "throttle_policy_template_resource_default";
    private APIPolicy apiPolicy;

    public APIThrottlePolicyTemplateBuilder(APIPolicy apiPolicy) {
        this.apiPolicy = apiPolicy;
    }

    /**
     * Generate policy for api level throttling
     *
     * @param policy Policy with level 'api'. isAcrossAllUsers() method in policy is used to identify the level in
     *               the api level. Policy can have multiple pipelines and a default condition which will be used as
     *               else condition
     * @return throttle policies for api level
     * @throws APITemplateException throws if generation failure occur
     */
    public Map<String, String> getThrottlePolicyForAPILevel(APIPolicy policy) throws APITemplateException {

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for apiLevel :" + policy.toString());
        }
        Map<String, String> policyArray = new HashMap<String, String>();
        VelocityEngine velocityengine = initVelocityEngine();
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
                //                    conditionsSet.add(conditionString);

                context.put("condition", " AND " + conditionString);
                writer = new StringWriter();
                template.merge(context, writer);
                if (log.isDebugEnabled()) {
                    log.debug("Policy : " + writer.toString());
                }

                String policyName =
                        PolicyConstants.POLICY_LEVEL_RESOURCE + "_" + policy.getPolicyName() + "_condition_" + pipeline
                                .getId();
                policyArray.put(policyName, writer.toString());
            }
        }

        return policyArray;
    }

    /**
     * Generate default policy for api level throttling
     *
     * @param policy Policy with level 'api'. isAcrossAllUsers() method in policy is used to identify the level in
     *               the api level. Policy can have multiple pipelines and a default condition which will be used as
     *               else condition
     * @return throttle policies for default api
     * @throws APITemplateException throws if generation failure occur
     */
    public String getThrottlePolicyForAPILevelDefault(APIPolicy policy) throws APITemplateException {

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for apiLevel :" + policy.toString());
        }
        Set<String> conditionsSet = new HashSet<String>();

        VelocityEngine velocityengine = initVelocityEngine();
        Template template = velocityengine.getTemplate(getTemplatePathForAPIDefaultPolicy());
        StringWriter writer;
        VelocityContext context;

        List<Pipeline> pipelines = policy.getPipelines();

        if (pipelines != null) {
            for (Pipeline pipeline : pipelines) {
                String conditionString = getPolicyConditionForDefault(pipeline.getConditions());
                if (!StringUtils.isEmpty(conditionString)) {
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
        if (!StringUtils.isEmpty(conditionSetString)) {
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
    }

    private String getTemplatePathForAPI() {
        return policyTemplateLocation + POLICY_VELOCITY_RESOURCE + ".xml";
    }

    private String getTemplatePathForAPIDefaultPolicy() {
        return policyTemplateLocation + POLICY_VELOCITY_RESOURCE_DEFAULT + ".xml";
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
                conditionString = conditionString + " AND " + condition.getCondition();
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
                conditionString = conditionString + " AND " + conditionStringComplete;
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
                conditionString = conditionString + " OR " + conditionIsolated;
            }
            i++;
        }

        if (!StringUtils.isEmpty(conditionString)) {
            conditionString = PolicyConstants.INVERT_CONDITION + "(" + conditionString + ")";
        }

        return conditionString;
    }

    @Override
    public Map<String, String> getThrottlePolicyTemplate() {
        try {
            templateMap = getThrottlePolicyForAPILevel(apiPolicy);
            templateMap.put("default", getThrottlePolicyForAPILevelDefault(apiPolicy));
        } catch (APITemplateException e) {
            String errorMessage = "Error while creating template for advaced throttle policy.";
            log.error(errorMessage, e);
        }
        return templateMap;
    }

    public List<String> getAPIPolicyName() {
        List<String> names = new ArrayList<>();
        if (apiPolicy.getPipelines() != null) {
            for (Pipeline pipeline : apiPolicy.getPipelines()) {
                String policyName =
                        PolicyConstants.POLICY_LEVEL_RESOURCE + "_" + apiPolicy.getPolicyName() + "_condition_"
                                + pipeline.getId();
                names.add(policyName);
            }
        }
        String policyName = PolicyConstants.POLICY_LEVEL_RESOURCE + "_" + apiPolicy.getPolicyName() + "_default";
        names.add(policyName);
        return names;
    }
}
