package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;

import java.util.List;

/**
 * This is a Artifact Retriever type. this interface let users to retriever API artifacts from a storage.
 */
public interface ArtifactRetriever {

    /**
     * The init of the Artifact Retriever, this will be called only once.
     *
     * @throws ArtifactSynchronizerException if there are any configuration errors
     */
    void init() throws ArtifactSynchronizerException;

    /**
     * This method is used to retrieve data from the storage
     *
     * @param APIId              - UUID of the API
     * @param gatewayLabel       - Label subscribed by the gateway
     * @param gatewayInstruction - Whether this is to publish or remove the API from gateway
     * @return A String contains all the information about the API and gateway artifacts
     * @throws ArtifactSynchronizerException if there are any errors when retrieving the Artifacts
     */
    String retrieveArtifact(String APIId, String gatewayLabel, String gatewayInstruction)
            throws ArtifactSynchronizerException;

    /**
     * This method is used to retrieve data from the storage
     *
     * @param gatewayLabel       - Label subscribed by the gateway
     * @return A List of String contains all the information about the APIs and their corresponding gateway artifacts
     * @throws ArtifactSynchronizerException if there are any errors when retrieving the Artifacts
     */
     List<String> retrieveAllArtifacts(String gatewayLabel)
            throws ArtifactSynchronizerException;

    /**
     * Will be called after all publishing is done or if init fails
     */
    void disconnect();

    /**
     * The method to get name of the retriever
     *
     * @return Name of the retriever
     */
    String getName();

}
