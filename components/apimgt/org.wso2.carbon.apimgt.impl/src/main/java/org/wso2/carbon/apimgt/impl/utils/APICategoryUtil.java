/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import java.util.List;

public class APICategoryUtil {

    public static boolean isCategoryNameExists(String categoryName, int tenantID) throws APIManagementException {
        return ApiMgtDAO.getInstance().isAPICategoryNameExists(categoryName, tenantID);
    }

    public static List<APICategory> getAllAPICategoriesOfTenant(int tenantID) throws APIManagementException {
        return ApiMgtDAO.getInstance().getAllCategories(tenantID);
    }

    public static APICategory getAPICategoryByID(String apiCategoryId) throws APIManagementException {
        return ApiMgtDAO.getInstance().getAPICategoryByID(apiCategoryId);
    }

    public static List<API> getAPICategoryUsages(String apiCategoryId, String username) throws APIManagementException {
        APICategory apiCategory = getAPICategoryByID(apiCategoryId);

        //check whether there are any apis listed under this category
        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(username);
        return null;
    }

    public static List<APICategory> getAllAPICategories(int tenantID) throws APIManagementException {
        return ApiMgtDAO.getInstance().getAllCategories(tenantID);
    }
}
