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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "onpremise.api.gateway.common.component", 
         immediate = true)
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
    @Reference(
             name = "api.manager.config.service", 
             service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAPIManagerConfigurationService")
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
    @Reference(
             name = "config.context.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
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
    @Reference(
             name = "onPremiseInitObserver", 
             service = OnPremiseGatewayInitListener.class, 
             cardinality = ReferenceCardinality.MULTIPLE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "removeGatewayInitListener")
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
    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
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

