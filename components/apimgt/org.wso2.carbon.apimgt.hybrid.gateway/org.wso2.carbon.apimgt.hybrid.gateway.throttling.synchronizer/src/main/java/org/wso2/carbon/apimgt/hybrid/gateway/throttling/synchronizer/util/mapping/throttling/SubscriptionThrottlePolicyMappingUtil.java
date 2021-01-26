/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.util.mapping.throttling;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.SubscriptionThrottlePolicyListDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for mapping Subscription Level Throttling model and its sub components into REST API DTOs
 * and vice-versa
 */
public class SubscriptionThrottlePolicyMappingUtil {

    public static final String THROTTLING_CUSTOM_ATTRIBUTE_NAME = "name";
    public static final String THROTTLING_CUSTOM_ATTRIBUTE_VALUE = "value";

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

        byte[] customAttributes = subscriptionPolicy.getCustomAttributes();
        if (customAttributes != null) {
            List<CustomAttributeDTO> customAttributeDTOs = new ArrayList<>();
            JSONParser parser = new JSONParser();
            JSONArray attributeArray = (JSONArray) parser.parse(new String(subscriptionPolicy.getCustomAttributes()));
            for (Object attributeObj : attributeArray) {
                JSONObject attribute = (JSONObject) attributeObj;
                CustomAttributeDTO customAttributeDTO = CommonThrottleMappingUtil
                        .getCustomAttribute(attribute.get(THROTTLING_CUSTOM_ATTRIBUTE_NAME).toString(),
                                attribute.get(THROTTLING_CUSTOM_ATTRIBUTE_VALUE).toString());
                customAttributeDTOs.add(customAttributeDTO);
            }
            policyDTO.setCustomAttributes(customAttributeDTOs);
        }
        if (subscriptionPolicy.getDefaultQuotaPolicy() != null) {
            policyDTO.setDefaultLimit(
                    CommonThrottleMappingUtil.fromQuotaPolicyToDTO(subscriptionPolicy.getDefaultQuotaPolicy()));
        }
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
        subscriptionPolicy.setStopOnQuotaReach(dto.getStopOnQuotaReach());

        List<CustomAttributeDTO> customAttributes = dto.getCustomAttributes();
        if (customAttributes != null && customAttributes.size() > 0) {
            JSONArray customAttrJsonArray = new JSONArray();
            for (CustomAttributeDTO customAttributeDTO : customAttributes) {
                JSONObject attrJsonObj = new JSONObject();
                attrJsonObj.put(THROTTLING_CUSTOM_ATTRIBUTE_NAME, customAttributeDTO.getName());
                attrJsonObj.put(THROTTLING_CUSTOM_ATTRIBUTE_VALUE, customAttributeDTO.getValue());
                customAttrJsonArray.add(attrJsonObj);
            }
            subscriptionPolicy.setCustomAttributes(customAttrJsonArray.toJSONString().getBytes());
        }
        if (dto.getDefaultLimit() != null) {
            subscriptionPolicy
                    .setDefaultQuotaPolicy(CommonThrottleMappingUtil.fromDTOToQuotaPolicy(dto.getDefaultLimit()));
        }
        return subscriptionPolicy;
    }
}
 