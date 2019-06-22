/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.tracing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.tracing.TracingService;
import org.wso2.carbon.apimgt.tracing.TracingServiceImpl;

/**
 * @scr.component name="org.wso2.carbon.apimgt.tracing.internal.TracingServiceComponent" immediate="true"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */

public class TracingServiceComponent {

    private static final Log log = LogFactory.getLog(TracingServiceComponent.class);
    private ServiceRegistration registration;

    protected void activate(ComponentContext componentContext) {
        try {
            log.debug("Tracing Component activated");
            BundleContext bundleContext = componentContext.getBundleContext();
            registration = bundleContext.registerService(TracingService.class, TracingServiceImpl.getInstance(), null);
        } catch (Exception e) {
            log.error("Error occured in tracing component activation", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        log.debug("Tracing Component deactivated");
        registration.unregister();
    }

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

}
