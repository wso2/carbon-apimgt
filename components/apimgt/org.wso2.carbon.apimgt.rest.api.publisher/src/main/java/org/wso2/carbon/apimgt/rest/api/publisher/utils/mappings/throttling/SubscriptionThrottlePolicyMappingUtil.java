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

package org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.throttling;

import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionThrottlePolicyListDTO;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionThrottlePolicyMappingUtil {

    public static SubscriptionThrottlePolicyListDTO fromSubscriptionPolicyArrayToListDTO(
            SubscriptionPolicy[] subscriptionPolicies) throws UnsupportedThrottleLimitTypeException {
        SubscriptionThrottlePolicyListDTO listDTO = new SubscriptionThrottlePolicyListDTO();
        List<SubscriptionThrottlePolicyDTO> appPolicyDTOList = new ArrayList<>();
        if (subscriptionPolicies != null) {
            for (SubscriptionPolicy policy : subscriptionPolicies) {
                SubscriptionThrottlePolicyDTO dto = fromSubscriptionThrottlePolicyToDTO(policy);
                appPolicyDTOList.add(dto);
            }
        }
        listDTO.setCount(appPolicyDTOList.size());
        listDTO.setNext(""); //todo set next and previous
        listDTO.setPrevious("");
        return listDTO;
    }

    public static SubscriptionThrottlePolicyDTO fromSubscriptionThrottlePolicyToDTO(
            SubscriptionPolicy subscriptionPolicy) throws UnsupportedThrottleLimitTypeException {
        Base64 base64 = new Base64(false);
        SubscriptionThrottlePolicyDTO policyDTO = new SubscriptionThrottlePolicyDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(subscriptionPolicy, policyDTO);
        policyDTO.setBillingPlan(subscriptionPolicy.getBillingPlan());
        policyDTO.setRateLimitCount(subscriptionPolicy.getRateLimitCount());
        policyDTO.setRateLimitTimeUnit(subscriptionPolicy.getRateLimitTimeUnit());
        policyDTO.setStopOnQuotaReach(subscriptionPolicy.isStopOnQuotaReach());
        policyDTO.setCustomAttributes(base64.encodeToString(subscriptionPolicy.getCustomAttributes()));
        return policyDTO;
    }

    public static SubscriptionPolicy fromSubscriptionThrottlePolicyDTOToModel(SubscriptionThrottlePolicyDTO dto)
            throws UnsupportedThrottleLimitTypeException {
        Base64 base64 = new Base64(false);
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(dto.getPolicyName());
        subscriptionPolicy = CommonThrottleMappingUtil.updateFieldsFromDTOToPolicy(dto, subscriptionPolicy);
        subscriptionPolicy.setBillingPlan(dto.getBillingPlan());
        subscriptionPolicy.setRateLimitTimeUnit(dto.getRateLimitTimeUnit());
        subscriptionPolicy.setRateLimitCount(dto.getRateLimitCount());
        subscriptionPolicy.setStopOnQuotaReach(dto.getStopOnQuotaReach());
        subscriptionPolicy.setCustomAttributes(base64.decode(subscriptionPolicy.getCustomAttributes()));
        return subscriptionPolicy;
    }
}
