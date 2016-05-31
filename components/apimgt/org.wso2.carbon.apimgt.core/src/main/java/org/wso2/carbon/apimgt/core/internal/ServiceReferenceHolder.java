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

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

public class ServiceReferenceHolder {

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
        this.amConfigurationService = amConfigurationService;
        setAPIGatewayKeyCacheStatus(amConfigurationService.getAPIManagerConfiguration());
    }

    public void  setAPIGatewayKeyCacheStatus(APIManagerConfiguration config) {
        try {
                String serviceURL = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
                isGatewayAPIKeyValidationEnabled = Boolean.parseBoolean(serviceURL);
            } catch (Exception e) {
                isGatewayAPIKeyValidationEnabled = false;
            }
       }

    public boolean isGatewayAPIKeyValidationEnabled(){
        return isGatewayAPIKeyValidationEnabled;
    }

}
