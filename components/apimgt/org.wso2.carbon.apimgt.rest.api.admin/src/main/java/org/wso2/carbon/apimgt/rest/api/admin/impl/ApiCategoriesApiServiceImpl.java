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

package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.ApiCategoriesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.APICategoryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.Response;

public class ApiCategoriesApiServiceImpl extends ApiCategoriesApiService {
    private static final Log log = LogFactory.getLog(ApiCategoriesApiServiceImpl.class);

    public Response apiCategoriesGet() {
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantID = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
            List<APICategory> categoryList = apiAdmin.getAllAPICategoriesOfTenant(tenantID);
            APICategoryListDTO categoryListDTO =
                    APICategoryMappingUtil.fromCategoryListToCategoryListDTO(categoryList);
            return Response.ok().entity(categoryListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API categories";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response apiCategoriesPost(APICategoryDTO body) {
        APICategory apiCategory = null;
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String userName = RestApiUtil.getLoggedInUsername();
            apiCategory = APICategoryMappingUtil.fromCategoryDTOToCategory(body);
            APICategoryDTO categoryDTO = APICategoryMappingUtil.
                    fromCategoryToCategoryDTO(apiAdmin.addCategory(apiCategory, userName));
            URI location = new URI(RestApiConstants.RESOURCE_PATH_CATEGORY + "/" + categoryDTO.getId());
            return Response.created(location).entity(categoryDTO).build();
        } catch (APIManagementException | URISyntaxException e) {
            String errorMessage = "Error while adding new API Category '" + body.getName() + "' - " + e.getMessage() ;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response apiCategoriesApiCategoryIdPut(String apiCategoryId, APICategoryDTO body) {
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String userName = RestApiUtil.getLoggedInUsername();
            String tenantDomain = MultitenantUtils.getTenantDomain(userName);
            int tenantID = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
            APICategory apiCategoryToUpdate = APICategoryMappingUtil.fromCategoryDTOToCategory(body);
            APICategory apiCategoryOriginal = apiAdmin.getAPICategoryByID(apiCategoryId);
            if (apiCategoryOriginal == null) {
                String errorMsg = "No api category with the given category ID exists :" + apiCategoryId;
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            }

            //Override several properties as they are not allowed to be updated
            apiCategoryToUpdate.setName(apiCategoryOriginal.getName());
            apiCategoryToUpdate.setId(apiCategoryOriginal.getId());
            apiCategoryToUpdate.setTenantID(apiCategoryOriginal.getTenantID());

            //We allow to update API Category name given that the new category name is not taken yet
            String oldName = apiCategoryOriginal.getName();
            String updatedName = apiCategoryToUpdate.getName();
            if (!oldName.equals(updatedName) && apiAdmin.isCategoryNameExists(updatedName, apiCategoryId, tenantID)) {
                String errorMsg = "An API category already exists by the new API category name :" + updatedName;
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            }

            apiAdmin.updateCategory(apiCategoryToUpdate);
            APICategory updatedAPICategory = apiAdmin.getAPICategoryByID(apiCategoryId);
            APICategoryDTO updatedAPICategoryDTO = APICategoryMappingUtil.fromCategoryToCategoryDTO(updatedAPICategory);
            return Response.ok().entity(updatedAPICategoryDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating API Category '" + body.getName() + "' - " + e.getMessage() ;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response apiCategoriesApiCategoryIdDelete(String apiCategoryId, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String userName = RestApiUtil.getLoggedInUsername();
            apiAdmin.deleteCategory(apiCategoryId, userName);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting API Category '" + apiCategoryId + "' - " + e.getMessage() ;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
