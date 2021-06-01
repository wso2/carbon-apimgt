/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.api.dto.ConditionDTO;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ConditionDto;
import org.wso2.carbon.apimgt.impl.dto.ConditionDto.IPCondition;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used by {@code ThrottleHandler} to determine Applicability of Throttling Conditions.
 * This is only
 * used in the flows where Advanced Throttling policies are used. When defining an Advanced Policy the capability has
 * been provided to add several condition groups. Using a condition group you can enable a special quota upon a
 * specific condition is met. For example you can specify something like, allow 50 req/min if User-Agent header is
 * Mozilla. Decision Engine keeps a track of which attributes are present in the request and which keys have been
 * throttled out. In order to see if those keys are applicable for the request, GW too should run some checks by
 * going through the attributes used for those conditions. What this class does is performing those checks.
 */
public class ThrottleConditionEvaluator {

    private ThrottleConditionEvaluator() {

    }

    private static class ThrottleEvaluatorHolder {

        private static ThrottleConditionEvaluator evaluator = new ThrottleConditionEvaluator();
    }

    public static ThrottleConditionEvaluator getInstance() {

        return ThrottleEvaluatorHolder.evaluator;
    }

    /**
     * When called, provides a list of Applicable Condition Groups for the current request.
     *
     * @param synapseContext        Message Context of the incoming request.
     * @param authenticationContext AuthenticationContext populated by {@code APIAuthenticationHandler}
     * @param inputConditionGroups  All Condition Groups Attached with the resource/API being invoked.
     * @return List of ConditionGroups applicable for the current request.
     */
    public List<ConditionGroupDTO> getApplicableConditions(org.apache.synapse.MessageContext synapseContext,
                                                           AuthenticationContext authenticationContext,
                                                           ConditionGroupDTO[] inputConditionGroups) {

        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>(inputConditionGroups.length);
        ConditionGroupDTO defaultGroup = null;

        for (ConditionGroupDTO conditionGroup : inputConditionGroups) {
            if (APIConstants.THROTTLE_POLICY_DEFAULT.equals(conditionGroup.getConditionGroupId())) {
                defaultGroup = conditionGroup;
            } else if (isConditionGroupApplicable(synapseContext, authenticationContext, conditionGroup)) {
                matchingConditions.add(conditionGroup);
            }
        }

        // If no matching ConditionGroups are present, apply the default group.
        if (matchingConditions.isEmpty()) {
            matchingConditions.add(defaultGroup);
        }

        return matchingConditions;
    }

    private boolean isConditionGroupApplicable(org.apache.synapse.MessageContext synapseContext,
                                               AuthenticationContext authenticationContext,
                                               ConditionGroupDTO conditionGroup) {

        ConditionDTO[] conditions = conditionGroup.getConditions();

        boolean evaluationState = true;

        if (conditions.length == 0) {
            evaluationState = false;
        }

        // When multiple conditions have been specified, all the conditions should occur.
        for (ConditionDTO condition : conditions) {
            evaluationState = evaluationState & isConditionApplicable(synapseContext, authenticationContext, condition);

            // If one of the conditions are false, rest will evaluate to false. So no need to check the rest.
            if (!evaluationState) {
                return false;
            }
        }
        return evaluationState;
    }

