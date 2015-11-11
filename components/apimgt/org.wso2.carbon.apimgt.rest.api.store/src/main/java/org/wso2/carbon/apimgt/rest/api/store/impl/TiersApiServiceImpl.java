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

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.TierMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.exception.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** This is the service implementation class for Store tier related operations
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
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            Set<Tier> tiers = apiConsumer.getTiers();
            List<Tier> tierList = new ArrayList<>();
            if (tiers != null)
                tierList.addAll(tiers);
            TierListDTO tierListDTO = TierMappingUtil.fromTierListToDTO(tierList, limit, offset);
            TierMappingUtil.setPaginationParams(tierListDTO, limit, offset, tierList.size());
            return Response.ok().entity(tierListDTO).build();
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
}
