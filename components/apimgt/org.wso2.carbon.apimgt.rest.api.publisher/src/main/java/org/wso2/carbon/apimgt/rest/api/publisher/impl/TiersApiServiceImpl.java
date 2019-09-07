/*
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

package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.TierMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This is the service implementation class for Publisher tier related operations
 */
public class TiersApiServiceImpl extends TiersApiService {

    private static final Log log = LogFactory.getLog(TiersApiServiceImpl.class);

    /**
     * Retrieves all the Tiers
     *
     * @param tierLevel   tier level (api/application or resource)
     * @param limit max number of objects returns
     * @param offset starting index
     * @param accept accepted media type of the client
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted tiers
     */
    @Override
    public Response tiersTierLevelGet(String tierLevel, Integer limit, Integer offset, String accept,
            String ifNoneMatch) {

        //pre-processing
        //setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            List<Tier> tierList = new ArrayList<>();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            if (StringUtils.isBlank(tierLevel)) {
                RestApiUtil.handleBadRequest("tierLevel cannot be empty", log);
            }

            //retrieves the tier based on the given tier-level
            if (TierDTO.TierLevelEnum.api.toString().equals(tierLevel)) {
                Map<String, Tier> apiTiersMap = APIUtil.getTiers(APIConstants.TIER_API_TYPE, tenantDomain);
                if (apiTiersMap != null) {
                    tierList.addAll(apiTiersMap.values());
                }
            } else if (TierDTO.TierLevelEnum.application.toString().equals(tierLevel)) {
                Map<String, Tier> appTiersMap = APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, tenantDomain);
                if (appTiersMap != null) {
                    tierList.addAll(appTiersMap.values());
                }
            } else if (TierDTO.TierLevelEnum.resource.toString().equals(tierLevel)) {
                Map<String, Tier> resourceTiersMap =
                        APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, tenantDomain);
                if (resourceTiersMap != null) {
                    tierList.addAll(resourceTiersMap.values());
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
        }
        return null;
    }

    /**
     * Adds a new Tier
     *
     * @param tierLevel   tier level (api/application or resource)
     * @param body TierDTO specifying the new Tier to be added
     * @param contentType Content-Type header value
     * @return newly added Tier object
     */
    @Override
    public Response tiersTierLevelPost(TierDTO body, String tierLevel, String contentType) {
        URI createdTierUri;
        try {
            RestApiPublisherUtils.validateTierLevels(tierLevel);
            //we currently support adding/updating/deleting only API tiers
            RestApiUtil.checkAllowedMethodForResource("POST", RestApiConstants.RESOURCE_PATH_TIERS + "/" + tierLevel);

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            Tier foundTier = APIUtil.getTierFromCache(body.getName(), tenantDomain);
            if (foundTier != null) {
                RestApiUtil.handleResourceAlreadyExistsError("Requested tier '" + body.getName() + "' already exists",
                        log);
            }

            Tier newTier = TierMappingUtil.fromDTOtoTier(body);
            apiProvider.addTier(newTier);
            createdTierUri = new URI(RestApiConstants.RESOURCE_PATH_TIERS + "/" + body.getName());
            Tier addedTier = APIUtil.getTierFromCache(body.getName(), tenantDomain);
            TierDTO addedTierDTO = TierMappingUtil.fromTierToDTO(addedTier, TierDTO.TierLevelEnum.api.toString());
            return Response.created(createdTierUri).entity(addedTierDTO).build();
        } catch (APIManagementException | URISyntaxException e) {
            String errorMessage = "Error while adding tier " + body.getName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /** 
     * Updates permission of a tier specified by name
     * 
     * @param tierName name of the tier
     * @param tierLevel   tier level (api/application or resource)
     * @param ifMatch If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param permissions tier permissions with type and roles
     * @return 200 OK response if successfully updated permission
     */
    @Override
    public Response tiersUpdatePermissionPost(String tierName, String tierLevel, String ifMatch,
            String ifUnmodifiedSince, TierPermissionDTO permissions) {
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            RestApiPublisherUtils.validateTierLevels(tierLevel);
            //we currently support updating tier permission only for API tiers
            if (!TierDTO.TierLevelEnum.api.toString().equals(tierLevel)) {
                RestApiUtil.handleBadRequest("Allowed tierLevel(s) for update permission is [api]", log);
            }

            //check whether the requested tier exists
            Tier foundTier = APIUtil.getTierFromCache(tierName, tenantDomain);
            if (foundTier == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_TIER, tierName, log);
            }

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            if (permissions.getRoles().size() > 0) {
                String roles = StringUtils.join(permissions.getRoles(), ",");
                String permissionType = permissions.getPermissionType().toString();
                if (APIUtil.isAdvanceThrottlingEnabled()) {
                    apiProvider.updateThrottleTierPermissions(tierName, permissionType, roles);
                } else {
                    apiProvider.updateTierPermissions(tierName, permissionType, roles);
                }
                return Response.ok().build();
            } else {
                RestApiUtil.handleBadRequest("roles should be specified", log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding tier permissions for " + tierName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Returns the matched tier to the given name
     *
     * @param tierName name of the tier
     * @param tierLevel   tier level (api/application or resource)
     * @param accept accepted media type of the client
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return TierDTO matched to the given tier name
     */
    @Override
    public Response tiersTierLevelTierNameGet(String tierName, String tierLevel, String accept,
            String ifNoneMatch, String ifModifiedSince) {

        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            TierDTO.TierLevelEnum tierType;
            Tier foundTier = null;

            if (StringUtils.isBlank(tierLevel)) {
                RestApiUtil.handleBadRequest("tierLevel cannot be empty", log);
            }

            //retrieves the tier based on the given tier-level
            if (TierDTO.TierLevelEnum.api.toString().equals(tierLevel)) {
                foundTier = APIUtil.getTierFromCache(tierName, tenantDomain);
                tierType = TierDTO.TierLevelEnum.api;
            } else if (TierDTO.TierLevelEnum.application.toString().equals(tierLevel)) {
                Map<String, Tier> appTiersMap = APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, tenantDomain);
                tierType = TierDTO.TierLevelEnum.application;
                if (appTiersMap != null) {
                    foundTier = RestApiUtil.findTier(appTiersMap.values(), tierName);
                }
            } else if (TierDTO.TierLevelEnum.resource.toString().equals(tierLevel)) {
                Map<String, Tier> resourceTiersMap =
                        APIUtil.getTiers(APIConstants.TIER_RESOURCE_TYPE, tenantDomain);
                tierType = TierDTO.TierLevelEnum.resource;
                if (resourceTiersMap != null) {
                    foundTier = RestApiUtil.findTier(resourceTiersMap.values(), tierName);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(
                        "type should be one of " + Arrays.toString(TierDTO.TierLevelEnum.values()), log);
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
            String errorMessage = "Error while retrieving tiers";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Updates an existing Tier 
     * 
     * @param tierName name of the tier to be updated
     * @param body TierDTO object as the new tier
     * @param tierLevel   tier level (api/application or resource)
     * @param contentType Content-Type header value
     * @param ifMatch If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return updated tier object
     */
    @Override
    public Response tiersTierLevelTierNamePut(String tierName, TierDTO body, String tierLevel,
            String contentType, String ifMatch, String ifUnmodifiedSince) {
        try {
            RestApiPublisherUtils.validateTierLevels(tierLevel);
            //we currently support adding/updating/deleting only API tiers
            RestApiUtil.checkAllowedMethodForResource("PUT", RestApiConstants.RESOURCE_PATH_TIERS + "/" + tierLevel);

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //check whether the requested tier exists
            Tier foundTier = APIUtil.getTierFromCache(tierName, tenantDomain);
            if (foundTier == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_TIER, tierName, log);
            }

            //overriding some properties
            body.setName(tierName);

            Tier tierToUpdate = TierMappingUtil.fromDTOtoTier(body);
            apiProvider.updateTier(tierToUpdate);

            Tier updatedTier = APIUtil.getTierFromCache(body.getName(), tenantDomain);
            TierDTO updatedTierDTO = TierMappingUtil.fromTierToDTO(updatedTier, TierDTO.TierLevelEnum.api.toString());
            return Response.ok().entity(updatedTierDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating tier " + tierName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Deletes a tier specified by the name
     * 
     * @param tierName name of the tier to be deleted
     * @param tierLevel  tier level (api/application or resource)
     * @param ifMatch If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return 200 response if successful
     */
    @Override
    public Response tiersTierLevelTierNameDelete(String tierName, String tierLevel, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            RestApiPublisherUtils.validateTierLevels(tierLevel);
            //we currently support adding/updating/deleting only API tiers
            RestApiUtil.checkAllowedMethodForResource("DELETE", RestApiConstants.RESOURCE_PATH_TIERS + "/" + tierLevel);

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            Tier tier = APIUtil.getTierFromCache(tierName, tenantDomain);
            if (tier != null) {
                apiProvider.removeTier(tier);
                return Response.ok().build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_TIER, tierName, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting tier " + tierName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
