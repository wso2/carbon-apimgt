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

package org.wso2.apk.apimgt.rest.api.admin.v1.common.impl;

import org.wso2.apk.apimgt.api.APIAdmin;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.impl.APIAdminImpl;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;

public class TenantConfigCommonImpl {

    private TenantConfigCommonImpl() {
    }

    /**
     * Get the tenant conf
     *
     * @return tenant-conf.json
     * @throws APIManagementException When an internal error occurs
     */
    public static String exportTenantConfig() throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        return apiAdmin.getTenantConfig(RestApiCommonUtil.getLoggedInUserTenantDomain());
    }

    /**
     * Update the tenant config
     *
     * @param body Tenant config
     * @throws APIManagementException When an internal error occurs
     */
    public static void updateTenantConfig(String body) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        apiAdmin.updateTenantConfig(RestApiCommonUtil.getLoggedInUserTenantDomain(), body);
    }
}
