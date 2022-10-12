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

package org.wso2.apk.apimgt.rest.api.store.v1.common.impl;

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.APICategory;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.store.v1.common.mappings.APICategoryMappingUtil;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.APICategoryListDTO;

import java.util.List;

/**
 * This is the service implementation class for api categories api service operations
 */
public class ApiCategoryServiceImpl {

    private ApiCategoryServiceImpl() {
    }

    /**
     *
     * @param organization organization
     * @return APICategoryListDTO
     */
    public static APICategoryListDTO getApiCategories(String organization) throws APIManagementException {
        try {
            List<APICategory> categoryList = APIUtil.getAllAPICategoriesOfOrganization(organization);
            return APICategoryMappingUtil.fromCategoryListToCategoryListDTO(categoryList);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API categories";
            throw new APIManagementException(errorMessage, e.getErrorHandler());
        }
    }
}
