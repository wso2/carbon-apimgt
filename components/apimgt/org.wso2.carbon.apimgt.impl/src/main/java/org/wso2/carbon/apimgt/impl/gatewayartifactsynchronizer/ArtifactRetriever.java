package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ConnectionUnavailableException;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.TestConnectionNotSupportedException;

/**
 * This is a Artifact Retriever type. this interface let users to retriever API artifacts from a storage.
 */
public interface ArtifactRetriever {

    /**
     * The init of the Retriever, this will be called only once.
     *
     * @throws ArtifactSynchronizerException if there are any configuration errors
     */
    void init() throws ArtifactSynchronizerException;

    /**
     * Used to test the connection
     *
     * @throws TestConnectionNotSupportedException if test connection is not supported by the retriever
     * @throws ConnectionUnavailableException      if it cannot connect to the storage
     */
    void testConnect() throws TestConnectionNotSupportedException, ConnectionUnavailableException;

    /**
     * Will be called to connect to the storage before APIs are retrieved
     *
     * @throws ConnectionUnavailableException if it cannot connect to the storage
     */
    void connect() throws ConnectionUnavailableException;

    /**
     * This method is used to retrieve data from the storage
     *
     * @throws ArtifactSynchronizerException if there are any errors when retrieving the Artifacts
     */
    GatewayAPIDTO retrieveArtifacts(String APIId, String APIName, String label) throws ArtifactSynchronizerException;

    /**
     * The init of the publisher, this will be called only once.
     *
     * @throws ArtifactSynchronizerException if there are any errors in the process
     */
    void deleteArtifacts(GatewayAPIDTO gatewayAPIDTO) throws ArtifactSynchronizerException;

    /**
     * Will be called after all publishing is done, or when ConnectionUnavailableException is thrown
     */
    void disconnect();

    /**
     * Will be called at the end to clean all the resources consumed
     */
    void destroy();

    /**
     * The method to get notifier
     *
     * @return type of the notifier
     */
    public String getType();

}
