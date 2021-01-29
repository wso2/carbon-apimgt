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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerDataService;
import org.wso2.carbon.apimgt.keymgt.handlers.KeyValidationHandler;
import org.wso2.carbon.apimgt.keymgt.service.KeyManagerDataServiceImpl;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

@Component(
         name = "api.keymgt.component", 
         immediate = true)
public class APIKeyMgtServiceComponent {

    private static Log log = LogFactory.getLog(APIKeyMgtServiceComponent.class);


    private ServiceRegistration serviceRegistration = null;

    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            APIKeyMgtDataHolder.initData();
            log.debug("Key Manager User Operation Listener is enabled.");
            // Register subscription datastore related service

            // Register KeyManagerDataService
            serviceRegistration = ctxt.getBundleContext().registerService(KeyManagerDataService.class.getName(),
                    new KeyManagerDataServiceImpl(), null);

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
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
        if (log.isDebugEnabled()) {
            log.debug("Realm Service is set in the API KeyMgt bundle.");
        }
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
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

    /**
     * Initialize the KeyValidation Handlers Service dependency
     *
     * @param keyValidationHandler Key Validation handler reference
     */
    @Reference(
            name = "key.validation.handler.service",
            service = KeyValidationHandler.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeKeyValidationHandler")
    protected void addKeyValidationHandler(KeyValidationHandler keyValidationHandler, Map<String, Object> properties) {
        if (properties.containsKey(APIConstants.KeyManager.REGISTERED_TENANT_DOMAIN)){
            String tenantDomain = (String) properties.get(APIConstants.KeyManager.REGISTERED_TENANT_DOMAIN);
            ServiceReferenceHolder.getInstance().addKeyValidationHandler(tenantDomain, keyValidationHandler);
        }
    }

    /**
     * De-reference the KeyValidation Handler Dependency
     *
     * @param keyValidationHandler keyValidationHandler Reference to Defreference
     */
    protected void removeKeyValidationHandler(KeyValidationHandler keyValidationHandler, Map<String, Object> properties) {
        if (properties.containsKey(APIConstants.KeyManager.REGISTERED_TENANT_DOMAIN)){
            String tenantDomain = (String) properties.get(APIConstants.KeyManager.REGISTERED_TENANT_DOMAIN);
            ServiceReferenceHolder.getInstance().removeKeyValidationHandler(tenantDomain);
        }
    }
}

