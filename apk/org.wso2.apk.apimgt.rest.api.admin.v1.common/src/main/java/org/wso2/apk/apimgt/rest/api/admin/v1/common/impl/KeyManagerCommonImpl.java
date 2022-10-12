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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.apache.commons.lang3.StringUtils;
import org.wso2.apk.apimgt.api.APIAdmin;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.apk.apimgt.impl.APIAdminImpl;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.kmclient.ApacheFeignHttpClient;
import org.wso2.carbon.apimgt.impl.kmclient.KMClientErrorDecoder;
import org.wso2.carbon.apimgt.impl.kmclient.model.OpenIDConnectDiscoveryClient;
import org.wso2.carbon.apimgt.impl.kmclient.model.OpenIdConnectConfiguration;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings.KeyManagerMappingUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.KeyManagerDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.KeyManagerListDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.KeyManagerWellKnownResponseDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;

import java.util.List;

public class KeyManagerCommonImpl {

    private static final String KEY_MANAGER_NOT_FOUND = "Requested KeyManager not found";

    private KeyManagerCommonImpl() {
    }

    /**
     * Get key manager well-known information
     *
     * @param url  Well-known endpoint url
     * @param type Key manager type
     * @return Well-known information
     * @throws APIManagementException When an internal error occurs
     */
    public static KeyManagerWellKnownResponseDTO getWellKnownInfoKeyManager(String url, String type)
            throws APIManagementException {
        if (StringUtils.isNotEmpty(url)) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            OpenIDConnectDiscoveryClient openIDConnectDiscoveryClient =
                    Feign.builder().client(new ApacheFeignHttpClient(APIUtil.getHttpClient(url)))
                            .encoder(new GsonEncoder(gson)).decoder(new GsonDecoder(gson))
                            .errorDecoder(new KMClientErrorDecoder())
                            .target(OpenIDConnectDiscoveryClient.class, url);
            OpenIdConnectConfiguration openIdConnectConfiguration =
                    openIDConnectDiscoveryClient.getOpenIdConnectConfiguration();
            if (openIdConnectConfiguration != null) {
                KeyManagerWellKnownResponseDTO keyManagerWellKnownResponseDTO = KeyManagerMappingUtil
                        .fromOpenIdConnectConfigurationToKeyManagerConfiguration(openIdConnectConfiguration);
                keyManagerWellKnownResponseDTO.getValue().setWellKnownEndpoint(url);
                keyManagerWellKnownResponseDTO.getValue().setType(type);
                return keyManagerWellKnownResponseDTO;
            }
        }
        return new KeyManagerWellKnownResponseDTO();
    }

    /**
     * Get all key managers
     *
     * @param organization Tenant organization
     * @return List of key managers
     * @throws APIManagementException When an internal error occurs
     */
    public static KeyManagerListDTO getAllKeyManagers(String organization) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        List<KeyManagerConfigurationDTO> keyManagerConfigurationsByOrganization =
                apiAdmin.getKeyManagerConfigurationsByOrganization(organization);
        return KeyManagerMappingUtil.toKeyManagerListDTO(keyManagerConfigurationsByOrganization);
    }

    /**
     * Remove a key manager
     *
     * @param keyManagerId Key manager ID
     * @param organization Tenant organization
     * @throws APIManagementException When an internal error occurs
     */
    public static void removeKeyManager(String keyManagerId, String organization) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                apiAdmin.getKeyManagerConfigurationById(organization, keyManagerId);
        if (keyManagerConfigurationDTO != null) {
            apiAdmin.deleteKeyManagerConfigurationById(organization, keyManagerConfigurationDTO);

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.KEY_MANAGER,
                    new Gson().toJson(keyManagerConfigurationDTO), APIConstants.AuditLogConstants.DELETED,
                    RestApiCommonUtil.getLoggedInUsername());
        } else {
            throw new APIManagementException(KEY_MANAGER_NOT_FOUND, ExceptionCodes.KEY_MANAGER_NOT_FOUND);
        }
    }

    /**
     * Get key manager configuration details
     *
     * @param keyManagerId Key manager ID
     * @param organization Tenant organization
     * @return Key manager details
     * @throws APIManagementException When an internal error occurs
     */
    public static KeyManagerDTO getKeyManagerConfiguration(String keyManagerId, String organization)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                apiAdmin.getKeyManagerConfigurationById(organization, keyManagerId);
        if (keyManagerConfigurationDTO != null) {
            return KeyManagerMappingUtil.toKeyManagerDTO(keyManagerConfigurationDTO);
        }
        throw new APIManagementException(KEY_MANAGER_NOT_FOUND, ExceptionCodes.KEY_MANAGER_NOT_FOUND);
    }

    /**
     * Update key manager
     *
     * @param keyManagerId Key manager ID
     * @param body         Requested key manager changes
     * @param organization Tenant organization
     * @return Updated key manager details
     * @throws APIManagementException When an internal error occurs
     */
    public static KeyManagerDTO updateKeyManager(String keyManagerId, KeyManagerDTO body, String organization)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                KeyManagerMappingUtil.toKeyManagerConfigurationDTO(organization, body);
        keyManagerConfigurationDTO.setUuid(keyManagerId);
        KeyManagerConfigurationDTO oldKeyManagerConfigurationDTO =
                apiAdmin.getKeyManagerConfigurationById(organization, keyManagerId);
        if (oldKeyManagerConfigurationDTO == null) {
            throw new APIManagementException(KEY_MANAGER_NOT_FOUND, ExceptionCodes.KEY_MANAGER_NOT_FOUND);
        } else {
            if (!oldKeyManagerConfigurationDTO.getName().equals(keyManagerConfigurationDTO.getName())) {
                throw new APIManagementException("Key Manager name couldn't able to change",
                        ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
            }
            KeyManagerConfigurationDTO retrievedKeyManagerConfigurationDTO =
                    apiAdmin.updateKeyManagerConfiguration(keyManagerConfigurationDTO);
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.KEY_MANAGER,
                    new Gson().toJson(keyManagerConfigurationDTO),
                    APIConstants.AuditLogConstants.UPDATED, RestApiCommonUtil.getLoggedInUsername());
            return KeyManagerMappingUtil.toKeyManagerDTO(retrievedKeyManagerConfigurationDTO);
        }
    }

    /**
     * Add a new key manager
     *
     * @param body         Requested key manager details
     * @param organization Tenant organization
     * @return Created key manager details
     * @throws APIManagementException When an internal error occurs
     */
    public static KeyManagerDTO addNewKeyManager(KeyManagerDTO body, String organization)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                KeyManagerMappingUtil.toKeyManagerConfigurationDTO(organization, body);
        KeyManagerConfigurationDTO createdKeyManagerConfiguration =
                apiAdmin.addKeyManagerConfiguration(keyManagerConfigurationDTO);
        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.KEY_MANAGER,
                new Gson().toJson(keyManagerConfigurationDTO),
                APIConstants.AuditLogConstants.CREATED, RestApiCommonUtil.getLoggedInUsername());
        return KeyManagerMappingUtil.toKeyManagerDTO(createdKeyManagerConfiguration);
    }
}
