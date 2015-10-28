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

package org.wso2.carbon.apimgt.rest.api.impl;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.rest.api.TagsApiService;
import org.wso2.carbon.apimgt.rest.api.dto.TagDTO;
import org.wso2.carbon.apimgt.rest.api.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.utils.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.utils.mappings.TagMappingUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;

public class TagsApiServiceImpl extends TagsApiService {
    @Override
    public Response tagsGet(String accept,String ifNoneMatch,String query){

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String username = RestApiUtil.getLoggedInUsername();
        Set<Tag> tags;
        try {
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);
            tags = apiConsumer.getAllTags(tenantDomain);
            List<TagDTO> tagDTOs = new ArrayList<>();
            for (Tag tag : tags) {
                TagDTO tagDTO = TagMappingUtil.fromTagToDTO(tag);
                tagDTOs.add(tagDTO);
            }
            return Response.ok().entity(tagDTOs).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
