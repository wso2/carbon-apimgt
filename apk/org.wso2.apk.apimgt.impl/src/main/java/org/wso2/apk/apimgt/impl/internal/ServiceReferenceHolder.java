/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.apk.apimgt.impl.internal;

import org.wso2.apk.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.apk.apimgt.impl.APIManagerConfigurationService;
import org.wso2.apk.apimgt.impl.caching.CacheProvider;
import org.wso2.apk.apimgt.impl.config.APIMConfigService;
import org.wso2.apk.apimgt.impl.config.APIMConfigServiceImpl;
import org.wso2.apk.apimgt.impl.recommendationmgt.AccessTokenGenerator;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private APIManagerConfigurationService amConfigurationService;
    private APIMConfigService apimConfigService;
    private CacheProvider cacheProvider;

    private AccessTokenGenerator accessTokenGenerator;
    private Map<String, KeyManagerConnectorConfiguration> keyManagerConnectorConfigurationMap = new HashMap<>();

    private KeyStore trustStore;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {

        return instance;
    }

    public APIManagerConfigurationService getAPIManagerConfigurationService() {

        return amConfigurationService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigurationService) {

        this.amConfigurationService = amConfigurationService;
    }

    public void setAPIMConfigService(APIMConfigService apimConfigService) {
        this.apimConfigService = apimConfigService;
    }

    public APIMConfigService getApimConfigService() {
        if (apimConfigService != null){
            return apimConfigService;
        }
        return new APIMConfigServiceImpl();
    }

    public CacheProvider getCacheStore() {

        return cacheProvider;
    }

    public void setCacheStoreService(CacheProvider cacheProvider) {

        this.cacheProvider = cacheProvider;
    }

    public AccessTokenGenerator getAccessTokenGenerator() {
        return accessTokenGenerator;
    }

    public void setAccessTokenGenerator(AccessTokenGenerator accessTokenGenerator) {
        this.accessTokenGenerator = accessTokenGenerator;
    }

    public void addKeyManagerConnectorConfiguration(String type,
                                                    KeyManagerConnectorConfiguration keyManagerConnectorConfiguration) {
        keyManagerConnectorConfigurationMap.put(type, keyManagerConnectorConfiguration);
    }

    public void removeKeyManagerConnectorConfiguration(String type) {
        keyManagerConnectorConfigurationMap.remove(type);
    }

    public KeyManagerConnectorConfiguration getKeyManagerConnectorConfiguration(String type) {
        return keyManagerConnectorConfigurationMap.get(type);
    }

    public Map<String, KeyManagerConnectorConfiguration> getKeyManagerConnectorConfigurations() {
        return keyManagerConnectorConfigurationMap;
    }

    public KeyStore getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }
}
