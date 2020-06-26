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

package org.wso2.carbon.apimgt.gateway.listeners;

import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * Class for loading synapse artifacts to memory on initial server startup
 */
public class GatewayStartupListener implements ServerStartupObserver {

    @Override
    public void completingServerStartup() {
        deployArtifactsAtStartup();
    }

    private static void  deployArtifactsAtStartup(){
        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties =
                ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
            InMemoryAPIDeployer inMemoryAPIDeployer = new InMemoryAPIDeployer();
            inMemoryAPIDeployer.deployAllAPIsAtGatewayStartup(gatewayArtifactSynchronizerProperties.getGatewayLabels());
        }
    }

    @Override
    public void completedServerStartup() {

    }
}
