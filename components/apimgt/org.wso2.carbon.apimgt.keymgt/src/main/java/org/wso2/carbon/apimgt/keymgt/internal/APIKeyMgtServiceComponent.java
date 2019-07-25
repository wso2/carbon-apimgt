/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.keymgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.keymgt.ScopesIssuer;
import org.wso2.carbon.apimgt.keymgt.events.APIMOAuthEventInterceptor;
import org.wso2.carbon.apimgt.keymgt.handlers.UserPostSelfRegistrationHandler;
import org.wso2.carbon.apimgt.keymgt.issuers.AbstractScopesIssuer;
import org.wso2.carbon.apimgt.keymgt.issuers.PermissionBasedScopeIssuer;
import org.wso2.carbon.apimgt.keymgt.issuers.RoleBasedScopesIssuer;
import org.wso2.carbon.apimgt.keymgt.listeners.KeyManagerUserOperationListener;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.oauth.event.OAuthEventInterceptor;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "api.keymgt.component", 
         immediate = true)
public class APIKeyMgtServiceComponent {

    private static Log log = LogFactory.getLog(APIKeyMgtServiceComponent.class);

    private static KeyManagerUserOperationListener listener = null;

    private ServiceRegistration serviceRegistration = null;

    private boolean tokenRevocationEnabled;

    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            APIKeyMgtDataHolder.initData();
            listener = new KeyManagerUserOperationListener();
            serviceRegistration = ctxt.getBundleContext().registerService(UserOperationEventListener.class.getName(), listener, null);
            log.debug("Key Manager User Operation Listener is enabled.");
            // Checking token revocation feature enabled config
            tokenRevocationEnabled = APIManagerConfiguration.isTokenRevocationEnabled();
            if (tokenRevocationEnabled) {
                // object creation for implemented OAuthEventInterceptor interface in IS
                APIMOAuthEventInterceptor interceptor = new APIMOAuthEventInterceptor();
                // registering the interceptor class to the bundle
                serviceRegistration = ctxt.getBundleContext().registerService(OAuthEventInterceptor.class.getName(), interceptor, null);
                // Creating an event adapter to receive token revocation messages
                configureEventPublisherProperties();
                log.debug("Key Manager OAuth Event Interceptor is enabled.");
            } else {
                log.debug("Token Revocation Notifier Feature is disabled.");
            }

            // Activating UserPostSelfRegistration handler component
            try {
                serviceRegistration = ctxt.getBundleContext()
                        .registerService(AbstractEventHandler.class.getName(), new UserPostSelfRegistrationHandler(),
                                null);
            } catch (Exception e) {
                log.error("Error while activating custom User selfRegistration handler component.", e);
            }

