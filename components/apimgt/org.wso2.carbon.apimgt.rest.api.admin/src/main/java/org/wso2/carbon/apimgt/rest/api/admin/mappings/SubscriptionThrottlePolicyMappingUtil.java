/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.mappings;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.SubscriptionThrottlePolicyException;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for mapping Subscription Level Throttling model and its sub components into REST API DTOs
 * and vice-versa
 */
public class SubscriptionThrottlePolicyMappingUtil {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionThrottlePolicyMappingUtil.class);

    /**
     * Converts an array of Subscription Policy objects into a List DTO
     *
     * @param subscriptionPolicies Array of Subscription Policies
     * @return A List DTO of converted Subscription Policies
     * @throws SubscriptionThrottlePolicyException
     */
    public static SubscriptionThrottlePolicyListDTO fromSubscriptionPolicyArrayToListDTO(
            List<Policy> subscriptionPolicies) throws SubscriptionThrottlePolicyException {
        SubscriptionThrottlePolicyListDTO listDTO = new SubscriptionThrottlePolicyListDTO();
        List<SubscriptionThrottlePolicyDTO> subscriptionPolicyDTOList = new ArrayList<>();
        if (subscriptionPolicies != null) {
            for (Policy policy : subscriptionPolicies) {
                if (policy instanceof SubscriptionPolicy) {
                    SubscriptionThrottlePolicyDTO dto = fromSubscriptionThrottlePolicyToDTO((SubscriptionPolicy)
                            policy);
                    subscriptionPolicyDTOList.add(dto);
                } else {
                    log.error("policy object " + policy.toString() + " is not aa Subscription Policy, hence will not "
                            + "be considered");
                }
            }
        }
        listDTO.setCount(subscriptionPolicyDTOList.size());
        listDTO.setList(subscriptionPolicyDTOList);
        return listDTO;
    }

    /**
     * Converts a single Subscription Policy model into REST API DTO
     *
     * @param policy Subscription Policy model object
     * @return Converted Subscription policy REST API DTO object
     * @throws SubscriptionThrottlePolicyException
     */
    public static SubscriptionThrottlePolicyDTO fromSubscriptionThrottlePolicyToDTO(Policy policy)
            throws SubscriptionThrottlePolicyException {
        try {
            SubscriptionThrottlePolicyDTO policyDTO = new SubscriptionThrottlePolicyDTO();
            policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(policy, policyDTO);
            if (policy instanceof SubscriptionPolicy) {
                SubscriptionPolicy subscriptionPolicy = (SubscriptionPolicy) policy;
                policyDTO.setBillingPlan(subscriptionPolicy.getBillingPlan());
                policyDTO.setRateLimitCount(subscriptionPolicy.getRateLimitCount());
                policyDTO.setRateLimitTimeUnit(subscriptionPolicy.getRateLimitTimeUnit());
                policyDTO.setStopOnQuotaReach(subscriptionPolicy.isStopOnQuotaReach());
                byte[] customAttributes = subscriptionPolicy.getCustomAttributes();
                if (customAttributes != null && customAttributes.length > 0) {
                    List<CustomAttributeDTO> customAttributeDTOs = new ArrayList<>();
                    JSONParser parser = new JSONParser();
                    JSONArray attributeArray = (JSONArray) parser.parse(new String(customAttributes, StandardCharsets.UTF_8));
                    for (Object attributeObj : attributeArray) {
                        JSONObject attribute = (JSONObject) attributeObj;
                        CustomAttributeDTO customAttributeDTO = CommonThrottleMappingUtil.getCustomAttribute(
                                attribute.get(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_NAME).toString(),
                                attribute.get(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_VALUE).toString());
                        customAttributeDTOs.add(customAttributeDTO);
                    }
                    policyDTO.setCustomAttributes(customAttributeDTOs);
                }
            } else {
                log.error("policy object " + policy.toString() + " is not aa Subscription Policy, hence will not "
                        + "be considered");
            }
            if (policy.getDefaultQuotaPolicy() != null) {
                policyDTO.setDefaultLimit(CommonThrottleMappingUtil.fromQuotaPolicyToDTO(policy.getDefaultQuotaPolicy()));
            }
            return policyDTO;
        } catch (ParseException | UnsupportedThrottleLimitTypeException e) {
            throw new SubscriptionThrottlePolicyException(e.getMessage(), e);
        }
    }

    /**
     * Converts a single Subscription Policy DTO into a model object
     *
     * @param dto Subscription policy DTO object
     * @return Converted Subscription policy model object
     * @throws UnsupportedThrottleLimitTypeException
     */
    @SuppressWarnings("unchecked") public static SubscriptionPolicy fromSubscriptionThrottlePolicyDTOToModel(
            SubscriptionThrottlePolicyDTO dto) throws UnsupportedThrottleLimitTypeException {

        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(dto.getPolicyName());
        subscriptionPolicy = CommonThrottleMappingUtil.updateFieldsFromDTOToPolicy(dto, subscriptionPolicy);
        subscriptionPolicy.setBillingPlan(dto.getBillingPlan());
        subscriptionPolicy.setRateLimitTimeUnit(dto.getRateLimitTimeUnit());
        subscriptionPolicy.setRateLimitCount(dto.getRateLimitCount());
        subscriptionPolicy.setStopOnQuotaReach(dto.getStopOnQuotaReach());

        List<CustomAttributeDTO> customAttributes = dto.getCustomAttributes();
        if (customAttributes != null && customAttributes.size() > 0) {
            JSONArray customAttrJsonArray = new JSONArray();
            for (CustomAttributeDTO customAttributeDTO : customAttributes) {
                JSONObject attrJsonObj = new JSONObject();
                attrJsonObj.put(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_NAME, customAttributeDTO.getName());
                attrJsonObj.put(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_VALUE, customAttributeDTO.getValue());
                customAttrJsonArray.add(attrJsonObj);
            }
            subscriptionPolicy.setCustomAttributes(customAttrJsonArray.toJSONString().getBytes(StandardCharsets.UTF_8));
        }
        if (dto.getDefaultLimit() != null) {
            subscriptionPolicy
                    .setDefaultQuotaPolicy(CommonThrottleMappingUtil.fromDTOToQuotaPolicy(dto.getDefaultLimit()));
        }
        return subscriptionPolicy;
    }
}
 