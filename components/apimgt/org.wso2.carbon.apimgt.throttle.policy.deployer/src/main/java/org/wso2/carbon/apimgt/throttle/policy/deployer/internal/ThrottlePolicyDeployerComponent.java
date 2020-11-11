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
package org.wso2.carbon.apimgt.throttle.policy.deployer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.throttle.policy.deployer.utils.ThrottlePolicyStartupListener;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.event.processor.core.EventProcessorService;

/**
 * Throttle policy deployer component.
 */
@Component(
        name = "org.wso2.apimgt.throttle.policy.deployer",
        immediate = true)
public class ThrottlePolicyDeployerComponent {

    private static final Log log = LogFactory.getLog(ThrottlePolicyDeployerComponent.class);

    private ServiceRegistration registration;

    @Activate
    protected void activate(ComponentContext context) {

        log.debug("Activating component...");
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        if (configuration == null) {
            log.warn("API Manager Configuration not properly set.");
            return;
        }
        ThrottlePolicyStartupListener throttlePolicyStartupListener =
                new ThrottlePolicyStartupListener();
        registration = context.getBundleContext()
                .registerService(ServerStartupObserver.class, throttlePolicyStartupListener, null);
        registration = context.getBundleContext()
                .registerService(ServerShutdownHandler.class, throttlePolicyStartupListener, null);
    }

    @Reference(
            name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {

        log.debug("Setting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {

        log.debug("Unsetting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
    }

    @Reference(
            name = "event.processor.service",
            service = EventProcessorService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventProcessorService")
    protected void setEventProcessorService(EventProcessorService eventProcessorService) {

        log.debug("Setting EventProcessor Service");
        ServiceReferenceHolder.getInstance().setEventProcessorService(eventProcessorService);
    }

    protected void unsetEventProcessorService(EventProcessorService eventProcessorService) {

        log.debug("Unsetting EventProcessor Service");
        ServiceReferenceHolder.getInstance().setEventProcessorService(null);
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Deactivating component");
        }
        if (this.registration != null) {
            this.registration.unregister();
        }
    }

}
