package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.HashSet;
import java.util.Set;

public class GatewayArtifactSynchronizerProperties {

    private boolean syncArtifacts = false;
    private boolean fileBasedArtifactSynchronizer = true;
    private String publisher = APIConstants.GatewayArtifactSynchronizer.DEFAULT_PUBLISHER_NAME;
    private String retriever = APIConstants.GatewayArtifactSynchronizer.DEFAULT_RETRIEVER_NAME;
    private Set<String> gatewayLabels = new HashSet<>();

    public boolean isSyncArtifacts() {

        return syncArtifacts;
    }

    public void setSyncArtifacts(boolean syncArtifacts) {

        this.syncArtifacts = syncArtifacts;
    }

    public String getPublisher() {

        return publisher;
    }

    public void setPublisher(String publisher) {

        this.publisher = publisher;
    }

    public String getRetriever() {

        return retriever;
    }

    public void setRetriever(String retriever) {

        this.retriever = retriever;
    }

    public Set<String> getGatewayLabels() {

        return gatewayLabels;
    }

    public void setGatewayLabels(Set<String> gatewayLabels) {

        this.gatewayLabels = gatewayLabels;
    }

    public void setArtifactSynchronizer (String artifactSynchronizer){

        if (APIConstants.GatewayArtifactSynchronizer.IN_MEMORY_SYNCHRONIZER.equals(artifactSynchronizer)){
            this.fileBasedArtifactSynchronizer = false;
        }
    }

    public boolean isFileBasedArtifactSynchronizer(){

        return this.fileBasedArtifactSynchronizer;
    }

    public boolean isInMemoryArtifactSynchronizer(){

        return !this.fileBasedArtifactSynchronizer;
    }
}
