package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TierDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Map;
import java.util.Set;

import org.wso2.carbon.apimgt.rest.api.store.v1.utils.mappings.ThrottlingPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.impl.indexing.indexer.DocumentIndexer.log;

public class ThrottlingPoliciesApiServiceImpl extends ThrottlingPoliciesApiService {

    @Override
    public Response throttlingPoliciesPolicyLevelGet(
            String policyLevel, Integer limit, Integer offset, String ifNoneMatch, String xWSO2Tenant) {

        //pre-processing
        //setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            List<Tier> throttlingPolicyList = new ArrayList<>();

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            if (StringUtils.isBlank(policyLevel)) {
                RestApiUtil.handleBadRequest("tierLevel cannot be empty", log);
            }

            //retrieves the tier based on the given tier-level
            if (ThrottlingPolicyDTO.PolicyLevelEnum.subscription.toString().equals(policyLevel)) {
                Map<String, Tier> apiTierMap = APIUtil.getTiers(
                        APIConstants.TIER_API_TYPE, requestedTenantDomain);
                if (apiTierMap != null) {

                    String username = RestApiUtil.getLoggedInUsername();
                    APIConsumer apiConsumer = RestApiUtil.getConsumer(username);

                    Set<TierPermission> TierPermissions = apiConsumer.getTierPermissions();
                    for (TierPermission tierPermission : TierPermissions) {
                        Tier tier = apiTierMap.get(tierPermission.getTierName());
                        tier.setTierPermission(tierPermission);
                        apiTierMap.put(tierPermission.getTierName(), tier);
                    }

                    // Removing denied Tiers
                    Set<String> deniedTiers = apiConsumer.getDeniedTiers();

                    for (String tierName : deniedTiers) {
                        apiTierMap.remove(tierName);
                    }

                    throttlingPolicyList.addAll(apiTierMap.values());
                }
            } else if (TierDTO.TierLevelEnum.application.toString().equals(policyLevel)) {
                Map<String, Tier> appTierMap =
                        APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, requestedTenantDomain);
                if (appTierMap != null) {
                    throttlingPolicyList.addAll(appTierMap.values());
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(
                        "tierLevel should be one of " + Arrays.toString(TierDTO.TierLevelEnum.values()), log);
            }

            ThrottlingPolicyListDTO tierListDTO = ThrottlingPolicyMappingUtil.fromTierListToDTO(throttlingPolicyList, policyLevel, limit, offset);
            ThrottlingPolicyMappingUtil.setPaginationParams(tierListDTO, policyLevel, limit, offset, throttlingPolicyList.size());
            return Response.ok().entity(tierListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving tiers";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingPoliciesPolicyLevelPolicyIdGet(String tierName, String policyLevel, String xWSO2Tenant, String ifNoneMatch) {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            TierDTO.TierLevelEnum tierType;
            Tier foundTier = null;

            if (StringUtils.isBlank(policyLevel)) {
                RestApiUtil.handleBadRequest("policyLevel cannot be empty", log);
            }

            //retrieves the tier based on the given tier-level
            if (ThrottlingPolicyDTO.PolicyLevelEnum.subscription.toString().equals(policyLevel)) {
                tierType = TierDTO.TierLevelEnum.subscription;
                foundTier = APIUtil.getTierFromCache(tierName, requestedTenantDomain);
            } else if (TierDTO.TierLevelEnum.application.toString().equals(policyLevel)) {
                tierType = TierDTO.TierLevelEnum.application;
                Map<String, Tier> appTierMap = APIUtil
                        .getTiers(APIConstants.TIER_APPLICATION_TYPE, requestedTenantDomain);
                if (appTierMap != null) {
                    foundTier = RestApiUtil.findTier(appTierMap.values(), tierName);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(
                        "tierLevel should be one of " + Arrays.toString(TierDTO.TierLevelEnum.values()), log);
                return null;
            }

            //returns if the tier is found, otherwise send 404
            if (foundTier != null) {
                return Response.ok()
                        .entity(ThrottlingPolicyMappingUtil.fromTierToDTO(foundTier, tierType.toString()))
                        .build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_TIER, tierName, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the tier with name " + tierName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
