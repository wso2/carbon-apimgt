/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.federated.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FederatedApiKeyConnector;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class to instantiate FederatedApiKeyConnector instances based on environment configuration.
 */
public class FederatedApiKeyConnectorFactory {

    private static final Log log = LogFactory.getLog(FederatedApiKeyConnectorFactory.class);
    private static final Map<String, FederatedApiKeyConnector> apiKeyConnectorCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap<>();

    private FederatedApiKeyConnectorFactory() {

    }

    /**
     * Loads and initializes the appropriate FederatedApiKeyConnector for the given environment.
     *
     * @param environment  The environment for which api key connector is requested.
     * @param organization The organization of the request.
     * @return An initialized FederatedApiKeyConnector.
     * @throws APIManagementException If error occurs during instantiation or initialization.
     */
    public static FederatedApiKeyConnector getApiKeyConnector(Environment environment, String organization)
            throws APIManagementException {

        String cacheKey = getCacheKey(organization, environment.getUuid());
        FederatedApiKeyConnector cachedConnector = apiKeyConnectorCache.get(cacheKey);
        if (cachedConnector != null) {
            return cachedConnector;
        }

        Object lock = lockMap.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            cachedConnector = apiKeyConnectorCache.get(cacheKey);
            if (cachedConnector != null) {
                return cachedConnector;
            }

            GatewayAgentConfiguration agentConfiguration = ServiceReferenceHolder.getInstance()
                    .getExternalGatewayConnectorConfiguration(environment.getGatewayType());
            if (agentConfiguration == null) {
                throw new APIManagementException("Gateway Agent Configuration not found for type: "
                        + environment.getGatewayType());
            }

            String implementationClassName = agentConfiguration.getApiKeyConnectorImplementation();
            if (implementationClassName == null || implementationClassName.isEmpty()) {
                throw new APIManagementException("API Key Connector Implementation class not found for gateway type: "
                        + environment.getGatewayType());
            }

            try {
                APIAdminImpl apiAdmin = new APIAdminImpl();
                Environment resolvedEnvironment = apiAdmin.getEnvironmentWithoutPropertyMasking(
                        organization, environment.getUuid());
                resolvedEnvironment = apiAdmin.decryptGatewayConfigurationValues(resolvedEnvironment);
                FederatedApiKeyConnector agent = instantiateApiKeyConnector(implementationClassName, resolvedEnvironment,
                        organization);
                apiKeyConnectorCache.put(cacheKey, agent);
                return agent;
            } catch (ReflectiveOperationException e) {
                String msg = "Error while initializing Federated API Key Connector for type: "
                        + environment.getGatewayType();
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        }
    }

    /**
     * Evicts the cached connector and synchronization lock for the given environment.
     *
     * @param organization organization name
     * @param environmentUuid environment UUID
     */
    public static void evictApiKeyConnector(String organization, String environmentUuid) {

        String cacheKey = getCacheKey(organization, environmentUuid);
        apiKeyConnectorCache.remove(cacheKey);
        lockMap.remove(cacheKey);
    }

    /**
     * Creates a non-cached API key agent initialized with the provided environment object.
     * This is used for pre-save onboarding flows where environment configuration is not persisted yet.
     *
     * @param environment  environment configuration payload
     * @param organization organization name
     * @return initialized non-cached connector
     * @throws APIManagementException if initialization fails
     */
    public static FederatedApiKeyConnector getTransientApiKeyConnector(Environment environment, String organization)
            throws APIManagementException {

        GatewayAgentConfiguration agentConfiguration = ServiceReferenceHolder.getInstance()
                .getExternalGatewayConnectorConfiguration(environment.getGatewayType());
        if (agentConfiguration == null) {
            throw new APIManagementException("Gateway Agent Configuration not found for type: "
                    + environment.getGatewayType());
        }
        String implementationClassName = agentConfiguration.getApiKeyConnectorImplementation();
        if (implementationClassName == null || implementationClassName.isEmpty()) {
            throw new APIManagementException("API Key Connector Implementation class not found for gateway type: "
                    + environment.getGatewayType());
        }
        try {
            return instantiateApiKeyConnector(implementationClassName, environment, organization);
        } catch (ReflectiveOperationException e) {
            String msg = "Error while initializing transient Federated API Key Connector for type: "
                    + environment.getGatewayType();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    private static FederatedApiKeyConnector instantiateApiKeyConnector(String implementationClassName,
                                                                Environment environment, String organization)
            throws ReflectiveOperationException, APIManagementException {

        Class<?> clazz = Class.forName(implementationClassName);
        if (!FederatedApiKeyConnector.class.isAssignableFrom(clazz)) {
            String msg = "Configured API Key Connector class " + implementationClassName + " does not implement "
                    + FederatedApiKeyConnector.class.getName();
            throw new APIManagementException(msg);
        }
        try {
            FederatedApiKeyConnector agent = (FederatedApiKeyConnector) clazz.getDeclaredConstructor().newInstance();
            agent.init(environment, organization);
            return agent;
        } catch (RuntimeException e) {
            String msg = "Error while instantiating Federated API Key Connector implementation: "
                    + implementationClassName;
            throw new APIManagementException(msg, e);
        }
    }

    private static String getCacheKey(String organization, String environmentUuid) {

        return organization + ":" + environmentUuid;
    }
}
