/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.service;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.TokenHandlingDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.Arrays;
import java.util.UUID;

/**
 * This class is responsible for calling the KM management services in WSO2 API-M KeyManager Profile server.
 */
public final class KeyMgtRegistrationService {

    private static final Log log = LogFactory.getLog(KeyMgtRegistrationService.class);

    private KeyMgtRegistrationService() {

        throw new IllegalStateException("Service class for key manager registration");
    }

    public static void registerDefaultKeyManager(String tenantDomain) throws APIManagementException {

        synchronized (KeyMgtRegistrationService.class.getName().concat(tenantDomain)) {
            ApiMgtDAO instance = ApiMgtDAO.getInstance();
            if (instance.getKeyManagerConfigurationByName(tenantDomain, APIConstants.KeyManager.DEFAULT_KEY_MANAGER) ==
                    null) {
                APIManagerConfigurationService apiManagerConfigurationService =
                        ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();

                KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                keyManagerConfigurationDTO.setName(APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
                keyManagerConfigurationDTO.setEnabled(true);
                keyManagerConfigurationDTO.setUuid(UUID.randomUUID().toString());
                keyManagerConfigurationDTO.setTenantDomain(tenantDomain);
                keyManagerConfigurationDTO.setDescription(APIConstants.KeyManager.DEFAULT_KEY_MANAGER_DESCRIPTION);
                if (apiManagerConfigurationService != null &&
                        apiManagerConfigurationService.getAPIManagerConfiguration() != null) {
                    String defaultKeyManagerType =
                            apiManagerConfigurationService.getAPIManagerConfiguration()
                                    .getFirstProperty(APIConstants.DEFAULT_KEY_MANAGER_TYPE);
                    if (StringUtils.isNotEmpty(defaultKeyManagerType)) {
                        keyManagerConfigurationDTO.setType(defaultKeyManagerType);
                    } else {
                        keyManagerConfigurationDTO.setType(APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE);
                    }
                }
                TokenHandlingDto tokenHandlingDto = new TokenHandlingDto();
                tokenHandlingDto.setEnable(true);
                tokenHandlingDto.setType(TokenHandlingDto.TypeEnum.REFERENCE);
                tokenHandlingDto.setValue(APIConstants.KeyManager.UUID_REGEX);
                keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.TOKEN_FORMAT_STRING,
                        new Gson().toJson(Arrays.asList(tokenHandlingDto)));
                instance.addKeyManagerConfiguration(keyManagerConfigurationDTO);
            }
        }
    }
}
