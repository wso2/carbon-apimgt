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
package org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.hybrid.gateway.common.OnPremiseGatewayInitListener;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.ThrottlingSynchronizer;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "micro.gateway.throttling.synchronizer.component", 
         immediate = true)
public class ThrottlingSynchronizerComponent {

    private static final Log log = LogFactory.getLog(ThrottlingSynchronizerComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        BundleContext bundleContext = ctx.getBundleContext();
        bundleContext.registerService(OnPremiseGatewayInitListener.class.getName(), new ThrottlingSynchronizer(), null);
        if (log.isDebugEnabled()) {
            log.debug("OnPremise Throttling Synchronizer bundle is activated");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
    }

    @Reference(
             name = "api.manager.config.service", 
             service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {
        log.debug("API manager configuration service bound to Throttling Synchronizer");
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(service);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {
        log.debug("API manager configuration service unbound from Throttling Synchronizer");
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

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

    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }

    @Reference(
             name = "config.context.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configCtx) {
        ServiceReferenceHolder.getInstance().setConfigContextService(configCtx);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtx) {
        ServiceReferenceHolder.getInstance().setConfigContextService(null);
    }
}

