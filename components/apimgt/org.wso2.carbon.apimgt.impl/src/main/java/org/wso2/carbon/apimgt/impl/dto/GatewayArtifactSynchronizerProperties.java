package org.wso2.carbon.apimgt.impl.dto;

public class GatewayArtifactSynchronizerProperties {

    private boolean syncArtifacts = false;
    private boolean skipLocalCopy = false;
    private String publisher;
    private String retriever;
    private String gatewayLabel = "DefaultGateway";

    public boolean isSyncArtifacts() {

        return syncArtifacts;
    }

    public void setSyncArtifacts(boolean syncArtifacts) {

        this.syncArtifacts = syncArtifacts;
    }

    public boolean isSkipLocalCopy() {

        return skipLocalCopy;
    }

    public void setSkipLocalCopy(boolean skipLocalCopy) {

        this.skipLocalCopy = skipLocalCopy;
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

    public String getGatewayLabel() {

        return gatewayLabel;
    }

    public void setGatewayLabel(String gatewayLabel) {

        this.gatewayLabel = gatewayLabel;
    }
}
