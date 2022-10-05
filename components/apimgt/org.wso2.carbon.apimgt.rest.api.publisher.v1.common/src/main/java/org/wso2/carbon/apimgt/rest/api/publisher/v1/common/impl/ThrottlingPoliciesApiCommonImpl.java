/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ThrottlingPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyListDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.apimgt.api.model.policy.PolicyConstants.EVENT_COUNT_TYPE;

/**
 * Utility class for operations related to ThrottlingPoliciesApiService
 */
public class ThrottlingPoliciesApiCommonImpl {

    private ThrottlingPoliciesApiCommonImpl() {
        //To hide the default constructor
    }

    public static ThrottlingPolicyListDTO getAllThrottlingPolicies(String policyLevel, Integer limit, Integer offset)
            throws APIManagementException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        List<Tier> tierList = getThrottlingPolicyList(policyLevel, false);
        ThrottlingPolicyListDTO policyListDTO = ThrottlingPolicyMappingUtil
                .fromTierListToDTO(tierList, policyLevel, limit, offset);
        ThrottlingPolicyMappingUtil.setPaginationParams(policyListDTO, policyLevel, limit, offset, tierList.size());
        return policyListDTO;
    }

    /**
     * Returns the throttling policies which belongs to the given policy level
     *
     * @param policyLevel
     * @return list of throttling policies
     */
    public static List<Tier> getThrottlingPolicyList(String policyLevel, boolean includeAsyncPolicies)
            throws APIManagementException {

        List<Tier> tierList = new ArrayList<>();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

        if (StringUtils.isBlank(policyLevel)) {
            throw new APIManagementException(ExceptionCodes.POLICY_LEVEL_EMPTY);
        }

        //retrieves the tier based on the given tier-level
        if (ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString().equals(policyLevel)) {
            Map<String, Tier> apiTiersMap = APIUtil.getTiers(APIConstants.TIER_API_TYPE, tenantDomain);
            tierList.addAll(apiTiersMap.values());
            // if includeAsyncPolicies is not set, remove the async API policies from the list.
            if (!includeAsyncPolicies) {
                tierList = tierList.stream().filter(tier -> !PolicyConstants.EVENT_COUNT_TYPE.equals(
                        tier.getQuotaPolicyType())).collect(Collectors.toList());
            }
        } else if (ThrottlingPolicyDTO.PolicyLevelEnum.API.toString().equals(policyLevel)) {
            Map<String, Tier> resourceTiersMap =
                    APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, tenantDomain);
            tierList.addAll(resourceTiersMap.values());
        } else {
            String errorMessage = "Policy Level should be one of " +
                    Arrays.toString(ThrottlingPolicyDTO.PolicyLevelEnum.values());
            throw new APIMgtResourceNotFoundException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.POLICY_LEVEL_NOT_FOUND, policyLevel));
        }
        return tierList;
    }

    public static SubscriptionPolicyListDTO getSubscriptionThrottlingPolicies(Integer limit, Integer offset)
            throws APIManagementException {

        List<SubscriptionPolicy> streamingPolicies = getStreamingPolicies();
        SubscriptionPolicyListDTO subscriptionPolicyListDTO = new SubscriptionPolicyListDTO();
        List<SubscriptionPolicyDTO> subscriptionPolicyDTOs = subscriptionPolicyListDTO.getList();
        if (subscriptionPolicyDTOs == null) {
            subscriptionPolicyDTOs = new ArrayList<>();
            subscriptionPolicyListDTO.setList(subscriptionPolicyDTOs);
        }
        int size = streamingPolicies.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = Math.min(offset + limit - 1, size - 1);

        for (int i = start; i <= end; i++) {
            subscriptionPolicyDTOs.add(ThrottlingPolicyMappingUtil.fromSubscriptionToDTO(streamingPolicies.get(i), i));
        }
        subscriptionPolicyListDTO.setCount(subscriptionPolicyDTOs.size());
        return subscriptionPolicyListDTO;
    }

    /**
     * Gets all streaming policies for the user
     *
     * @return Streaming policies for user
     * @throws APIManagementException If error occurred while retrieving streaming policies
     */
    private static List<SubscriptionPolicy> getStreamingPolicies() throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        Policy[] policies = apiProvider.getPolicies(userName, PolicyConstants.POLICY_LEVEL_SUB);
        List<SubscriptionPolicy> streamingPolicies = new ArrayList<>();
        for (Policy policy : policies) {
            if (EVENT_COUNT_TYPE.equals(policy.getDefaultQuotaPolicy().getType())) {
                streamingPolicies.add((SubscriptionPolicy) policy);
            }
        }
        return streamingPolicies;
    }

    public static ThrottlingPolicyDTO getThrottlingPolicyByName(String policyName, String policyLevel)
            throws APIManagementException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        ThrottlingPolicyDTO.PolicyLevelEnum policyLevelEnum;
        Tier foundTier = null;

        if (StringUtils.isBlank(policyLevel)) {
            throw new APIManagementException(ExceptionCodes.POLICY_LEVEL_EMPTY);
        }

        //retrieves the tier based on the given tier-level
        if (ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString().equals(policyLevel)) {
            foundTier = APIUtil.getPolicyByName(PolicyConstants.POLICY_LEVEL_SUB, policyName, tenantDomain);
            policyLevelEnum = ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION;
        } else if (ThrottlingPolicyDTO.PolicyLevelEnum.API.toString().equals(policyLevel)) {
            Map<String, Tier> resourceTiersMap =
                    APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, tenantDomain);
            policyLevelEnum = ThrottlingPolicyDTO.PolicyLevelEnum.API;

            foundTier = findTier(resourceTiersMap.values(), policyName);

        } else {
            String errorMessage = "Policy Level should be one of " +
                    Arrays.toString(ThrottlingPolicyDTO.PolicyLevelEnum.values());
            throw new APIMgtResourceNotFoundException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.POLICY_LEVEL_NOT_FOUND, policyLevel));
        }

        if (foundTier != null) {
            return ThrottlingPolicyMappingUtil.fromTierToDTO(foundTier, policyLevelEnum.toString());
        }
        throw new APIMgtResourceNotFoundException("Requested throttling policy with Id '" + policyName + "' not found",
                ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_TYPE_AND_ID,
                        "throttling policy", policyName));
    }

    /**
     * Search the tier in the given collection of Tiers. Returns it if it is included there. Otherwise return null
     *
     * @param tiers    Tier Collection
     * @param tierName Tier to find
     * @return Matched tier with its name
     */
    public static Tier findTier(Collection<Tier> tiers, String tierName) {

        for (Tier tier : tiers) {
            if (tier.getName() != null && tierName != null && tier.getName().equals(tierName)) {
                return tier;
            }
        }
        return null;
    }

}
