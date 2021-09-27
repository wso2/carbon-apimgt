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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APICategoryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;

import javax.ws.rs.core.Response;

public class ApiCategoriesApiServiceImpl implements ApiCategoriesApiService {
    private static final Log log = LogFactory.getLog(ApiCategoriesApiServiceImpl.class);
    
    public Response getAllAPICategories(MessageContext messageContext) {
          try {
              String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
              List<APICategory> categoryList = APIUtil.getAllAPICategoriesOfOrganization(tenantDomain);
              APICategoryListDTO categoryListDTO =
                      APICategoryMappingUtil.fromCategoryListToCategoryListDTO(categoryList);
              return Response.ok().entity(categoryListDTO).build();
          } catch (APIManagementException e) {
              String errorMessage = "Error while retrieving API categories";
              RestApiUtil.handleInternalServerError(errorMessage, e, log);
          }
          return null;
    }
}
