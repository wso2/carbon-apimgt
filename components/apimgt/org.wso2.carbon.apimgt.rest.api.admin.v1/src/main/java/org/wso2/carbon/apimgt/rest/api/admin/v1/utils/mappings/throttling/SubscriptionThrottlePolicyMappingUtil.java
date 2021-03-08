/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.throttling;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyPermissionDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping Subscription Level Throttling model and its sub components into REST API DTOs
 * and vice-versa
 */
public class SubscriptionThrottlePolicyMappingUtil {

    private static final String SUBSCRIPTION_THROTTLE_POLICY_TYPE = "SubscriptionThrottlePolicy";
    /**
     * Converts an array of Subscription Policy objects into a List DTO
     *
     * @param subscriptionPolicies Array of Subscription Policies
     * @return A List DTO of converted Subscription Policies
     * @throws UnsupportedThrottleLimitTypeException
     * @throws ParseException
     */
    public static SubscriptionThrottlePolicyListDTO fromSubscriptionPolicyArrayToListDTO(
            SubscriptionPolicy[] subscriptionPolicies) throws UnsupportedThrottleLimitTypeException, ParseException {
        SubscriptionThrottlePolicyListDTO listDTO = new SubscriptionThrottlePolicyListDTO();
        List<SubscriptionThrottlePolicyDTO> subscriptionPolicyDTOList = new ArrayList<>();
        if (subscriptionPolicies != null) {
            for (SubscriptionPolicy policy : subscriptionPolicies) {
                SubscriptionThrottlePolicyDTO dto = fromSubscriptionThrottlePolicyToDTO(policy);
                subscriptionPolicyDTOList.add(dto);
            }
        }
        listDTO.setCount(subscriptionPolicyDTOList.size());
        listDTO.setList(subscriptionPolicyDTOList);
        return listDTO;
    }

    /**
     * Converts a single Subscription Policy model into REST API DTO
     *
     * @param subscriptionPolicy Subscription Policy model object
     * @return Converted Subscription policy REST API DTO object
     * @throws UnsupportedThrottleLimitTypeException
     * @throws ParseException
     */
    public static SubscriptionThrottlePolicyDTO fromSubscriptionThrottlePolicyToDTO(
            SubscriptionPolicy subscriptionPolicy) throws UnsupportedThrottleLimitTypeException, ParseException {
        SubscriptionThrottlePolicyDTO policyDTO = new SubscriptionThrottlePolicyDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(subscriptionPolicy, policyDTO);
        policyDTO.setBillingPlan(subscriptionPolicy.getBillingPlan());
        policyDTO.setRateLimitCount(subscriptionPolicy.getRateLimitCount());
        policyDTO.setRateLimitTimeUnit(subscriptionPolicy.getRateLimitTimeUnit());
        policyDTO.setStopOnQuotaReach(subscriptionPolicy.isStopOnQuotaReach());
        policyDTO.setGraphQLMaxComplexity(subscriptionPolicy.getGraphQLMaxComplexity());
        policyDTO.setGraphQLMaxDepth(subscriptionPolicy.getGraphQLMaxDepth());
        policyDTO.setSubscriberCount(subscriptionPolicy.getSubscriberCount());

        byte[] customAttributes = subscriptionPolicy.getCustomAttributes();
        if (customAttributes != null) {
            List<CustomAttributeDTO> customAttributeDTOs = new ArrayList<>();
            JSONParser parser = new JSONParser();
            JSONArray attributeArray = (JSONArray) parser.parse(new String(subscriptionPolicy.getCustomAttributes()));
            for (Object attributeObj : attributeArray) {
                JSONObject attribute = (JSONObject) attributeObj;
                CustomAttributeDTO customAttributeDTO = CommonThrottleMappingUtil
                        .getCustomAttribute(attribute.get(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_NAME).toString(),
                                attribute.get(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_VALUE).toString());
                customAttributeDTOs.add(customAttributeDTO);
            }
            policyDTO.setCustomAttributes(customAttributeDTOs);
        }
        if (subscriptionPolicy.getDefaultQuotaPolicy() != null) {
            policyDTO.setDefaultLimit(
                    CommonThrottleMappingUtil.fromQuotaPolicyToDTO(subscriptionPolicy.getDefaultQuotaPolicy()));
        }
        policyDTO.setType(SUBSCRIPTION_THROTTLE_POLICY_TYPE);
        return policyDTO;
    }

