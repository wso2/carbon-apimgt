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

import org.apache.commons.lang3.StringUtils;
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

    // We use environmentUuid instead of env name because persistent external mapping tables use envUuid
    public static FederatedApiKeyConnector getTenantApiKeyConnectorInstance(String organization, String environmentUuid)
            throws APIManagementException {

        synchronized (environmentUuid.intern()) {
            try {
                APIAdminImpl apiAdmin = new APIAdminImpl();
                Environment resolvedEnvironment = apiAdmin.getEnvironmentWithoutPropertyMasking(organization,
                        environmentUuid);
                if (resolvedEnvironment != null) {
                    resolvedEnvironment = apiAdmin.decryptGatewayConfigurationValues(resolvedEnvironment);

                    GatewayAgentConfiguration gatewayAgentConfiguration = ServiceReferenceHolder.getInstance()
                            .getExternalGatewayConnectorConfiguration(resolvedEnvironment.getGatewayType());
                    if (gatewayAgentConfiguration != null) {
                        String connectorImplementation = StringUtils.trimToNull(
                                gatewayAgentConfiguration.getApiKeyConnectorImplementation());
                        if (connectorImplementation == null) {
                            throw new APIManagementException("No federated API key connector implementation is "
                                    + "configured for gateway type: " + resolvedEnvironment.getGatewayType());
                        }
                        FederatedApiKeyConnector connector = (FederatedApiKeyConnector) Class.forName(
                                connectorImplementation).getDeclaredConstructor().newInstance();
                        connector.init(resolvedEnvironment);
                        return connector;
                    }
                    throw new APIManagementException("No gateway agent configuration found for gateway type: "
                            + resolvedEnvironment.getGatewayType());
                }
                throw new APIManagementException("Gateway environment not found for UUID: " + environmentUuid);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException | InvocationTargetException | ClassCastException e) {
                String msg = "Error while loading environments for tenant " + organization;
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        }
    }
}
