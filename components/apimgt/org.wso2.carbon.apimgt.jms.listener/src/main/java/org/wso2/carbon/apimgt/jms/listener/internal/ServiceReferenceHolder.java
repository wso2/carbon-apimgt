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

import org.wso2.carbon.apimgt.gateway.service.APIThrottleDataService;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

/**
 * Class for keeping service references.
 */
public class ServiceReferenceHolder {

    private static ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private APIThrottleDataService throttleDataService;
    private ThrottleDataHolder throttleDataHolder;
    private APIManagerConfiguration apimConfiguration;

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    private ServiceReferenceHolder() {
    }

    public void setAPIThrottleDataService(APIThrottleDataService dataService) {
        if (dataService != null) {
            throttleDataService = dataService;
            throttleDataHolder = dataService.getThrottleDataHolder();
        } else {
            throttleDataHolder = null;
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

    public ThrottleDataHolder getThrottleDataHolder() {
        return throttleDataHolder;
    }
}
