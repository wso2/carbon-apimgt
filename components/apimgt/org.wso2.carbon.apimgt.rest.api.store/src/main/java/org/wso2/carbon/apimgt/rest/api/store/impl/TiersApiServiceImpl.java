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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.TierMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/** 
 * This is the service implementation class for Store tier related operations
 */
public class TiersApiServiceImpl extends TiersApiService {

    private static final Log log = LogFactory.getLog(TiersApiServiceImpl.class);

    /** Retrieves all the Tiers
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
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            if (!StringUtils.isBlank(tierLevel)) {
                if (TierDTO.TierLevelEnum.api.toString().equals(tierLevel)) {
                    Set<Tier> apiTiers = apiConsumer.getTiers(APIConstants.TIER_API_TYPE, tenantDomain);
                    if (apiTiers != null) {
                        tierList.addAll(apiTiers);
                    }
                } else if (TierDTO.TierLevelEnum.application.toString().equals(tierLevel)){
                    Set<Tier> appTiers = apiConsumer.getTiers(APIConstants.TIER_APPLICATION_TYPE, tenantDomain);
                    if (appTiers != null) {
                        tierList.addAll(appTiers);
                    }
                } else {
                    throw RestApiUtil.buildNotFoundException(
                            "tierLevel should be one of " + Arrays.toString(TierDTO.TierLevelEnum.values()));
                }
            } else {
                throw RestApiUtil.buildBadRequestException("tierLevel cannot be empty");
            }

            TierListDTO tierListDTO = TierMappingUtil.fromTierListToDTO(tierList, tierLevel, limit, offset);
            TierMappingUtil.setPaginationParams(tierListDTO, tierLevel, limit, offset, tierList.size());
            return Response.ok().entity(tierListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving tiers";
            handleException(errorMessage, e);
        }
        return null;
    }

    /** Returns the matched tier to the given name
     * 
     * @param tierLevel   tier level (api/application or resource)
     * @param tierName name of the tier
     * @param accept accepted media type of the client
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return TierDTO matched to the given tier name
     */
    @Override
    public Response tiersTierLevelTierNameGet(String tierName, String tierLevel, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        try {
            TierDTO.TierLevelEnum tierType;
            Tier foundTier;
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            if (!StringUtils.isBlank(tierLevel)) {
                if (TierDTO.TierLevelEnum.api.toString().equals(tierLevel)) {
                    foundTier = APIUtil.getTierFromCache(tierName, tenantDomain);
                    tierType = TierDTO.TierLevelEnum.api;
                } else if (TierDTO.TierLevelEnum.application.toString().equals(tierLevel)){
                    Set<Tier> appTiers = apiConsumer.getTiers(APIConstants.TIER_APPLICATION_TYPE, tenantDomain);
                    foundTier = RestApiUtil.findTier(appTiers, tierName);
                    tierType = TierDTO.TierLevelEnum.application;
                } else {
                    throw RestApiUtil.buildNotFoundException(
                            "tierLevel should be one of " + Arrays.toString(TierDTO.TierLevelEnum.values()));
                }
                if (foundTier != null) {
                    return Response.ok()
                            .entity(TierMappingUtil.fromTierToDTO(foundTier, tierType.toString()))
                            .build();
                } else {
                    throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_TIER, tierName);
                }
            } else {
                throw RestApiUtil.buildBadRequestException("tierLevel cannot be empty");
            }

        } catch (APIManagementException e) {
            handleException("Error while retrieving the tier with name " + tierName, e);
            return null;
        }
    }

    private void handleException(String msg, Throwable t) throws InternalServerErrorException {
        log.error(msg, t);
        throw new InternalServerErrorException(t);
    }
}
