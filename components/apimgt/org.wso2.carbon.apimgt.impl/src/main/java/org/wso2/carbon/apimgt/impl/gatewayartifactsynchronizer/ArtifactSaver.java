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

import java.io.File;

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
     * @param apiId
     * @param name
     * @param version
     * @param revision
     * @param tenantDomain
     * @param artifact
     * @throws ArtifactSynchronizerException
     */
    void saveArtifact(String apiId, String name, String version, String revision, String tenantDomain, File artifact)
            throws ArtifactSynchronizerException;


    /**
     * This method is used to remove deployable artifact of an API to the storage
     *
     * @throws ArtifactSynchronizerException if there are any errors in the process
     */
    void removeArtifact(String apiId, String name, String version, String revision, String tenantDomain)
            throws ArtifactSynchronizerException;

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
