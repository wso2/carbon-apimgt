/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.impl.internal;

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
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.impl.APIMGovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.ComplianceEvaluationScheduler;
import org.wso2.carbon.apimgt.governance.impl.listener.APIMGovServerStartupShutdownListener;
import org.wso2.carbon.apimgt.governance.impl.observer.APIMGovernanceConfigDeployer;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil;
import org.wso2.carbon.apimgt.governance.impl.validator.ValidationEngineService;
import org.wso2.carbon.apimgt.governance.impl.validator.ValidationEngineServiceImpl;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.jms.listener.JMSListenerShutDownService;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * This class represents the Governance Component
 */
@Component(
        name = "org.wso2.apimgt.governance.impl.services",
        immediate = true)
public class GovernanceComponent {

    private static final Log log = LogFactory.getLog(GovernanceComponent.class);
    private ServiceRegistration registration;

    @Activate
    protected void activate(ComponentContext componentContext) throws Exception {

        log.info("Activating APIM Governance component");

        try {
            BundleContext bundleContext = componentContext.getBundleContext();
            APIMGovServerStartupShutdownListener startupShutdownListener
                    = new APIMGovServerStartupShutdownListener();
            registration = bundleContext
                    .registerService(ServerStartupObserver.class, startupShutdownListener, null);
            registration = bundleContext
                    .registerService(ServerShutdownHandler.class, startupShutdownListener, null);
            registration = bundleContext
                    .registerService(JMSListenerShutDownService.class, startupShutdownListener, null);

            log.info("Successfully registered server lifecycle listeners");

            APIMGovernanceDBUtil.initialize();
            log.info("APIM Governance database utilities initialized");

            ComplianceEvaluationScheduler.initialize();
            log.info("Compliance evaluation scheduler initialized");

            String migrationEnabled = System.getProperty(APIMGovernanceConstants.MIGRATE);
            if (migrationEnabled == null) {
                APIMGovernanceConfigDeployer configDeployer = new APIMGovernanceConfigDeployer();
                bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), configDeployer, null);
                log.info("APIM Governance config deployer registered");
            } else {
                log.info("Migration mode enabled, skipping config deployer registration");
            }

            log.info("APIM Governance component activated successfully");
        } catch (Exception e) {
            log.error("Error activating APIM Governance component", e);
            throw e;
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        log.info("Deactivating APIM Governance component");
        try {
            ComplianceEvaluationScheduler.shutdown();
            log.info("Compliance evaluation scheduler shutdown completed");
            
            if (registration != null) {
                registration.unregister();
                log.info("Server lifecycle listeners unregistered");
            }
            log.info("APIM Governance component deactivated successfully");
        } catch (Exception e) {
            log.error("Error during APIM Governance component deactivation", e);
        }
    }

    @Reference(
            name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        log.info("Setting API Manager configuration service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        log.info("Unsetting API Manager configuration service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
    }


    @Reference(
            name = "org.wso2.carbon.apimgt.governance.engine.SpectralValidationEngine",
            service = ValidationEngine.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetValidationEngineService"
    )
    protected void setValidationEngineService(ValidationEngine validationEngine) {
        log.info("Setting validation engine service");
        ValidationEngineService validationEngineService = new ValidationEngineServiceImpl(validationEngine);
        ServiceReferenceHolder.getInstance().setValidationEngineService(validationEngineService);
    }

    protected void unsetValidationEngineService(ValidationEngine validationEngine) {
        log.info("Unsetting validation engine service");
        ServiceReferenceHolder.getInstance().setValidationEngineService(null);
    }

}
