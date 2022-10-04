/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.utils.mappings.APICategoryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APICategoryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiCategoriesCommonImpl {

    private ApiCategoriesCommonImpl() {
    }

    /**
     * Get all available categories
     *
     * @param organization Tenant organization
     * @return List of API category details
     * @throws APIManagementException When an internal error occurs
     */
    public static APICategoryListDTO getAllCategories(String organization) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        List<APICategory> categoryList = apiAdmin.getAPICategoriesOfOrganization(organization);
        return APICategoryMappingUtil.fromCategoryListToCategoryListDTO(categoryList);
    }

    /**
     * Add an API category
     *
     * @param body         New API category details
     * @param organization Tenant organization
     * @return API category details
     * @throws APIManagementException When an internal error occurs
     */
    public static APICategoryDTO addCategory(APICategoryDTO body, String organization) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        APICategory apiCategory = APICategoryMappingUtil.fromCategoryDTOToCategory(body);
        if (StringUtils.isNotEmpty(apiCategory.getName())) {
            String regExSpecialChars = "!@#$%^&*(),?\"{}[\\]|<>";
            String regExSpecialCharsReplaced = regExSpecialChars.replaceAll(".", "\\\\$0");
            Pattern pattern = Pattern.compile("[" + regExSpecialCharsReplaced + "\\s" + "]");// include \n,\t, space
            Matcher matcher = pattern.matcher(apiCategory.getName());
            if (matcher.find()) {
                throw new APIManagementException(ExceptionCodes.CATEGORY_NAME_CONTAINS_SPECIAL_CHARS);
            }
            if (apiCategory.getName().length() > 255) {
                throw new APIManagementException(ExceptionCodes.CATEGORY_NAME_TOO_LONG);
            }
        } else {
            throw new APIManagementException(ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        return APICategoryMappingUtil.
                fromCategoryToCategoryDTO(apiAdmin.addCategory(apiCategory, userName, organization));
    }

    /**
     * Update an existing API category
     *
     * @param apiCategoryId API category UUID
     * @param body          New API category details
     * @param organization  Tenant organization
     * @return API category details
     * @throws APIManagementException When an internal error occurs
     */
    public static APICategoryDTO updateCategory(String apiCategoryId, APICategoryDTO body, String organization)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        APICategory apiCategoryToUpdate = APICategoryMappingUtil.fromCategoryDTOToCategory(body);
        APICategory apiCategoryOriginal = apiAdmin.getAPICategoryByID(apiCategoryId);

        //Override several properties as they are not allowed to be updated
        apiCategoryToUpdate.setName(apiCategoryOriginal.getName());
        apiCategoryToUpdate.setId(apiCategoryOriginal.getId());
        apiCategoryToUpdate.setTenantID(apiCategoryOriginal.getTenantID());
        apiCategoryToUpdate.setOrganization(organization);

        //We allow to update API Category name given that the new category name is not taken yet
        String oldName = apiCategoryOriginal.getName();
        String updatedName = apiCategoryToUpdate.getName();
        if (!oldName.equals(updatedName) && apiAdmin.isCategoryNameExists(updatedName, apiCategoryId,
                organization)) {
            String errorMsg = "An API category already exists by the new API category name :" + updatedName;
            throw new APIManagementException(errorMsg,
                    ExceptionCodes.from(ExceptionCodes.CATEGORY_ALREADY_EXISTS, updatedName));
        }

        apiAdmin.updateCategory(apiCategoryToUpdate);
        APICategory updatedAPICategory = apiAdmin.getAPICategoryByID(apiCategoryId);
        return APICategoryMappingUtil.fromCategoryToCategoryDTO(updatedAPICategory);
    }

    /**
     * Delete an API category
     *
     * @param apiCategoryId API category UUID
     * @throws APIManagementException When an internal error occurs
     */
    public static void removeCategory(String apiCategoryId) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        apiAdmin.deleteCategory(apiCategoryId, userName);
    }
}
