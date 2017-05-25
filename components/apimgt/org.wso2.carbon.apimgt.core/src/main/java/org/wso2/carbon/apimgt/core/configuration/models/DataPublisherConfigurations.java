package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold DataPublisher configurations
 */
public class DataPublisherConfigurations {

    @Element(description = "Reciever URL")
    private String receiverURL = "tcp://localhost:7611";
    @Element(description = "Data publisher credentials")
    private CredentialConfigurations dataPublisherCredentials = new CredentialConfigurations();

    public String getReceiverURL() {
        return receiverURL;
    }

    public CredentialConfigurations getDataPublisherCredentials() {
        return dataPublisherCredentials;
    }
}