            // loading white listed scopes
            List<String> whitelist = null;
            APIManagerConfigurationService configurationService = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
            if (configurationService != null) {
                // Read scope whitelist from Configuration.
                whitelist = configurationService.getAPIManagerConfiguration().getProperty(APIConstants.WHITELISTED_SCOPES);
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
            APIKeyMgtDataHolder.addScopesIssuer(permissionBasedScopeIssuer.getPrefix(), permissionBasedScopeIssuer);
            APIKeyMgtDataHolder.addScopesIssuer(roleBasedScopesIssuer.getPrefix(), roleBasedScopesIssuer);
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
        APIKeyMgtDataHolder.setRegistryService(registryService);
        if (log.isDebugEnabled()) {
            log.debug("Registry Service is set in the API KeyMgt bundle.");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        APIKeyMgtDataHolder.setRegistryService(null);
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
        APIKeyMgtDataHolder.setRealmService(realmService);
        if (log.isDebugEnabled()) {
            log.debug("Realm Service is set in the API KeyMgt bundle.");
        }
    }

    protected void unsetRealmService(RealmService realmService) {
        APIKeyMgtDataHolder.setRealmService(null);
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
        APIKeyMgtDataHolder.setAmConfigService(amcService);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API handlers");
        }
        APIKeyMgtDataHolder.setAmConfigService(null);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

    /**
     * Add scope issuer to the map.
     * @param scopesIssuer scope issuer.
     */
    @Reference(
             name = "scope.issuer.service", 
             service = org.wso2.carbon.apimgt.keymgt.issuers.AbstractScopesIssuer.class, 
             cardinality = ReferenceCardinality.MULTIPLE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "removeScopeIssuers")
    protected void addScopeIssuer(AbstractScopesIssuer scopesIssuer) {
        APIKeyMgtDataHolder.addScopesIssuer(scopesIssuer.getPrefix(), scopesIssuer);
    }

    /**
     * unset scope issuer.
     * @param scopesIssuer
     */
    protected void removeScopeIssuers(AbstractScopesIssuer scopesIssuer) {
        APIKeyMgtDataHolder.setScopesIssuers(null);
    }

    /**
     * Get INetAddress by host name or  IP Address
     *
     * @param host name or host IP String
     * @return InetAddress
     * @throws java.net.UnknownHostException
     */
    private InetAddress getHostAddress(String host) throws UnknownHostException {
        String[] splittedString = host.split("\\.");
        boolean value = checkIfIP(splittedString);
        if (!value) {
            return InetAddress.getByName(host);
        }
        byte[] byteAddress = new byte[4];
        for (int i = 0; i < splittedString.length; i++) {
            if (Integer.parseInt(splittedString[i]) > 127) {
                byteAddress[i] = Integer.valueOf(Integer.parseInt(splittedString[i]) - 256).byteValue();
            } else {
                byteAddress[i] = Byte.parseByte(splittedString[i]);
            }
        }
        return InetAddress.getByAddress(byteAddress);
    }

    /**
     * Check the hostname is IP or String
     *
     * @param ip IP
     * @return true/false
     */
    private boolean checkIfIP(String[] ip) {
        for (int i = 0; i < ip.length; i++) {
            try {
                Integer.parseInt(ip[i]);
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method to configure wso2event type event adapter to be used for token revocation.
     */
    private void configureEventPublisherProperties() {
        OutputEventAdapterConfiguration adapterConfiguration = new OutputEventAdapterConfiguration();
        adapterConfiguration.setName(APIConstants.TOKEN_REVOCATION_EVENT_PUBLISHER);
        adapterConfiguration.setType(APIConstants.BLOCKING_EVENT_TYPE);
        adapterConfiguration.setMessageFormat(APIConstants.BLOCKING_EVENT_TYPE);
        Map<String, String> adapterParameters = new HashMap<>();
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        ThrottleProperties.TrafficManager trafficManager = configuration.getThrottleProperties().getTrafficManager();
        adapterParameters.put(APIConstants.RECEIVER_URL, trafficManager.getReceiverUrlGroup());
        adapterParameters.put(APIConstants.AUTHENTICATOR_URL, trafficManager.getAuthUrlGroup());
        adapterParameters.put(APIConstants.USERNAME, trafficManager.getUsername());
        adapterParameters.put(APIConstants.PASSWORD, trafficManager.getPassword());
        adapterParameters.put(APIConstants.PROTOCOL, trafficManager.getType());
        adapterParameters.put(APIConstants.PUBLISHING_MODE, APIConstants.NON_BLOCKING);
        adapterParameters.put(APIConstants.PUBLISHING_TIME_OUT, "0");
        adapterConfiguration.setStaticProperties(adapterParameters);
        try {
            ServiceReferenceHolder.getInstance().getOutputEventAdapterService().create(adapterConfiguration);
        } catch (OutputEventAdapterException e) {
            log.warn("Exception occurred while creating token revocation event adapter. Token Revocation may not " + "work properly", e);
        }
    }

    /**
     * Initialize the Output EventAdapter Service dependency
     *
     * @param outputEventAdapterService Output EventAdapter Service reference
     */
    @Reference(
             name = "event.output.adapter.service", 
             service = org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetOutputEventAdapterService")
    protected void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        ServiceReferenceHolder.getInstance().setOutputEventAdapterService(outputEventAdapterService);
    }

    /**
     * De-reference the Output EventAdapter Service dependency.
     *
     * @param outputEventAdapterService
     */
    protected void unsetOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        ServiceReferenceHolder.getInstance().setOutputEventAdapterService(null);
    }
}

