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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.TierMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.exception.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** This is the service implementation class for Publisher tier related operations
 *
 */
public class TiersApiServiceImpl extends TiersApiService {

    /** Retrieves all the Tiers
     *
     * @param limit max number of objects returns
     * @param offset starting index
     * @param accept accepted media type of the client
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted tiers
     */
    @Override
    public Response tiersGet(Integer limit, Integer offset, String accept, String ifNoneMatch) {

        //pre-processing
        //setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            Set<Tier> tiers = apiProvider.getTiers();
            List<Tier> tierList = new ArrayList<>();

            for (Tier tier : tiers) {
                tierList.add(tier);
            }
            TierListDTO tierListDTO = TierMappingUtil.fromTierListToDTO(tierList, limit, offset);
            TierMappingUtil.setPaginationParams(tierListDTO, limit, offset, tierList.size());
            return Response.ok().entity(tierListDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    /** Adds a new Tier 
     * 
     * @param body TierDTO specifying the new Tier to be added
     * @param contentType Content-Type header value
     * @return newly added Tier object
     */
    @Override
    public Response tiersPost(TierDTO body, String contentType) {
        URI createdTierUri;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            Tier newTier = TierMappingUtil.fromDTOtoTier(body);
            apiProvider.addTier(newTier);

            createdTierUri = new URI(RestApiConstants.RESOURCE_PATH_TIERS + "/" + body.getName());
            Tier addedTier = APIUtil.getTierFromCache(body.getName(), tenantDomain);
            TierDTO addedTierDTO = TierMappingUtil.fromTiertoDTO(addedTier);
            return Response.created(createdTierUri).entity(addedTierDTO).build();
        } catch (APIManagementException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
    }

    /** Updates permission of a tier specified by name
     * 
     * @param tierName name of the tier
     * @param ifMatch If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param permissions 
     * @return 200 OK response if successfully updated permission
     */
    @Override 
    public Response tiersUpdatePermissionPost(String tierName, String ifMatch, String ifUnmodifiedSince,
            TierPermissionDTO permissions) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            if (permissions.getRoles().size() > 0 ) {
                String roles = StringUtils.join(permissions.getRoles(), ",");
                String permissionType = permissions.getPermissionType().toString();
                apiProvider.updateTierPermissions(tierName, permissionType, roles);
                return Response.ok().build();
            } else {
                throw new BadRequestException();
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    /** Returns the matched tier to the given name
     *
     * @param tierName name of the tier
     * @param accept accepted media type of the client
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return TierDTO matched to the given tier name
     */
    @Override
    public Response tiersTierNameGet(String tierName, String accept, String ifNoneMatch,
            String ifModifiedSince) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        try {
            Tier tier = APIUtil.getTierFromCache(tierName, tenantDomain);
            if (tier != null) {
                return Response.ok().entity(TierMappingUtil.fromTiertoDTO(tier)).build();
            } else {
                throw new NotFoundException();
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    /** Updates an existing Tier 
     * 
     * @param tierName name of the tier to be updated
     * @param body TierDTO object as the new tier
     * @param contentType Content-Type header value
     * @param ifMatch If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return updated tier object
     */
    @Override 
    public Response tiersTierNamePut(String tierName, TierDTO body, String contentType, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            Tier tierToUpdate = TierMappingUtil.fromDTOtoTier(body);
            apiProvider.updateTier(tierToUpdate);

            Tier updatedTier = APIUtil.getTierFromCache(body.getName(), tenantDomain);
            TierDTO updatedTierDTO = TierMappingUtil.fromTiertoDTO(updatedTier);
            return Response.ok().entity(updatedTierDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    /** Deletes a tier specified by the name
     * 
     * @param tierName name of the tier to be deleted
     * @param ifMatch If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return 200 response if successful
     */
    @Override 
    public Response tiersTierNameDelete(String tierName, String ifMatch, String ifUnmodifiedSince){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            Tier tier = APIUtil.getTierFromCache(tierName, tenantDomain);
            if (tier != null) {
                apiProvider.removeTier(tier);
                return Response.ok().build();
            } else {
                throw new NotFoundException();
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }
    
}
