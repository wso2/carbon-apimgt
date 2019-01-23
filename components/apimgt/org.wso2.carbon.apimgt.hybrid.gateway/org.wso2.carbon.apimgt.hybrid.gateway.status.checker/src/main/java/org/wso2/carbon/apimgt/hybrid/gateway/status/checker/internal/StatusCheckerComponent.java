/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.apimgt.hybrid.gateway.status.checker.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.core.ServerStartupHandler;

/**
 * @scr.component name="org.wso2.carbon.apimgt.micro.gateway.status.checker" immediate="true"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */
public class StatusCheckerComponent {

    private static final Log log = LogFactory.getLog(StatusCheckerComponent.class);

    /**
     * Method to activate bundle.
     *
     * @param context OSGi component context.
     */
    protected void activate(ComponentContext context) {
        try {
            //Register the server start up handler which hold the execution of its invoke method until the server starts
            context.getBundleContext()
                   .registerService(ServerStartupHandler.class.getName(), new StatusCheckerServerStartListener(), null);
            if (log.isDebugEnabled()) {
                log.debug("StatusCheckerComponent bundle activated successfully.");
            }
        } catch (Throwable e) {
            log.error("Error while creating StatusCheckerComponent bundle.", e);
        }
    }

    /**
     * Method to deactivate bundle.
     *
     * @param context OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("StatusCheckerComponent bundle is deactivated.");
        }
    }

    /**
     * Set APIManager Configuration service to the bundle's {@link ServiceReferenceHolder}
     *
     * @param service APIManager Configuration service
     */
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {
        log.debug("API manager configuration service bound to the On-Prem Gw Status Checker");
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(service);
    }

    /**
     * Unset APIManager Configuration service in the bundle's {@link ServiceReferenceHolder}
     *
     * @param service APIManager Configuration service
     */
    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {
        log.debug("API manager configuration service unbound from the On-Prem Gw Status Checker");
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

}
