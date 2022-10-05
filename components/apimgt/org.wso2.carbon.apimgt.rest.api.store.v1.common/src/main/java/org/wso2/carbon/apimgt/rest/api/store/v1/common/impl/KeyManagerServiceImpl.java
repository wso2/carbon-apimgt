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

package org.wso2.carbon.apimgt.rest.api.store.v1.common.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.KeyManagerListDTO;

import java.util.List;

/**
 * This is the service implementation class for keyManagers api service operations
 */
public class KeyManagerServiceImpl {

    private static final Log log = LogFactory.getLog(KeyManagerServiceImpl.class);

    private KeyManagerServiceImpl() {
    }

    /**
     *
     * @param organization
     * @return
     */
    public static KeyManagerListDTO getKeyManagers(String organization) throws APIManagementException  {

        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            List<KeyManagerConfigurationDTO> keyManagerConfigurations =
                    apiAdmin.getKeyManagerConfigurationsByOrganization(organization);
            return KeyManagerMappingUtil.toKeyManagerListDto(keyManagerConfigurations);
        } catch (APIManagementException e) {
            String message = "Error while retrieving keyManager Details for organization " + organization;
            throw new APIManagementException(message, e.getErrorHandler());
        }
    }
}
