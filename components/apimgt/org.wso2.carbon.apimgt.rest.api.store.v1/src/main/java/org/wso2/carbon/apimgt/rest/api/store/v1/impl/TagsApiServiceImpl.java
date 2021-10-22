/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.TagsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TagListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.TagMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TagsApiServiceImpl implements TagsApiService {

    private static final Log log = LogFactory.getLog(TagsApiService.class);

    @Override
    public Response tagsGet(Integer limit, Integer offset, String xWSO2Tenant, String ifNoneMatch,
            MessageContext messageContext) {
        //pre-processing
        limit = limit != null ? limit : RestApiConstants.TAG_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.TAG_OFFSET_DEFAULT;

        Set<Tag> tagSet;
        List<Tag> tagList = new ArrayList<>();
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            tagSet = apiConsumer.getAllTags(organization);
            if (tagSet != null) {
                tagList.addAll(tagSet);
            }
            TagListDTO tagListDTO = TagMappingUtil.fromTagListToDTO(tagList, limit, offset);
            TagMappingUtil.setPaginationParams(tagListDTO, limit, offset, tagList.size());
            return Response.ok().entity(tagListDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving tags", e, log);
        } 
        return null;
    }

}
