/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.jms.listener.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.caching.CacheInvalidationService;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerDataService;
import org.wso2.carbon.apimgt.impl.throttling.APIThrottleDataService;
import org.wso2.carbon.apimgt.impl.token.RevokedTokenService;

/**
 * Class for keeping service references.
 */
public class ServiceReferenceHolder {

    private static ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private APIThrottleDataService throttleDataService;
    private APIManagerConfiguration apimConfiguration;
    private CacheInvalidationService cacheInvalidationService;
    private RevokedTokenService revokedTokenService;
    private KeyManagerConfigurationService keyManagerService;
    private KeyManagerDataService keyManagerDataService;

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    private ServiceReferenceHolder() {
    }

    public void setAPIThrottleDataService(APIThrottleDataService dataService) {
        if (dataService != null) {
            throttleDataService = dataService;
        } else {
            throttleDataService = null;
        }
    }

    public APIThrottleDataService getAPIThrottleDataService() {
        return throttleDataService;
    }

    public APIManagerConfiguration getAPIMConfiguration() {
        return apimConfiguration;
    }

    public void setAPIMConfigurationService(APIManagerConfigurationService configurationService) {
        if (configurationService == null) {
            this.apimConfiguration = null;
        } else {
            this.apimConfiguration = configurationService.getAPIManagerConfiguration();
        }

    }

    public void setCacheInvalidationService(CacheInvalidationService cacheInvalidationService) {
        this.cacheInvalidationService = cacheInvalidationService;

    }

    public CacheInvalidationService getCacheInvalidationService() {

        return cacheInvalidationService;
    }

    public void setRevokedTokenService(RevokedTokenService revokedTokenService) {
        this.revokedTokenService = revokedTokenService;
    }

    public RevokedTokenService getRevokedTokenService() {

        return revokedTokenService;
    }

    public void setKeyManagerService(KeyManagerConfigurationService keyManagerService) {
        this.keyManagerService = keyManagerService;
    }

    public KeyManagerConfigurationService getKeyManagerService() {

        return keyManagerService;
    }

    public KeyManagerDataService getKeyManagerDataService() {
        return keyManagerDataService;
    }

    public void setKeyManagerDataService(KeyManagerDataService keyManagerDataService) {
        this.keyManagerDataService = keyManagerDataService;
    }
}
