/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.registry.synchronizer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.apimgt.hybrid.gateway.common.OnPremiseGatewayInitListener;
import org.wso2.carbon.apimgt.hybrid.gateway.registry.synchronizer.RegistrySynchronizer;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name=
 * "RegistrySynchronizationServiceComponent"
 * immediate="true"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */
public class RegistrySynchronizationServiceComponent {
    private static final Log log = LogFactory.getLog(RegistrySynchronizationServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        BundleContext bundleContext = ctx.getBundleContext();
        bundleContext.registerService(OnPremiseGatewayInitListener.class.getName(), new RegistrySynchronizer(), null);
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating registry synchronization feature bundle.");
        }
    }

    /**
     * Method to set realm service
     *
     * @param realmService realm service
     */
    protected void setRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Method to remove realm service
     *
     * @param realmService realm service
     */
    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }

    /**
     * Method to set registry service.
     *
     * @param registryService service to get tenant data.
     */
    protected void setRegistryService(RegistryService registryService) {
        ServiceReferenceHolder.getInstance().setRegistryService(registryService);
    }

    /**
     * Method to unset registry service.
     *
     * @param registryService service to get registry data.
     */
    protected void unsetRegistryService(RegistryService registryService) {
        ServiceReferenceHolder.getInstance().setRegistryService(null);
    }

    /**
     * Method to unset ConfigurationContextService
     *
     * @param ccService configuration context service
     */
    protected void unsetConfigurationContextService(ConfigurationContextService ccService) {
        ServiceReferenceHolder.getInstance().setConfigContextService(null);
    }

    /**
     * Method to set ConfigurationContextService
     *
     * @param ccService configuration context service
     */
    protected void setConfigurationContextService(ConfigurationContextService ccService) {
        ServiceReferenceHolder.getInstance().setConfigContextService(ccService);
    }

    /**
     * Method to set API Manager Configuration Service
     *
     * @param service API Manager Configuration Service
     */
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {
        log.debug("API manager configuration service bound to Tenant Initializer.");
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(service);
    }

    /**
     * Method to unset API Manager Configuration Service
     *
     * @param service API Manager Configuration Service
     */
    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {
        log.debug("API manager configuration service unbound from Tenant Initializer.");
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

}