    private boolean isConditionApplicable(org.apache.synapse.MessageContext synapseContext,
                                          AuthenticationContext authenticationContext,
                                          ConditionDTO condition) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synapseContext)
                .getAxis2MessageContext();

        boolean state = false;
        switch (condition.getConditionType()) {
            case PolicyConstants.IP_RANGE_TYPE: {
                state = isWithinIP(axis2MessageContext, condition);
                break;
            }
            case PolicyConstants.IP_SPECIFIC_TYPE: {
                state = isMatchingIP(axis2MessageContext, condition);
                break;
            }
            case PolicyConstants.QUERY_PARAMETER_TYPE: {
                state = isQueryParamPresent(axis2MessageContext, condition);
                break;
            }
            case PolicyConstants.JWT_CLAIMS_TYPE: {
                state = isJWTClaimPresent(authenticationContext, condition);
                break;
            }
            case PolicyConstants.HEADER_TYPE: {
                state = isHeaderPresent(axis2MessageContext, condition);
                break;
            }
        }

        if (condition.isInverted()) {
            state = !state;
        }

        return state;
    }

    private boolean isHeaderPresent(MessageContext messageContext, ConditionDTO condition) {

        Map<String, String> transportHeaderMap = (Map<String, String>) messageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (transportHeaderMap != null) {
            String value = transportHeaderMap.get(condition.getConditionName());
            if (value == null) {
                return false;
            }
            Pattern pattern = Pattern.compile(condition.getConditionValue());
            Matcher matcher = pattern.matcher(value);
            return matcher.find();
        }
        return false;
    }

    private boolean isHeaderPresent(MessageContext messageContext, ConditionDto.HeaderConditions condition) {

        Map<String, String> transportHeaderMap = (Map<String, String>) messageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        boolean status = true;
        for (Map.Entry<String, String> headerEntry : condition.getValues().entrySet()) {
            if (transportHeaderMap != null) {
                String value = transportHeaderMap.get(headerEntry.getKey());
                if (StringUtils.isEmpty(value)) {
                    status = false;
                    break;
                } else {
                    Pattern pattern = Pattern.compile(headerEntry.getValue());
                    Matcher matcher = pattern.matcher(value);
                    status = status && matcher.find();
                }
            }
        }

        if (condition.isInvert()) {
            return !status;
        } else {
            return status;
        }
    }

    private boolean isJWTClaimPresent(AuthenticationContext authenticationContext, ConditionDTO condition) {

        Map assertions = GatewayUtils.getJWTClaims(authenticationContext);
        if (assertions != null) {
            Object value = assertions.get(condition.getConditionName());
            if (value == null) {
                return false;
            } else if (value instanceof String) {
                String valueString = (String) value;
                Pattern pattern = Pattern.compile(condition.getConditionValue());
                Matcher matcher = pattern.matcher(valueString);
                return matcher.find();
            }
        }
        return false;
    }

    private boolean isJWTClaimPresent(AuthenticationContext authenticationContext, ConditionDto.JWTClaimConditions
            condition) {

        Map assertions = GatewayUtils.getJWTClaims(authenticationContext);
        boolean status = true;

        for (Map.Entry<String, String> jwtClaim : condition.getValues().entrySet()) {
            Object value = assertions.get(jwtClaim.getKey());
            if (value == null) {
                status = false;
                break;
            } else if (value instanceof String) {
                String valueString = (String) value;
                Pattern pattern = Pattern.compile(jwtClaim.getValue());
                Matcher matcher = pattern.matcher(valueString);
                status = status && matcher.find();
            } else {
                status = false;
            }
        }
        if (condition.isInvert()) {
            return !status;
        } else {
            return status;
        }
    }

    private boolean isQueryParamPresent(MessageContext messageContext, ConditionDto.QueryParamConditions condition) {

        Map<String, String> queryParamMap = GatewayUtils.getQueryParams(messageContext);
        boolean status = true;

        for (Map.Entry<String, String> queryParam : condition.getValues().entrySet()) {
            String value = queryParamMap.get(queryParam.getKey());
            if (value == null) {
                status = false;
                break;
            } else {
                Pattern pattern = Pattern.compile(queryParam.getValue());
                Matcher matcher = pattern.matcher(value);
                status = status && matcher.find();
            }
        }
        if (condition.isInvert()) {
            return !status;
        } else {
            return status;
        }
    }

    private boolean isQueryParamPresent(MessageContext messageContext, ConditionDTO condition) {

        Map<String, String> queryParamMap = GatewayUtils.getQueryParams(messageContext);

        if (queryParamMap != null) {
            String value = queryParamMap.get(condition.getConditionName());
            if (value == null) {
                return false;
            }
            Pattern pattern = Pattern.compile(condition.getConditionValue());
            Matcher matcher = pattern.matcher(value);
            return matcher.find();
        }
        return false;
    }

    private boolean isMatchingIP(MessageContext messageContext, ConditionDTO condition) {

        String currentIpString = GatewayUtils.getIp(messageContext);
        return currentIpString.equals(condition.getConditionValue());
    }

    private boolean isWithinIP(MessageContext messageContext, ConditionDTO condition) {
        // For an IP Range Condition, starting IP is set as a the name, ending IP as the value.
        BigInteger startIp = APIUtil.ipToBigInteger(condition.getConditionName());
        BigInteger endIp = APIUtil.ipToBigInteger(condition.getConditionValue());

        String currentIpString = GatewayUtils.getIp(messageContext);
        if (!currentIpString.isEmpty()) {
            BigInteger currentIp = APIUtil.ipToBigInteger(currentIpString);

            return startIp.compareTo(currentIp) <= 0 && endIp.compareTo(currentIp) >= 0;
        }
        return false;
    }

    private boolean isWithinIP(MessageContext messageContext, ConditionDto.IPCondition ipCondition) {

        String currentIpString = GatewayUtils.getIp(messageContext);
        boolean status;
        if (StringUtils.isNotEmpty(currentIpString)) {
            BigInteger currentIp = APIUtil.ipToBigInteger(currentIpString);
            status = ipCondition.getStartingIp().compareTo(currentIp) <= 0
                    && ipCondition.getEndingIp().compareTo(currentIp) >= 0;
        } else {
            return false;
        }
        if (ipCondition.isInvert()) {
            return !status;
        } else {
            return status;
        }
    }

    private boolean isMatchingIP(MessageContext messageContext, ConditionDto.IPCondition ipCondition) {

        String currentIpString = GatewayUtils.getIp(messageContext);
        BigInteger longValueOfIp = APIUtil.ipToBigInteger(currentIpString);

        if (ipCondition.isInvert()) {
            return !longValueOfIp.equals(ipCondition.getSpecificIp());
        }
        return longValueOfIp.equals(ipCondition.getSpecificIp());
    }

    public String getThrottledInCondition(org.apache.synapse.MessageContext synCtx, AuthenticationContext authContext,
                                          Map<String, List<ConditionDto>> conditionDtoMap) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();
        String condition = null;
        for (Map.Entry<String, List<ConditionDto>> conditionList : conditionDtoMap.entrySet()) {
            if (!"default".equals(conditionList.getKey())) {
                boolean pipeLineStatus = isThrottledWithinCondition(axis2MessageContext, authContext, conditionList
                        .getValue());
                if (pipeLineStatus) {
                    condition = conditionList.getKey();
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(condition)) {
            if (conditionDtoMap.containsKey("default")) {
                List<ConditionDto> conditionDtoList = conditionDtoMap.get("default");
                if (conditionDtoList != null && !conditionDtoList.isEmpty()) {
                    boolean pipeLineStatus = isThrottledWithinCondition(axis2MessageContext, authContext,
                            conditionDtoList);
                    if (!pipeLineStatus) {
                        condition = "default";
                    }
                } else {
                    condition = "default";
                }
            }
        }
        return condition;
    }

    private boolean isThrottledWithinCondition(MessageContext axis2MessageContext, AuthenticationContext authContext,
                                               List<ConditionDto> conditionDtoList) {

        ThrottleProperties throttleProperties = ServiceReferenceHolder.getInstance().getThrottleProperties();
        boolean status = true;
        for (ConditionDto condition : conditionDtoList) {
            status = true;
            if (condition.getIpCondition() != null) {
                if (!isMatchingIP(axis2MessageContext, condition.getIpCondition())) {
                    status = false;
                }
            } else if (condition.getIpRangeCondition() != null) {
                if (!isWithinIP(axis2MessageContext, condition.getIpRangeCondition())) {
                    status = false;
                }
            }
            if (condition.getHeaderConditions() != null && throttleProperties.isEnableHeaderConditions() &&
                    !condition.getHeaderConditions().getValues().isEmpty()) {
                if (!isHeaderPresent(axis2MessageContext, condition.getHeaderConditions())) {
                    status = false;
                }
            }
            if (condition.getJwtClaimConditions() != null && throttleProperties.isEnableJwtConditions() &&
                    !condition.getJwtClaimConditions().getValues().isEmpty()) {
                if (!isJWTClaimPresent(authContext, condition.getJwtClaimConditions())) {
                    status = false;
                }
            }
            if (condition.getQueryParameterConditions() != null && throttleProperties.isEnableQueryParamConditions() &&
                    !condition.getQueryParameterConditions().getValues().isEmpty()) {
                if (!isQueryParamPresent(axis2MessageContext, condition.getQueryParameterConditions())) {
                    status = false;
                }
            }
            if (status) {
                break;
            }
        }
        return status;
    }
}
