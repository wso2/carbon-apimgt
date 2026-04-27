/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FederatedApiKeyConnector;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.api.model.GatewayDeployer;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.dto.OrganizationGatewayDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class GatewayHolder {
    private static Log log = LogFactory.getLog(GatewayHolder.class);
    private static final Map<String, OrganizationGatewayDto> organizationWiseMap = new HashMap<>();

    public static GatewayDeployer getTenantGatewayInstance(String organization, String gatewayName) {
        /* At the moment we fetch the environment from DB each time */
        synchronized (gatewayName.intern()) {
            try {
                Map<String, Environment> environmentMap = APIUtil.getEnvironments(organization);
                Environment environment = environmentMap.get(gatewayName);
                if (environment != null) {
                    // environment fetched from DB might have encrypted properties, hence need to decrypt before
                    // initializing the deployer
                    APIAdminImpl apiAdmin = new APIAdminImpl();
                    Environment resolvedEnvironment = apiAdmin.getEnvironmentWithoutPropertyMasking(organization,
                            environment.getUuid());
                    resolvedEnvironment = apiAdmin.decryptGatewayConfigurationValues(resolvedEnvironment);

                    GatewayAgentConfiguration gatewayAgentConfiguration = ServiceReferenceHolder.getInstance().
                            getExternalGatewayConnectorConfiguration(environment.getGatewayType());
                    if (gatewayAgentConfiguration != null) {
                        GatewayDeployer deployer = (GatewayDeployer) Class.forName(gatewayAgentConfiguration
                                        .getGatewayDeployerImplementation()).getDeclaredConstructor().newInstance();
                        deployer.init(resolvedEnvironment);
                        return deployer;

                    }
                    return null;
                }
            } catch (APIManagementException | ClassNotFoundException | NoSuchMethodException |
                     InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("Error while loading environments for tenant " + organization, e);
                return null;
            }
        }
        return null;
    }

    public static FederatedApiKeyConnector getTenantApiKeyConnectorInstance(Environment environment)
            throws APIManagementException {

        synchronized (environment.getUuid().intern()) {
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
                Environment resolvedEnvironment = environment;
                resolvedEnvironment = apiAdmin.decryptGatewayConfigurationValues(resolvedEnvironment);

                Class<?> clazz = Class.forName(implementationClassName);
                if (!FederatedApiKeyConnector.class.isAssignableFrom(clazz)) {
                    throw new APIManagementException("Configured API Key Connector class " + implementationClassName
                            + " does not implement " + FederatedApiKeyConnector.class.getName());
                }
                FederatedApiKeyConnector connector = (FederatedApiKeyConnector) clazz.getDeclaredConstructor()
                        .newInstance();
                connector.init(resolvedEnvironment);
                return connector;
            } catch (ReflectiveOperationException e) {
                String msg = "Error while initializing Federated API Key Connector for type: "
                        + environment.getGatewayType();
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            } catch (RuntimeException e) {
                String msg = "Error while instantiating Federated API Key Connector implementation: "
                        + implementationClassName;
                throw new APIManagementException(msg, e);
            }
        }
    }
}
