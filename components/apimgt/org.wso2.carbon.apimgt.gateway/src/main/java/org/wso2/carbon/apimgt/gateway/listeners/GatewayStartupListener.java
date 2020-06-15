package org.wso2.carbon.apimgt.gateway.listeners;

import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.core.ServerStartupObserver;

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
