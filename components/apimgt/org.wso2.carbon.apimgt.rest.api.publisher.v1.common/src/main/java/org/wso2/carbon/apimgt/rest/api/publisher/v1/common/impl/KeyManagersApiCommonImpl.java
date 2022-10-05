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

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.KeyManagerListDTO;

import java.util.List;

/**
 * Utility class for operations related to KeyManagersApiService
 */
public class KeyManagersApiCommonImpl {

    private KeyManagersApiCommonImpl() {
        //To hide the default constructor
    }

    public static KeyManagerListDTO getAllKeyManagers(String organization) throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        List<KeyManagerConfigurationDTO> keyManagerConfigurations =
                apiAdmin.getKeyManagerConfigurationsByOrganization(organization);
        return KeyManagerMappingUtil.toKeyManagerListDto(keyManagerConfigurations);
    }

}
