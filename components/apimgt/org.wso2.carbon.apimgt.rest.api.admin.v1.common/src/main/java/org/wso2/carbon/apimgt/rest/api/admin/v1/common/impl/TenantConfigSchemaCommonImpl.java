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

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;

public class TenantConfigSchemaCommonImpl {

    private TenantConfigSchemaCommonImpl() {
    }

    /**
     * Get tenant config schema
     *
     * @return Tenant config schema
     */
    public static String exportTenantConfigSchema() {
        APIAdmin apiAdmin = new APIAdminImpl();
        return apiAdmin.getTenantConfigSchema(RestApiCommonUtil.getLoggedInUserTenantDomain());
    }
}
