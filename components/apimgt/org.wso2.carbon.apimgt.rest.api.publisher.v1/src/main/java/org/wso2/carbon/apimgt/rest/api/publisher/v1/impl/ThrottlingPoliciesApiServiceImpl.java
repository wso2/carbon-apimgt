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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ThrottlingPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ThrottlingPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.api.model.policy.PolicyConstants.EVENT_COUNT_TYPE;

/**
 * This is the service implementation class for Publisher throttling policies related operations
 */
public class ThrottlingPoliciesApiServiceImpl implements ThrottlingPoliciesApiService {

    private static final Log log = LogFactory.getLog(ThrottlingPoliciesApiServiceImpl.class);

    /**
     * Retrieves all the Tiers
     *
     * @param policyLevel tier level (api/application or resource)
     * @param limit       max number of objects returns
     * @param offset      starting index
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted tiers
     */
    @Override
    public Response getAllThrottlingPolicies(String policyLevel, Integer limit, Integer offset,
                                             String ifNoneMatch, MessageContext messageContext) {
        //pre-processing
        //setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        List<Tier> tierList = getThrottlingPolicyList(policyLevel, false);
        ThrottlingPolicyListDTO policyListDTO = ThrottlingPolicyMappingUtil
                .fromTierListToDTO(tierList, policyLevel, limit, offset);
        //todo: set total counts properly
        ThrottlingPolicyMappingUtil.setPaginationParams(policyListDTO, policyLevel, limit, offset, tierList.size());
        return Response.ok().entity(policyListDTO).build();
    }

    /**
     * Retrieves all the Tiers
     *
     * @param limit       max number of objects returns
     * @param offset      starting index
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted tiers
     */

    @Override
    public Response getSubscriptionThrottlingPolicies(Integer limit, Integer offset, String ifNoneMatch,
                                                      MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        String userName = RestApiCommonUtil.getLoggedInUsername();
        Policy[] policies = apiProvider.getPolicies(userName, PolicyConstants.POLICY_LEVEL_SUB);
        List<SubscriptionPolicy> streamingPolicies = new ArrayList<>();
        for (Policy policy : policies) {
            if (EVENT_COUNT_TYPE.equals(policy.getDefaultQuotaPolicy().getType())) {
                streamingPolicies.add((SubscriptionPolicy) policy);
            }
        }
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
        return Response.ok().entity(subscriptionPolicyListDTO).build();
    }

    /**
     * Returns the matched throttling policy to the given policy name
     *
     * @param policyName  name of the throttling policy
     * @param policyLevel throttling policy level (subscription or api)
     * @param ifNoneMatch If-None-Match header value
     * @return ThrottlingPolicyDTO matched to the given throttling policy name
     */
    @Override
    public Response getThrottlingPolicyByName(String policyName, String policyLevel, String ifNoneMatch,
                                              MessageContext messageContext) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            ThrottlingPolicyDTO.PolicyLevelEnum policyLevelEnum;
            Tier foundTier = null;

            if (StringUtils.isBlank(policyLevel)) {
                RestApiUtil.handleBadRequest("policyLevel cannot be empty", log);
            }

            //retrieves the tier based on the given tier-level
            if (ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString().equals(policyLevel)) {
                foundTier = APIUtil.getTierFromCache(policyName, tenantDomain);
                policyLevelEnum = ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION;
            } else if (ThrottlingPolicyDTO.PolicyLevelEnum.API.toString().equals(policyLevel)) {
                Map<String, Tier> resourceTiersMap =
                        APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, tenantDomain);
                policyLevelEnum = ThrottlingPolicyDTO.PolicyLevelEnum.API;
                if (resourceTiersMap != null) {
                    foundTier = RestApiUtil.findTier(resourceTiersMap.values(), policyName);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(
                        "policyLevel should be one of " + Arrays.toString(ThrottlingPolicyDTO.PolicyLevelEnum.values()),
                        log);
                return null;
            }

            //returns if the tier is found, otherwise send 404
            if (foundTier != null) {
                return Response.ok()
                        .entity(ThrottlingPolicyMappingUtil.fromTierToDTO(foundTier, policyLevelEnum.toString()))
                        .build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_THROTTLING_POLICY, policyName, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving throttling policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Returns the throttling policies which belongs to the given policy level
     *
     * @param policyLevel
     * @return list of throttling policies
     */
    public List<Tier> getThrottlingPolicyList(String policyLevel, boolean includeAsyncPolicies) {
        try {
            List<Tier> tierList = new ArrayList<>();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

            if (StringUtils.isBlank(policyLevel)) {
                RestApiUtil.handleBadRequest("policyLevel cannot be empty", log);
            }

            //retrieves the tier based on the given tier-level
            if (ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString().equals(policyLevel)) {
                Map<String, Tier> apiTiersMap = APIUtil.getTiers(APIConstants.TIER_API_TYPE, tenantDomain);
                tierList.addAll(apiTiersMap.values());
                // if includeAsyncPolicies is not set, remove the async API policies from the list.
                if (!includeAsyncPolicies) {
                    tierList = tierList.stream().filter(x -> !PolicyConstants.EVENT_COUNT_TYPE.equals(
                            x.getQuotaPolicyType())).collect(Collectors.toList());
                }
            } else if (ThrottlingPolicyDTO.PolicyLevelEnum.API.toString().equals(policyLevel)) {
                Map<String, Tier> resourceTiersMap =
                        APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, tenantDomain);
                tierList.addAll(resourceTiersMap.values());
            } else {
                RestApiUtil.handleResourceNotFoundError(
                        "policyLevel should be one of " +
                                Arrays.toString(ThrottlingPolicyDTO.PolicyLevelEnum.values()), log);
            }
            return tierList;
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving tiers";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

}
