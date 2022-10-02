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

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.store.v1.TagsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.impl.TagServiceImpl;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TagListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class TagsApiServiceImpl implements TagsApiService {

    @Override
    public Response tagsGet(Integer limit, Integer offset, String xWSO2Tenant, String ifNoneMatch,
            MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        TagListDTO tagListDTO = TagServiceImpl.tagsGet(limit, offset, organization);
        return Response.ok().entity(tagListDTO).build();
    }
}
