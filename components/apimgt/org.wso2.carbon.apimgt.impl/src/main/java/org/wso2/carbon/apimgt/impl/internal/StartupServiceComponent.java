/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.listeners.ServerStartupListener;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.core.ServerStartupObserver;

@Component(name = "org.wso2.carbon.apimgt.impl.internal.StartupServiceComponent", immediate = true)
public class StartupServiceComponent {

    private static final Log log = LogFactory.getLog(StartupServiceComponent.class);

    private ServiceRegistration registration;

    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            log.debug("Startup Service Component activated");
            BundleContext bundleContext = componentContext.getBundleContext();
            registration = bundleContext
                    .registerService(ServerStartupObserver.class.getName(), new ServerStartupListener(), null);
        } catch (Exception e) {
            log.error("Error occurred in startup service component activation", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        log.debug("Startup Service Component deactivated");
        registration.unregister();
    }

    @Reference(name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }
}
