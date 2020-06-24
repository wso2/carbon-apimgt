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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.core.ServerStartupObserver;

public class GatewayStartupListener implements ServerStartupObserver,Runnable {
    private static final Log log = LogFactory.getLog(GatewayStartupListener.class);

    @Override
    public void completingServerStartup() {
        startListener();
    }

    private boolean  deployArtifactsAtStartup(){
        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties =
                ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
        boolean flag = false;
        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
            InMemoryAPIDeployer inMemoryAPIDeployer = new InMemoryAPIDeployer();
            flag = inMemoryAPIDeployer.deployAllAPIsAtGatewayStartup(gatewayArtifactSynchronizerProperties.getGatewayLabels());
        }
        return flag;
    }

    @Override
    public void completedServerStartup() {

    }

    public void startListener() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        deployArtifactsInGateway();
    }

    private void deployArtifactsInGateway() {
        long retryDuration = 10000;
        double reconnectionProgressionFactor = 2.0;
        long maxReconnectDuration = 1000 * 60 * 60; // 1 hour

        while (true) {
            boolean isArtifactsDeployed = deployArtifactsAtStartup();
            if (isArtifactsDeployed) {
                log.info("Synapse Artifacts deployed Successfully in the Gateway");
                break;
            } else {
                retryDuration = (long) (retryDuration * reconnectionProgressionFactor);
                if (retryDuration > maxReconnectDuration) {
                    retryDuration = maxReconnectDuration;
                }
                log.error("Unable to deploy synapse artifacts at gateway. Next retry in " + (retryDuration / 1000)
                        + " seconds");
                try {
                    Thread.sleep(retryDuration);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }
}