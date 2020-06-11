/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.TierMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the service implementation class for Store tier related operations
 */
public class TiersApiServiceImpl extends TiersApiService {

    private static final Log log = LogFactory.getLog(TiersApiServiceImpl.class);

    /**
     * Retrieves all the Tiers
     *
     * @param tierLevel   tier level (api/application or resource)
     * @param limit       max number of objects returns
     * @param offset      starting index
     * @param accept      accepted media type of the client
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted tiers
     */
    @Override
    public Response tiersTierLevelGet(String tierLevel, Integer limit, Integer offset, String xWSO2Tenant,
                                      String accept, String ifNoneMatch) {
        //pre-processing
        //setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            List<Tier> tierList = new ArrayList<>();

            if (!APIUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            if (StringUtils.isBlank(tierLevel)) {
                RestApiUtil.handleBadRequest("tierLevel cannot be empty", log);
            }

            //retrieves the tier based on the given tier-level
            if (TierDTO.TierLevelEnum.api.toString().equals(tierLevel)) {
                Map<String, Tier> apiTierMap = APIUtil.getTiers(APIConstants.TIER_API_TYPE, requestedTenantDomain);
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

                    tierList.addAll(apiTierMap.values());
                }
            } else if (TierDTO.TierLevelEnum.application.toString().equals(tierLevel)) {
                Map<String, Tier> appTierMap =
                        APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, requestedTenantDomain);
                if (appTierMap != null) {
                    tierList.addAll(appTierMap.values());
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(
                        "tierLevel should be one of " + Arrays.toString(TierDTO.TierLevelEnum.values()), log);
            }

            TierListDTO tierListDTO = TierMappingUtil.fromTierListToDTO(tierList, tierLevel, limit, offset);
            TierMappingUtil.setPaginationParams(tierListDTO, tierLevel, limit, offset, tierList.size());
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

    /**
     * Returns the matched tier to the given name
     *
     * @param tierLevel       tier level (api/application or resource)
     * @param tierName        name of the tier
     * @param accept          accepted media type of the client
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return TierDTO matched to the given tier name
     */
    @Override
    public Response tiersTierLevelTierNameGet(String tierName, String tierLevel, String xWSO2Tenant, String accept,
                                              String ifNoneMatch, String ifModifiedSince) {

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {

            if (!APIUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            TierDTO.TierLevelEnum tierType;
            Tier foundTier = null;

            if (StringUtils.isBlank(tierLevel)) {
                RestApiUtil.handleBadRequest("tierLevel cannot be empty", log);
            }

            //retrieves the tier based on the given tier-level
            if (TierDTO.TierLevelEnum.api.toString().equals(tierLevel)) {
                tierType = TierDTO.TierLevelEnum.api;
                foundTier = APIUtil.getTierFromCache(tierName, requestedTenantDomain);
            } else if (TierDTO.TierLevelEnum.application.toString().equals(tierLevel)) {
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
                        .entity(TierMappingUtil.fromTierToDTO(foundTier, tierType.toString()))
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

    @Override
    public String tiersTierLevelGetGetLastUpdatedTime(String tierLevel, Integer limit, Integer offset, String xWSO2Tenant, String accept, String ifNoneMatch) {
        return null;
    }

    @Override
    public String tiersTierLevelTierNameGetGetLastUpdatedTime(String tierName, String tierLevel, String xWSO2Tenant, String accept, String ifNoneMatch, String ifModifiedSince) {
        return null;
    }

}
