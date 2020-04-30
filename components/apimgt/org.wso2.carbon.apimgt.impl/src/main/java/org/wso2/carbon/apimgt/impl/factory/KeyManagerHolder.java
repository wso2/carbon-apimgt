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

package org.wso2.carbon.apimgt.impl.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerConfigurationsDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is a factory class.you have to use this when you need to initiate classes by reading config file.
 * for example key manager class will be initiate from here.
 */
public class KeyManagerHolder {

    private static Log log = LogFactory.getLog(KeyManagerHolder.class);
    private static Map<String, Map<String, KeyManager>> keyManagerMap = new HashMap<>();
    private static Map<String,KeyManager> tenantKeyManager = new HashMap<>();

    public static void addKeyManagerConfiguration(String tenantDomain, String name, String type,
                                                  KeyManagerConfiguration keyManagerConfiguration)
            throws APIManagementException {

        Map<String, KeyManager> tenantWiseKeyManagerMap = keyManagerMap.getOrDefault(tenantDomain, new HashMap<>());
        KeyManager keyManager = tenantWiseKeyManagerMap.get(name);
        if (keyManager == null) {
            APIManagerConfiguration apiManagerConfiguration =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                            .getAPIManagerConfiguration();
            if (APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(type)) {
                keyManager = new AMDefaultKeyManagerImpl();
                keyManager.loadConfiguration(keyManagerConfiguration);
                tenantWiseKeyManagerMap.put(APIConstants.KeyManager.DEFAULT_KEY_MANAGER, keyManager);
            }
            KeyManagerConfigurationsDto keyManagerConfigurationsDto =
                    apiManagerConfiguration.getKeyManagerConfigurationsDto();
            if (keyManagerConfigurationsDto != null) {
                KeyManagerConfigurationsDto.KeyManagerConfigurationDto keyManagerConfigurationDto =
                        keyManagerConfigurationsDto.getKeyManagerConfiguration().get(type);
                if (keyManagerConfigurationDto != null &&
                        StringUtils.isNotEmpty(keyManagerConfigurationDto.getImplementationClass())) {
                    try {
                        keyManager =
                                (KeyManager) Class.forName(keyManagerConfigurationDto.getImplementationClass())
                                        .newInstance();
                        keyManager.loadConfiguration(keyManagerConfiguration);
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                        throw new APIManagementException("Error while loading keymanager configuration", e);
                    }

                }
            }
            tenantWiseKeyManagerMap.put(name, keyManager);
        }
        keyManagerMap.put(tenantDomain, tenantWiseKeyManagerMap);
    }


    /**
     * This method will take hardcoded class name from api-manager.xml file and will return that class's instance.
     * This class should be implementation class of keyManager.
     *
     * @return keyManager instance.
     */
    public static KeyManager getKeyManagerInstance(String tenantDomain) {

        if (tenantKeyManager.containsKey(tenantDomain)) {
            return tenantKeyManager.get(tenantDomain);
        }

        Map<String, KeyManager> tenantWiseKeyManger = keyManagerMap.get(tenantDomain);
        Iterator<KeyManager> iterator = tenantWiseKeyManger.values().iterator();
        if (iterator.hasNext()) {
            KeyManager effectiveKeyManager = iterator.next();
            tenantKeyManager.put(tenantDomain, effectiveKeyManager);
            return effectiveKeyManager;
        }
        return null;
    }

    public static Map<String, KeyManager> getTenantKeyManagers(String tenantDomain) {

        Map<String, KeyManager> tenantWiseKeyManger = keyManagerMap.get(tenantDomain);
        return tenantWiseKeyManger;
    }



    private KeyManagerHolder() {

    }

    public static void updateKeyManagerConfiguration(String tenantDomain, String name, String type,
                                                     KeyManagerConfiguration keyManagerConfiguration, boolean enabled)
            throws APIManagementException {

        Map<String, KeyManager> tenantWiseKeyManagerMap = keyManagerMap.get(tenantDomain);
        if (tenantWiseKeyManagerMap == null) {
            throw new APIManagementException("KeyManager didn't configured for tenant" + tenantDomain);
        }
        KeyManager keyManager = tenantWiseKeyManagerMap.get(name);
        if (keyManager == null) {
            throw new APIManagementException(
                    "KeyManager " + name + " didn't configured for tenant" + tenantDomain);
        }
        if (enabled) {
            APIManagerConfiguration apiManagerConfiguration =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                            .getAPIManagerConfiguration();
            if (APIConstants.KeyManager.DEFAULT_KEY_MANAGER.equals(type)) {
                keyManager = new AMDefaultKeyManagerImpl();
                keyManager.loadConfiguration(keyManagerConfiguration);
                tenantWiseKeyManagerMap.put(APIConstants.KeyManager.DEFAULT_KEY_MANAGER, keyManager);
            }
            KeyManagerConfigurationsDto keyManagerConfigurationsDto =
                    apiManagerConfiguration.getKeyManagerConfigurationsDto();
            if (keyManagerConfigurationsDto != null) {
                KeyManagerConfigurationsDto.KeyManagerConfigurationDto keyManagerConfigurationDto =
                        keyManagerConfigurationsDto.getKeyManagerConfiguration().get(type);
                if (keyManagerConfigurationDto != null &&
                        StringUtils.isNotEmpty(keyManagerConfigurationDto.getImplementationClass())) {
                    try {
                        keyManager =
                                (KeyManager) Class.forName(keyManagerConfigurationDto.getImplementationClass())
                                        .newInstance();
                        keyManager.loadConfiguration(keyManagerConfiguration);
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                        throw new APIManagementException("Error while loading keymanager configuration", e);
                    }

                }
            }
            tenantWiseKeyManagerMap.put(name, keyManager);
        } else {
            tenantWiseKeyManagerMap.remove(name);
        }
        keyManagerMap.put(tenantDomain, tenantWiseKeyManagerMap);
        tenantWiseKeyManagerMap.remove(tenantDomain);
    }

    public static void removeKeyManagerConfiguration(String tenantDomain, String name) {

        Map<String, KeyManager> tenantWiseKeyManagerMap = keyManagerMap.get(tenantDomain);
        if (tenantWiseKeyManagerMap != null){
            tenantWiseKeyManagerMap.remove(name);
            keyManagerMap.put(tenantDomain,tenantWiseKeyManagerMap);
            tenantWiseKeyManagerMap.remove(tenantDomain);
        }
    }
}
