/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.apk.apimgt.rest.api.store.v1.common.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.apk.apimgt.api.APIConsumer;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.Tier;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;
import org.wso2.apk.apimgt.rest.api.store.v1.common.mappings.ThrottlingPolicyMappingUtil;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.ThrottlingPolicyListDTO;

import java.util.Arrays;
import java.util.List;

/**
 * This is the service implementation class for throttling policies operations
 */
public class ThrottlingPoliciesServiceImpl {

    private ThrottlingPoliciesServiceImpl() {
    }

    /**
     *
     * @param policyLevel
     * @param limit
     * @param offset
     * @param organization
     * @return
     */
    public static ThrottlingPolicyListDTO throttlingPoliciesPolicyLevelGet(
            String policyLevel, Integer limit, Integer offset, String organization) throws APIManagementException {
        //pre-processing
        //setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        List<Tier> throttlingPolicyList = getThrottlingPolicyList(policyLevel, organization);
        ThrottlingPolicyListDTO tierListDTO = ThrottlingPolicyMappingUtil.fromTierListToDTO(throttlingPolicyList,
                policyLevel, limit, offset);
        ThrottlingPolicyMappingUtil.setPaginationParams(tierListDTO, policyLevel, limit, offset,
                throttlingPolicyList.size());
        return tierListDTO;
    }

    /**
     *
     * @param policyId
     * @param policyLevel
     * @param organization
     * @return
     */
    public static ThrottlingPolicyDTO throttlingPoliciesPolicyLevelPolicyIdGet(String policyId, String policyLevel,
            String organization) throws APIManagementException {

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            int policyType;
            Tier foundTier = null;

            if (StringUtils.isBlank(policyLevel)) {
                throw new APIManagementException(ExceptionCodes.POLICY_LEVEL_EMPTY);
            }

            //retrieves the tier based on the given tier-level
            if (ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString().equals(policyLevel)) {
                policyType = APIConstants.TIER_API_TYPE;
            } else if (ThrottlingPolicyDTO.PolicyLevelEnum.APPLICATION.toString().equals(policyLevel)) {
                policyType = APIConstants.TIER_APPLICATION_TYPE;
            } else {
                String errorMessage = "Policy Level should be one of " +
                        Arrays.toString(ThrottlingPolicyDTO.PolicyLevelEnum.values());
                throw new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.POLICY_LEVEL_NOT_FOUND, policyLevel));
            }
            foundTier = apiConsumer.getThrottlePolicyByName(policyId, policyType, organization);
            //returns if the tier is found, otherwise send 404
            if (foundTier != null) {
                return ThrottlingPolicyMappingUtil.fromTierToDTO(foundTier,
                        ThrottlingPolicyDTO.PolicyLevelEnum.fromValue(policyLevel).toString());
            } else {
                throw new APIMgtResourceNotFoundException("Policy tier not found",
                        ExceptionCodes.from(ExceptionCodes.THROTTLE_TIER_NOT_FOUND));
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the tier with name " + policyId;
            throw new APIManagementException(errorMessage, e.getErrorHandler());
        }
    }

    /**
     * Returns the throttling policies which belongs to the given policy level
     * @param policyLevel
     * @return list of throttling policies
     */
    public static List<Tier> getThrottlingPolicyList(String policyLevel, String organization)
            throws APIManagementException {

        List<Tier> throttlingPolicyList;
        int tierLevel = -1;
        try {
            if (StringUtils.isBlank(policyLevel)) {
                throw new APIManagementException(ExceptionCodes.POLICY_LEVEL_EMPTY);
            }
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

            //retrieves the tier based on the given tier-level
            if (ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString().equals(policyLevel)) {
                tierLevel = APIConstants.TIER_API_TYPE;
            } else if (ThrottlingPolicyDTO.PolicyLevelEnum.APPLICATION.toString().equals(policyLevel)) {
                tierLevel = APIConstants.TIER_APPLICATION_TYPE;
            } else {
                String errorMessage = "Tier Level should be one of " +
                        Arrays.toString(ThrottlingPolicyDTO.PolicyLevelEnum.values());
                throw new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.POLICY_LEVEL_NOT_FOUND, policyLevel));
            }
            throttlingPolicyList = apiConsumer.getThrottlePolicies(tierLevel, organization);

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving tiers";
            throw new APIManagementException(errorMessage, e.getErrorHandler());
        }
        return throttlingPolicyList;
    }
}
