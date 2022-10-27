/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.impl.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.KeyManager;
import org.wso2.apk.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.apk.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.apk.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.ConfigurationHolder;
import org.wso2.apk.apimgt.impl.dto.KeyManagerDto;
import org.wso2.apk.apimgt.impl.dto.OrganizationKeyManagerDto;
import org.wso2.apk.apimgt.impl.internal.ServiceReferenceHolder;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a factory class. You have to use this when you need to initiate classes by reading config file.
 * for example key manager class will be initiated from here.
 */
public class KeyManagerHolder {

    private static Log log = LogFactory.getLog(KeyManagerHolder.class);
    private static final Map<String, OrganizationKeyManagerDto> organizationWiseMap = new HashMap<>();

    public static void addKeyManagerConfiguration(String organization, String name, String type,
                                                  KeyManagerConfiguration keyManagerConfiguration)
            throws APIManagementException {

        String issuer = (String) keyManagerConfiguration.getParameter(APIConstants.KeyManager.ISSUER);

        OrganizationKeyManagerDto organizationKeyManagerDto = organizationWiseMap.get(organization);
        if (organizationKeyManagerDto == null) {
            organizationKeyManagerDto = new OrganizationKeyManagerDto();
        }
        if (organizationKeyManagerDto.getKeyManagerByName(name) != null) {
            log.warn("Key Manager " + name + " already initialized in tenant " + organization);
        }
        if (keyManagerConfiguration.isEnabled() && !KeyManagerConfiguration.TokenType.EXCHANGED
                .equals(keyManagerConfiguration.getTokenType())) {
            KeyManager keyManager = null;
            ConfigurationHolder apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String defaultKeyManagerType = apiManagerConfiguration
                    .getFirstProperty(APIConstants.DEFAULT_KEY_MANAGER_TYPE);
            KeyManagerConnectorConfiguration keyManagerConnectorConfiguration = ServiceReferenceHolder.getInstance()
                    .getKeyManagerConnectorConfiguration(type);
            if (keyManagerConnectorConfiguration != null) {
                if (StringUtils.isNotEmpty(keyManagerConnectorConfiguration.getImplementation())) {
                    try {
                        keyManager = (KeyManager) Class.forName(keyManagerConnectorConfiguration.getImplementation())
                                .getDeclaredConstructor().newInstance();
                        keyManager.setTenantDomain(organization);
                        if (StringUtils.isNotEmpty(defaultKeyManagerType) && defaultKeyManagerType.equals(type)) {
                            keyManagerConfiguration.addParameter(APIConstants.KEY_MANAGER_USERNAME,
                                    apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME));
                            keyManagerConfiguration.addParameter(APIConstants.KEY_MANAGER_PASSWORD,
                                    apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD));
                        }
                        keyManager.loadConfiguration(keyManagerConfiguration);
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException
                            | NoSuchMethodException | InvocationTargetException e) {
                        throw new APIManagementException("Error while loading keyManager configuration", e);
                    }
                }
            } else {
                if (APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(type)) {
                    keyManager = new AMDefaultKeyManagerImpl();
                    keyManager.setTenantDomain(organization);
                    keyManager.loadConfiguration(keyManagerConfiguration);
                }
            }
            KeyManagerDto keyManagerDto = new KeyManagerDto();
            keyManagerDto.setName(name);
            keyManagerDto.setIssuer(issuer);
            keyManagerDto.setKeyManager(keyManager);
            organizationKeyManagerDto.putKeyManagerDto(keyManagerDto);
            organizationWiseMap.put(organization, organizationKeyManagerDto);
        }
    }


    public static Map<String, KeyManagerDto> getTenantKeyManagers(String tenantDomain) {

        OrganizationKeyManagerDto organizationKeyManagerDto = getTenantKeyManagerDto(tenantDomain);
        if (organizationKeyManagerDto != null) {
            return organizationKeyManagerDto.getKeyManagerMap();
        } else {
            return Collections.emptyMap();
        }
    }

    private KeyManagerHolder() {

    }

    public static void updateKeyManagerConfiguration(String organization, String name, String type,
                                                     KeyManagerConfiguration keyManagerConfiguration)
            throws APIManagementException {

        removeKeyManagerConfiguration(organization, name);
        if (keyManagerConfiguration.isEnabled()) {
            addKeyManagerConfiguration(organization, name, type, keyManagerConfiguration);
        }
    }

    public static void removeKeyManagerConfiguration(String tenantDomain, String name) {

        OrganizationKeyManagerDto organizationKeyManagerDto = organizationWiseMap.get(tenantDomain);
        if (organizationKeyManagerDto != null) {
            organizationKeyManagerDto.removeKeyManagerDtoByName(name);
        }
    }

    public static KeyManager getKeyManagerInstance(String tenantDomain, String keyManagerName) {

        OrganizationKeyManagerDto organizationKeyManagerDto = getTenantKeyManagerDto(tenantDomain);
        if (organizationKeyManagerDto != null) {
            KeyManagerDto keyManagerDto = organizationKeyManagerDto.getKeyManagerByName(keyManagerName);
            if (keyManagerDto == null) {
                return null;
            }
            return keyManagerDto.getKeyManager();
        }
        return null;
    }

    private static OrganizationKeyManagerDto getTenantKeyManagerDto(String tenantDomain) {

        OrganizationKeyManagerDto organizationKeyManagerDto = organizationWiseMap.get(tenantDomain);
        if (organizationKeyManagerDto == null) {
            synchronized ("KeyManagerHolder".concat(tenantDomain).intern()) {
                organizationKeyManagerDto = organizationWiseMap.get(tenantDomain);
                if (organizationKeyManagerDto == null) {
                    // TODO: previously km configs are retrieved through eventhub api path /keymanagers
//                    new KeyManagerConfigurationDataRetriever(tenantDomain).run();
//                    organizationKeyManagerDto = organizationWiseMap.get(tenantDomain);
                }
            }
        }
        return organizationKeyManagerDto;
    }
}
