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

package org.wso2.carbon.apimgt.governance.generic.internal;

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
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.generic.GenericValidationEngine;
import org.wso2.carbon.apimgt.governance.generic.observer.GenericStartupObserver;
import org.wso2.carbon.apimgt.governance.generic.service.DeprecationGuideScheduler;
import org.wso2.carbon.apimgt.governance.impl.validator.ValidationEngineFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.core.ServerStartupObserver;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OSGi component for the Generic module.
 * Handles component lifecycle and service registration.
 */
@Component(
        name = "org.wso2.carbon.apimgt.governance.generic",
        immediate = true
)
public class GenericComponent {

    private static final Log log = LogFactory.getLog(GenericComponent.class);
    private static final AtomicBoolean activated = new AtomicBoolean(false);
    private ServiceRegistration<?> startupObserverRegistration;
    private GenericValidationEngine genericValidationEngine;

    @Activate
    protected void activate(ComponentContext componentContext) {
        if (!activated.compareAndSet(false, true)) {
            log.debug("API Generic component already activated, skipping");
            return;
        }

        log.info("Activating API Generic component for API deduplication");

        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            // Register startup observer for LSH index hydration
            GenericStartupObserver startupObserver = new GenericStartupObserver();
            startupObserverRegistration = bundleContext.registerService(
                    ServerStartupObserver.class.getName(), startupObserver, null);

            // Create and register GenericValidationEngine for GENERIC category
            genericValidationEngine = new GenericValidationEngine();
            ValidationEngineFactory.registerValidationEngine(RuleCategory.GENERIC, genericValidationEngine);
            log.info("Registered GenericValidationEngine for GENERIC rule category");

            log.info("API Generic component activated successfully");

        } catch (Exception e) {
            log.error("Error activating API Generic component", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        log.info("Deactivating API Generic component");

        // Shutdown the Deprecation Guide scheduler
        DeprecationGuideScheduler.shutdown();

        if (startupObserverRegistration != null) {
            startupObserverRegistration.unregister();
        }

        // Unregister the validation engine
        ValidationEngineFactory.unregisterValidationEngine(RuleCategory.GENERIC);

        activated.set(false);
        log.info("API Generic component deactivated");
    }

    @Reference(
            name = "api.manager.config.service",
            service = APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService"
    )
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        GenericServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        GenericServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }
}
