package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.ThrottlingPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Map;
import java.util.Set;

import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.ThrottlingPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.impl.indexing.indexer.DocumentIndexer.log;

public class ThrottlingPoliciesApiServiceImpl implements ThrottlingPoliciesApiService {

    @Override
    public Response throttlingPoliciesPolicyLevelGet(
            String policyLevel, Integer limit, Integer offset, String ifNoneMatch, String xWSO2Tenant,
            MessageContext messageContext) {
        //pre-processing
        //setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        String organization = RestApiUtil.getOrganization(messageContext);
        List<Tier> throttlingPolicyList = getThrottlingPolicyList(policyLevel, organization);
        ThrottlingPolicyListDTO tierListDTO = ThrottlingPolicyMappingUtil.fromTierListToDTO(throttlingPolicyList,
                policyLevel, limit, offset);
        ThrottlingPolicyMappingUtil.setPaginationParams(tierListDTO, policyLevel, limit, offset,
                throttlingPolicyList.size());
        return Response.ok().entity(tierListDTO).build();
    }

    @Override
    public Response throttlingPoliciesPolicyLevelPolicyIdGet(String policyId, String policyLevel, String xWSO2Tenant,
            String ifNoneMatch, MessageContext messageContext) {
        String organization = RestApiUtil.getOrganization(messageContext);
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
                return Response.ok()
                        .entity(ThrottlingPolicyMappingUtil.fromTierToDTO(foundTier,
                                ThrottlingPolicyDTO.PolicyLevelEnum.fromValue(policyLevel).toString())).build();
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
    public List<Tier> getThrottlingPolicyList(String policyLevel, String organization) {

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
