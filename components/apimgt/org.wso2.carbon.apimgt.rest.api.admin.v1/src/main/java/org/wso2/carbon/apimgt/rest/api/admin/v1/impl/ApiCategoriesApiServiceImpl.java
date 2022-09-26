/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ApiCategoriesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.ApiCategoriesCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APICategoryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

public class ApiCategoriesApiServiceImpl implements ApiCategoriesApiService {

    @Override
    public Response getAllCategories(MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getOrganization(messageContext);
        APICategoryListDTO categoryListDTO = ApiCategoriesCommonImpl.getAllCategories(organization);
        return Response.ok().entity(categoryListDTO).build();
    }

    @Override
    public Response addCategory(APICategoryDTO body, MessageContext messageContext) throws APIManagementException {
        try {
            String organization = RestApiUtil.getOrganization(messageContext);
            APICategoryDTO categoryDTO = ApiCategoriesCommonImpl.addCategory(body, organization);
            URI location = new URI(RestApiConstants.RESOURCE_PATH_CATEGORY + RestApiConstants.PATH_DELIMITER
                    + categoryDTO.getId());
            return Response.created(location).entity(categoryDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while adding new API Category '" + body.getName() + "' - " + e.getMessage();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    @Override
    public Response updateCategory(String apiCategoryId, APICategoryDTO body, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getOrganization(messageContext);
        APICategoryDTO updatedAPICategoryDTO = ApiCategoriesCommonImpl
                .updateCategory(apiCategoryId, body, organization);
        return Response.ok().entity(updatedAPICategoryDTO).build();
    }

    @Override
    public Response removeCategory(String apiCategoryId, MessageContext messageContext) throws APIManagementException {
        ApiCategoriesCommonImpl.removeCategory(apiCategoryId);
        return Response.ok().build();
    }
}
