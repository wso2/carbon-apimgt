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
 *
 */
package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyPermissionInfoDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThrottlingPolicyMappingUtil {

    /**
     * Converts a List object of Tiers into a DTO
     *
     * @param throttlingPolicyList a list of Tier objects
     * @param policyLevel          the policy level(eg: application or subscription)
     * @param limit                max number of objects returned
     * @param offset               starting index
     * @return TierListDTO object containing TierDTOs
     */
    public static ThrottlingPolicyListDTO fromTierListToDTO(List<Tier> throttlingPolicyList, String policyLevel, int limit,
                                                            int offset) {

        ThrottlingPolicyListDTO throttlingPolicyListDTO = new ThrottlingPolicyListDTO();
        List<ThrottlingPolicyDTO> throttlingPolicyDTOs = throttlingPolicyListDTO.getList();
        if (throttlingPolicyDTOs == null) {
            throttlingPolicyDTOs = new ArrayList<>();
            throttlingPolicyListDTO.setList(throttlingPolicyDTOs);
        }

        //identifying the proper start and end indexes
        int size = throttlingPolicyList.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= size - 1 ? offset + limit - 1 : size - 1;

        for (int i = start; i <= end; i++) {
            Tier tier = throttlingPolicyList.get(i);
            throttlingPolicyDTOs.add(fromTierToDTO(tier, policyLevel));
        }
        throttlingPolicyListDTO.setCount(throttlingPolicyDTOs.size());
        return throttlingPolicyListDTO;
    }

    /**
     * Sets pagination urls for a TierListDTO object given pagination parameters and url parameters
     *
     * @param tierListDTO a TierListDTO object
     * @param limit       max number of objects returned
     * @param offset      starting index
     * @param size        max offset
     */
    public static void setPaginationParams(ThrottlingPolicyListDTO tierListDTO, String tierLevel, int limit, int offset, int size) {

        String paginatedPrevious = "";
        String paginatedNext = "";

        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getTiersPaginatedURL(tierLevel,
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getTiersPaginatedURL(tierLevel,
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setNext(paginatedNext);
        paginationDTO.setPrevious(paginatedPrevious);
        tierListDTO.setPagination(paginationDTO);
    }

    /**
     * Converts a Tier object into TierDTO
     *
     * @param throttlingPolicy Tier object
     * @param tierLevel        tier level (api/application or resource)
     * @return TierDTO corresponds to Tier object
     */
    public static ThrottlingPolicyDTO fromTierToDTO(Tier throttlingPolicy, String tierLevel) {

        ThrottlingPolicyDTO dto = new ThrottlingPolicyDTO();
        dto.setName(throttlingPolicy.getName());
        dto.setDescription(throttlingPolicy.getDescription());
        dto.setRequestCount(throttlingPolicy.getRequestCount());
        dto.setUnitTime(throttlingPolicy.getUnitTime());
        dto.setTimeUnit(throttlingPolicy.getTimeUnit());
        dto.setRateLimitCount(throttlingPolicy.getRateLimitCount());
        dto.setRateLimitTimeUnit(throttlingPolicy.getRateLimitTimeUnit());
        dto.setStopOnQuotaReach(throttlingPolicy.isStopOnQuotaReached());
        dto.setPolicyLevel(ThrottlingPolicyDTO.PolicyLevelEnum.valueOf(tierLevel.toUpperCase()));
        dto = setTierPermissions(dto, throttlingPolicy);
        if (throttlingPolicy.getQuotaPolicyType() != null) {
            dto.setQuotaPolicyType(mapQuotaPolicyTypeFromModeltoDTO(throttlingPolicy.getQuotaPolicyType()));
        }
        if (throttlingPolicy.getTierPlan() != null) {
            dto.setTierPlan(ThrottlingPolicyDTO.TierPlanEnum.valueOf(throttlingPolicy.getTierPlan()));
        }
        if (throttlingPolicy.getTierAttributes() != null) {
            Map<String, String> additionalProperties = new HashMap<>();
            for (String key : throttlingPolicy.getTierAttributes().keySet()) {
                additionalProperties.put(key, throttlingPolicy.getTierAttributes().get(key).toString());
            }
            dto.setAttributes(additionalProperties);
        }
        MonetizationInfoDTO monetizationInfoDTO = new MonetizationInfoDTO();
        Map<String, String> monetizationAttributeMap = throttlingPolicy.getMonetizationAttributes();
        if (MapUtils.isNotEmpty(monetizationAttributeMap)) {
            monetizationInfoDTO.setBillingCycle(monetizationAttributeMap.get(APIConstants.Monetization.BILLING_CYCLE));
            monetizationInfoDTO.setCurrencyType(monetizationAttributeMap.get(APIConstants.Monetization.CURRENCY));
            if (StringUtils.isNotBlank(monetizationAttributeMap.get(APIConstants.Monetization.FIXED_PRICE))) {
                monetizationInfoDTO.setFixedPrice(monetizationAttributeMap.get(APIConstants.Monetization.FIXED_PRICE));
                monetizationInfoDTO.setBillingType(MonetizationInfoDTO.BillingTypeEnum.FIXEDPRICE);
            } else {
                monetizationInfoDTO.setPricePerRequest(monetizationAttributeMap.
                        get(APIConstants.Monetization.PRICE_PER_REQUEST));
                monetizationInfoDTO.setBillingType(MonetizationInfoDTO.BillingTypeEnum.DYNAMICRATE);
            }
        }
        dto.setMonetizationAttributes(monetizationInfoDTO);
        return dto;
    }

    /**
     * map quot policy type from data model to DTO
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

    /**
     * Fills the tier information on TierDTO
     *
     * @param throttlingPolicyDTO Object Containing throttling policy DTOs
     * @param throttlingPolicy    Throttling Policy object
     * @return ThrottlingPolicyDTO with permission info
     */
    public static ThrottlingPolicyDTO setTierPermissions(ThrottlingPolicyDTO throttlingPolicyDTO, Tier throttlingPolicy) {

        ThrottlingPolicyPermissionInfoDTO tierPermission = new ThrottlingPolicyPermissionInfoDTO();

        // If no permission found for the tier, the default permission will be applied
        if (throttlingPolicy.getTierPermission() == null ||
                throttlingPolicy.getTierPermission().getPermissionType() == null) {
            tierPermission.setType(ThrottlingPolicyPermissionInfoDTO.TypeEnum.valueOf("ALLOW"));
            List<String> roles = new ArrayList<>();
            roles.add("Internal/everyone");
            tierPermission.setRoles(roles);
        } else {
            String permissionType = throttlingPolicy.getTierPermission().getPermissionType();
            tierPermission.setType(ThrottlingPolicyPermissionInfoDTO.TypeEnum.valueOf(permissionType.toUpperCase()));
            tierPermission.setRoles(Arrays.asList(throttlingPolicy.getTierPermission().getRoles()));
        }
        throttlingPolicyDTO.setThrottlingPolicyPermissions(tierPermission);

        return throttlingPolicyDTO;
    }

}
