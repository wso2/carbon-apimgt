package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;

/**
 * This is a Artifact Saver type. these interface let users to save API artifacts to a storage.
 */
public interface ArtifactSaver {

    /**
     * The init of the Artifact saver, this will be called only once.
     *
     * @throws ArtifactSynchronizerException if there are any configuration errors
     */
    void init() throws ArtifactSynchronizerException;

    /**
     * This method is used to save deployable artifact of an API to the storage
     *
     * @param gatewayRuntimeArtifacts - A String contains all the information about the API and gateway artifacts
     * @throws ArtifactSynchronizerException if there are any errors in the process
     */
    void saveArtifact(String gatewayRuntimeArtifacts, String gatewayInstruction) throws ArtifactSynchronizerException;

    /**
     * This method will return true if the API is published in any of the Gateways
     *
     * @param apiId - UUID of the API
     * @return True if API is published in any of the Gateways. False if published in none
     */
    boolean isAPIPublished(String apiId);

    /**
     * Will be called after all saving is done, or when init fails
     */
    void disconnect();

    /**
     * This method will return the name of artifact saver implementation
     *
     * @return Name of the Artifact saver
     */
    String getName();

}
