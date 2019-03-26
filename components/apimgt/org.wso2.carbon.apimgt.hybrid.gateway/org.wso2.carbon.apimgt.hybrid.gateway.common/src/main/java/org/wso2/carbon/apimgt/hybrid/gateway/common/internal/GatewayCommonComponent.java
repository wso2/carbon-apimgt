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
package org.wso2.carbon.apimgt.hybrid.gateway.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.hybrid.gateway.common.OnPremiseGatewayInitListener;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="onpremise.api.gateway.common.component" immediate="true"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 * @scr.reference name="user.realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="onPremiseInitObserver"
 * interface="OnPremiseGatewayInitListener"
 * cardinality="0..n" policy="dynamic"  bind="addGatewayInitListener" unbind="removeGatewayInitListener"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService
 */
public class GatewayCommonComponent {

    private static final Log log = LogFactory.getLog(GatewayCommonComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("OnPremise Gateway Common bundle is activated");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
    }

    /**
     * Method to set APIManagerConfigurationService
     *
     * @param service API Manager Configuration Service
     */
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to Gateway Common component");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(service);
    }

    /**
     * Method to unset APIManagerConfigurationService
     *
     * @param service API Manager Configuration Service
     */
    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from Gateway Common component");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

    /**
     * Method to set Realm service
     *
     * @param  realmService Realm Service
     */
    protected void setRealmService(RealmService realmService) {
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("Realm service initialized");
        }
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Method to unset Realm service
     *
     * @param  realmService Realm Service
     */
    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }

    /**
     * Method to set ConfigurationContextService
     *
     * @param configCtx configuration context service
     */
    protected void setConfigurationContextService(ConfigurationContextService configCtx) {
        ServiceReferenceHolder.getInstance().setConfigContextService(configCtx);
    }

    /**
     * Method to unset ConfigurationContextService
     *
     * @param configCtx configuration context service
     */
    protected void unsetConfigurationContextService(ConfigurationContextService configCtx) {
        ServiceReferenceHolder.getInstance().setConfigContextService(null);
    }

    /**
     * Method to add a gateway initialization listener
     *
     * @param listener micro gateway initialization listener
     */
    protected void addGatewayInitListener(OnPremiseGatewayInitListener listener) {
        synchronized (GatewayCommonComponent.class) {
            ServiceReferenceHolder.getInstance().getListeners().add(listener);
            if (log.isDebugEnabled()) {
                log.debug("Added OnPremiseGatewayInitListener : " + listener.getClass().getName());
            }
        }
    }

    /**
     * Method to remove a gateway initialization listener
     *
     * @param listener micro gateway initialization listener
     */
    protected void removeGatewayInitListener(OnPremiseGatewayInitListener listener) {
        synchronized (GatewayCommonComponent.class) {
            ServiceReferenceHolder.getInstance().getListeners().remove(listener);
            if (log.isDebugEnabled()) {
                log.debug("Removed OnPremiseGatewayInitListener : " + listener.getClass().getName());
            }
        }
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
}
