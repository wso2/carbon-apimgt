/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.cache.invalidation.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.CacheInvalidationConfiguration;

import java.util.UUID;

/**
 *This class holds the services and data which used in internally.
 */
public class DataHolder {

    private static final String nodeId = UUID.randomUUID().toString();
    private static final DataHolder instance = new DataHolder();
    private APIManagerConfigurationService apiManagerConfigurationService;
    private Boolean started = false;
    private CacheInvalidationConfiguration cacheInvalidationConfiguration;

    private DataHolder() {

    }

    public static DataHolder getInstance() {

        return instance;
    }

    public static String getNodeId() {

        return nodeId;
    }

    public APIManagerConfigurationService getAPIManagerConfigurationService() {

        return apiManagerConfigurationService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {

        this.apiManagerConfigurationService = amcService;
        if (apiManagerConfigurationService != null &&
                apiManagerConfigurationService.getAPIManagerConfiguration() != null) {
            cacheInvalidationConfiguration =
                    apiManagerConfigurationService.getAPIManagerConfiguration().getCacheInvalidationConfiguration();
        }
    }

    public boolean isStarted() {

        return started;
    }

    public void setStarted(Boolean started) {

        this.started = started;
    }

    public CacheInvalidationConfiguration getCacheInvalidationConfiguration() {

        return cacheInvalidationConfiguration;
    }
}
