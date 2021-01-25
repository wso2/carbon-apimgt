/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
            throws ArtifactSynchronizerException, IOException;

    /**
     * This method is used to retrieve data from the storage
     *
     * @param apiName        - Name of the API
     * @param version        - version of the API
     * @param tenantDomain   - Tenant Domain of the API
     * @return A Map conatin APIId and label associated with
     * @throws ArtifactSynchronizerException if there are any errors when retrieving the Attributes
     */
    Map<String, String> retrieveAttributes(String apiName, String version, String tenantDomain)
            throws ArtifactSynchronizerException;

    /**
     * This method is used to retrieve data from the storage
     *
     * @param gatewayLabel       - Label subscribed by the gateway
     * @param tenantDomain
     * @return A List of String contains all the information about the APIs and their corresponding gateway artifacts
     * @throws ArtifactSynchronizerException if there are any errors when retrieving the Artifacts
     */
     List<String> retrieveAllArtifacts(String gatewayLabel, String tenantDomain) throws ArtifactSynchronizerException, IOException;

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
