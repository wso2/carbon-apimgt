/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.registry.synchronizer.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.registry.synchronizer.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class GovernanceRegistrySyncClient {

    private static final Log log = LogFactory
            .getLog(GovernanceRegistrySyncClient.class);
    private Registry remoteRegistry;
    private Registry governanceRegistry;

    public GovernanceRegistrySyncClient(String username, String password) throws OnPremiseGatewayException {
        ConfigurationContext configContext = ServiceReferenceHolder.getInstance().
                getConfigContextService().getClientConfigContext();
        String serverUrl = ConfigManager.getConfigurationDTO().getUrl_management_console() + "/services/";

        try {
            remoteRegistry = new WSRegistryServiceClient(serverUrl, username, password, 600000, configContext);
        } catch (RegistryException e) {
            String errorMsg = "Failed to create a service client to api publisher";
            log.error(errorMsg, e);
            throw new OnPremiseGatewayException(errorMsg, e);
        }

        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            APIUtil.loadTenantRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            governanceRegistry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (RegistryException e) {
            String errorMsg = "Failed to get local registry service reference.";
            log.error(errorMsg, e);
            throw new OnPremiseGatewayException(errorMsg, e);
        }
    }

    public void copyRegistryResourceFromRemoteToLocal(String path) throws OnPremiseGatewayException {
        log.info("Synchronizing the registry resource " + path);
        String govRegistryPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path;
        try {
            if (remoteRegistry.resourceExists(govRegistryPath)) {
                Resource resource = remoteRegistry.get(govRegistryPath);
                governanceRegistry.put(path, resource);
                log.info("Successfully copied the registry resource: " + path);
            } else {
                log.warn("Registry resource at path:" + path + " does not exists.");
            }
        } catch (RegistryException e) {
            String errorMsg = "Failed to copy registry resource at path:" + path + " to local server.";
            log.error(errorMsg, e);
            throw new OnPremiseGatewayException(errorMsg, e);
        }
    }

}
