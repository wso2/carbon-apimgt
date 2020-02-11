/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.keymgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.issuers.AbstractScopesIssuer;
import org.wso2.carbon.apimgt.keymgt.token.JWTGenerator;
import org.wso2.carbon.apimgt.keymgt.token.TokenGenerator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.Map;

public class APIKeyMgtDataHolder {

    private static RegistryService registryService;
    private static RealmService realmService;
    private static APIManagerConfigurationService amConfigService;
    private static Boolean isKeyCacheEnabledKeyMgt = true;
    private static TokenGenerator tokenGenerator;
    private static Map<String, AbstractScopesIssuer> scopesIssuers = new HashMap<String, AbstractScopesIssuer>();
    private static final Log log = LogFactory.getLog(APIKeyMgtDataHolder.class);

    // Scope used for marking Application Tokens
    private static String applicationTokenScope;

    public static Boolean getKeyCacheEnabledKeyMgt() {
        return isKeyCacheEnabledKeyMgt;
    }

    public static void setKeyCacheEnabledKeyMgt(Boolean keyCacheEnabledKeyMgt) {
        isKeyCacheEnabledKeyMgt = keyCacheEnabledKeyMgt;
    }


    public static APIManagerConfigurationService getAmConfigService() {
        return amConfigService;
    }

    public static void setAmConfigService(APIManagerConfigurationService amConfigService) {
        APIKeyMgtDataHolder.amConfigService = amConfigService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        APIKeyMgtDataHolder.registryService = registryService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        APIKeyMgtDataHolder.realmService = realmService;
    }

    public static void initData() {
        try {
            APIKeyMgtDataHolder.isKeyCacheEnabledKeyMgt = getInitValues(APIConstants.KEY_MANAGER_TOKEN_CACHE);

            APIManagerConfiguration configuration = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();

            if (configuration == null) {
                log.error("API Manager configuration is not initialized");
            } else {

                applicationTokenScope = configuration.getFirstProperty(APIConstants
                                                                               .APPLICATION_TOKEN_SCOPE);
                JWTConfigurationDto jwtConfigurationDto = configuration.getJwtConfigurationDto();
                if (log.isDebugEnabled()) {
                    log.debug("JWTGeneration enabled : " + jwtConfigurationDto.isEnabled());
                }

                if (jwtConfigurationDto.isEnabled()) {
                    if (jwtConfigurationDto.getJwtGeneratorImplClass() == null) {
                        tokenGenerator = new JWTGenerator();
                    } else {
                        try {
                            tokenGenerator = (TokenGenerator) APIUtil
                                    .getClassForName(jwtConfigurationDto.getJwtGeneratorImplClass()).newInstance();
                        } catch (InstantiationException e) {
                            log.error(
                                    "Error while instantiating class " + jwtConfigurationDto.getJwtGeneratorImplClass(),
                                    e);
                        } catch (IllegalAccessException e) {
                            log.error(e);
                        } catch (ClassNotFoundException e) {
                            log.error("Cannot find the class " + jwtConfigurationDto.getJwtGeneratorImplClass() + e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occur while initializing API KeyMgt Data Holder.Default configuration will be used." + e.toString());
        }
    }

    private static boolean getInitValues(String constVal) {
        String val = getAmConfigService().getAPIManagerConfiguration().getFirstProperty(constVal);
        if (val != null) {
            return Boolean.parseBoolean(val);
        }
        return false;
    }

    public static boolean isJwtGenerationEnabled(){

        APIManagerConfiguration configuration = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (configuration == null){
            return false;
        }
        JWTConfigurationDto jwtConfigurationDto = configuration.getJwtConfigurationDto();
        return jwtConfigurationDto.isEnabled();
    }

    // Returns the implementation for JWTTokenGenerator.
    public static TokenGenerator getTokenGenerator() {
        return tokenGenerator;
    }

    public static String getApplicationTokenScope() {
        return applicationTokenScope;
    }

    /**
     * Add scope issuers to the map.
     * @param prefix prefix of the scope issuer.
     * @param scopesIssuer scope issuer instance.
     */
    public static void addScopesIssuer(String prefix, AbstractScopesIssuer scopesIssuer) {
        scopesIssuers.put(prefix, scopesIssuer);
    }

    public static void setScopesIssuers(Map<String, AbstractScopesIssuer> scpIssuers) {
        scopesIssuers = scpIssuers;
    }

    public static Map<String, AbstractScopesIssuer> getScopesIssuers() {
        return scopesIssuers;
    }
}
