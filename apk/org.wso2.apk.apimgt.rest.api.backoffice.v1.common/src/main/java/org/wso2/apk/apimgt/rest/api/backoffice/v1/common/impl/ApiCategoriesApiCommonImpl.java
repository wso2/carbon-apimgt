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

package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.impl;

import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.mappings.APICategoryMappingUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APICategoryListDTO;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.APICategory;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;

import java.util.List;

/**
 * Utility class for operations related to ApiCategoriesApiService
 */
public class ApiCategoriesApiCommonImpl {

    private ApiCategoriesApiCommonImpl() {
        //To hide the default constructor
    }

    public static APICategoryListDTO getAllAPICategories() throws APIManagementException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        List<APICategory> categoryList = APIUtil.getAllAPICategoriesOfOrganization(tenantDomain);
        return APICategoryMappingUtil.fromCategoryListToCategoryListDTO(categoryList);
    }

}
