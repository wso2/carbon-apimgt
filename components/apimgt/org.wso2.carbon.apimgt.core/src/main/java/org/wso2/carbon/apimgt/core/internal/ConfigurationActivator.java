package org.wso2.carbon.apimgt.core.internal;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.secvault.SecureVault;

/**
 * Class used to activate configuration loading
 */
@Component (
        name = "org.wso2.carbon.apimgt.core",
        immediate = true
)
public class ConfigurationActivator {
    private static final Logger log = LoggerFactory.getLogger(ServiceReferenceHolder.class);

    /**
     * Get the ConfigProvider service.
     * This is the bind method that gets called for ConfigProvider service registration that satisfy the policy.
     *
     * @param configProvider the ConfigProvider service that is registered as a service.
     */
    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider")
    protected void registerConfigProvider(ConfigProvider configProvider) {
        ServiceReferenceHolder.getInstance().setConfigProvider(configProvider);

    }

    /**
     * This is the unbind method for the above reference that gets called for ConfigProvider instance un-registrations.
     *
     * @param configProvider the ConfigProvider service that get unregistered.
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        ServiceReferenceHolder.getInstance().setConfigProvider(null);
    }

    /**
     * Get the SecureVault service.
     * This is the bind method that gets called for SecureVault service registration,
     * which is registered by the 5.2.0-m3 kernel at org.wso2.carbon.kernel.internal.securevault.SecureVaultComponent
     *
     * @param secureVault the SecureVault service that is registered as a service.
     */
    @Reference(
            name = "org.wso2.carbon.kernel.securevault.SecureVault",
            service = SecureVault.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterSecureVault")
    protected void registerSecureVault(SecureVault secureVault) {
        ServiceReferenceHolder.getInstance().setSecureVault(secureVault);
    }

    /**
     * This is the unbind method, which gets called for SecureVault instance un-registrations.
     *
     * @param secureVault the SecureVault service that get unregistered.
     */
    protected void unregisterSecureVault(SecureVault secureVault) {
        ServiceReferenceHolder.getInstance().setSecureVault(null);
    }
}
