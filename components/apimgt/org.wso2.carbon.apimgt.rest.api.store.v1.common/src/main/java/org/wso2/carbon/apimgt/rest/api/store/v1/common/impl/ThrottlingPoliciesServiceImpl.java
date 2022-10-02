package org.wso2.carbon.apimgt.rest.api.store.v1.common.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.mappings.ThrottlingPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.wso2.carbon.apimgt.impl.indexing.indexer.DocumentIndexer.log;

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
            String policyLevel, Integer limit, Integer offset, String organization) {
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
            String organization) {

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            int policyType;
            Tier foundTier = null;

            if (StringUtils.isBlank(policyLevel)) {
                RestApiUtil.handleBadRequest("policyLevel cannot be empty", log);
            }

            //retrieves the tier based on the given tier-level
            if (ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString().equals(policyLevel)) {
                policyType = APIConstants.TIER_API_TYPE;
            } else if (ThrottlingPolicyDTO.PolicyLevelEnum.APPLICATION.toString().equals(policyLevel)) {
                policyType = APIConstants.TIER_APPLICATION_TYPE;
            } else {
                RestApiUtil.handleResourceNotFoundError(
                        "Policy level should be one of " +
                                Arrays.toString(ThrottlingPolicyDTO.PolicyLevelEnum.values()), log);
                return null;
            }
            foundTier = apiConsumer.getThrottlePolicyByName(policyId, policyType, organization);
            //returns if the tier is found, otherwise send 404
            if (foundTier != null) {
                return ThrottlingPolicyMappingUtil.fromTierToDTO(foundTier,
                        ThrottlingPolicyDTO.PolicyLevelEnum.fromValue(policyLevel).toString());
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_TIER, policyId, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the tier with name " + policyId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Returns the throttling policies which belongs to the given policy level
     * @param policyLevel
     * @return list of throttling policies
     */
    public static List<Tier> getThrottlingPolicyList(String policyLevel, String organization) {

        List<Tier> throttlingPolicyList = new ArrayList<>();
        int tierLevel = -1;
        try {
            if (StringUtils.isBlank(policyLevel)) {
                RestApiUtil.handleBadRequest("tierLevel cannot be empty", log);
            }
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

            //retrieves the tier based on the given tier-level
            if (ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString().equals(policyLevel)) {
                tierLevel = APIConstants.TIER_API_TYPE;
            } else if (ThrottlingPolicyDTO.PolicyLevelEnum.APPLICATION.toString().equals(policyLevel)) {
                tierLevel = APIConstants.TIER_APPLICATION_TYPE;
            } else {
                RestApiUtil.handleResourceNotFoundError("tierLevel should be one of " +
                        Arrays.toString(ThrottlingPolicyDTO.PolicyLevelEnum.values()), log);
            }
            throttlingPolicyList = apiConsumer.getThrottlePolicies(tierLevel, organization);

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving tiers";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return throttlingPolicyList;
    }
}
