/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.wso2.carbon.apimgt.jms.listener.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.caching.CacheInvalidationService;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerDataService;
import org.wso2.carbon.apimgt.impl.throttling.APIThrottleDataService;
import org.wso2.carbon.apimgt.impl.token.RevokedTokenService;
import org.wso2.carbon.apimgt.jms.listener.utils.JMSListenerStartupShutdownListener;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;

@Component(
        name = "org.wso2.apimgt.jms.listener",
        immediate = true)
public class JMSListenerComponent {

    private static final Log log = LogFactory.getLog(JMSListenerComponent.class);

    private ServiceRegistration registration;

    @Activate
    protected void activate(ComponentContext context) {

        log.debug("Activating component...");
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        if (configuration != null) {
            if (!configuration.getThrottleProperties().getJmsConnectionProperties().isEnabled()) {
                return;
            }
        } else {
            log.warn("API Manager Configuration not properly set.");
        }
        JMSListenerStartupShutdownListener jmsListenerStartupShutdownListener =
                new JMSListenerStartupShutdownListener();
        registration = context.getBundleContext()
                .registerService(ServerStartupObserver.class, jmsListenerStartupShutdownListener, null);
        registration = context.getBundleContext()
                .registerService(ServerShutdownHandler.class, jmsListenerStartupShutdownListener, null);
    }

    @Reference(
            name = "throttle.data.service",
            service = APIThrottleDataService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIThrottleDataService")
    protected void setAPIThrottleDataService(APIThrottleDataService throttleDataService) {

        log.debug("Setting APIThrottleDataService");
        ServiceReferenceHolder.getInstance().setAPIThrottleDataService(throttleDataService);
    }

    protected void unsetAPIThrottleDataService(APIThrottleDataService throttleDataService) {

        log.debug("Un-setting APIThrottleDataService");
        ServiceReferenceHolder.getInstance().setAPIThrottleDataService(null);
    }

    @Reference(
            name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {

        log.debug("Setting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {

        log.debug("Setting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
    }

    @Reference(
            name = "revoke.token.service",
            service = RevokedTokenService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRevokedTokenService")
    protected void setRevokedTokenService(RevokedTokenService revokedTokenService) {

        log.debug("Setting Revoked Token Service");
        ServiceReferenceHolder.getInstance().setRevokedTokenService(revokedTokenService);
    }

    protected void unsetRevokedTokenService(RevokedTokenService revokedTokenService) {

        log.debug("unSetting Revoked Token Service");
        ServiceReferenceHolder.getInstance().setRevokedTokenService(null);
    }

    @Reference(
            name = "key.manager.service",
            service = KeyManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetKeyManagerService")
    protected void setKeyManagerService(KeyManagerConfigurationService keyManagerService) {

        log.debug("Setting KeyManager Configuration Service");
        ServiceReferenceHolder.getInstance().setKeyManagerService(keyManagerService);
    }

    protected void unsetKeyManagerService(KeyManagerConfigurationService keyManagerService) {

        log.debug("unSetting KeyManager Configuration Service");
        ServiceReferenceHolder.getInstance().setKeyManagerService(null);
    }

    @Reference(
            name = "api.manager.cache.invalidation.service",
            service = CacheInvalidationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCacheInvalidationService")

    protected void setCacheInvalidationService(CacheInvalidationService cacheInvalidationService) {

        log.debug("Setting Cache Invalidation Service");
        ServiceReferenceHolder.getInstance().setCacheInvalidationService(cacheInvalidationService);

    }

    protected void unsetCacheInvalidationService(CacheInvalidationService cacheInvalidationService) {

        log.debug("Setting Cache Invalidation Service");
        ServiceReferenceHolder.getInstance().setCacheInvalidationService(null);

    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Deactivating component");
        }
        if (this.registration != null) {
            this.registration.unregister();
        }
    }
    
    @Reference(
            name = "keymanager.data.service",
            service = KeyManagerDataService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetKeyManagerDataService")
    protected void setKeyManagerDataService(KeyManagerDataService keymanagerDataService) {

        log.debug("Setting KeyManagerDataService");
        ServiceReferenceHolder.getInstance().setKeyManagerDataService(keymanagerDataService);
    }

    protected void unsetKeyManagerDataService(KeyManagerDataService keymanagerDataService) {

        log.debug("Un-setting KeyManagerDataService");
        ServiceReferenceHolder.getInstance().setKeyManagerDataService(null);
    }

}

