/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.policy.EventCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EventCountLimitDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyListDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping APIM core tier related objects into REST API Tier related DTOs.
 */
public class ThrottlingPolicyMappingUtil {

    /**
     * Converts a List object of Tiers into a DTO.
     *
     * @param tiers  a list of Tier objects
     * @param limit  max number of objects returned
     * @param offset starting index
     * @return ThrottlingPolicyListDTO object containing ThrottlingPolicyDTOs
     */
    public static ThrottlingPolicyListDTO fromTierListToDTO(List<Tier> tiers, String tierLevel, int limit, int offset) {

        ThrottlingPolicyListDTO throttlingPolicyListDTO = new ThrottlingPolicyListDTO();
        List<ThrottlingPolicyDTO> throttlingPolicyListDTOList = throttlingPolicyListDTO.getList();
        if (throttlingPolicyListDTOList == null) {
            throttlingPolicyListDTOList = new ArrayList<>();
            throttlingPolicyListDTO.setList(throttlingPolicyListDTOList);
        }

        //identifying the proper start and end indexes
        int size = tiers.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = Math.min(offset + limit - 1, size - 1);

        for (int i = start; i <= end; i++) {
            Tier tier = tiers.get(i);
            throttlingPolicyListDTOList.add(fromTierToDTO(tier, tierLevel));
        }
        throttlingPolicyListDTO.setCount(throttlingPolicyListDTOList.size());
        return throttlingPolicyListDTO;
    }

    /**
     * Sets pagination urls for a ThrottlingPolicyListDTO object given pagination parameters and url parameters.
     *
     * @param throttlingPolicyListDTO a ThrottlingPolicyListDTO object
     * @param tierLevel               tier level (api/application or resource)
     * @param limit                   max number of objects returned
     * @param offset                  starting index
     * @param size                    max offset
     */
    public static void setPaginationParams(ThrottlingPolicyListDTO throttlingPolicyListDTO, String tierLevel,
                                           int limit, int offset, int size) {

        String paginatedPrevious = "";
        String paginatedNext = "";

        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getTiersPaginatedURL(tierLevel,
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getTiersPaginatedURL(tierLevel,
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }

        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        throttlingPolicyListDTO.setPagination(paginationDTO);
    }

    /**
     * Converts a Tier object into ThrottlingPolicyDTO.
     *
     * @param tier      Tier object
     * @param tierLevel tier level (api/application or resource)
     * @return ThrottlingPolicyDTO corresponds to Tier object
     */
    public static ThrottlingPolicyDTO fromTierToDTO(Tier tier, String tierLevel) {

        ThrottlingPolicyDTO dto = new ThrottlingPolicyDTO();
        dto.setName(tier.getName());
        dto.setDescription(tier.getDescription());
        if (StringUtils.isEmpty(tier.getDisplayName())) {
            dto.setDisplayName(tier.getName());
        } else {
            dto.setDisplayName(tier.getDisplayName());
        }
        dto.setRequestCount(tier.getRequestCount());
        dto.setUnitTime(tier.getUnitTime());
        dto.setStopOnQuotaReach(tier.isStopOnQuotaReached());
        dto.setPolicyLevel((ThrottlingPolicyDTO.PolicyLevelEnum.fromValue(tierLevel)));
        dto.setTimeUnit(tier.getTimeUnit());
        dto.setRateLimitCount(tier.getRateLimitCount());
        dto.setRateLimitTimeUnit(tier.getRateLimitTimeUnit());
        dto.setDataUnit(tier.getBandwidthDataUnit());
        if (tier.getQuotaPolicyType() != null) {
            dto.setQuotaPolicyType(mapQuotaPolicyTypeFromModeltoDTO(tier.getQuotaPolicyType()));
        }
        if (tier.getTierPlan() != null) {
            dto.setTierPlan(ThrottlingPolicyDTO.TierPlanEnum.fromValue(tier.getTierPlan()));
        }
        if (tier.getTierAttributes() != null) {
            Map<String, String> additionalProperties = new HashMap<>();
            for (String key : tier.getTierAttributes().keySet()) {
                additionalProperties.put(key, tier.getTierAttributes().get(key).toString());
            }
            dto.setAttributes(additionalProperties);
        }
        return dto;
    }

    /**
     * Map quota policy type from data model to DTO.
     *
     * @param quotaPolicyType quota policy type
     * @return ThrottlingPolicyDTO.QuotaPolicyTypeEnum
     */
    private static ThrottlingPolicyDTO.QuotaPolicyTypeEnum mapQuotaPolicyTypeFromModeltoDTO(String quotaPolicyType) {

        switch (quotaPolicyType) {
            case PolicyConstants.REQUEST_COUNT_TYPE:
                return ThrottlingPolicyDTO.QuotaPolicyTypeEnum.fromValue(PolicyConstants.
                        REQUEST_COUNT_TYPE.toUpperCase());
            case PolicyConstants.BANDWIDTH_TYPE:
                return ThrottlingPolicyDTO.QuotaPolicyTypeEnum.fromValue(PolicyConstants.BANDWIDTH_TYPE.toUpperCase());
            default:
                return null;
        }
    }

    public static SubscriptionPolicyDTO fromSubscriptionToDTO(SubscriptionPolicy subscriptionPolicy,
                                                              int position) {
        SubscriptionPolicyDTO dto = new SubscriptionPolicyDTO();
        dto.setRateLimitCount(subscriptionPolicy.getRateLimitCount());
        dto.setRateLimitTimeUnit(subscriptionPolicy.getRateLimitTimeUnit());
        dto.setStopOnQuotaReach(subscriptionPolicy.isStopOnQuotaReach());
        ThrottleLimitDTO limitDTO = new ThrottleLimitDTO();
        limitDTO.setType(ThrottleLimitDTO.TypeEnum.EVENTCOUNTLIMIT);
        EventCountLimit eventCountLimit = (EventCountLimit) subscriptionPolicy.getDefaultQuotaPolicy()
                .getLimit();
        EventCountLimitDTO eventCountLimitDTO = new EventCountLimitDTO();
        eventCountLimitDTO.setEventCount(eventCountLimit.getEventCount());
        eventCountLimitDTO.setTimeUnit(eventCountLimit.getTimeUnit());
        eventCountLimitDTO.setUnitTime(eventCountLimit.getUnitTime());
        limitDTO.setEventCount(eventCountLimitDTO);
        dto.setDefaultLimit(limitDTO);
        dto.setSubscriberCount(subscriptionPolicy.getSubscriberCount());
        dto.setDisplayName(subscriptionPolicy.getDisplayName());
        dto.setDescription(subscriptionPolicy.getDescription());
        dto.setIsDeployed(subscriptionPolicy.isDeployed());
        dto.setPolicyName(subscriptionPolicy.getPolicyName());
        dto.setBillingPlan(subscriptionPolicy.getBillingPlan());
        dto.setPolicyId(position);
        dto.setUuid(subscriptionPolicy.getUUID());
        dto.setIsDeployed(subscriptionPolicy.isDeployed());
        dto.setTenantDomain(subscriptionPolicy.getTenantDomain());
        dto.setTenantId(subscriptionPolicy.getTenantId());
        return dto;
    }
}
