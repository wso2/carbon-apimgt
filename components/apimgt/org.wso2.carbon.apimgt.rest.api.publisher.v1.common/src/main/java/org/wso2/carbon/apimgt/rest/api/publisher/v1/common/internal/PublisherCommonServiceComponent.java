/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com/).
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.internal;

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
import org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService;
import org.wso2.carbon.apimgt.impl.APIMDependencyConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.GatewayArtifactGenerator;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.SynapseArtifactGenerator;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OSGi component responsible for managing and configuring the API Manager Configuration Service in the
 * API Publisher common component. This class activates the OSGi service and binds/unbinds the necessary services.
 */
@Component(
        name = "api.publisher.common.component",
        immediate = true)
public class PublisherCommonServiceComponent {

    private static final Log log = LogFactory.getLog(PublisherCommonServiceComponent.class);
    private SynapseArtifactGenerator gatewayArtifactGenerator;
    private ServiceRegistration serviceRegistration;

    /**
     * Activates the component.
     *
     * @param context The component context.
     */
    @Activate
    protected void activate(ComponentContext context) {

        log.debug("PublisherCommonService Component activated");
        gatewayArtifactGenerator = new SynapseArtifactGenerator();
        gatewayArtifactGenerator.initialize();
        serviceRegistration = context.getBundleContext().registerService(GatewayArtifactGenerator.class.getName(),
                gatewayArtifactGenerator, null);
    }

    /**
     * Deactivate publisher common component.
     *
     * @param context OSGI Component Context
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Deactivating PublisherCommonService Component");
        if (serviceRegistration != null) {
            try {
                log.debug("Unregistering PublisherCommonService...");
                serviceRegistration.unregister();
            } catch (IllegalStateException exception) {
                log.warn("Service already unregistered", exception);
            }
        }
        if (gatewayArtifactGenerator != null) {
            try {
                gatewayArtifactGenerator.shutdown();
            } catch (Exception e) {
                log.error("Error shutting down gateway artifact generator", e);
            }
        }
    }

    /**
     * Sets the API Manager Configuration Service. This method is called  when the API Manager Configuration Service
     * becomes available.
     *
     * @param configurationService The API Manager Configuration Service instance.
     */
    @Reference(
            name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY,
            policy = org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting APIM Configuration Service");
        }
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    /**
     * Unsets the API Manager Configuration Service.
     *
     * @param configurationService The API Manager Configuration Service instance.
     */
    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting APIM Configuration Service");
        }
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
    }

    /**
     * Sets the Realm Service. This method is called when the Realm Service becomes available.
     *
     * @param realmService The Realm Service instance.
     */
    @Reference(
            name = "user.realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("Realm service initialized");
        }
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets the Realm Service.
     *
     * @param realmService The Realm Service instance.
     */
    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }

    @Reference(name = "org.wso2.carbon.apimgt.governance.service",
            service = APIMGovernanceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIMGovernanceService")
    protected void setAPIMGovernanceService(APIMGovernanceService service) {
        if (log.isDebugEnabled()) {
            log.debug("APIMGovernanceService set in PublisherServiceComponent");
        }
        ServiceReferenceHolder.getInstance().setAPIMGovernanceService(service);
    }

    protected void unsetAPIMGovernanceService(APIMGovernanceService service) {
        if (log.isDebugEnabled()) {
            log.debug("APIMGovernanceService unset in PublisherServiceComponent");
        }
        ServiceReferenceHolder.getInstance().setAPIMGovernanceService(null);
    }

    @Reference(name = "apim.dependency.config.service", service = APIMDependencyConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIMDependencyConfigurationService")
    protected void setAPIMDependencyConfigurationService(APIMDependencyConfigurationService service) {

        log.debug("Setting APIM Dependency Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMDependencyConfigurationService(service);
    }

    protected void unsetAPIMDependencyConfigurationService(APIMDependencyConfigurationService service) {
        ServiceReferenceHolder.getInstance().setAPIMDependencyConfigurationService(null);
    }
}
