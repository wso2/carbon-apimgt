/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.throttle.policy.deployer.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.policy.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.APIPolicyConditionGroup;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApplicationPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApiPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.Condition;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.GlobalPolicy;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.*;

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
     *
     * @param path
     */
    public void setPolicyTemplateLocation(String path) {
        policyTemplateLocation = path;
    }


    /**
     * Generate policy for api level throttling
     *
     * @param policy Policy with level 'api'. isAcrossAllUsers() method in policy is used to identify the level in
     *               the api level. Policy can have multiple pipelines and a default condition which will be used as
     *               else condition
     * @return
     * @throws APITemplateException
     */
    public Map<String, String> getThrottlePolicyForAPILevel(ApiPolicy policy) throws APITemplateException {

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for apiLevel :" + policy.toString());
        }
        Map<String, String> policyArray = new HashMap<String, String>();
        Set<String> conditionsSet = new HashSet<String>();

        try {
            VelocityEngine velocityengine = new VelocityEngine();
            velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                    CommonsLogLogChute.class.getName());
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
                velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            }
            velocityengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            velocityengine.init();
            Template template = velocityengine.getTemplate(getTemplatePathForAPI());
            StringWriter writer;
            VelocityContext context;

            if (policy.getConditionGroups() != null) {

                for (APIPolicyConditionGroup conditionGroup : policy.getConditionGroups()) {
                    if (conditionGroup.getDefaultLimit() == null) {
                        continue;
                    }
                    context = new VelocityContext();
                    setConstantContext(context);
                    context.put("policy", policy);

                    context.put("quotaPolicy", conditionGroup.getDefaultLimit());
                    context.put("pipeline", "condition_" + conditionGroup.getConditionGroupId());

                    String conditionString = getPolicyCondition(conditionGroup.getCondition());

                    JSONArray conditions = new JSONArray();
                    conditions.add(getPolicyConditionJson(conditionGroup.getCondition()));
                    conditionsSet.add(conditionString);

                    context.put("condition", " AND " + conditionString);
                    context.put("evaluatedConditions", new String(Base64.encodeBase64(conditions.toJSONString()
                            .getBytes())));
                    writer = new StringWriter();
                    template.merge(context, writer);
                    if (log.isDebugEnabled()) {
                        log.debug("Policy : " + writer.toString());
                    }

                    String policyName = policy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_RESOURCE + "_" +
                            policy.getName() + "_condition_" + conditionGroup.getConditionGroupId();
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
     *               the api level. Policy can have multiple pipelines and a default condition which will be used as
     *               else condition
     * @return
     * @throws APITemplateException
     */
    public String getThrottlePolicyForAPILevelDefault(ApiPolicy policy) throws APITemplateException {

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for apiLevel :" + policy.toString());
        }
        Set<String> conditionsSet = new HashSet<String>();

        try {
            VelocityEngine velocityengine = new VelocityEngine();
            velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                    CommonsLogLogChute.class.getName());
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
                velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            }
            velocityengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            velocityengine.init();
            Template template = velocityengine.getTemplate(getTemplatePathForAPIDefaultPolicy());
            StringWriter writer;
            VelocityContext context;

            List<APIPolicyConditionGroup> conditionGroups = policy.getConditionGroups();
            JSONArray policyConditionJson = new JSONArray();
            if (conditionGroups != null) {
                for (APIPolicyConditionGroup conditionGroup : conditionGroups) {
                    policyConditionJson.add(getPolicyConditionJson(conditionGroup.getCondition()));
                    String conditionString = getPolicyConditionForDefault(conditionGroup.getCondition());
                    if (!StringUtils.isEmpty(conditionString)) {
                        conditionsSet.add(conditionString);
                    }
                }
            }

            // for default one
            context = new VelocityContext();
            setConstantContext(context);
            //default policy is defined as 'elseCondition'
            context.put("policy", policy);

            context.put("quotaPolicy", policy.getDefaultLimit());
            context.put("evaluatedConditions", new String(Base64.encodeBase64(policyConditionJson.toJSONString().getBytes())));
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
        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }
    }

    /**
     * Generate policy for global level.
     *
     * @param policy policy with level 'global'. Multiple pipelines are not allowed. Can define more than one condition
     *               as set of conditions. all these conditions should be passed as a single pipeline
     * @return
     * @throws APITemplateException
     */
    public String getThrottlePolicyForGlobalLevel(GlobalPolicy policy) throws APITemplateException {
        StringWriter writer = new StringWriter();

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for globalLevel :" + policy.toString());
        }
        try {
            VelocityEngine velocityengine = new VelocityEngine();
            velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                    CommonsLogLogChute.class.getName());
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
                velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            }
            velocityengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            velocityengine.init();

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
            throw new APITemplateException("Velocity Error", e);
        }

        return writer.toString();
    }

    /**
     * Generate application level policy.
     *
     * @param policy policy with level 'app'. Multiple pipelines are not allowed. Can define more than one condition
     *               as set of conditions. all these conditions should be passed as a single pipeline
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
            velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                    CommonsLogLogChute.class.getName());
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
                velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            }
            velocityengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            velocityengine.init();
            Template template = velocityengine.getTemplate(getTemplatePathForApplication());

            VelocityContext context = new VelocityContext();
            setConstantContext(context);
            context.put("policy", policy);
            context.put("quotaPolicy", policy.getDefaultLimit());
            template.merge(context, writer);
            if (log.isDebugEnabled()) {
                log.debug("Policy : " + writer.toString());
            }

        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }
        return writer.toString();
    }

    /**
     * Generate policy for subscription level.
     *
     * @param policy policy with level 'sub'. Multiple pipelines are not allowed. Can define more than one condition
     *               as set of conditions. all these conditions should be passed as a single pipeline
     * @return
     * @throws APITemplateException
     */
    public String getThrottlePolicyForSubscriptionLevel(org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicy policy) throws APITemplateException {
        StringWriter writer = new StringWriter();

        if (log.isDebugEnabled()) {
            log.debug("Generating policy for subscriptionLevel :" + policy.toString());
        }

        if (!(policy instanceof SubscriptionPolicy)) {
            throw new APITemplateException("Invalid policy level :  Has to be 'sub'");
        }
        try {
            VelocityEngine velocityengine = new VelocityEngine();
            velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                    CommonsLogLogChute.class.getName());
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
                velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            }
            velocityengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            velocityengine.init();
            Template t = velocityengine.getTemplate(getTemplatePathForSubscription());

            VelocityContext context = new VelocityContext();
            setConstantContext(context);
            context.put("policy", policy);
            context.put("quotaPolicy", policy.getDefaultLimit());
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

    private String getTemplatePathForAPI() {
        return policyTemplateLocation + ThrottlePolicyTemplateBuilder.POLICY_VELOCITY_RESOURCE + ".xml";
    }

    private String getTemplatePathForAPIDefaultPolicy() {
        return policyTemplateLocation + ThrottlePolicyTemplateBuilder.POLICY_VELOCITY_RESOURCE_DEFAULT + ".xml";
    }

    private String getTemplatePathForGlobal() {
        return policyTemplateLocation + ThrottlePolicyTemplateBuilder.POLICY_VELOCITY_GLOBAL + ".xml";
    }

    private String getTemplatePathForApplication() {
        return policyTemplateLocation + ThrottlePolicyTemplateBuilder.POLICY_VELOCITY_APP + ".xml";
    }

    private String getTemplatePathForSubscription() {
        return policyTemplateLocation + ThrottlePolicyTemplateBuilder.POLICY_VELOCITY_SUB + ".xml";
    }

    private static String getVelocityLogger() {
        if (!"not-defined".equalsIgnoreCase(velocityLogPath)) {
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
     *
     * @param conditions
     * @return
     */
    private static String getPolicyCondition(Set<Condition> conditions) {
        String conditionString = null;
        int i = 0;
        for (Condition condition : conditions) {
            org.wso2.carbon.apimgt.api.model.policy.Condition mappedCondition =
                    PolicyMappingUtil.mapCondition(condition);

            if (i == 0) {
                conditionString = mappedCondition.getCondition();
            } else {
                conditionString = conditionString + " AND " + mappedCondition.getCondition();
            }
            i++;
        }
        return conditionString;
    }


    /**
     * Produces final condition inside a pipeline
     *
     * @param conditions
     * @return
     */
    private static JSONObject getPolicyConditionJson(Set<Condition> conditions) {
        JSONObject tempCondition = new JSONObject();
        for (Condition condition : conditions) {

            org.wso2.carbon.apimgt.api.model.policy.Condition mappedCondition =
                    PolicyMappingUtil.mapCondition(condition);
            JSONObject conditionJson;
            if (tempCondition.containsKey(mappedCondition.getType().toLowerCase())) {
                conditionJson = (JSONObject) tempCondition.get(mappedCondition.getType().toLowerCase());
            } else {
                conditionJson = new JSONObject();
            }
            tempCondition.put(mappedCondition.getType().toLowerCase(), conditionJson);
            if (PolicyConstants.IP_SPECIFIC_TYPE.equals(mappedCondition.getType())) {
                IPCondition ipCondition = (IPCondition) mappedCondition;
                if (IPCondition.isIPv6Address(ipCondition.getSpecificIP())) {
                    conditionJson.put("specificIp",
                            String.valueOf(APIUtil.ipToBigInteger(ipCondition.getSpecificIP())));
                } else {
                    conditionJson.put("specificIp", ipCondition.ipToLong(ipCondition.getSpecificIP()));
                }

            } else if (PolicyConstants.IP_RANGE_TYPE.equals(mappedCondition.getType())) {
                IPCondition ipRangeCondition = (IPCondition) mappedCondition;
                if (IPCondition.isIPv6Address(ipRangeCondition.getStartingIP())
                        && IPCondition.isIPv6Address(ipRangeCondition.getEndingIP())) {
                    conditionJson.put("startingIp",
                            String.valueOf(APIUtil.ipToBigInteger(ipRangeCondition.getStartingIP())));
                    conditionJson.put("endingIp",
                            String.valueOf(APIUtil.ipToBigInteger(ipRangeCondition.getEndingIP())));
                } else {
                    conditionJson.put("startingIp", ipRangeCondition.ipToLong(ipRangeCondition.getStartingIP()));
                    conditionJson.put("endingIp", ipRangeCondition.ipToLong(ipRangeCondition.getEndingIP()));
                }

            } else if (mappedCondition instanceof QueryParameterCondition) {
                QueryParameterCondition queryParameterCondition = (QueryParameterCondition) mappedCondition;
                JSONObject values;
                if (conditionJson.containsKey("values")) {
                    values = (JSONObject) conditionJson.get("values");
                } else {
                    values = new JSONObject();
                    conditionJson.put("values", values);
                }
                values.put(queryParameterCondition.getParameter(), queryParameterCondition.getValue());
            } else if (mappedCondition instanceof HeaderCondition) {
                HeaderCondition headerCondition = (HeaderCondition) mappedCondition;
                JSONObject values;
                if (conditionJson.containsKey("values")) {
                    values = (JSONObject) conditionJson.get("values");
                } else {
                    values = new JSONObject();
                    conditionJson.put("values", values);
                }
                values.put(headerCondition.getHeaderName(), headerCondition.getValue());
            } else if (mappedCondition instanceof JWTClaimsCondition) {
                JWTClaimsCondition jwtClaimsCondition = (JWTClaimsCondition) mappedCondition;
                JSONObject values;
                if (conditionJson.containsKey("values")) {
                    values = (JSONObject) conditionJson.get("values");
                } else {
                    values = new JSONObject();
                    conditionJson.put("values", values);
                }
                values.put(jwtClaimsCondition.getClaimUrl(), jwtClaimsCondition.getAttribute());
            }
            conditionJson.put("invert", mappedCondition.isInvertCondition());
        }
        return tempCondition;
    }

    /**
     * Produces final condition inside a pipeline for default policy with null string
     *
     * @param conditions
     * @return
     */
    private static String getPolicyConditionForDefault(Set<Condition> conditions) {
        String conditionString = null;
        int i = 0;
        for (Condition condition : conditions) {
            org.wso2.carbon.apimgt.api.model.policy.Condition mappedCondition =
                    PolicyMappingUtil.mapCondition(condition);
            String conditionStringComplete = mappedCondition.getCondition();
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

        if (!StringUtils.isEmpty(conditionString)) {
            conditionString = PolicyConstants.INVERT_CONDITION + "(" + conditionString + ")";
        }

        return conditionString;
    }

    private static void setConstantContext(VelocityContext context) {

        context.put("ACROSS_ALL", PolicyConstants.ACROSS_ALL);
        context.put("PER_USER", PolicyConstants.PER_USER);
        context.put("POLICY_LEVEL_API", PolicyConstants.POLICY_LEVEL_API);
        context.put("POLICY_LEVEL_APP", PolicyConstants.POLICY_LEVEL_APP);
        context.put("POLICY_LEVEL_SUB", PolicyConstants.POLICY_LEVEL_SUB);
        context.put("POLICY_LEVEL_GLOBAL", PolicyConstants.POLICY_LEVEL_GLOBAL);
        context.put("REQUEST_COUNT_TYPE", PolicyConstants.REQUEST_COUNT_TYPE);
        context.put("BANDWIDTH_TYPE", PolicyConstants.BANDWIDTH_TYPE);
    }
}
