/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.v1.common.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.mappings.TagMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TagListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class has TagsApi service related Implementation
 */
public class TagServiceImpl {

    private static final Log log = LogFactory.getLog(TagServiceImpl.class);

    private TagServiceImpl() {
    }

    /**
     *
     * @param limit
     * @param offset
     * @param organization
     * @return
     */
    public static TagListDTO tagsGet(Integer limit, Integer offset, String organization) throws APIManagementException {
        //pre-processing
        limit = limit != null ? limit : RestApiConstants.TAG_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.TAG_OFFSET_DEFAULT;

        Set<Tag> tagSet;
        List<Tag> tagList = new ArrayList<>();
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            tagSet = apiConsumer.getAllTags(organization);
            if (tagSet != null) {
                tagList.addAll(tagSet);
            }
            TagListDTO tagListDTO = TagMappingUtil.fromTagListToDTO(tagList, limit, offset);
            TagMappingUtil.setPaginationParams(tagListDTO, limit, offset, tagList.size());
            return tagListDTO;
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving tags";
            throw new APIManagementException(errorMessage, e.getErrorHandler());
        }
    }

}
