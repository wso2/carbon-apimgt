/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.gatekeeper.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.governance.gatekeeper.observer.GatekeeperStartupObserver;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * OSGi component for the Gatekeeper module.
 * Handles component lifecycle and service registration.
 */
@Component(
        name = "org.wso2.carbon.apimgt.governance.gatekeeper",
        immediate = true
)
public class GatekeeperComponent {

    private static final Log log = LogFactory.getLog(GatekeeperComponent.class);
    private ServiceRegistration<?> startupObserverRegistration;

    @Activate
    protected void activate(ComponentContext componentContext) {
        log.info("Activating API Gatekeeper component for API deduplication");

        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            // Register startup observer for LSH index hydration
            GatekeeperStartupObserver startupObserver = new GatekeeperStartupObserver();
            startupObserverRegistration = bundleContext.registerService(
                    ServerStartupObserver.class.getName(), startupObserver, null);

            log.info("API Gatekeeper component activated successfully");

        } catch (Exception e) {
            log.error("Error activating API Gatekeeper component", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        log.info("Deactivating API Gatekeeper component");

        if (startupObserverRegistration != null) {
            startupObserverRegistration.unregister();
        }

        log.info("API Gatekeeper component deactivated");
    }

    @Reference(
            name = "api.manager.config.service",
            service = APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService"
    )
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        GatekeeperServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        GatekeeperServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }
}
