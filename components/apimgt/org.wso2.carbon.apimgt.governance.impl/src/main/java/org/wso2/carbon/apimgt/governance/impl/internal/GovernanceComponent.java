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
import org.wso2.carbon.apimgt.governance.impl.GovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.config.GovernanceConfiguration;
import org.wso2.carbon.apimgt.governance.impl.config.GovernanceConfigurationService;
import org.wso2.carbon.apimgt.governance.impl.config.GovernanceConfigurationServiceImpl;
import org.wso2.carbon.apimgt.governance.impl.ComplianceEvaluationScheduler;
import org.wso2.carbon.apimgt.governance.impl.observer.GovernanceConfigDeployer;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceDBUtil;
import org.wso2.carbon.apimgt.governance.impl.validator.ValidationEngineService;
import org.wso2.carbon.apimgt.governance.impl.validator.ValidationEngineServiceImpl;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

@Component(
        name = "org.wso2.apimgt.governance.impl.services",
        immediate = true)
public class GovernanceComponent {

    private static final Log log = LogFactory.getLog(GovernanceComponent.class);
    ServiceRegistration registration;

    private GovernanceConfiguration configuration = new GovernanceConfiguration();

    @Activate
    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Governance component activated");
        }

        BundleContext bundleContext = componentContext.getBundleContext();

        String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
        configuration.load(filePath);

        GovernanceConfigurationServiceImpl configurationService =
                new GovernanceConfigurationServiceImpl(configuration);
        ServiceReferenceHolder.getInstance().setGovernanceConfigurationService(configurationService);
        GovernanceDBUtil.initialize();
        ComplianceEvaluationScheduler.initialize();

        String migrationEnabled = System.getProperty(GovernanceConstants.MIGRATE);
        if (migrationEnabled == null) {
            GovernanceConfigDeployer configDeployer = new GovernanceConfigDeployer();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), configDeployer, null);
        }
        registration = componentContext.getBundleContext()
                .registerService(GovernanceConfigurationService.class.getName(),
                        configurationService, null);
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        ComplianceEvaluationScheduler.shutdown();
        if (log.isDebugEnabled()) {
            log.debug("Deactivating Governance component");
        }

        registration.unregister();
    }

    @Reference(
            name = "org.wso2.carbon.apimgt.governance.engine.SpectralValidationEngine",
            service = ValidationEngine.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetValidationEngineService"
    )
    protected void setValidationEngineService(ValidationEngine validationEngine) {
        ValidationEngineService validationEngineService = new ValidationEngineServiceImpl(validationEngine);
        ServiceReferenceHolder.getInstance().setValidationEngineService(validationEngineService);
    }

    protected void unsetValidationEngineService(ValidationEngine validationEngine) {
        ServiceReferenceHolder.getInstance().setValidationEngineService(null);
    }

}
