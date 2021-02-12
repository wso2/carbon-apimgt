/*
 *Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.apimgt.tokenmgt.internal;

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
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.tokenmgt.handlers.SessionDataPublisherImpl;
import org.wso2.carbon.apimgt.tokenmgt.listeners.KeyManagerUserOperationListener;
import org.wso2.carbon.apimgt.tokenmgt.util.TokenMgtDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "api.tokenmgt.component",
        immediate = true)
public class APITokenMgtServiceComponent {

    private static Log log = LogFactory.getLog(APITokenMgtServiceComponent.class);

    private static KeyManagerUserOperationListener listener = null;

    private ServiceRegistration serviceRegistration = null;

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            TokenMgtDataHolder.initData();
            listener = new KeyManagerUserOperationListener();
            serviceRegistration =
                    ctxt.getBundleContext().registerService(UserOperationEventListener.class.getName(), listener, null);
            log.debug("Key Manager User Operation Listener is enabled.");
            // registering logout token revoke listener
            try {
                SessionDataPublisherImpl dataPublisher = new SessionDataPublisherImpl();
                ctxt.getBundleContext()
                        .registerService(AuthenticationDataPublisher.class.getName(), dataPublisher, null);
                log.debug("SessionDataPublisherImpl bundle is activated");
            } catch (Throwable e) {
                log.error("SessionDataPublisherImpl bundle activation Failed", e);
            }
        } catch (Exception e) {
            log.error("Failed to initialize key management service.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        if (log.isDebugEnabled()) {
            log.info("Key Manager User Operation Listener is deactivated.");
        }
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        TokenMgtDataHolder.setRegistryService(registryService);
        if (log.isDebugEnabled()) {
            log.debug("Registry Service is set in the API KeyMgt bundle.");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {

        TokenMgtDataHolder.setRegistryService(null);
        if (log.isDebugEnabled()) {
            log.debug("Registry Service is unset in the API KeyMgt bundle.");
        }
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        TokenMgtDataHolder.setRealmService(realmService);
        if (log.isDebugEnabled()) {
            log.debug("Realm Service is set in the API KeyMgt bundle.");
        }
    }

    protected void unsetRealmService(RealmService realmService) {

        TokenMgtDataHolder.setRealmService(null);
        if (log.isDebugEnabled()) {
            log.debug("Realm Service is unset in the API KeyMgt bundle.");
        }
    }

    @Reference(
            name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {

        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to the API handlers");
        }
        TokenMgtDataHolder.setAmConfigService(amcService);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {

        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API handlers");
        }
        TokenMgtDataHolder.setAmConfigService(null);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }
}

