package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.HashSet;
import java.util.Set;

public class GatewayArtifactSynchronizerProperties {

    private boolean syncEnabled = false;
    private boolean publishDirectlyToGatewayEnabled = true;
    private boolean retrieveFromStorageEnabled = false;
    private String saverName = APIConstants.GatewayArtifactSynchronizer.DEFAULT_SAVER_NAME;
    private String retrieverName = APIConstants.GatewayArtifactSynchronizer.DEFAULT_RETRIEVER_NAME;
    private Set<String> gatewayLabels = new HashSet<>();

    public String getSaverName() {

        return saverName;
    }

    public void setSaverName(String saverName) {

        this.saverName = saverName;
    }

    public String getRetrieverName() {

        return retrieverName;
    }

    public void setRetrieverName(String retrieverName) {

        this.retrieverName = retrieverName;
    }

    public Set<String> getGatewayLabels() {

        return gatewayLabels;
    }

    public void setGatewayLabels(Set<String> gatewayLabels) {

        this.gatewayLabels = gatewayLabels;
    }

    public boolean isPublishDirectlyToGatewayEnabled() {

        return publishDirectlyToGatewayEnabled;
    }

    public void setPublishDirectlyToGatewayEnabled(boolean publishDirectlyToGatewayEnabled) {

        this.publishDirectlyToGatewayEnabled = publishDirectlyToGatewayEnabled;
    }

    public boolean isRetrieveFromStorageEnabled() {

        return retrieveFromStorageEnabled;
    }

    public void setRetrieveFromStorageEnabled(boolean retrieveFromStorageEnabled) {

        this.retrieveFromStorageEnabled = retrieveFromStorageEnabled;
    }

    public boolean isSyncEnabled() {

        return syncEnabled;
    }

    public void setSyncEnabled(boolean syncEnabled) {

        this.syncEnabled = syncEnabled;
    }
}
