/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.store.v1.ApiCategoriesApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.impl.ApiCategoryServiceImpl;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class ApiCategoriesApiServiceImpl implements ApiCategoriesApiService {

    public Response apiCategoriesGet(String xWSO2Tenant, MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APICategoryListDTO categoryListDTO = ApiCategoryServiceImpl.getApiCategories(organization);
        return Response.ok().entity(categoryListDTO).build();
    }
}
