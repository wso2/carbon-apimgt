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

package org.wso2.carbon.apimgt.notification.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.handlers.EventHandler;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerEventHandler;
import org.wso2.carbon.apimgt.notification.DefaultKeyManagerEventHandlerImpl;
import org.wso2.carbon.apimgt.notification.NotificationEventService;
import org.wso2.carbon.apimgt.notification.TenantManagementEventHandler;
import org.wso2.carbon.apimgt.notification.WebhooksDeliveryEventHandler;
import org.wso2.carbon.apimgt.notification.WebhooksSubscriptionEventHandler;
import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * This class used to activate Api manager notification bundle.
 */
@Component(name = "apim.notification.component", immediate = true)
public class ApimgtNotificationServiceComponent {

    private static final Log log = LogFactory.getLog(ApimgtNotificationServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        log.info("Activating API Manager notification service component");
        ctxt.getBundleContext().registerService(KeyManagerEventHandler.class, new DefaultKeyManagerEventHandlerImpl(),
                null);
        ctxt.getBundleContext().registerService(EventHandler.class, new WebhooksSubscriptionEventHandler(),
                null);
        ctxt.getBundleContext().registerService(EventHandler.class, new WebhooksDeliveryEventHandler(),
                null);
        ctxt.getBundleContext().registerService(EventHandler.class, new TenantManagementEventHandler(),
                null);
        ctxt.getBundleContext().registerService(NotificationEventService.class, new NotificationEventService(), null);
        log.info("API Manager notification service component activated successfully");
    }

    @Reference(
            name = "keymgt.event.handlers",
            service = KeyManagerEventHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeKeyManagerEventHandlers")
    protected void addKeyManagerEventHandlers(KeyManagerEventHandler keyManagerEventHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Adding key manager event handler: " + keyManagerEventHandler.getType());
        }
        ServiceReferenceHolder.getInstance().addEventHandler(keyManagerEventHandler.getType(),
                keyManagerEventHandler);
    }

    protected void removeKeyManagerEventHandlers(KeyManagerEventHandler keyManagerEventHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Removing key manager event handler: " + keyManagerEventHandler.getType());
        }
        ServiceReferenceHolder.getInstance().removeEventHandlers(keyManagerEventHandler.getType());
    }

    @Reference(
            name = "event.handlers",
            service = EventHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeEventHandlers")
    protected void addEventHandlers(EventHandler eventHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Adding event handler: " + eventHandler.getType());
        }
        ServiceReferenceHolder.getInstance().addEventHandler(eventHandler.getType(),
                eventHandler);
    }

    protected void removeEventHandlers(EventHandler eventHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Removing event handler: " + eventHandler.getType());
        }
        ServiceReferenceHolder.getInstance().removeEventHandlers(eventHandler.getType());
    }

    @Reference(
            name = "apimgt.event.stream.service.reference",
            service = org.wso2.carbon.event.stream.core.EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventStreamService) {

        ServiceReferenceHolder.getInstance().setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {

        ServiceReferenceHolder.getInstance().setEventStreamService(null);
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
