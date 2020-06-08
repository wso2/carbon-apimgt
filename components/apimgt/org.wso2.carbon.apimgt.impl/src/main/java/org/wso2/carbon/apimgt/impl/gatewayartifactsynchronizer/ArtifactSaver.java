package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;

import java.util.Set;

/**
 * This is a Artifact Publisher type. these interface let users to publish API artifacts to a storage.
 */
public interface ArtifactSaver {

    /**
     * The init of the publisher, this will be called only once.
     *
     * @throws ArtifactSynchronizerException if there are any configuration errors
     */
    void init() throws ArtifactSynchronizerException;

    /**
     * This method is used to publish a deployable artifact of an API to the storage
     *
     * @param gatewayAPIDTO - DTO contains all the information about the API and gateway artifacts
     * @throws ArtifactSynchronizerException if there are any errors in the process
     */
    void saveArtifact(GatewayAPIDTO gatewayAPIDTO) throws ArtifactSynchronizerException;

    /**
     * This method is used to update artifact of an API already exists in the storage
     *
     * @param gatewayAPIDTO         - DTO contains all the information about the API and gateway artifacts
     * @param gatewayInstruction    - Instruction to gateway whether to deploy or undeploy the API with given artifact
     * @throws ArtifactSynchronizerException if there are any errors in the process
     */
    void updateArtifact(GatewayAPIDTO gatewayAPIDTO, String gatewayInstruction) throws ArtifactSynchronizerException;

    /**
     * This method will return all the existing gateway labels related to the API
     *
     * @param apiId     - UUID of the API
     * @return All the existing labels related to the api
     */
    Set<String> getExistingLabelsForAPI(String apiId);

    /**
     * Will be called after all saving is done, or when init fails
     */
    void disconnect();


    /**
     * The method to get the name of saver implementation
     *
     * @return Name of the saver
     */
    String getName();

}
