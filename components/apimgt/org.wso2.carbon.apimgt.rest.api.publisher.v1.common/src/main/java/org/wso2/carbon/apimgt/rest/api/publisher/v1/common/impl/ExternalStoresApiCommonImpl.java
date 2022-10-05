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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ExternalStoreMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExternalStoreListDTO;

import java.util.Set;

/**
 * Utility class for operations related to ExternalStoresApiService
 */
public class ExternalStoresApiCommonImpl {

    private ExternalStoresApiCommonImpl() {
        //To hide the default constructor
    }

    public static ExternalStoreListDTO getAllExternalStores() throws APIManagementException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        Set<APIStore> apiStores = APIUtil.getExternalAPIStores(APIUtil.getTenantIdFromTenantDomain(tenantDomain));
        return ExternalStoreMappingUtil.fromExternalStoreCollectionToDTO(apiStores);
    }
}
