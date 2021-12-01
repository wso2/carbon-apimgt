/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.cache.invalidation.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.cache.invalidation.APIMgtCacheInvalidationRequestSender;
import org.wso2.carbon.apimgt.cache.invalidation.APIMgtServerStartupListener;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.CacheInvalidationConfiguration;
import org.wso2.carbon.apimgt.impl.jms.listener.JMSListenerShutDownService;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;

import javax.cache.CacheInvalidationRequestSender;
import javax.cache.event.CacheEntryListener;


/**
 * This class used to initialize Global Cache Invalidation service.
 */
@Component(
        name = "org.wso2.apimgt.cache.invalidation.listener",
        immediate = true)
public class CacheInvalidationServiceComponent {

    ServiceRegistration cacheInvalidationRequestSenderServiceRegistration;

    @Activate
    protected void activate(ComponentContext context) {

        CacheInvalidationConfiguration cacheInvalidationConfiguration;
        BundleContext bundleContext = context.getBundleContext();
        if (DataHolder.getInstance().getAPIManagerConfigurationService() != null) {
            cacheInvalidationConfiguration =
                    DataHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                            .getCacheInvalidationConfiguration();
            if (cacheInvalidationConfiguration.isEnabled()) {
                APIMgtCacheInvalidationRequestSender apiMgtCacheInvalidationRequestSender =
                        new APIMgtCacheInvalidationRequestSender(cacheInvalidationConfiguration);
                cacheInvalidationRequestSenderServiceRegistration = bundleContext
                        .registerService(CacheInvalidationRequestSender.class, apiMgtCacheInvalidationRequestSender,
                                null);
                cacheInvalidationRequestSenderServiceRegistration = bundleContext
                        .registerService(CacheEntryListener.class, apiMgtCacheInvalidationRequestSender, null);
                APIMgtServerStartupListener apimgtCacheInvalidationServerStartupListener =
                        new APIMgtServerStartupListener();
                cacheInvalidationRequestSenderServiceRegistration = bundleContext
                        .registerService(ServerStartupObserver.class, apimgtCacheInvalidationServerStartupListener,
                                null);
                cacheInvalidationRequestSenderServiceRegistration = bundleContext
                        .registerService(ServerShutdownHandler.class, apimgtCacheInvalidationServerStartupListener,
                                null);
                cacheInvalidationRequestSenderServiceRegistration = bundleContext
                        .registerService(JMSListenerShutDownService.class,
                                apimgtCacheInvalidationServerStartupListener, null);
            }
        }
    }

    @Reference(name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {

        DataHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {

        DataHolder.getInstance().setAPIManagerConfigurationService(null);
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        if (cacheInvalidationRequestSenderServiceRegistration != null) {
            cacheInvalidationRequestSenderServiceRegistration.unregister();
        }
    }

}
