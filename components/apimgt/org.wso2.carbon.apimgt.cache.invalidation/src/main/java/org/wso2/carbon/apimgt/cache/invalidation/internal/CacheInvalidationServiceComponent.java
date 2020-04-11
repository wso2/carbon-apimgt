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
import org.wso2.carbon.apimgt.cache.invalidation.APIMgtCacheInvalidationRequestSender;
import org.wso2.carbon.apimgt.cache.invalidation.APImgtServerStartupListener;
import org.wso2.carbon.apimgt.cache.invalidation.ApimgtServerShutDownListener;
import org.wso2.carbon.apimgt.cache.invalidation.CachingConstants;
import org.wso2.carbon.apimgt.cache.invalidation.utils.JMSTransportHandler;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.CacheInvalidationConfiguration;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import java.util.HashMap;
import java.util.Map;

import javax.cache.CacheInvalidationRequestSender;
import javax.cache.event.CacheEntryListener;

@Component(
        name = "org.wso2.apimgt.cache.invalidation.listener",
        immediate = true)

public class CacheInvalidationServiceComponent {

    private static final Log log = LogFactory.getLog(CacheInvalidationServiceComponent.class);

    ServiceRegistration cacheInvalidationRequestSenderServiceRegistration;

    @Activate
    protected void activate(ComponentContext context) {

        CacheInvalidationConfiguration cacheInvalidationConfiguration;
        BundleContext bundleContext = context.getBundleContext();
        if (DataHolder.getInstance().getAPIManagerConfigurationService() != null) {
            cacheInvalidationConfiguration =
                    DataHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                            .getCacheInvalidationConfiguration();
            configureEventPublisherProperties(cacheInvalidationConfiguration);
            APIMgtCacheInvalidationRequestSender apiMgtCacheInvalidationRequestSender =
                    new APIMgtCacheInvalidationRequestSender(cacheInvalidationConfiguration);
            cacheInvalidationRequestSenderServiceRegistration = bundleContext
                    .registerService(CacheInvalidationRequestSender.class, apiMgtCacheInvalidationRequestSender, null);
            cacheInvalidationRequestSenderServiceRegistration =
                    bundleContext.registerService(CacheEntryListener.class, apiMgtCacheInvalidationRequestSender, null);
            cacheInvalidationRequestSenderServiceRegistration =
                    bundleContext.registerService(ServerStartupObserver.class, new APImgtServerStartupListener(), null);
            cacheInvalidationRequestSenderServiceRegistration =
                    bundleContext.registerService(ServerShutdownHandler.class,
                            new ApimgtServerShutDownListener(), null);
            if (cacheInvalidationConfiguration.isEnabled()) {
                JMSTransportHandler jmsTransportHandler = new JMSTransportHandler(cacheInvalidationConfiguration);
                DataHolder.getInstance().setJmsTransportHandler(jmsTransportHandler);
            }
        }
    }

    /**
     * Initialize the Output EventAdapter Service dependency
     *
     * @param outputEventAdapterService Output EventAdapter Service reference
     */
    @Reference(
            name = "cluster.output.adapter.service",
            service = org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOutputEventAdapterService")
    protected void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {

        DataHolder.getInstance().setOutputEventAdapterService(outputEventAdapterService);
    }

    /**
     * De-reference the Output EventAdapter Service dependency.
     *
     * @param outputEventAdapterService
     */
    protected void unsetOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {

        DataHolder.getInstance().setOutputEventAdapterService(null);
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

    private void configureEventPublisherProperties(CacheInvalidationConfiguration cacheInvalidationConfiguration) {

        OutputEventAdapterConfiguration adapterConfiguration = new OutputEventAdapterConfiguration();
        adapterConfiguration.setName(CachingConstants.CACHING_EVENT_PUBLISHER);
        adapterConfiguration.setType(CachingConstants.CACHING_EVENT_TYPE);
        adapterConfiguration.setMessageFormat(CachingConstants.CACHING_EVENT_FORMAT);
        Map<String, String> adapterParameters = new HashMap();

        if (cacheInvalidationConfiguration.isEnabled()) {
            adapterParameters.put(APIConstants.RECEIVER_URL, cacheInvalidationConfiguration.getReceiverUrlGroup());
            adapterParameters.put(APIConstants.AUTHENTICATOR_URL, cacheInvalidationConfiguration.getAuthUrlGroup());
            adapterParameters.put(APIConstants.USERNAME, cacheInvalidationConfiguration.getUsername());
            adapterParameters.put(APIConstants.PASSWORD, cacheInvalidationConfiguration.getPassword());
            adapterParameters.put(APIConstants.PROTOCOL, CachingConstants.BINARY);
            adapterParameters.put(APIConstants.PUBLISHING_MODE, APIConstants.NON_BLOCKING);
            adapterParameters.put(APIConstants.PUBLISHING_TIME_OUT, "0");
            adapterConfiguration.setStaticProperties(adapterParameters);
            try {
                DataHolder.getInstance().getOutputEventAdapterService().create(adapterConfiguration);
            } catch (OutputEventAdapterException e) {
                log.warn("Exception occurred while creating WSO2 Event Adapter. Cache Invalidation not work " +
                        "properly", e);
            }
        } else {
            log.info("Global Cache invalidation not enabled");
        }
    }
}