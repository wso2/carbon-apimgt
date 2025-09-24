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

package org.wso2.carbon.apimgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

public class ServiceReferenceHolder {

    private static final Log log = LogFactory.getLog(ServiceReferenceHolder.class);

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private APIManagerConfigurationService amConfigurationService;
    private boolean isGatewayAPIKeyValidationEnabled;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public APIManagerConfigurationService getAPIManagerConfigurationService() {
        return amConfigurationService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting API Manager Configuration Service");
        }
        this.amConfigurationService = amConfigurationService;
        if (amConfigurationService != null) {
            setAPIGatewayKeyCacheStatus(amConfigurationService.getAPIManagerConfiguration());
            log.info("API Manager Configuration Service set successfully");
        } else {
            log.warn("Null API Manager Configuration Service provided");
        }
    }

    public void  setAPIGatewayKeyCacheStatus(APIManagerConfiguration config) {
        if (log.isDebugEnabled()) {
            log.debug("Setting API Gateway key cache status");
        }
        try {
            if (config != null) {
                String serviceURL = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
                isGatewayAPIKeyValidationEnabled = Boolean.parseBoolean(serviceURL);
                if (log.isDebugEnabled()) {
                    log.debug("Gateway API key validation enabled: " + isGatewayAPIKeyValidationEnabled);
                }
            } else {
                log.warn("API Manager configuration is null, disabling gateway API key validation");
                isGatewayAPIKeyValidationEnabled = false;
            }
        } catch (Exception e) {
            log.error("Error occurred while setting API Gateway key cache status", e);
            isGatewayAPIKeyValidationEnabled = false;
        }
    }

    public boolean isGatewayAPIKeyValidationEnabled(){
        return isGatewayAPIKeyValidationEnabled;
    }

}
