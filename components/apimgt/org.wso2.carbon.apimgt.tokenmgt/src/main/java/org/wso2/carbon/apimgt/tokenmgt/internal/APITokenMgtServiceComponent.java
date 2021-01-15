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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.tokenmgt.ScopesIssuer;
import org.wso2.carbon.apimgt.tokenmgt.handlers.SessionDataPublisherImpl;
import org.wso2.carbon.apimgt.tokenmgt.issuers.AbstractScopesIssuer;
import org.wso2.carbon.apimgt.tokenmgt.issuers.PermissionBasedScopeIssuer;
import org.wso2.carbon.apimgt.tokenmgt.issuers.RoleBasedScopesIssuer;
import org.wso2.carbon.apimgt.tokenmgt.listeners.KeyManagerUserOperationListener;
import org.wso2.carbon.apimgt.tokenmgt.util.TokenMgtDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

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

            // loading white listed scopes
            List<String> whitelist = null;
            APIManagerConfigurationService configurationService =
                    org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance()
                            .getAPIManagerConfigurationService();
            boolean accessTokenBindingEnable;

            String firstProperty = configurationService.getAPIManagerConfiguration()
                    .getFirstProperty(APIConstants.AccessTokenBinding.ACCESS_TOKEN_BINDING_ENABLED);
            if (firstProperty != null) {
                accessTokenBindingEnable = Boolean.parseBoolean(firstProperty);
            } else {
                accessTokenBindingEnable = false;
            }
            if (!accessTokenBindingEnable) {
                // registering logout token revoke listener
                try {
                    SessionDataPublisherImpl dataPublisher = new SessionDataPublisherImpl();
                    ctxt.getBundleContext()
                            .registerService(AuthenticationDataPublisher.class.getName(), dataPublisher, null);
                    log.debug("SessionDataPublisherImpl bundle is activated");
                } catch (Throwable e) {
                    log.error("SessionDataPublisherImpl bundle activation Failed", e);
                }
            }
            if (configurationService != null) {
                // Read scope whitelist from Configuration.
                whitelist =
                        configurationService.getAPIManagerConfiguration().getProperty(APIConstants.ALLOWED_SCOPES);
                // If whitelist is null, default scopes will be put.
                if (whitelist == null) {
                    whitelist = new ArrayList<String>();
                    whitelist.add(APIConstants.OPEN_ID_SCOPE_NAME);
                    whitelist.add(APIConstants.DEVICE_SCOPE_PATTERN);
                }
            } else {
                log.debug("API Manager Configuration couldn't be read successfully. Scopes might not work correctly.");
            }
            PermissionBasedScopeIssuer permissionBasedScopeIssuer = new PermissionBasedScopeIssuer();
            RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuer();
            TokenMgtDataHolder.addScopesIssuer(permissionBasedScopeIssuer.getPrefix(), permissionBasedScopeIssuer);
            TokenMgtDataHolder.addScopesIssuer(roleBasedScopesIssuer.getPrefix(), roleBasedScopesIssuer);
            if (log.isDebugEnabled()) {
                log.debug("Permission based scope Issuer and Role based scope issuers are loaded.");
            }
            ScopesIssuer.loadInstance(whitelist);
            if (log.isDebugEnabled()) {
                log.debug("Identity API Key Mgt Bundle is started.");
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

    /**
     * Add scope issuer to the map.
     * @param scopesIssuer scope issuer.
     */
    @Reference(
            name = "scope.issuer.service",
            service = org.wso2.carbon.apimgt.tokenmgt.issuers.AbstractScopesIssuer.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeScopeIssuers")
    protected void addScopeIssuer(AbstractScopesIssuer scopesIssuer) {

        TokenMgtDataHolder.addScopesIssuer(scopesIssuer.getPrefix(), scopesIssuer);
    }

    /**
     * unset scope issuer.
     * @param scopesIssuer
     */
    protected void removeScopeIssuers(AbstractScopesIssuer scopesIssuer) {

        TokenMgtDataHolder.setScopesIssuers(null);
    }

}

