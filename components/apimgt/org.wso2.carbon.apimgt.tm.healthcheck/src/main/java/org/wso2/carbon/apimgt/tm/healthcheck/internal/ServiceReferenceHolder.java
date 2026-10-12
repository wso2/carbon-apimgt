/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.tm.healthcheck.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

/**
 * Holds the OSGi service references this bundle receives via {@code @Reference} injection, so that
 * plain (non-OSGi-aware) classes elsewhere in this bundle - such as
 * {@link org.wso2.carbon.apimgt.tm.healthcheck.ControlPlaneConnectionConfigResolver} - can reach them
 * without needing an OSGi component context of their own.
 *
 * <p>This follows the same single-instance "holder" pattern already used elsewhere in carbon-apimgt
 * (e.g. {@code org.wso2.carbon.apimgt.jms.listener.internal.ServiceReferenceHolder}) for the same
 * reason: a bundle's declarative-services component is the only place OSGi injects a reference, but
 * many other classes in the bundle need to read it.</p>
 */
public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder INSTANCE = new ServiceReferenceHolder();

    private APIManagerConfigurationService apiManagerConfigurationService;

    private ServiceReferenceHolder() {

    }

    /**
     * Returns this bundle's single {@code ServiceReferenceHolder} instance.
     *
     * @return the holder instance
     */
    public static ServiceReferenceHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Called by {@link TMHealthCheckServiceComponent} when OSGi binds/unbinds the
     * {@link APIManagerConfigurationService}.
     *
     * @param configurationService the bound service, or {@code null} when it is being unbound
     */
    public void bindApiManagerConfigurationService(APIManagerConfigurationService configurationService) {

        this.apiManagerConfigurationService = configurationService;
    }

    /**
     * Returns the live {@link APIManagerConfiguration} - the same configuration object the Traffic
     * Manager's own Event Hub publisher/receiver code reads - fetched fresh from the bound service on
     * every call rather than cached on this holder, so callers always see the current configuration.
     *
     * @return the current {@link APIManagerConfiguration}, or {@code null} if the configuration service
     *         is not currently bound
     */
    public APIManagerConfiguration getApiManagerConfiguration() {

        if (apiManagerConfigurationService == null) {
            return null;
        }
        return apiManagerConfigurationService.getAPIManagerConfiguration();
    }
}
