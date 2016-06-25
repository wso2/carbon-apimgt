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

package org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.throttling;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.NameValuePairDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionThrottlePolicyMappingUtil {

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
        listDTO.setNext(""); //todo set next and previous
        listDTO.setPrevious("");
        return listDTO;
    }

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
            List<NameValuePairDTO> nameValuePairDTOs = new ArrayList<>();
            JSONParser parser = new JSONParser();
            JSONArray attributeArray = (JSONArray) parser.parse(new String(subscriptionPolicy.getCustomAttributes()));
            for (Object attributeObj : attributeArray) {
                JSONObject attribute = (JSONObject) attributeObj;
                NameValuePairDTO pairDTO = CommonThrottleMappingUtil
                        .getNameValuePair(attribute.get(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_NAME).toString(),
                                attribute.get(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_VALUE).toString());
                nameValuePairDTOs.add(pairDTO);
            }
            policyDTO.setCustomAttributes(nameValuePairDTOs);
        }
        return policyDTO;
    }

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

        List<NameValuePairDTO> customAttributes = dto.getCustomAttributes();
        if (customAttributes != null && customAttributes.size() > 0) {
            JSONArray customAttrJsonArray = new JSONArray();
            for (NameValuePairDTO pairDTO : customAttributes) {
                JSONObject attrJsonObj = new JSONObject();
                attrJsonObj.put(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_NAME, pairDTO.getName());
                attrJsonObj.put(RestApiConstants.THROTTLING_CUSTOM_ATTRIBUTE_VALUE, pairDTO.getValue());
                customAttrJsonArray.add(attrJsonObj);
            }
            subscriptionPolicy.setCustomAttributes(customAttrJsonArray.toJSONString().getBytes());
        }
        return subscriptionPolicy;
    }
}
 