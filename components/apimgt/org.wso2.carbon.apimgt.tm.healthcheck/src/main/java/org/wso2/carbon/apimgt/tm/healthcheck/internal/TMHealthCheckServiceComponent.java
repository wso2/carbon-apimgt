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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.tm.healthcheck.TMControlPlaneConnectivityHealthChecker;
import org.wso2.carbon.healthcheck.api.core.HealthChecker;

/**
 * Declarative-services (DS) component for this standalone OSGi bundle.
 *
 * <p>On activation, it registers a {@link TMControlPlaneConnectivityHealthChecker} instance as an OSGi
 * {@link HealthChecker} service - the same way carbon-health-check's own built-in
 * {@code HealthMonitorServiceComponent} registers {@code DataSourceHealthChecker} - so the existing,
 * unmodified {@code CarbonHealthCheckService} aggregator and {@code GET /health} endpoint pick it up
 * automatically (see {@code contracts/health-checker-spi.md} "Registration contract").</p>
 *
 * <p>Because this feature ships as its own bundle, separate from {@code org.wso2.carbon.apimgt.impl}
 * (research.md &sect;1a), it cannot reach {@code APIManagerConfigurationService} via same-bundle static
 * access the way code inside that bundle does; it obtains it through its own OSGi {@code @Reference}
 * below and stores it in {@link ServiceReferenceHolder} for the rest of this bundle to use.</p>
 */
@Component(
        name = "org.wso2.carbon.apimgt.tm.healthcheck.internal.TMHealthCheckServiceComponent",
        immediate = true)
public class TMHealthCheckServiceComponent {

    private static final Log log = LogFactory.getLog(TMHealthCheckServiceComponent.class);

    @Reference(
            name = "api.manager.config.service",
            service = APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {

        if (log.isDebugEnabled()) {
            log.debug("Setting APIManagerConfigurationService on " + this.getClass().getSimpleName());
        }
        ServiceReferenceHolder.getInstance().bindApiManagerConfigurationService(service);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting APIManagerConfigurationService on " + this.getClass().getSimpleName());
        }
        ServiceReferenceHolder.getInstance().bindApiManagerConfigurationService(null);
    }

    @Activate
    protected void activate(ComponentContext ctxt) {

        ctxt.getBundleContext().registerService(
                HealthChecker.class.getName(), new TMControlPlaneConnectivityHealthChecker(), null);
        if (log.isDebugEnabled()) {
            log.debug("Registered TMControlPlaneConnectivityHealthChecker as a HealthChecker OSGi service");
        }
    }
}
