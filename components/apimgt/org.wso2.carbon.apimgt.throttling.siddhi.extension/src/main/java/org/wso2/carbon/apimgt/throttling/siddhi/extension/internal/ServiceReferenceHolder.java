/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.throttling.siddhi.extension.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

/**
 * Singleton class to hold references to OSGi services used by the Siddhi extension.
 * This class provides access to the {@link APIManagerConfigurationService} instance.
 */
public class ServiceReferenceHolder {
    
    private static final Log log = LogFactory.getLog(ServiceReferenceHolder.class);
    
    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private APIManagerConfigurationService amConfigurationService;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private ServiceReferenceHolder() {

    }

    /**
     * Returns the singleton instance of {@code ServiceReferenceHolder}.
     *
     * @return singleton instance of ServiceReferenceHolder
     */
    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    /**
     * Returns the {@link APIManagerConfigurationService} instance.
     *
     * @return APIManagerConfigurationService instance
     */
    public APIManagerConfigurationService getAPIManagerConfigurationService() {
        return amConfigurationService;
    }

    /**
     * Sets the {@link APIManagerConfigurationService} instance.
     *
     * @param amConfigurationService APIManagerConfigurationService instance to set
     */
    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigurationService) {
        this.amConfigurationService = amConfigurationService;
        if (log.isDebugEnabled()) {
            log.debug("API Manager Configuration Service " + (amConfigurationService != null ? "set" : "unset"));
        }
    }

}

