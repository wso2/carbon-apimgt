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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.rest.api.store.TagsApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.TagListDTO;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.TagMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is the service implementation class for Store tag related operations
 */
public class TagsApiServiceImpl extends TagsApiService {

    private static final Log log = LogFactory.getLog(TagsApiService.class);

    /**
     * Retrieves all tags
     *
     * @param limit       max number of objects returns
     * @param offset      starting index
     * @param accept      accepted media type of the client
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted tags
     */
    @Override
    public Response tagsGet(Integer limit, Integer offset, String xWSO2Tenant, String accept, String ifNoneMatch) {

        //pre-processing
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        Set<Tag> tagSet;
        List<Tag> tagList = new ArrayList<>();
        try {
            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);
            tagSet = apiConsumer.getAllTags(requestedTenantDomain);
            if (tagSet != null)
                tagList.addAll(tagSet);
            TagListDTO tagListDTO = TagMappingUtil.fromTagListToDTO(tagList, limit, offset);
            TagMappingUtil.setPaginationParams(tagListDTO, limit, offset, tagList.size());
            return Response.ok().entity(tagListDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving tags", e, log);
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public String tagsGetGetLastUpdatedTime(Integer limit, Integer offset, String xWSO2Tenant, String accept, String ifNoneMatch) {
        return null;
    }

}