    /**
     * Converts a single Subscription Policy DTO into a model object
     *
     * @param dto Subscription policy DTO object
     * @return Converted Subscription policy model object
     * @throws UnsupportedThrottleLimitTypeException
     */
    @SuppressWarnings("unchecked")
    public static SubscriptionPolicy fromSubscriptionThrottlePolicyDTOToModel(SubscriptionThrottlePolicyDTO dto)
            throws UnsupportedThrottleLimitTypeException {

        //update mandatory fields such as tenantDomain etc.
        dto = CommonThrottleMappingUtil.updateDefaultMandatoryFieldsOfThrottleDTO(dto);

        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(dto.getPolicyName());
        subscriptionPolicy = CommonThrottleMappingUtil.updateFieldsFromDTOToPolicy(dto, subscriptionPolicy);
        subscriptionPolicy.setBillingPlan(dto.getBillingPlan());
        subscriptionPolicy.setRateLimitTimeUnit(dto.getRateLimitTimeUnit());
        subscriptionPolicy.setRateLimitCount(dto.getRateLimitCount());
        subscriptionPolicy.setSubscriberCount(dto.getSubscriberCount());
        subscriptionPolicy.setStopOnQuotaReach(dto.isStopOnQuotaReach());
        if (dto.getGraphQLMaxComplexity() != null) {
            subscriptionPolicy.setGraphQLMaxComplexity(dto.getGraphQLMaxComplexity());
        }
        if (dto.getGraphQLMaxDepth() != null) {
            subscriptionPolicy.setGraphQLMaxDepth(dto.getGraphQLMaxDepth());
        }
        List<CustomAttributeDTO> customAttributes = dto.getCustomAttributes();
        if (customAttributes != null && customAttributes.size() > 0) {
            JSONArray customAttrJsonArray = new JSONArray();
            for (CustomAttributeDTO customAttributeDTO : customAttributes) {
                JSONObject attrJsonObj = new JSONObject();
                attrJsonObj.put(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_NAME, customAttributeDTO.getName());
                attrJsonObj.put(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_VALUE, customAttributeDTO.getValue());
                customAttrJsonArray.add(attrJsonObj);
            }
            subscriptionPolicy.setCustomAttributes(customAttrJsonArray.toJSONString().getBytes());
        } else {
            //if no custom attributes are set, assign an empty byte array
            subscriptionPolicy.setCustomAttributes(new JSONArray().toJSONString().getBytes());
        }
        if (dto.getMonetization() != null &&
                StringUtils.isNotBlank(dto.getMonetization().getMonetizationPlan().name())) {
            String tierMonetizationPlan = dto.getMonetization().getMonetizationPlan().toString();
            subscriptionPolicy.setMonetizationPlan(tierMonetizationPlan);
            if (dto.getMonetization().getProperties() != null) {
                Map<String, String> tierMonetizationProperties = new HashMap<>();
                Map<String, String> props = dto.getMonetization().getProperties();
                for (Map.Entry<String, String> entry : props.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    tierMonetizationProperties.put(key, value);
                }
                subscriptionPolicy.setMonetizationPlanProperties(tierMonetizationProperties);
            }
        }
        if (dto.getDefaultLimit() != null) {
            subscriptionPolicy
                    .setDefaultQuotaPolicy(CommonThrottleMappingUtil.fromDTOToQuotaPolicy(dto.getDefaultLimit()));
        }
        return subscriptionPolicy;
    }

    /**
     * Converts a Subscription Throttle Policy Permission model object into a DTO object
     *
     * @param model Subscription Throttle Policy Permission model object
     * @return mapped REST API DTO object
     */
    public static SubscriptionThrottlePolicyPermissionDTO fromSubscriptionThrottlePolicyPermissionToDTO(TierPermissionDTO model) {
        SubscriptionThrottlePolicyPermissionDTO permissionDTO = new SubscriptionThrottlePolicyPermissionDTO();
        if (APIConstants.TIER_PERMISSION_ALLOW.equals(model.getPermissionType())) {
            permissionDTO.setPermissionType(SubscriptionThrottlePolicyPermissionDTO.PermissionTypeEnum.ALLOW);
        } else {
            permissionDTO.setPermissionType(SubscriptionThrottlePolicyPermissionDTO.PermissionTypeEnum.DENY);
        }
        if (model.getRoles() != null) {
            permissionDTO.setRoles(Arrays.asList(model.getRoles()));
        }
        return permissionDTO;
    }
}
 